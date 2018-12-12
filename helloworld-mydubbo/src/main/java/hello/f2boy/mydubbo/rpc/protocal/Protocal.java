package hello.f2boy.mydubbo.rpc.protocal;

import hello.f2boy.mydubbo.rpc.exchange.Exchange;

/**
 * 解析
 */
public interface Protocal {

    /**
     * 协议头字节数
     */
    int HEAD_LENGTH = 4;

    String DEFAULT_CHARSET = "UTF-8";

    /**
     * 解析请求byte数组
     */
    Exchange parse(byte[] input);

}
