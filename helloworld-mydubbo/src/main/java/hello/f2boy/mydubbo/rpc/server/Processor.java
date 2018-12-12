package hello.f2boy.mydubbo.rpc.server;

import hello.f2boy.mydubbo.rpc.protocal.Protocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * tcp连接处理器
 */
class Processor extends Thread {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static ThreadGroup threadGroup = new ThreadGroup("dubbo-rpc-processor");

    private Selector selector;
    private ByteBuffer receiveBuff = ByteBuffer.allocate(256);
    private ByteBuffer sendBuff = ByteBuffer.allocate(256);
    private final BlockingQueue<SocketChannel> newConnections = new LinkedBlockingDeque<>();

    public Processor(String name) {
        super(threadGroup, name);
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

            @SuppressWarnings("unchecked")
            List<Byte> headBytes = ((ArrayList[]) selectionKey.attachment())[0];
            @SuppressWarnings("unchecked")
            List<Byte> bodyBytes = ((ArrayList[]) selectionKey.attachment())[1];

            String message = "";
            while (receiveBuff.hasRemaining()) {

                byte b = receiveBuff.get();

                if (headBytes.size() < Protocal.HEAD_LENGTH) {
                    headBytes.add(b);
                } else {
                    int length = (headBytes.get(3) & 0xFF) |
                            (headBytes.get(2) & 0xFF) << 8 |
                            (headBytes.get(1) & 0xFF) << 16 |
                            (headBytes.get(0) & 0xFF) << 24;
                    
                    if (bodyBytes.size() < length) {
                        bodyBytes.add(b);
                    }

                    if (bodyBytes.size() == length) {
                        byte[] body = new byte[length];
                        for (int i = 0; i < bodyBytes.size(); i++) {
                            body[i] = bodyBytes.get(i);
                        }

                        headBytes.clear();
                        bodyBytes.clear();

                        new Handler() {
                            @Override
                            public void writeResp(byte[] out) {
                                // TODO: 2018/12/12 tcp write response
                                log.info("client = "+client);
                                send(socketChannel, "you are "+client);
//                                send(socketChannel, "bye\n");
                            }
                        }.handle(body);
                    }
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
                    // 注册感兴趣读事件，并且绑定两个附件，用于记录每次rpc请求的head和body。head统一4个字节，body每次不一样，初始默认8k
                    socketChannel.register(selector, SelectionKey.OP_READ, new ArrayList[]{new ArrayList<Byte>(4), new ArrayList<Byte>(8 * 1024)});
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
