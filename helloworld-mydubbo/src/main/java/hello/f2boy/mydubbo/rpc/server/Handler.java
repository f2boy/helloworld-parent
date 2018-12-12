package hello.f2boy.mydubbo.rpc.server;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;
import hello.f2boy.mydubbo.rpc.protocal.JsonProtocol;
import hello.f2boy.mydubbo.rpc.protocal.Protocol;

import java.util.HashMap;
import java.util.Map;

public abstract class Handler {

    public abstract void writeResp(byte[] out);

    public void handle(byte[] input) {
        Protocol protocol = new JsonProtocol();

        Request request = protocol.toRequest(input);
        System.out.println("request = " + new Gson().toJson(request));

        Response response = new Response();
        response.setCode("100");

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "asdfqwer");
        map.put("key2", 12345);
        response.setData(map);

        byte[] out = protocol.toOut(response);
        this.writeResp(out);
    }

}
