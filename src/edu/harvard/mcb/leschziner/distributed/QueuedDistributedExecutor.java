package edu.harvard.mcb.leschziner.distributed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

/**
 * A queue that sits in front of an unbounded, distributed executor service,
 * holding onto tasks until cluster capacity is available.
 * 
 * @author spartango
 * 
 */
public class QueuedDistributedExecutor implements Runnable {
    // Time between cluster capacity checks
    public static final int                                POLL_TIME           = 250;

    // Default number of tasks that can be run on each node
    public static int                                      defaultNodeCapacity = 8;

    // The underlying executor
    private final ExecutorService                          executor;

    // Number of active tasks
    private final AtomicNumber                             activeTasks;
    // Cluster nodes
    private final Cluster                                  cluster;
    // Number of tasks that can be run per node
    private final int                                      nodeCapacity;

    // Tasks waiting to be sent to the executor
    private final BlockingQueue<DistributedProcessingTask> queuedTasks;

    // Thread that executes queued tasks when there is capacity available
    private boolean                                        running;
    private final Thread                                   execThread;

    /**
     * Builds a distributed executor that queues tasks until cluster capacity is
     * available, calculating capacity from the number of nodes and a default
     * number of nodes
     * 
     * @param name
     *            of distributed executor which will disperse tasks across a
     *            cluster
     */
    public QueuedDistributedExecutor(String executorName) {
        this(executorName, defaultNodeCapacity);
    }

    /**
     * Builds a distributed executor that queues tasks until cluster capacity is
     * available
     * 
     * @param name
     *            of distributed executor which will disperse tasks across a
     *            cluster
     * @param nodeCapacity
     *            : number of tasks that can be run on each node
     */
    public QueuedDistributedExecutor(String executorName, int nodeCapacity) {
        this.executor = DefaultExecutor.getExecutor(executorName);
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();
        this.cluster = Hazelcast.getCluster();

        this.activeTasks = storage.getAtomicNumber(executorName
                                                   + DistributedProcessingTask.ACTIVE_SUFFIX);
        this.nodeCapacity = nodeCapacity;
        this.queuedTasks = new LinkedBlockingQueue<DistributedProcessingTask>();

        // Spin up the consumer thread
        this.running = true;
        execThread = new Thread(this);
        execThread.start();
    }

    /**
     * Request execution of a task
     * 
     * @param task
     */
    public void execute(DistributedProcessingTask task) {
        queuedTasks.add(task);
    }

    /**
     * Prevent any unexecuted tasks from being executed
     */
    public void shutdown() {
        running = false;
        execThread.interrupt();
        executor.shutdown();
    }

    /**
     * Pulls from the queue when there is capacity available
     */
    @Override public void run() {
        while (running) {
            try {
                // Check for capacity available vs number of running tasks
                if (activeTasks.get() < getCapacity()) {
                    // If capacity is available, grab any queued tasks
                    DistributedProcessingTask task = queuedTasks.take();
                    task.markActive();
                    executor.execute(task);
                } else {
                    // Sleep for a bit, waiting for tasks to finish
                    Thread.sleep(POLL_TIME);
                }
            } catch (InterruptedException e) {
                // Go around and check that we're still supposed to be
                // running
            }
        }

    }

    private int getCapacity() {
        return cluster.getMembers().size() * nodeCapacity;
    }

}
