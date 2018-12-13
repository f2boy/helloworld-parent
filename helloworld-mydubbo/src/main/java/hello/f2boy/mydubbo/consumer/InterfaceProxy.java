package hello.f2boy.mydubbo.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceProxy {

    public static Map<String, Object> proxyImplMap = new ConcurrentHashMap<>();
    private static Consumer consumer = Consumer.getInstance();

    @SuppressWarnings("unchecked")
    public static <T> T getProxyImpl(Class<T> clazz) {
        proxyImplMap.putIfAbsent(clazz.getName(), Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return consumer.invoke(clazz.getName(), method.getName(), args);
            }
        }));

        return (T) proxyImplMap.get(clazz.getName());
    }

    public InterfaceProxy() {
        System.out.println("aadfadfadfadfadfadfadaf");
    }

    public static void main(String[] args) {
    }

}
