package edu.harvard.mcb.leschziner.distributed;

import java.io.Serializable;

import com.hazelcast.core.Hazelcast;

public abstract class DistributedProcessingTask implements Serializable,
                                               Runnable {
    /**
     * 
     */
    private static final long  serialVersionUID = -7555516754323196893L;

    // SUFFIXES for counters of task activity
    public static final String PENDING_SUFFIX   = "_pending";
    public static final String ACTIVE_SUFFIX    = "_active";

    // Name of distributed executor which handles the task
    private final String       executorName;

    /**
     * Build a new task that can be distributed for execution
     * 
     * @param executorName
     */
    public DistributedProcessingTask(String executorName) {
        this.executorName = executorName;
    }

    /**
     * Actually do whatever the task must do
     */
    public abstract void process();

    /**
     * Run the task, noting its completion in the pending and activity trackers
     * when it finishes
     */
    @Override public void run() {
        process();
        markComplete();
    }

    /**
     * Mark this task pending, noting that it hasn't been completed and may be
     * either queued or actively underway.
     */
    public void markPending() {
        Hazelcast.getAtomicNumber(executorName + PENDING_SUFFIX)
                 .incrementAndGet();
    }

    /**
     * Mark this task as actively being run
     */
    public void markActive() {
        Hazelcast.getAtomicNumber(executorName + ACTIVE_SUFFIX)
                 .incrementAndGet();
    }

    /**
     * Mark this task complete
     */
    private void markComplete() {
        Hazelcast.getAtomicNumber(executorName + PENDING_SUFFIX)
                 .decrementAndGet();
        Hazelcast.getAtomicNumber(executorName + ACTIVE_SUFFIX)
                 .decrementAndGet();
    }

}
