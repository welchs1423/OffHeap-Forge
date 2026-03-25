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
import java.sql.ResultSet;
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

            // 🔥 [Phase 53] 예열 시간 0초! Oracle DB에서 가장 높은 SEQ_ID를 가져오는 스마트 복구 엔진
            long dbMaxSeq = 0;
            System.out.println("🔍 [Smart Recovery] DB에서 마지막으로 작업한 위치를 찾습니다...");
            try {
                java.util.Properties props = new java.util.Properties();
                props.put("user", "system");
                props.put("password", "oracle");
                props.put("oracle.net.CONNECT_TIMEOUT", "3000");
                String url = "jdbc:oracle:thin:@127.0.0.1:1522/XEPDB1";

                try (Connection conn = DriverManager.getConnection(url, props);
                     PreparedStatement pstmt = conn.prepareStatement("SELECT NVL(MAX(SEQ_ID), 0) FROM FORGE_METRICS");
                     ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dbMaxSeq = rs.getLong(1);
                    }
                }
                System.out.println("✅ [Smart Recovery] Oracle DB 최신 SEQ_ID 확인 완료: " + dbMaxSeq);
            } catch (Exception e) {
                System.err.println("⚠️ [Smart Recovery] DB 조회 실패 (0번부터 시작합니다): " + e.getMessage());
            }

            PaddedSequence head = new PaddedSequence();
            PaddedSequence tail = new PaddedSequence();

            // 🔥 과거의 유령(Sequence Overlap) 번호를 쫓아가지 않고, DB 최신 번호로 즉시 덮어씌움!
            head.set(dbMaxSeq);
            tail.set(dbMaxSeq);
            final long startHead = dbMaxSeq;

            System.out.println("🚀 [Engine] 시퀀스 " + dbMaxSeq + " 번부터 딜레이 없이 즉시 가동합니다!");

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
                        System.out.println("🔌 [Network] Rust 클라이언트 연결 감지! (데이터 수신 대기 중...)");
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        int bytesRead = -1;

                        try {
                            bytesRead = client.read(buffer);
                        } catch (java.io.IOException e) {
                            System.out.println("🔌 [Network] Rust 클라이언트 연결 끊김. (다음 연결 대기 중...)");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (bytesRead > 0) {
                            buffer.flip();
                            long currentTail = tail.get();

                            // 무한히 증가하는 tail값을 1024로 나눈 나머지로 인덱스 변환! (Bitwise AND 연산)
                            int index = (int)(currentTail & 1023);

                            if (buffer.remaining() >= 8) {
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, index, buffer.getLong());
                                tail.set(currentTail + 1);
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, 1024, tail.get());
                            }
                        } else if (bytesRead == -1) {
                            System.out.println("🔌 [Network] Rust 클라이언트 정리 완료.");
                            client.close();
                            key.cancel();
                        }
                    }
                }
            }
        }
    }
}