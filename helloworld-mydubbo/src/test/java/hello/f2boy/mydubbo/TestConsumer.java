package hello.f2boy.mydubbo;

import hello.f2boy.mydubbo.consumer.Consumer;
import hello.f2boy.mydubbo.registry.Registry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TestConsumer {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testSubscribeService() throws IOException, InterruptedException {

        log.info("开始测试消费者");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String address = "127.0.0.1:2181";
        Registry registry = new Registry(address);
        Consumer consumer = new Consumer(registry);

        String interfaceName = "hello.f2boy.mydubbo.testService";
        consumer.subscribeService(interfaceName, "127.0.0.1");

        consumer.invoke(interfaceName, "hello");

//        countDownLatch.await();
    }

}
