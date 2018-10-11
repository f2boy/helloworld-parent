package hello.f2boy.mydubbo.io.nio1;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Nio1Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 1234));
        socketChannel.configureBlocking(true);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        buffer.put("I love you\n".getBytes());
        buffer.put("Do you love me?\n".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer);
            socketChannel.write(buffer);
        }

        buffer.clear();
        buffer.put("bye\n".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer);
            socketChannel.write(buffer);
        }

        InputStream is = socketChannel.socket().getInputStream();
        String input = "";
        int c;
        while ((c = is.read()) != -1) {
            input += (char) c;
        }
        System.out.println(input);

        socketChannel.close();
    }

}
