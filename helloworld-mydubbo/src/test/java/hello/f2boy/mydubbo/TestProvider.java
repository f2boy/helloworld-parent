package hello.f2boy.mydubbo;

import hello.f2boy.mydubbo.provider.Provider;
import hello.f2boy.mydubbo.provider.Service;
import hello.f2boy.mydubbo.registry.Registry;
import org.junit.Test;

import java.io.IOException;

public class TestProvider {

    @Test
    public void testRegisterService() throws IOException {
        String address = "127.0.0.1:2181";
        Registry registry = new Registry(address);
        Provider provider = new Provider(registry);

        String interfaceName = "hello.f2boy.mydubbo.testService";
        Service service = new Service(interfaceName, null);
        service.setProviderIp("127.0.0.1");
        provider.registerService(service);
    }

    public static void main(String[] args) throws IOException {
        new TestProvider().testRegisterService();
    }
}
