package hello.f2boy.distributed.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext-test-zookeeper.xml"})
public class TestZookeeperDistributedLock {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private CuratorFramework curatorFramework;

    public int var = 0;
    private ZookeeperDistributedLock lock;

    @Before
    public void setUp() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorFramework = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        this.curatorFramework.start();

        lock = new ZookeeperDistributedLock(curatorFramework, "test-zookeeper-lock");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLock() throws InterruptedException {
        int times = 3;
        CountDownLatch countDownLatch = new CountDownLatch(times);

        for (int i = 0; i < times; i++) {
            new Thread(() -> {
                String currentRequestId = UUID.randomUUID().toString();
                String seqNodeName = lock.lock(currentRequestId);
                var++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("var = " + var);
                lock.unlock(currentRequestId, seqNodeName);
                countDownLatch.countDown();
            }).start();
            Thread.sleep(10);
        }

        countDownLatch.await();
        log.info("final var = " + var);
    }

    // 测试可重入
    @Test
    public void testReentrant() throws InterruptedException {

    }

}
