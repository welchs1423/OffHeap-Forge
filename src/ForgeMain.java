import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.ByteBuffer;

void main() throws Exception {
    Path filePath = Path.of("ipc_queue.bin");
    try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
         ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

        serverSocket.bind(new InetSocketAddress(8080));
        System.out.println("Zero-Copy Server Start: http://localhost:8080");
        System.out.println("Connect with your browser to download the offheap queue data.");

        SocketChannel client = serverSocket.accept();

        long fileSize = fileChannel.size();
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Length: " + fileSize + "\r\n" +
                "Content-Disposition: attachment; filename=\"queue_dump.bin\"\r\n\r\n";

        client.write(ByteBuffer.wrap(header.getBytes()));

        long transferred = fileChannel.transferTo(0, fileSize, client);

        client.close();
        System.out.println("Zero-Copy Transfer Complete");
        System.out.println("Transferred Bytes: " + transferred);
    }
}