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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Registry registry;

    private Map<String, Socket> connectionMap = new ConcurrentHashMap<>();
    private Map<String, List<String>> providerMap = new ConcurrentHashMap<>();

    private Consumer(Registry registry) {
        this.registry = registry;
    }

    private static Consumer instance = null;

    public static Consumer init(Registry registry) {
        instance = new Consumer(registry);
        return instance;
    }

    public static Consumer getInstance() {
        if (instance == null) {
            throw new RuntimeException("Consumer instance is not inited!");
        }
        return instance;
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
        providerMap.put(interfaceName, providers);

        for (String provider : providers) {
            connectionMap.computeIfAbsent(provider, k -> {
                try {
                    return new Socket(provider, 20880);
                } catch (IOException e) {
                    log.error("连接{}:20880端口异常", provider, e);
                    return null;
                }
            });
        }
    }

    public Object invoke(String interfaceName, String methodName, Object[] params) {
        List<String> providers = providerMap.get(interfaceName);
        Socket socket = connectionMap.get(providers.get(new Random().nextInt(connectionMap.size())));

        try {
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            Request request = new Request();
            request.setInterfaceName(interfaceName);
            request.setMethodName(methodName);
            request.setParams(params);

            byte[] out = new JsonProtocol().toByte(request);
            bos.write(out);
            bos.flush();

            List<Byte> inputBytes = new ArrayList<>(512);
            int bodyLength = 0;

            InputStream is = socket.getInputStream();
            int c;
            while ((c = is.read()) != -1) {
                inputBytes.add((byte) c);
                log.debug("inputBytes.size() = " + inputBytes.size());

                if (bodyLength == 0 && inputBytes.size() >= Protocol.HEAD_LENGTH) {
                    bodyLength = (inputBytes.get(3) & 0xFF) |
                            (inputBytes.get(2) & 0xFF) << 8 |
                            (inputBytes.get(1) & 0xFF) << 16 |
                            (inputBytes.get(0) & 0xFF) << 24;
                    if (bodyLength <= 0) {
                        throw new RuntimeException("无效的请求（head字节转为int后小于等于0）");
                    }
                }

                if (inputBytes.size() < Protocol.HEAD_LENGTH + bodyLength) {
                    continue;
                }

                byte[] input = new byte[Protocol.HEAD_LENGTH + bodyLength];
                for (int i = 0; i < inputBytes.size(); i++) {
                    input[i] = inputBytes.get(i);
                }

                Response response = new JsonProtocol().toResponse(input);
                log.info("request: {}, response: {}", new Gson().toJson(request), new Gson().toJson(response));
                return response.getData();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public void close() {
        for (Socket socket : connectionMap.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
