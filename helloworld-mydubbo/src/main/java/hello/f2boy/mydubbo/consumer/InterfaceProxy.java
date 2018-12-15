package hello.f2boy.mydubbo.consumer;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceProxy {

    private static Map<String, Object> proxyImplMap = new ConcurrentHashMap<>();
    private static Consumer consumer = Consumer.getInstance();

    @SuppressWarnings("unchecked")
    public static <T> T getProxyImpl(Class<T> clazz) {
        return (T) proxyImplMap.computeIfAbsent(clazz.getName(), k -> Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            return consumer.invoke(clazz.getName(), method.getName(), args);
        }));
    }

}
