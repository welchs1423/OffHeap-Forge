import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NettyGate {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(9999));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Low-Latency TCP Gate Start on Port 9999");
        System.out.println("Waiting for External High-Frequency Traffic...");

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
                    System.out.println("New High-Speed Client Connected: " + client.getRemoteAddress());
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    int bytesRead = client.read(buffer);

                    if (bytesRead > 0) {
                        buffer.flip();
                        long receivedData = buffer.getLong();
                        System.out.println("Network Packet Inbound: " + receivedData);
                        System.out.println("Routing to Off-Heap Forge Engine...");
                    } else if (bytesRead == -1) {
                        client.close();
                    }
                }
            }
        }
    }
}