package hello.f2boy.mydubbo.io.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class NioClient2 {

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean blockMode = new Random().nextBoolean();

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 1234));
        socketChannel.configureBlocking(blockMode);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        buffer.put("I love you\n".getBytes());
        buffer.put("Do you love me?\n".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer);
            socketChannel.write(buffer);
        }

        Thread.sleep(100);

        buffer.clear();
        buffer.put("bye\n".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer);
            socketChannel.write(buffer);
        }

        String resp = "";
        if (blockMode) {
            System.out.println("\nSocket方式读");
            InputStream is = socketChannel.socket().getInputStream();
            int c;
            while ((c = is.read()) != -1) {
                resp += (char) c;
            }
        }
        // SocketChannel方式读，可设置为非阻塞模式
        else {
            System.out.println("\nSocketChannel方式读");
            buffer.clear();
            while (socketChannel.read(buffer) != -1) { // 非阻塞模式下，此处while条件会立即返回，也就是说会陷入死循环
                Thread.sleep(1);
            }
            buffer.flip();
            System.out.println("receive-buffer: " + buffer);
            while (buffer.hasRemaining()) {
                resp += (char) buffer.get();
            }
        }
        System.out.println(resp);

        socketChannel.close();
    }

}
