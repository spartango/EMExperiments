package edu.harvard.mcb.leschziner.distributed;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public abstract class DistributedTaskHandler {
    // Distributed Executor
    protected final String        executorName;
    private final ExecutorService executor;
    protected final AtomicNumber  pendingCount;

    public DistributedTaskHandler() {
        executorName = this.getClass().getName() + "_" + this.hashCode();
        // Configure Hazelcast's threadpool
        int cpus = Runtime.getRuntime().availableProcessors();
        Hazelcast.getConfig().getExecutorConfig(executorName)
                 .setCorePoolSize(cpus);
        Hazelcast.getConfig().getExecutorConfig(executorName)
                 .setMaxPoolSize(cpus);
        executor = Hazelcast.getExecutorService(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName);
    }

    public void execute(Runnable command) {
        pendingCount.incrementAndGet();
        executor.execute(command);
    }

    public <T> Future<T> submit(Callable<T> task) {
        pendingCount.incrementAndGet();
        return executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        pendingCount.incrementAndGet();
        return executor.submit(task, result);
    }

    public Future<?> submit(Runnable task) {
        pendingCount.incrementAndGet();
        return executor.submit(task);
    }

    public void stop() {
        executor.shutdown();
    }

    public boolean isActive() {
        return pendingCount.get() > 0;
    }

    public long getPendingCount() {
        return pendingCount.get();
    }
}
