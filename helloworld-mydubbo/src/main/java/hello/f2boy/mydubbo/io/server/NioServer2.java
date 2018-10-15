package hello.f2boy.mydubbo.io.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServer2 {

    private static void onAccept(SelectionKey selectionKey) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        Selector selector = selectionKey.selector();

        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ, new ByteBuffer[]{ByteBuffer.allocate(256), ByteBuffer.allocate(256)});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void onRead(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
            String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

            ByteBuffer readBuff = ((ByteBuffer[]) selectionKey.attachment())[0];
            ByteBuffer writeBuff = ((ByteBuffer[]) selectionKey.attachment())[1];

            socketChannel.read(readBuff);
            readBuff.flip();
            String message = "";
            while (readBuff.hasRemaining()) {
                char c = (char) readBuff.get();
                message += c;
                if (c == '\n') {
                    System.out.print(client + " send message: " + message);

                    writeBuff.put(("your message is: " + message).getBytes());
                    if (message.equals("bye\n")) {
                        writeBuff.put(message.getBytes());
                    }

                    message = "";
                }
            }
            readBuff.compact();

            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void onWrite(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
            String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

            ByteBuffer writeBuff = ((ByteBuffer[]) selectionKey.attachment())[1];
            writeBuff.flip();
            while (writeBuff.hasRemaining()) {
                socketChannel.write(writeBuff);
            }
            if (writeBuff.limit() >= 4) {
                writeBuff.position(writeBuff.limit() - 4);
//            System.out.println("writeBuff: " + writeBuff);
                String message = "";
                while (writeBuff.hasRemaining()) {
                    message += (char) writeBuff.get();
                }
                if (message.equals("bye\n")) {
                    selectionKey.cancel();
                    socketChannel.close();
                    System.out.println(client + " process complete.\n");
                    return;
                }
            }

            writeBuff.clear();
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            if (selector.select(1000) == 0) {
//                System.out.println("selector 没有事件");
                continue;
            }

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                if (selectionKey.isConnectable()) {
                    System.out.println("---------------------connectable---------------------");
                }
                if (selectionKey.isAcceptable()) {
                    System.out.println("---------------------acceptable---------------------");
                    onAccept(selectionKey);
                }
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    System.out.println("---------------------readable---------------------");
                    onRead(selectionKey);
                }
                if (selectionKey.isValid() && selectionKey.isWritable()) {
                    System.out.println("---------------------writable---------------------");
                    onWrite(selectionKey);
                }
                iter.remove();
            }
        }

    }

}
