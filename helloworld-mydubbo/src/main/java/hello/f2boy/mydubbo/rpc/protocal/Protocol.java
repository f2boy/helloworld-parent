package hello.f2boy.mydubbo.rpc.protocal;

import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;
import hello.f2boy.mydubbo.rpc.exchange.Exchange;

/**
 * 解析
 */
public interface Protocol {

    /**
     * 协议头字节数
     */
    int HEAD_LENGTH = 4;

    String DEFAULT_CHARSET = "UTF-8";

//    /**
//     * 解析请求byte数组
//     */
//    Exchange parse(byte[] input);

    /**
     * 请求转为byte字节（包含头部的4个字节）
     */
    byte[] toOut(Request request);

    /**
     * 响应转为byte字节（包含头部的4个字节）
     */
    byte[] toOut(Response response);

    /**
     * tcp接收到的byte数组，转为请求对象（只包含body）
     */
    Request toRequest(byte[] body);

    /**
     * tcp接收到的byte数组，转为响应对象（只包含body）
     */
    Response toResponse(byte[] body);

}
