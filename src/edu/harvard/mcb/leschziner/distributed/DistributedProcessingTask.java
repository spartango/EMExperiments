package edu.harvard.mcb.leschziner.distributed;

import java.io.Serializable;

import edu.harvard.mcb.leschziner.event.CompletionEvent;
import edu.harvard.mcb.leschziner.event.ErrorEvent;
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
        long startTime = System.currentTimeMillis();
        try {
            process();
        } catch (Exception e) {
            // Sandbox the processing
            markError("", e, 0);
        }
        markComplete(System.currentTimeMillis() - startTime);
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
    protected void markError(String error, int lineNumber) {
        System.err.println("Error on " + this + ": " + error);
        DefaultStorageEngine.getStorageEngine()
                            .getBufferedQueue(executorName)
                            .add(new ErrorEvent(this.getClass().getName(),
                                                error,
                                                lineNumber));
    }

    /**
     * Report an error on this task, with the exception that caused it
     * 
     * @param error
     * @param e
     */
    public void markError(String error, Exception e, int lineNumber) {
        String errorString = error + ": " + e.getMessage();
        System.err.println(errorString);
        DefaultStorageEngine.getStorageEngine()
                            .getBufferedQueue(executorName)
                            .add(new ErrorEvent(this.getClass().getName(),
                                                errorString,
                                                lineNumber,
                                                e.getClass().getSimpleName()));
    }

    /**
     * Mark this task complete
     */
    private void markComplete(long runtime) {
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();
        storage.getAtomicNumber(executorName + PENDING_SUFFIX)
               .decrementAndGet();
        storage.getAtomicNumber(executorName + ACTIVE_SUFFIX).decrementAndGet();

        // Put an event on our event queue
        storage.getBufferedQueue(executorName)
               .add(new CompletionEvent(this.getClass().getName(), runtime));
    }

}
