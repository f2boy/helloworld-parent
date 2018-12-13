package hello.f2boy.mydubbo;

import hello.f2boy.mydubbo.consumer.Consumer;
import hello.f2boy.mydubbo.consumer.InterfaceProxy;
import hello.f2boy.mydubbo.registry.Registry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class TestConsumer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setup() throws Exception {
        log.info("---------------初始注册中心和消费者---------------");
        String address = "127.0.0.1:2181";
        Registry registry = Registry.getInstance(address);
        Consumer.init(registry);
        log.info("---------------初始注册中心和消费者 完毕---------------");
    }

    @Test
    public void testSubscribeService() throws Exception {

        log.info("---------------开始测试消费者---------------");

        // 订阅服务
        String interfaceName = HelloService.class.getName();
        Consumer consumer = Consumer.getInstance();
        consumer.subscribeService(interfaceName, "127.0.0.1");

        // 调用服务
        HelloService helloService = InterfaceProxy.getProxyImpl(HelloService.class);
        helloService.hello("王小二", "张小三");

        CountDownLatch countDownLatch = new CountDownLatch(1);
//        countDownLatch.await();
    }


    public static void main(String[] args) throws Exception {
        TestConsumer testConsumer = new TestConsumer();
        testConsumer.setup();
        testConsumer.testSubscribeService();
    }

}
