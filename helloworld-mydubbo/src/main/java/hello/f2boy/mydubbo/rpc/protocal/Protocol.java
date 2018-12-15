package hello.f2boy.mydubbo.rpc.protocal;

import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;

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
     * 请求转为byte字节
     */
    byte[] toByte(Request request);

    /**
     * 响应转为byte字节
     */
    byte[] toByte(Response response);

    /**
     * tcp接收到的byte数组，转为请求对象
     */
    Request toRequest(byte[] bytes);

    /**
     * tcp接收到的byte数组，转为响应对象
     */
    Response toResponse(byte[] bytes);

}
