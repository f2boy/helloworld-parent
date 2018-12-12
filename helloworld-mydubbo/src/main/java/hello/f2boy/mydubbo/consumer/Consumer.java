package hello.f2boy.mydubbo.consumer;

import com.google.gson.Gson;
import hello.f2boy.mydubbo.registry.Registry;
import hello.f2boy.mydubbo.rpc.Request;
import hello.f2boy.mydubbo.rpc.protocal.Protocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Registry registry;

    private Map<String, Socket> connectionMap = new ConcurrentHashMap<>();
    private Map<String, List<String>> providerMap = new ConcurrentHashMap<>();

    public Consumer(Registry registry) {
        this.registry = registry;
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

    public void invoke(String interfaceName, String methodName, Object[] params) {
        List<String> providers = providerMap.get(interfaceName);
        Socket socket = connectionMap.get(providers.get(0));

        try {

            Request request = new Request();
            request.setInterfaceName(interfaceName);
            request.setMethodName(methodName);
            request.setParams(params);

            byte[] body = new Gson().toJson(request).getBytes(Protocal.DEFAULT_CHARSET);

            int bodyLength = body.length;
            byte[] head = new byte[4];
            head[0] = (byte) ((bodyLength >> 24) & 0xFF);
            head[1] = (byte) ((bodyLength >> 16) & 0xFF);
            head[2] = (byte) ((bodyLength >> 8) & 0xFF);
            head[3] = (byte) (bodyLength & 0xFF);

            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(head);
            bos.write(body);
            bos.flush();
            
            bos.write(head);
            bos.write(body);
            bos.flush();

            InputStream is = socket.getInputStream();
            String resp = "";
            int c;
            while ((c = is.read()) != -1) {
                resp += (char) c;
            }

            log.info("resp = [{}]", resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
