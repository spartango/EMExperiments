package edu.harvard.mcb.leschziner.distributed;

import com.hazelcast.core.AtomicNumber;

import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

/**
 * Handles tasks to be distributed for execution, keeping track of how many are
 * incomplete
 * 
 * @author spartango
 * 
 */
public abstract class DistributedTaskHandler {

    // Distributed Executor
    protected final String                  executorName;
    private final QueuedDistributedExecutor executor;

    // Number of tasks either awaiting execution or being executed
    protected final AtomicNumber            pendingCount;

    protected final AtomicNumber            totalRequests;

    /**
     * Constructs a task handler, prepping the distributed executor and task
     * tracker
     */
    public DistributedTaskHandler() {
        executorName = this.getClass().getSimpleName() + "_" + this.hashCode();
        // Use a queued executor to prevent flooding the cluster with tasks
        executor = new QueuedDistributedExecutor(executorName);
        pendingCount = DefaultStorageEngine.getStorageEngine()
                                           .getAtomicNumber(executorName
                                                            + DistributedProcessingTask.PENDING_SUFFIX);
        totalRequests = DefaultStorageEngine.getStorageEngine()
                                            .getAtomicNumber(executorName
                                                             + "_totalreqs");
    }

    /**
     * Request execution of a task sometime in the future
     * 
     * @param command
     *            to be executed
     */
    public void execute(DistributedProcessingTask command) {
        totalRequests.incrementAndGet();
        command.markPending();
        executor.execute(command);
    }

    /**
     * Kill this handler, preventing any new tasks from being added and any
     * unexecuted tasks from being executed
     */
    public void stop() {
        executor.shutdown();
    }

    /**
     * Checks if this handler has tasks pending
     * 
     * @return tasks pending?
     */
    public boolean isActive() {
        return pendingCount.get() > 0;
    }

    /**
     * Checks how many tasks remain to be completed
     * 
     * @return number of incomplete tasks
     */
    public long getPendingCount() {
        return pendingCount.get();
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }
}
