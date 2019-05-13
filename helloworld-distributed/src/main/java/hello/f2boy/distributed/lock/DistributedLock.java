package hello.f2boy.distributed.lock;

public interface DistributedLock {

    void lock(String currentRequestId);

    void unlock(String currentRequestId);

}
