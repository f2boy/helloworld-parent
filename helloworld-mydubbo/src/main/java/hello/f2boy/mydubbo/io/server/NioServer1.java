package hello.f2boy.mydubbo.io.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServer1 {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.configureBlocking(true);

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
            String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

            new Thread(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(256);
                String message = "";
                try {
                    while (socketChannel.isOpen() && socketChannel.read(buffer) != -1) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            char c = (char) buffer.get();
                            message += c;
                            if (c == '\n') {
                                System.out.print(client + " send message: " + message);
                                if (message.equals("bye\n")) {
                                    socketChannel.close();
                                    break;
                                }
                                message = "";
                            }
                        }
                        buffer.compact();
                    }
                    System.out.println(client + " process complete.\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
