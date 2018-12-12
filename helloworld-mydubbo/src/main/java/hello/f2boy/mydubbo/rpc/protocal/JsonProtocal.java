package hello.f2boy.mydubbo.rpc.protocal;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.exchange.Exchange;

import java.nio.charset.Charset;

public class JsonProtocal implements Protocal {

    @Override
    public Exchange parse(byte[] input) {
        Exchange exchange = new Exchange();

        String s = new String(input, Charset.forName(DEFAULT_CHARSET));

        Gson gson = new Gson();
        Request request = gson.fromJson(s, Request.class);

        exchange.setRequest(request);
        return exchange;
    }

    public static void main(String[] args) {
        Request request = new Request();
        request.setInterfaceName("com.f2boy.test.hahaha");
        request.setParams(new String[]{"aaa", "我爱你"});
        Gson gson = new Gson();
        String s = gson.toJson(request);
        System.out.println("s = " + s);

        Exchange exchange = new JsonProtocal().parse(s.getBytes(Charset.forName(DEFAULT_CHARSET)));
        System.out.println("exchange = " + gson.toJson(exchange));
    }
}
