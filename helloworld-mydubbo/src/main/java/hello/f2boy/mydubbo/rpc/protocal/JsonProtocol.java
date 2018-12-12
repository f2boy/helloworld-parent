package hello.f2boy.mydubbo.rpc.protocal;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class JsonProtocol implements Protocol {

    @Override
    public byte[] toOut(Request request) {
        return objectToOut(request);
    }

    @Override
    public byte[] toOut(Response response) {
        return objectToOut(response);
    }

    private byte[] objectToOut(Serializable o) {
        byte[] body;
        try {
            body = new Gson().toJson(o).getBytes(Protocol.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        byte[] head = new byte[4];
        head[0] = (byte) ((body.length >> 24) & 0xFF);
        head[1] = (byte) ((body.length >> 16) & 0xFF);
        head[2] = (byte) ((body.length >> 8) & 0xFF);
        head[3] = (byte) (body.length & 0xFF);

        byte[] result = Arrays.copyOf(head, head.length + body.length);
        System.arraycopy(body, 0, result, head.length, body.length);

        return result;
    }

    @Override
    public Request toRequest(byte[] body) {
        String s = new String(body, Charset.forName(DEFAULT_CHARSET));
        return new Gson().fromJson(s, Request.class);
    }

    @Override
    public Response toResponse(byte[] body) {
        String s = new String(body, Charset.forName(DEFAULT_CHARSET));
        return new Gson().fromJson(s, Response.class);
    }

    public static void main(String[] args) {
        Request request = new Request();
        request.setInterfaceName("com.f2boy.test.hahaha");
        request.setParams(new String[]{"aaa", "我爱你"});
        Gson gson = new Gson();
        String s = gson.toJson(request);
        System.out.println("s = " + s);

        request = new JsonProtocol().toRequest(s.getBytes(Charset.forName(DEFAULT_CHARSET)));
        System.out.println("request = " + gson.toJson(request));
    }

}
