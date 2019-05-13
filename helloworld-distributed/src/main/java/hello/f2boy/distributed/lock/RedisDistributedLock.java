package hello.f2boy.distributed.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisDistributedLock implements DistributedLock {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private RedisTemplate<String, String> redisTemplate;

    public RedisDistributedLock(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void lock(String lockKey, String currentRequestId) {
        while (true) {
            boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, currentRequestId, 10, TimeUnit.SECONDS);
            if (success) {
                log.info("{} lock key success.[key={}]", currentRequestId, lockKey);
                break;
            }

            long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
            if (ttl > 0) {
                try {
                    Thread.sleep(Math.min(ttl, 100));
                } catch (InterruptedException e) {
                    throw new RuntimeException("lock fail with InterruptedException", e);
                }
            }
        }
    }

    @Override
    public void unlock(String lockKey, String currentRequestId) {
        redisTemplate.delete(lockKey);
        log.info("{} release key success.[key={}]", currentRequestId, lockKey);
    }

}

