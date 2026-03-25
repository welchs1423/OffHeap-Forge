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
        System.load(Path.of("native_core.dll").toAbsolutePath().toString());
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        final MethodHandle getHwTimer = Linker.nativeLinker().downcallHandle(
                lookup.find("get_hw_timer").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG)
        );

        final MethodHandle getHwFreq = Linker.nativeLinker().downcallHandle(
                lookup.find("get_hw_freq").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG)
        );

        long tempFreq = 1;
        try {
            tempFreq = (long) getHwFreq.invokeExact();
            System.out.println("⚙️ [Native Bridge] C/C++ Hardware Timer Loaded! (Freq: " + tempFreq + ")");
        } catch (Throwable t) { t.printStackTrace(); }
        final long CPU_FREQ = tempFreq;

        try (Arena arena = Arena.ofShared(); Selector selector = Selector.open()) {
            FileChannel channel = FileChannel.open(Path.of("pipeline-rust", "forge-data.dat"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MemorySegment ringBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, (1024 + 1) * 8L, arena);

            FileChannel feedbackChannel = FileChannel.open(Path.of("pipeline-rust", "forge-feedback.dat"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MemorySegment feedbackSegment = feedbackChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8, arena);

            Thread feedbackWatcher = new Thread(() -> {
                long lastAlertCount = 0;
                while (true) {
                    long currentAlertCount = feedbackSegment.get(ValueLayout.JAVA_LONG, 0);
                    if (currentAlertCount > lastAlertCount) {
                        System.out.println("⚠️ [Java 엔진] Rust로부터 긴급 피드백 수신! 누적 경고 횟수: " + currentAlertCount);
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

            Thread dbFlusher = new Thread(() -> {
                System.out.println("💡 [SIMD Flusher] Thread is ALIVE! Starting initialization...");
                try {
                    long flushHead = startHead;
                    long[] batchBuffer = new long[4];

                    java.util.Properties props = new java.util.Properties();
                    props.put("user", "system");
                    props.put("password", "oracle");
                    props.put("oracle.net.CONNECT_TIMEOUT", "3000");
                    props.put("oracle.jdbc.ReadTimeout", "3000");

                    String url = "jdbc:oracle:thin:@127.0.0.1:1522/XEPDB1";
                    try (Connection conn = DriverManager.getConnection(url, props)) {
                        System.out.println("🚀 [Oracle DB] Connected! Vectorized Flusher Standby.");

                        String upsertSql = "MERGE INTO FORGE_METRICS t " +
                                "USING (SELECT ? AS seq, ? AS val FROM dual) s " +
                                "ON (t.SEQ_ID = s.seq) " +
                                "WHEN NOT MATCHED THEN INSERT (SEQ_ID, METRIC_VAL) VALUES (s.seq, s.val)";
                        PreparedStatement pstmt = conn.prepareStatement(upsertSql);

                        while (true) {
                            long currentHead = head.get();
                            long available = currentHead - flushHead;

                            if (available >= 4) {
                                int ringIndex = (int)(flushHead & 1023);

                                if (ringIndex <= 1024 - 4) {
                                    long startTick = (long) getHwTimer.invokeExact();

                                    LongVector vec = LongVector.fromMemorySegment(SPECIES, ringBuffer, ringIndex * 8L, ByteOrder.LITTLE_ENDIAN);
                                    vec.intoArray(batchBuffer, 0);

                                    for(int i=0; i<4; i++) {
                                        pstmt.setLong(1, flushHead + i);
                                        pstmt.setLong(2, batchBuffer[i]);
                                        pstmt.addBatch();
                                    }
                                    pstmt.executeBatch();

                                    long endTick = (long) getHwTimer.invokeExact();
                                    double elapsedMicros = (endTick - startTick) * 1_000_000.0 / CPU_FREQ;

                                    flushHead += 4;
                                    System.out.printf("⚡ [Profiler] 4건 Batch Insert 소요시간: %.2f μs\n", elapsedMicros);
                                } else {
                                    pstmt.setLong(1, flushHead);
                                    pstmt.setLong(2, ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, ringIndex));
                                    pstmt.executeUpdate();
                                    flushHead++;
                                }
                            } else {
                                Thread.sleep(10);
                            }
                        }
                    }
                } catch (Throwable e) {
                    System.err.println("⚠️ [DB Flusher FATAL ERROR] 원인: " + e.toString());
                }
            });
            dbFlusher.setDaemon(true);
            dbFlusher.start();

            // 🔥 원본 복구: 아주 완벽했던 생산자-소비자 모델로 돌아갑니다.
            Thread consumer = new Thread(() -> {
                while (true) {
                    while (tail.get() == head.get()) { Thread.onSpinWait(); }
                    head.set(head.get() + 1);
                }
            });
            consumer.setDaemon(true);
            consumer.start();

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Zero-Contention Network Engine Online (Port 9999)");

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
                        // 🔥 CCTV 1번: 러스트가 자바(9999포트)에 들어오면 무조건 찍힙니다!
                        System.out.println("🔌 [Network] Rust 클라이언트 연결 감지! (데이터 수신 대기 중...)");
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        int bytesRead = client.read(buffer);

                        if (bytesRead > 0) {
                            buffer.flip();
                            long currentTail = tail.get();
                            if (currentTail - head.get() < 1024) {
                                int index = (int)(currentTail & 1023);
                                if (buffer.remaining() >= 8) {
                                    ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, index, buffer.getLong());
                                    tail.set(currentTail + 1);
                                    ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, 1024, tail.get());
                                }
                            }
                        } else if (bytesRead == -1) {
                            // 🔥 CCTV 2번: 러스트가 도망가면 찍힙니다.
                            System.out.println("🔌 [Network] Rust 클라이언트 연결 종료.");
                            client.close();
                        }
                    }
                }
            }
        }
    }
}