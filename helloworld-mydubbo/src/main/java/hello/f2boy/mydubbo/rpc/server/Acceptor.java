package hello.f2boy.mydubbo.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * tcp接收者
 */
public final class Acceptor {

    private Acceptor() {
    }

    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

    private final int PROCESSOR_NUM = 2;
    private final Processor[] processors = new Processor[PROCESSOR_NUM];

    private void onAccept(SelectionKey selectionKey, int current) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            InetSocketAddress remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
            String client = "[" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + "]";
            log.info("接收到新的连接: {}", client);
            int index = current % processors.length;
            if (processors[index] == null) {
                synchronized (processors) {
                    if (processors[index] == null) {
                        processors[index] = new Processor("processor-" + index);
                        processors[index].start();
                    }
                }
            }
            processors[index].addSocketChannel(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listen(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        log.info("------------------------------ 开始监听端口[" + port + "] ------------------------------");

        Acceptor acceptor = new Acceptor();
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
                    acceptor.onAccept(selectionKey, current++);
                }
            }
        }

    }

}
