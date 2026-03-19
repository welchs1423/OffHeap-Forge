import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;

class PaddedSequence {
    protected long p1, p2, p3, p4, p5, p6, p7;
    public volatile long value = 0;
    protected long p8, p9, p10, p11, p12, p13, p14;
    private static final VarHandle HANDLE;
    static {
        try { HANDLE = MethodHandles.lookup().findVarHandle(PaddedSequence.class, "value", long.class); }
        catch (ReflectiveOperationException e) { throw new Error(e); }
    }
    public long get() { return value; }
    public void set(long v) { HANDLE.setVolatile(this, v); }
}

public class ForgeMain {
    private static final int CAPACITY = 1024;
    private static final int MASK = CAPACITY - 1;

    public static void main(String[] args) throws Exception {
        try (Arena arena = Arena.ofShared(); Selector selector = Selector.open()) {
            MemorySegment ringBuffer = arena.allocate(ValueLayout.JAVA_LONG, CAPACITY);
            PaddedSequence head = new PaddedSequence();
            PaddedSequence tail = new PaddedSequence();

            // 1. 프로메테우스 메트릭 노출용 내장 HTTP 서버 (Port 9090)
            HttpServer metricsServer = HttpServer.create(new InetSocketAddress(9090), 0);
            metricsServer.createContext("/metrics", exchange -> {
                // 프로메테우스 포맷으로 현재 처리된 총량(head.get())을 응답
                String response = "# HELP offheap_forge_processed_total Total messages processed\n" +
                        "# TYPE offheap_forge_processed_total counter\n" +
                        "offheap_forge_processed_total " + head.get() + "\n";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            metricsServer.setExecutor(null);
            metricsServer.start();
            System.out.println("Telemetry Exporter Online (Port 9090) - Ready for Prometheus");

            // 2. 기존 엔진 네트워크 포트 (Port 9999)
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Zero-Contention Network Engine Online (Port 9999)");

            // [Consumer Thread]
            Thread consumer = new Thread(() -> {
                while (true) {
                    while (tail.get() == head.get()) { Thread.onSpinWait(); }
                    long data = ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, (int)(head.get() & MASK));
                    // 초고속 처리를 위해 콘솔 출력은 이제 주석 처리하거나 최소화합니다.
                    // System.out.println("-> [Engine Out] Processed Data: " + data);
                    head.set(head.get() + 1);
                }
            });
            consumer.setDaemon(true);
            consumer.start();

            // [Main Thread - Producer]
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
                            if (tail.get() - head.get() < CAPACITY) {
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, (int)(tail.get() & MASK), buffer.getLong());
                                tail.set(tail.get() + 1);
                            }
                        }
                    }
                }
            }
        }
    }
}