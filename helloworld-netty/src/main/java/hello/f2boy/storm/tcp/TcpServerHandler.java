package hello.f2boy.storm.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        logger.info("SERVER接收到消息:" + o);
        channelHandlerContext.channel().writeAndFlush("yes, server is accepted you ,nice !" + o);
    }
}