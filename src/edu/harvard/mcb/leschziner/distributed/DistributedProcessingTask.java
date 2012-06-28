package edu.harvard.mcb.leschziner.distributed;

import java.io.Serializable;

import com.hazelcast.core.Hazelcast;

public abstract class DistributedProcessingTask implements Runnable,
                                               Serializable {
    private final String executorName;

    public DistributedProcessingTask(String executorName) {
        this.executorName = executorName;
    }

    protected void markComplete() {
        Hazelcast.getAtomicNumber(executorName).decrementAndGet();
    }

}
