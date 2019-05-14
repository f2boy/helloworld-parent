package hello.f2boy.distributed.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext-test-redis.xml"})
public class TestRedisDistributedLock {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    public int var = 0;
    private DistributedLock lock;

    @Before
    public void setUp() throws Exception {
        lock = new RedisDistributedLock(redisTemplate, "test-redis-lock");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLock() throws InterruptedException {
        int times = 10;
        CountDownLatch countDownLatch = new CountDownLatch(times);

        for (int i = 0; i < times; i++) {
            Request request = new Request(UUID.randomUUID().toString(), countDownLatch);
            new Thread(request).start();
        }

        countDownLatch.await();
        log.info("final var = " + var);
    }

    // 测试可重入
    @Test
    public void testReentrant() throws InterruptedException {
        int times = 3;
        CountDownLatch countDownLatch = new CountDownLatch(times);

        for (int i = 0; i < times; i++) {
            new Thread(() -> {
                String currentRequestId = UUID.randomUUID().toString();
                lock.lock(currentRequestId);
                lock.lock(currentRequestId);
                lock.lock(currentRequestId);
                var++;
                log.info("var = " + var);
                lock.unlock(currentRequestId);
                var++;
                log.info("var = " + var);
                lock.unlock(currentRequestId);
                var++;
                log.info("var = " + var);
                lock.unlock(currentRequestId);
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        log.info("final var = " + var);
    }

    public class Request implements Runnable {
        private String currentRequestId;
        private CountDownLatch countDownLatch;

        public Request(String currentRequestId, CountDownLatch countDownLatch) {
            this.currentRequestId = currentRequestId;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            inc(this.currentRequestId, this.countDownLatch);
        }
    }

    private void inc(String currentRequestId, CountDownLatch countDownLatch) {
        lock.lock(currentRequestId);
        var++;
        log.info("var = " + var);
        lock.unlock(currentRequestId);
        countDownLatch.countDown();
    }

}
