package hello.f2boy.mydubbo.rpc.server;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.provider.Provider;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;
import hello.f2boy.mydubbo.rpc.protocal.JsonProtocol;
import hello.f2boy.mydubbo.rpc.protocal.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Handler {

    public abstract void writeResp(byte[] out);

    public void handle(byte[] input) {
        Protocol protocol = new JsonProtocol();

        Request request = protocol.toRequest(input);
        System.out.println("request = " + new Gson().toJson(request));

        Response response = new Response();

        String interfaceName = request.getInterfaceName();
        Object bean = Provider.getInstance().getServiceBean(interfaceName);
        if (bean == null) {
            response.setCode("-1");
            response.setData("service not exists");
        } else {
            Class<?> c = bean.getClass();
            Method method;
            try {
                if (request.getParams() == null || request.getParams().length == 0) {
                    method = c.getMethod(request.getMethodName());
                } else {
                    Class<?>[] parameterTypes = new Class[request.getParams().length];
                    for (int i = 0; i < request.getParams().length; i++) {
                        parameterTypes[i] = request.getParams()[i].getClass();
                    }
                    method = c.getMethod(request.getMethodName(), parameterTypes);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            Object res;
            try {
                res = method.invoke(bean, request.getParams());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            response.setCode("100");
            response.setData(res);

        }

        byte[] out = protocol.toOut(response);
        this.writeResp(out);
    }

}
