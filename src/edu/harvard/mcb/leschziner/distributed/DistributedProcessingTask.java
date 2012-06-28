package edu.harvard.mcb.leschziner.distributed;

import java.io.Serializable;

import com.hazelcast.core.Hazelcast;

public abstract class DistributedProcessingTask implements Serializable,
                                               Runnable {
    public static final String PENDING_SUFFIX = "_pending";

    private final String       executorName;

    public DistributedProcessingTask(String executorName) {
        this.executorName = executorName;
    }

    public abstract void process();

    @Override
    public void run() {
        process();
        markComplete();
    }

    public void markPending() {
        Hazelcast.getAtomicNumber(executorName + PENDING_SUFFIX)
                 .incrementAndGet();
    }

    private void markComplete() {
        Hazelcast.getAtomicNumber(executorName + PENDING_SUFFIX)
                 .decrementAndGet();
    }

}
