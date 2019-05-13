package hello.f2boy.distributed.lock;

public interface DistributedLock {

    void lock(String lockKey, String currentRequestId);

    void unlock(String lockKey, String currentRequestId);

}
