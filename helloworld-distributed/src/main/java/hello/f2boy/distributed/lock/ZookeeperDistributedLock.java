package hello.f2boy.distributed.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

public class ZookeeperDistributedLock extends AbstractOwnableSynchronizer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String LOCK_ROOT_PATH = "/myLock";

    private CuratorFramework client;
    private String lockKey;
    private String lockParentPath;
    private String pathPrefix;

    public ZookeeperDistributedLock(CuratorFramework client, String lockKey) {
        if (client == null || lockKey == null) {
            throw new NullPointerException();
        }

        if (client.getState() != CuratorFrameworkState.STARTED) {
            throw new RuntimeException("CuratorFramework's state is not started");
        }

        this.client = client;
        this.lockKey = lockKey;
        this.lockParentPath = LOCK_ROOT_PATH + "/" + lockKey;
        this.pathPrefix = this.lockParentPath + "/lock-";
    }

    public String lock(String currentRequestId) {

        // TODO: 2019/5/14 重入支持

        final String currentNodeName;   // 当前线程在zk上的临时顺序节点
        List<String> seqList;           // 争夺当前锁的所有线程创建的临时顺序节点
        try {
            String path = this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(pathPrefix, currentRequestId.getBytes());
            currentNodeName = path.substring(this.lockParentPath.length() + 1);
            seqList = this.client.getChildren().forPath(lockParentPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        seqList.sort(String::compareTo);
        log.info("currentNodeName = {}, seqList = {}", currentNodeName, seqList.toString());

        if (currentNodeName.equals(seqList.get(0))) {
            log.info("lock success. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, currentNodeName);
            return currentNodeName;
        }

        // 前一个顺序节点 
        String preNodeName = null;
        for (int i = 1; i < seqList.size(); i++) {
            String nodeName = seqList.get(i);
            if (nodeName.equals(currentNodeName)) {
                preNodeName = seqList.get(i - 1);
                break;
            }
        }
        try {
            // 监控前一个顺序节点，前一个节点被删除后，则当前线程获取锁
            this.client.getData().usingWatcher((CuratorWatcher) event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    synchronized (currentNodeName) {
                        currentNodeName.notify();
                    }
                }
            }).forPath(this.lockParentPath + "/" + preNodeName);
        } catch (KeeperException.NoNodeException e) {
            // 当前线程获得锁 
            log.info("lock success. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, currentNodeName);
            return currentNodeName;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("线程进入wait，等待watcher回调唤醒. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, currentNodeName);
        synchronized (currentNodeName) {
            try {
                currentNodeName.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("lock success. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, currentNodeName);
        return currentNodeName;
    }

    public void unlock(String currentRequestId, String seqNodeName) {
        // TODO: 2019/5/14 ACL权限认证

        try {
            this.client.delete().quietly().forPath(this.lockParentPath + "/" + seqNodeName);
            log.info("unlock success. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, seqNodeName);
        } catch (Exception e) {
            log.error("unlock exception. [key={}, currentRequestId={}, currentNodeName={}]", lockKey, currentRequestId, seqNodeName, e);
        }
    }

}

