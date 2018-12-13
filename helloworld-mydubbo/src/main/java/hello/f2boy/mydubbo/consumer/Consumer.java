package hello.f2boy.mydubbo.consumer;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.registry.Registry;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.Response;
import hello.f2boy.mydubbo.rpc.protocal.JsonProtocol;
import hello.f2boy.mydubbo.rpc.protocal.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Registry registry;

    private Map<String, Socket> connectionMap = new ConcurrentHashMap<>();
    private Map<String, List<String>> providerMap = new ConcurrentHashMap<>();

    private Consumer(Registry registry) {
        this.registry = registry;
    }

    private static Consumer consumer = null;

    public static Consumer init(Registry registry) {
        consumer = new Consumer(registry);
        return consumer;
    }

    public static Consumer getInstance() {
        if (consumer == null) {
            throw new RuntimeException("consumer is not inited!");
        }
        return consumer;
    }

    public void subscribeService(String interfaceName, String consumerIp) {
        registry.subscribeService(interfaceName, consumerIp);
        try {
            initConnection(interfaceName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initConnection(String interfaceName) throws IOException {
        List<String> providers = registry.findProviders(interfaceName);
        for (String provider : providers) {
            connectionMap.putIfAbsent(provider, new Socket(provider, 20880));
        }
        providerMap.put(interfaceName, providers);
    }

    public Object invoke(String interfaceName, String methodName, Object[] params) {
        List<String> providers = providerMap.get(interfaceName);
        Socket socket = connectionMap.get(providers.get(0));

        try {
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            Request request = new Request();
            request.setInterfaceName(interfaceName);
            request.setMethodName(methodName);
            request.setParams(params);

            byte[] out = new JsonProtocol().toOut(request);
            bos.write(out);
            bos.flush();

            List<Byte> headBytes = new ArrayList<>(Protocol.HEAD_LENGTH);
            List<Byte> bodyBytes = null;

            InputStream is = socket.getInputStream();
            int c;
            while ((c = is.read()) != -1) {
                if (headBytes.size() < Protocol.HEAD_LENGTH) {
                    headBytes.add((byte) c);
                } else {
                    int length = (headBytes.get(3) & 0xFF) |
                            (headBytes.get(2) & 0xFF) << 8 |
                            (headBytes.get(1) & 0xFF) << 16 |
                            (headBytes.get(0) & 0xFF) << 24;

                    if (bodyBytes == null) {
                        bodyBytes = new ArrayList<>(length);
                    }

                    if (bodyBytes.size() < length) {
                        bodyBytes.add((byte) c);
                    }

                    if (bodyBytes.size() == length) {
                        byte[] body = new byte[length];
                        for (int i = 0; i < bodyBytes.size(); i++) {
                            body[i] = bodyBytes.get(i);
                        }

                        Response response = new JsonProtocol().toResponse(body);
                        log.info("request: {}, response: {}", new Gson().toJson(request), new Gson().toJson(response));
                        return response.getData();
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

}
