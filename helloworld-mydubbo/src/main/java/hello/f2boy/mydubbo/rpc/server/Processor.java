package hello.f2boy.mydubbo.rpc.server;

import hello.f2boy.mydubbo.rpc.protocal.Protocol;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * tcp连接处理器
 */
class Processor extends Thread {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ThreadGroup threadGroup = new ThreadGroup("mydubbo-rpc-processor");
    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    private Selector selector;
    private ByteBuffer receiveBuff = ByteBuffer.allocate(256);
    private ByteBuffer sendBuff = ByteBuffer.allocate(256);
    private final BlockingQueue<SocketChannel> newConnections = new LinkedBlockingDeque<>();

    public Processor() {
        super(threadGroup, "mydubbo-rpc-processor-" + threadNumber.getAndIncrement());
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
        log.info("[" + this.getName() + "]添加一个新的SocketChannel, 当前处理的有效SocketChannel数量为: " + (validKeys + 1));
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

                if (headBytes.size() < Protocol.HEAD_LENGTH) {
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
                                log.info("响应客户端： " + client);
                                send(socketChannel, out);
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

    private void send(SocketChannel socketChannel, byte[] out) {
        try {
            sendBuff.clear();
            sendBuff.put(out);
            sendBuff.flip();
            while (sendBuff.hasRemaining()) {
                socketChannel.write(sendBuff);
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
                    socketChannel.register(selector, SelectionKey.OP_READ, new ArrayList[]{new ArrayList<>(Protocol.HEAD_LENGTH), new ArrayList<>(8 * 1024)});
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
                    log.debug("接收到消息");
                    handleRead(selectionKey);
                }
            }
        }
    }
}
