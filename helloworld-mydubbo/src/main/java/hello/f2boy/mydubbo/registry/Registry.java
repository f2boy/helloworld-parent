package hello.f2boy.mydubbo.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Registry {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String ROOT_PATH = "/mydubbo";
    private static final String PROVIDERS_FOLDER = "/providers";
    private static final String CONSUMERS_FOLDER = "/consumers";
    private CuratorFramework client;

    public Registry(String address) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(address, retryPolicy);
        client.start();
    }

    /**
     * 发布服务
     *
     * @param interfaceName 服务接口名称
     * @param providerIp    服务提供者ip
     */
    public void registerService(String interfaceName, String providerIp) {
        try {
            String path = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(ROOT_PATH + "/" + interfaceName + PROVIDERS_FOLDER + "/" + providerIp);
            log.info("发布服务: " + path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 订阅服务
     *
     * @param interfaceName 服务接口名称
     * @param consumerIp    服务订阅者ip
     */
    public void subscribeService(String interfaceName, String consumerIp) {
        try {
            String path = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(ROOT_PATH + "/" + interfaceName + CONSUMERS_FOLDER + "/" + consumerIp);
            log.info("订阅服务: " + path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> findProviders(String interfaceName) {
        try {
            return client.getChildren().forPath(ROOT_PATH + "/" + interfaceName + PROVIDERS_FOLDER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String address = "127.0.0.1:2181";
        Registry registry = new Registry(address);
        String interfaceName = "hello.f2boy.mydubbo.testService";
        registry.registerService(interfaceName, "127.0.0.1");
        registry.registerService(interfaceName, "127.0.0.2");
        registry.registerService(interfaceName, "127.0.0.3");
        registry.registerService(interfaceName, "127.0.0.4");

        registry.subscribeService(interfaceName, "127.0.1.1");
        registry.subscribeService(interfaceName, "127.0.1.2");

        List<String> providers = registry.findProviders(interfaceName);
        providers.forEach(System.out::println);

        countDownLatch.await();
    }
}
