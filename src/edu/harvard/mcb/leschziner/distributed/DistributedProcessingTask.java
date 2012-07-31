package edu.harvard.mcb.leschziner.distributed;

import java.io.Serializable;

import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

public abstract class DistributedProcessingTask implements
                                               Serializable,
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
        try {
            process();
        } catch (Exception e) {
            // Sandbox the processing
            markError(e.getClass().getSimpleName()
                              + " Thrown: "
                              + e.getMessage(),
                      e);
        }
        markComplete();
    }

    /**
     * Mark this task pending, noting that it hasn't been completed and may be
     * either queued or actively underway.
     */
    public void markPending() {
        DefaultStorageEngine.getStorageEngine()
                            .getAtomicNumber(executorName + PENDING_SUFFIX)
                            .incrementAndGet();
    }

    /**
     * Mark this task as actively being run
     */
    public void markActive() {
        DefaultStorageEngine.getStorageEngine()
                            .getAtomicNumber(executorName + ACTIVE_SUFFIX)
                            .incrementAndGet();
    }

    /**
     * Report an error on this task
     * 
     * @param error
     */
    protected void markError(String error) {
        // TODO Queue errors for aggregation
        System.err.println("Error on " + this + ": " + error);
    }

    /**
     * Report an error on this task, with the exception that caused it
     * 
     * @param error
     * @param e
     */
    protected void markError(String error, Exception e) {
        // TODO Queue errors for aggregation
        System.err.println("Error on " + this + ": " + error + " with " + e);
    }

    /**
     * Mark this task complete
     */
    private void markComplete() {
        // TODO send out event
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();
        storage.getAtomicNumber(executorName + PENDING_SUFFIX)
               .decrementAndGet();
        storage.getAtomicNumber(executorName + ACTIVE_SUFFIX).decrementAndGet();
    }

}
