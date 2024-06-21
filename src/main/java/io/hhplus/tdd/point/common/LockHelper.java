package io.hhplus.tdd.point.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class LockHelper {

    private final Map<Long, Lock> lockMap;

    public LockHelper() {
        // 동시성 이슈를 막기 위해 부분락을 사용하는 ConcurrentHashMap 을 이용
        this.lockMap = new ConcurrentHashMap<>();
    }

    public Lock getLock(Long id) {
        // computeIfAbsent 이걸 사용하면 원자성 보장, 만약 userId가 없다면 새로운 lock 을 생성
        return lockMap.computeIfAbsent(id, k -> new ReentrantLock());
    }

    public <T> T executeWithLock(Long id, Supplier<T> supplier) {
        Lock lock = getLock(id);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public void executeWithLock(Long id, Runnable runnable) {
        Lock lock = getLock(id);
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
}
