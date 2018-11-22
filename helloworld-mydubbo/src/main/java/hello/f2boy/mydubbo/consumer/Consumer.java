package hello.f2boy.mydubbo.consumer;

import hello.f2boy.mydubbo.registry.Registry;
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

    public void invoke(String interfaceName, String methodName) {
        List<String> providers = providerMap.get(interfaceName);
        Socket socket = connectionMap.get(providers.get(0));

        try {
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write((interfaceName + "##" + methodName + "\nbye\n").getBytes());
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
