package hello.f2boy.mydubbo.rpc.server;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.rpc.exchange.Exchange;
import hello.f2boy.mydubbo.rpc.protocal.JsonProtocal;

public abstract class Handler {

    public abstract void writeResp(byte[] out);

    public void handle(byte[] input) {
        Exchange exchange = new JsonProtocal().parse(input);
        System.out.println("exchange = " + new Gson().toJson(exchange));

        byte[] out = new byte[10];
        this.writeResp(out);
    }

}
