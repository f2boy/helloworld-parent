package hello.f2boy.distributed.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext-test.xml", "classpath*:spring/applicationContext-test-redis.xml"})
public class TestRedisDistributedLock {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StringRedisTemplate StringRedisTemplate;

    public int var = 0;

    @Test
    public void testLock() throws InterruptedException {
        DistributedLock lock = new RedisDistributedLock(StringRedisTemplate);
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println("Thread.currentThread() = " + Thread.currentThread().getName());
                lock.lock("test-lock", Thread.currentThread().getName());
                var++;
                log.info("var = " + var);
                lock.unlock("test-lock", Thread.currentThread().getName());
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        log.info("var = " + var);
    }

}
