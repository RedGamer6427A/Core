package dev.redgamer6427a.core.database.core;

import dev.redgamer6427a.core.performance.Performance;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A very simple Concurrency Manager
 *
 * @param <K> The key type
 */
public class ConcurrencyManager<K> {

    private static final ConcurrentHashMap<String, ConcurrencyManager<?>> managers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, ReentrantLock> locks = new ConcurrentHashMap<>();
    @Getter
    @Setter
    /*
     * The lock's timeout in milliseconds. Set to t =< 0 to remove timeouts.
     */
    private int lockTimeout = 5000;

    private ConcurrencyManager() {
    }

    /**
     * Get a Concurrency Manager of a specific table. Returns the same one for each table.
     *
     * @param table the table spoken of.
     * @param <K>   the key type.
     * @return the Concurrency Manager
     */
    @SuppressWarnings("unchecked")
    public static <K> ConcurrencyManager<K> of(@NotNull Table<K, ?> table) {
        return (ConcurrencyManager<K>) managers.computeIfAbsent(table.getTableName(), k -> new ConcurrencyManager<>());
    }


    /**
     * Acquire a lock on a key.
     *
     * @param key the key.
     * @throws TimeoutException     if the lock is active for more than 5 seconds.
     * @throws InterruptedException if the thread is interrupted.
     */
    public void lock(@NotNull K key) throws TimeoutException, InterruptedException {
        ReentrantLock lock = locks.computeIfAbsent(key, _ -> new ReentrantLock(true));
        if (lockTimeout <= 0) {
            lock.lock();
        } else {
            boolean acquired = lock.tryLock(lockTimeout, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new TimeoutException("Lock acquisition timed out.");
            }
        }
    }


    /**
     * Unlock a key.
     *
     * @param key the key.
     */
    public void unlock(@NotNull K key) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            locks.remove(key);
        }
    }

    /**
     * A high level wrapper for editing with locks.
     *
     * @param key the key edited.
     * @param r   the runnable that edits the key.
     * @return the time taken to edit the key.
     * @throws InterruptedException if the thread is interrupted.
     * @throws TimeoutException     if the lock takes more than five seconds to acquire.
     */
    public long edit(@NotNull K key, Runnable r) throws InterruptedException, TimeoutException {
        lock(key);
        long perf;
        try {
            perf = Performance.measure(r);
        } finally {
            unlock(key);
        }
        return perf;
    }
}

