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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.VectorSpecies;

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
    private static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_256;

    public static void main(String[] args) throws Exception {
        try (Arena arena = Arena.ofShared(); Selector selector = Selector.open()) {
            FileChannel channel = FileChannel.open(Path.of("forge-data.dat"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MemorySegment ringBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, (1024 + 1) * 8L, arena);

            FileChannel feedbackChannel = FileChannel.open(Path.of("forge-feedback.dat"),
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MemorySegment feedbackSegment = feedbackChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8, arena);

            // 2. 러스트 피드백 감시 스레드 가동
            Thread feedbackWatcher = new Thread(() -> {
                long lastAlertCount = 0;
                while (true) {
                    long currentAlertCount = feedbackSegment.get(ValueLayout.JAVA_LONG, 0);
                    if (currentAlertCount > lastAlertCount) {
                        System.out.println("[Java 엔진] Rust로부터 긴급 피드백 수신! 누적 경고 횟수: " + currentAlertCount);
                        lastAlertCount = currentAlertCount;
                    }
                    try { Thread.sleep(10); } catch (Exception e) {}
                }
            });
            feedbackWatcher.setDaemon(true);
            feedbackWatcher.start();

            PaddedSequence head = new PaddedSequence();
            PaddedSequence tail = new PaddedSequence();

            long recovered = 0;
            for (int i = 0; i < 1024; i++) {
                if (ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, i) != 0) { recovered++; }
                else { break; }
            }
            if (recovered > 0) {
                head.set(recovered);
                tail.set(recovered);
                System.out.println("🔥 [Crash Recovery] Restored " + recovered + " messages from Disk!");
            }

            final long startHead = recovered;

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
            System.out.println("Telemetry Exporter Online (Port 9090)");

            // 🚀 [SEASON 3] Vectorized DB Flusher (Background Thread)
            Thread dbFlusher = new Thread(() -> {
                // 스레드가 살아있는지 확인하는 생존 신고 로그!
                System.out.println("💡 [SIMD Flusher] Thread is ALIVE! Starting initialization...");
                try {
                    long flushHead = startHead;
                    long[] batchBuffer = new long[4];

                    // 오라클 무한대기 방지용 드라이버 레벨 속성 강제 주입
                    java.util.Properties props = new java.util.Properties();
                    props.put("user", "system");
                    props.put("password", "oracle");
                    props.put("oracle.net.CONNECT_TIMEOUT", "3000"); // 3초 연결 타임아웃
                    props.put("oracle.jdbc.ReadTimeout", "3000");    // 3초 읽기 타임아웃

                    String url = "jdbc:oracle:thin:@localhost:1521/XE";
                    System.out.println("⏳ [Oracle DB] Connecting to " + url + " ...");

                    try (Connection conn = DriverManager.getConnection(url, props)) {
                        System.out.println("🚀 [Oracle DB] Connected! Vectorized Flusher Standby.");
                        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO FORGE_METRICS (SEQ_ID, METRIC_VAL) VALUES (?, ?)");

                        while (true) {
                            long currentHead = head.get();
                            long available = currentHead - flushHead;

                            if (available >= 4) {
                                int ringIndex = (int)(flushHead & 1023);

                                if (ringIndex <= 1024 - 4) {
                                    LongVector vec = LongVector.fromMemorySegment(SPECIES, ringBuffer, ringIndex * 8L, ByteOrder.LITTLE_ENDIAN);
                                    vec.intoArray(batchBuffer, 0);

                                    for(int i=0; i<4; i++) {
                                        pstmt.setLong(1, flushHead + i);
                                        pstmt.setLong(2, batchBuffer[i]);
                                        pstmt.addBatch();
                                    }
                                    pstmt.executeBatch();
                                    flushHead += 4;
                                    System.out.println("⚡ [SIMD Flusher] 4건 Oracle Bulk Insert 완료!");
                                } else {
                                    pstmt.setLong(1, flushHead);
                                    pstmt.setLong(2, ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, ringIndex));
                                    pstmt.executeUpdate();
                                    flushHead++;
                                }
                            } else {
                                Thread.sleep(100);
                            }
                        }
                    }
                } catch (Throwable e) { // 🔥 치명적인 Error 급 오류까지 모두 포착!
                    System.err.println("⚠️ [DB Flusher FATAL ERROR] 원인: " + e.toString());
                    e.printStackTrace();
                }
            });
            dbFlusher.setDaemon(true);
            dbFlusher.start();

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Zero-Contention Network Engine Online (Port 9999)");

            Thread consumer = new Thread(() -> {
                while (true) {
                    while (tail.get() == head.get()) { Thread.onSpinWait(); }
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
                            long currentTail = tail.get();
                            if (currentTail - head.get() < 1024) {
                                int index = (int)(currentTail & 1023);
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, index, buffer.getLong());
                                tail.set(currentTail + 1);

                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, 1024, tail.get());
                            }
                        }
                    }
                }
            }
        }
    }
}