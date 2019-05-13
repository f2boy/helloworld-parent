package hello.f2boy.distributed.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

public class RedisDistributedLock extends AbstractOwnableSynchronizer implements DistributedLock {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private RedisTemplate<String, String> redisTemplate;
    private String lockKey;

    public RedisDistributedLock(RedisTemplate<String, String> redisTemplate, String lockKey) {
        if (redisTemplate == null || lockKey == null) {
            throw new NullPointerException();
        }

        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
    }

    @Override
    public void lock(String currentRequestId) {
        if (currentRequestId == null) {
            throw new NullPointerException();
        }

        final Thread current = Thread.currentThread();
        boolean success = false;

        // 支持重入
        if (current == getExclusiveOwnerThread()) {
            if (currentRequestId.equals(redisTemplate.opsForValue().get(lockKey))) {
                success = redisTemplate.expire(lockKey, 10, TimeUnit.SECONDS);
                if (success) {
                    // 重新校验，确保redis中锁被当前线程持有
                    success = currentRequestId.equals(redisTemplate.opsForValue().get(lockKey));
                }
            }
        }

        if (success) {
            log.info("reenter lock success. [key={}, currentRequestId={}]", lockKey, currentRequestId);
            return;
        }

        while (true) {
            success = redisTemplate.opsForValue().setIfAbsent(lockKey, currentRequestId, 10, TimeUnit.SECONDS);
            if (success) {
                log.info("lock success. [key={}, currentRequestId={}]", lockKey, currentRequestId);
                setExclusiveOwnerThread(current);
                break;
            }

            try {
                log.debug("wait for lock. [key={}, currentRequestId={}]", lockKey, currentRequestId);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException("lock fail with InterruptedException", e);
            }
        }
    }

    @Override
    public void unlock(String currentRequestId) {
        if (currentRequestId == null) {
            throw new NullPointerException();
        }

        final Thread current = Thread.currentThread();
        if (current != getExclusiveOwnerThread()) {
            log.debug("current isn't exclusiveOwnerThread. [key={}, currentRequestId={}]", lockKey, currentRequestId);
            return;
        }

        // TODO: 2019/5/13 此处两句redis操作有原子性问题，需改为lua脚本 
        if (currentRequestId.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey);
            setExclusiveOwnerThread(null);
            log.info("unlock success. [key={}, currentRequestId={}]", lockKey, currentRequestId);
        }
    }

}

