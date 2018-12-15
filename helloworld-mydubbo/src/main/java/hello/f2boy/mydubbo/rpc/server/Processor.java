package hello.f2boy.mydubbo.rpc.server;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.rpc.protocal.JsonProtocol;
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
            List<Byte> inputBytes = (List<Byte>) selectionKey.attachment();
            int bodyLength = 0;
            while (receiveBuff.hasRemaining()) {

                byte b = receiveBuff.get();
                inputBytes.add(b);

                if (bodyLength == 0 && inputBytes.size() >= Protocol.HEAD_LENGTH) {
                    bodyLength = (inputBytes.get(3) & 0xFF) |
                            (inputBytes.get(2) & 0xFF) << 8 |
                            (inputBytes.get(1) & 0xFF) << 16 |
                            (inputBytes.get(0) & 0xFF) << 24;
                    if (bodyLength <= 0) {
                        throw new RuntimeException("无效的请求（head字节转为int后小于等于0）");
                    }
                }

                if (inputBytes.size() < Protocol.HEAD_LENGTH + bodyLength) {
                    continue;
                }

                byte[] input = new byte[Protocol.HEAD_LENGTH + bodyLength];
                for (int i = 0; i < inputBytes.size(); i++) {
                    input[i] = inputBytes.get(i);
                }

                inputBytes.clear();

                new Handler() {
                    @Override
                    public void writeResp(byte[] out) {
                        log.debug("响应客户端: {} - {}", client, new Gson().toJson(new JsonProtocol().toResponse(out)));
                        send(socketChannel, out);
                    }
                }.handle(input);
            }

        } catch (IOException e) {
            log.error("socket读取异常", e);
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void send(SocketChannel socketChannel, byte[] out) {
        try {
            int offset = 0;
            int remain = out.length;
            while (remain > 0) {
                int len = Math.min(sendBuff.capacity(), remain);

                sendBuff.clear();
                sendBuff.put(out, offset, len);
                sendBuff.flip();
                while (sendBuff.hasRemaining()) {
                    socketChannel.write(sendBuff);
                }

                offset += len;
                remain = remain - len;
            }
        } catch (IOException e) {
            log.error("socket写入异常", e);
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
                    // 注册感兴趣读事件，并且绑定1个附件，用于记录每次rpc请求字节内容。初始默认1k
                    socketChannel.register(selector, SelectionKey.OP_READ, new ArrayList<>(1024));
                }

                if (selector.select(5000) == 0) {
                    continue;
                }
            } catch (IOException e) {
                log.error("socketChannel 异常", e);
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
