package hello.f2boy.mydubbo.io.server;

import hello.f2boy.mydubbo.PrintUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class NioServer3 {

    private static final int PROCESSOR_NUM = 2;

    private static final Processor[] processors = new Processor[PROCESSOR_NUM];

    private static class Processor extends Thread {

        private Selector selector;
        private ByteBuffer receiveBuff = ByteBuffer.allocate(256);
        private ByteBuffer sendBuff = ByteBuffer.allocate(256);
        private final BlockingQueue<SocketChannel> newConnections = new LinkedBlockingDeque<>();

        public Processor(String name) {
            super(name);
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void add(SocketChannel socketChannel) {
            newConnections.add(socketChannel);
            selector.wakeup();
            PrintUtils.println(this.getName() + " add new socket channel, now selector.keys() = " + (selector.keys().size() + 1));
        }

        private void handleRead(SelectionKey selectionKey) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
                String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

                receiveBuff.clear();
                if (socketChannel.read(receiveBuff) == -1) {
                    socketChannel.close();
                    return;
                }
                receiveBuff.flip();

                String message = "";
                while (receiveBuff.hasRemaining()) {
                    char c = (char) receiveBuff.get();
                    message += c;
                    if (c == '\n') {
                        PrintUtils.print(client + " send message: " + message);

                        String resp = "your message is: " + message;
                        send(socketChannel, resp);

                        if (message.equals("bye\n")) {
                            send(socketChannel, "bye\n");
                        }

                        message = "";
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.cancel();
                try {
                    socketChannel.close();
                } catch (IOException ignored) {
                }
            }
        }

        private void send(SocketChannel socketChannel, String resp) {
            try {

                InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
                String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";

                sendBuff.clear();
                sendBuff.put(resp.getBytes());
                sendBuff.flip();
                while (sendBuff.hasRemaining()) {
                    socketChannel.write(sendBuff);
                }

                if (resp.equals("bye\n")) {
                    socketChannel.close();
                    PrintUtils.println(client + " process complete.\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socketChannel.close();
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    while (!newConnections.isEmpty()) {
                        SocketChannel socketChannel = newConnections.poll();
                        if (socketChannel == null || !socketChannel.isOpen()) {
                            return;
                        }
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                    if (selector.select(5000) == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey selectionKey = iter.next();
                    iter.remove();
                    if (selectionKey.isReadable()) {
                        PrintUtils.println("------------------------------ received message ------------------------------");
                        handleRead(selectionKey);
                    }
                }
            }
        }
    }

    private static void onAccept(SelectionKey selectionKey, int current) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            int index = current % processors.length;
            if (processors[index] == null) {
                synchronized (processors) {
                    if (processors[index] == null) {
                        processors[index] = new Processor("processor-" + index);
                        processors[index].start();
                    }
                }
            }
            processors[index].add(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        int current = 0;
        while (true) {
            try {
                if (selector.select(1000) == 0) {
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                iter.remove();
                if (selectionKey.isAcceptable()) {
                    PrintUtils.println("------------------------------ accepted new connection ------------------------------");
                    onAccept(selectionKey, current++);
                }
            }
        }

    }

}
