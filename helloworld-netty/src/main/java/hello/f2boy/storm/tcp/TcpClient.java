package hello.f2boy.storm.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpClient {
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    public static String HOST = "127.0.0.1";
    public static int PORT = 9999;

    public static Bootstrap bootstrap = getBootstrap();
    public static Channel channel = getChannel(HOST, PORT);

    /**
     * 初始化Bootstrap
     *
     * @return Bootstrap
     */
    public static final Bootstrap getBootstrap() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class);
        b.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
            }
        });
        b.option(ChannelOption.SO_KEEPALIVE, true);
        return b;
    }

    /**
     * 设置channel
     *
     * @param host 地址
     * @param port 端口
     * @return Channel
     */
    public static final Channel getChannel(String host, int port) {
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            logger.error(String.format("连接Server(IP[%s],PORT[%s])失败", host, port), e);
            return null;
        }
        return channel;
    }

    /**
     * 发送消息
     *
     * @param msg 消息内容
     * @throws Exception 异常信息
     */
    public static void sendMsg(String msg) throws Exception {
        try {
            if (channel != null) {
                channel.writeAndFlush(msg);
            } else {
                logger.warn("消息发送失败,连接尚未建立!");
            }
        } catch (Exception e) {
            logger.error("发送消息异常", e);
        }
    }

    /**
     * 启动服务
     *
     * @param args 参数
     * @throws Exception 异常
     */
    public static void main(String[] args) throws Exception {
        try {
            long t0 = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                TcpClient.sendMsg(i + "你好1");
            }
            long t1 = System.nanoTime();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}


