package hello.f2boy.mydubbo.rpc.protocal;

import hello.f2boy.mydubbo.rpc.exchange.Exchange;

/**
 * 解析
 */
public interface Protocal {

    Exchange parse(byte[] input);

}
