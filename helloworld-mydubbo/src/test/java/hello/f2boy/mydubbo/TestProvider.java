package hello.f2boy.mydubbo;

import hello.f2boy.mydubbo.provider.Provider;
import hello.f2boy.mydubbo.provider.Service;
import hello.f2boy.mydubbo.registry.Registry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProvider {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setup() throws Exception {
        log.info("初始注册中心和提供者");
        String address = "127.0.0.1:2181";
        Registry registry = Registry.getInstance(address);
        Provider.init(registry);
        log.info("初始注册中心和提供者 完毕");
    }

    @Test
    public void testRegisterService() throws Exception {
        Provider provider = Provider.getInstance();

        String interfaceName = HelloService.class.getName();
        Service service = new Service(interfaceName, new HelloServiceImpl());
        service.setProviderIp("127.0.0.1");
        provider.registerService(service);
    }

    public static void main(String[] args) throws Exception {
        TestProvider testProvider = new TestProvider();
        testProvider.setup();
        testProvider.testRegisterService();
    }

}
