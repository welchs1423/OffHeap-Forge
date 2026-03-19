import java.lang.foreign.*;
import java.lang.invoke.*;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.Iterator;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class PaddedSequence {
    protected long p1, p2, p3, p4, p5, p6, p7;
    public volatile long value = 0;
    protected long p8, p9, p10, p11, p12, p13, p14;
    private static final VarHandle HANDLE;
    static {
        try { HANDLE = MethodHandles.lookup().findVarHandle(PaddedSequence.class, "value", long.class); }
        catch (Exception e) { throw new Error(e); }
    }
    public long get() { return value; }
    public void set(long v) { HANDLE.setVolatile(this, v); }
}

public class ForgeMain {
    public static void main(String[] args) throws Exception {
        try (Arena arena = Arena.ofShared(); Selector selector = Selector.open()) {

            FileChannel channel = FileChannel.open(Path.of("forge-data.dat"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MemorySegment ringBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 8L, arena);

            PaddedSequence head = new PaddedSequence();
            PaddedSequence tail = new PaddedSequence();

            HttpServer metricsServer = HttpServer.create(new InetSocketAddress(9090), 0);
            metricsServer.createContext("/metrics", exchange -> {
                String response = "# HELP offheap_forge_processed_total Total messages processed\n# TYPE offheap_forge_processed_total counter\noffheap_forge_processed_total " + head.get() + "\n";
                exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            metricsServer.setExecutor(null);
            metricsServer.start();
            System.out.println("Telemetry Exporter Online (Port 9090) - Ready for Prometheus");

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Zero-Contention Network Engine Online (Port 9999)");

            Thread consumer = new Thread(() -> {
                while (true) {
                    while (tail.get() == head.get()) { Thread.onSpinWait(); }
                    ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, (int)(head.get() & 1023));
                    head.set(head.get() + 1);
                }
            });
            consumer.setDaemon(true);
            consumer.start();

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isAcceptable()) {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        if (client.read(buffer) == 8) {
                            buffer.flip();
                            if (tail.get() - head.get() < 1024) {
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, (int)(tail.get() & 1023), buffer.getLong());
                                tail.set(tail.get() + 1);
                            }
                        }
                    }
                }
            }
        }
    }
}