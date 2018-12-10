package hello.f2boy.mydubbo.rpc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * tcp连接处理器
 */
class Processor extends Thread {

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

    public synchronized void addSocketChannel(SocketChannel socketChannel) {
        newConnections.add(socketChannel);
        selector.wakeup();

        int validKeys = 0;
        for (SelectionKey selectionKey : selector.keys()) {
            if (selectionKey.isValid()) validKeys++;
        }
        log.info("添加一个新的 socket channel, now its selector's valid keys = " + (validKeys + 1));
        log.info("");
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
                    log.info(client + " send message: " + message);

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
                log.info(client + " process complete.");
                int validKeys = 0;
                for (SelectionKey selectionKey : selector.keys()) {
                    if (selectionKey.isValid()) validKeys++;
                }
                log.info("now selector's valid keys = " + validKeys + "\n");
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
                    log.info("------------------------------ received message ------------------------------");
                    handleRead(selectionKey);
                }
            }
        }
    }
}
