package hello.f2boy.mydubbo.provider;

import hello.f2boy.mydubbo.registry.Registry;
import hello.f2boy.mydubbo.rpc.server.Acceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Provider {

    private Registry registry;

    private Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    private static Provider instance = null;

    private Provider(Registry registry) {
        this.registry = registry;

        new Thread(() -> {
            try {
                Acceptor.listen(20880);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void init(Registry registry) {
        instance = new Provider(registry);
    }

    public static Provider getInstance() {
        if (instance == null) {
            throw new RuntimeException("Provider instance is not inited!");
        }
        return instance;
    }

    public void registerService(Service service) {
        registry.registerService(service.getInterfaceName(), service.getProviderIp());
        serviceMap.put(service.getInterfaceName(), service.getBean());
    }

    public Object getServiceBean(String interfaceName) {
        return serviceMap.get(interfaceName);
    }

}
