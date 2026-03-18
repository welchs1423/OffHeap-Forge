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

class PaddedSequence {
    protected long p1, p2, p3, p4, p5, p6, p7;
    public volatile long value = 0;
    protected long p8, p9, p10, p11, p12, p13, p14;

    private static final VarHandle HANDLE;
    static {
        try {
            HANDLE = MethodHandles.lookup().findVarHandle(PaddedSequence.class, "value", long.class);
        } catch (ReflectiveOperationException e) { throw new Error(e); }
    }
    public long get() { return value; }
    public void set(long v) { HANDLE.setVolatile(this, v); }
}

public class ForgeMain {
    private static final int CAPACITY = 1024;
    private static final int MASK = CAPACITY - 1;

    public static void main(String[] args) throws Exception {
        try (Arena arena = Arena.ofShared(); Selector selector = Selector.open()) {
            // 오프힙 링 버퍼 할당
            MemorySegment ringBuffer = arena.allocate(ValueLayout.JAVA_LONG, CAPACITY);
            PaddedSequence head = new PaddedSequence(); // 소비자 인덱스
            PaddedSequence tail = new PaddedSequence(); // 생산자 인덱스

            // 네트워크 게이트웨이 설정 (Port 9999)
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Zero-Contention Engine Online (Port 9999)");

            // Consumer Thread: 오프힙 버퍼에서 데이터를 꺼내 실시간 처리
            Thread consumer = new Thread(() -> {
                System.out.println("Consumer Thread Standing By...");
                while (true) {
                    while (tail.get() == head.get()) { /* 데이터 대기 */ }
                    long data = ringBuffer.getAtIndex(ValueLayout.JAVA_LONG, (int)(head.get() & MASK));
                    System.out.println("Process Engine Out: " + data);
                    head.set(head.get() + 1);
                }
            });
            consumer.setDaemon(true);
            consumer.start();

            // Main Thread (Producer): 네트워크 패킷 수신 및 오프힙 버퍼 기록
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
                        System.out.println("New Traffic Source Connected");
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        if (client.read(buffer) == 8) {
                            buffer.flip();
                            long inboundData = buffer.getLong();

                            // 버퍼가 꽉 차지 않았을 때만 기록
                            if (tail.get() - head.get() < CAPACITY) {
                                ringBuffer.setAtIndex(ValueLayout.JAVA_LONG, (int)(tail.get() & MASK), inboundData);
                                tail.set(tail.get() + 1);
                            }
                        }
                    }
                }
            }
        }
    }
}