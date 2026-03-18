import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class NettyGate {
    public static void main(String[] args) throws Exception {
        Path queuePath = Path.of("ipc_queue.bin");
        try (FileChannel fc = FileChannel.open(queuePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
             Arena arena = Arena.ofShared();
             Selector selector = Selector.open()) {

            long fileSize = 8 + (10000 * 8);
            MemorySegment mmap = fc.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Integrated Network-to-OffHeap Gate Start");

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
                        System.out.println("Client Connected");
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8);

                        // 윈도우(PowerShell) 환경에 맞춰 리틀 엔디안으로 설정
                        buffer.order(ByteOrder.LITTLE_ENDIAN);

                        int bytesRead = client.read(buffer);

                        if (bytesRead == 8) {
                            buffer.flip();
                            long data = buffer.getLong();

                            // 오프힙 인덱스 0번 위치에 네트워크 데이터 즉시 기록 (Zero-Copy)
                            mmap.set(ValueLayout.JAVA_LONG, 8, data);

                            System.out.println("Network Data Inbound: " + data);
                            System.out.println("Successfully Synced to Off-Heap Memory!");
                        } else if (bytesRead == -1) {
                            client.close();
                        }
                    }
                }
            }
        }
    }
}