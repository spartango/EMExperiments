package edu.harvard.mcb.leschziner.distributed;

import java.util.concurrent.ExecutorService;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public abstract class DistributedTaskHandler {
    // Distributed Executor
    protected final String        executorName;
    private final ExecutorService executor;
    protected final AtomicNumber  pendingCount;

    public DistributedTaskHandler() {
        executorName = this.getClass().getName() + "_" + this.hashCode();

        executor = Hazelcast.getExecutorService(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName);
    }

    public void execute(DistributedProcessingTask command) {
        command.markPending();
        // TODO queue against available capacity
        executor.execute(command);
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
