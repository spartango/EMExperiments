package edu.harvard.mcb.leschziner.distributed;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public abstract class DistributedTaskHandler {
    // Distributed Executor
    protected final String                  executorName;
    private final QueuedDistributedExecutor executor;
    protected final AtomicNumber            pendingCount;

    public DistributedTaskHandler() {
        executorName = this.getClass().getName() + "_" + this.hashCode();
        // Use a queued executor to prevent flooding the cluster with tasks
        executor = new QueuedDistributedExecutor(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName
                                                 + DistributedProcessingTask.PENDING_SUFFIX);
    }

    public void execute(DistributedProcessingTask command) {
        command.markPending();
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
