import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.invoke.VarHandle;

void main() throws Exception {
    Path filePath = Path.of("ipc_queue.bin");
    try (FileChannel fileChannel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
         Arena arena = Arena.ofShared();
         ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

        long queueCapacity = 10000;
        long elementSize = 8;
        long headerSize = 8;
        long fileSize = headerSize + (queueCapacity * elementSize);

        MemorySegment mmap = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);
        VarHandle indexHandle = ValueLayout.JAVA_LONG.varHandle();

        serverSocket.bind(new InetSocketAddress(8080));
        System.out.println("NIO API Gateway Start: http://localhost:8080");
        System.out.println("Open your web browser and connect to the address above.");

        SocketChannel client = serverSocket.accept();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);

        long currentIndex = (long) indexHandle.getAndAdd(mmap, 0L, 1L);
        long safeIndex = currentIndex % queueCapacity;
        long dataOffset = headerSize + (safeIndex * elementSize);

        long orderId = System.currentTimeMillis();
        mmap.set(ValueLayout.JAVA_LONG, dataOffset, orderId);

        String response = "HTTP/1.1 200 OK\r\n\r\nOrder Inserted: " + orderId;
        client.write(ByteBuffer.wrap(response.getBytes()));
        client.close();

        System.out.println("Network Request Processed");
        System.out.println("Assigned Order ID: " + orderId);
        System.out.println("OffHeap Index: " + currentIndex);
    }
}