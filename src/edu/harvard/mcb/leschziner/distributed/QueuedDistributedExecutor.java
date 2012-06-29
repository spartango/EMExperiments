package edu.harvard.mcb.leschziner.distributed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;

/**
 * A queue that sits in front of an unbounded, distributed executor service,
 * holding onto tasks until cluster capacity is available.
 * 
 * @author spartango
 * 
 */
public class QueuedDistributedExecutor implements Runnable {
    public static final int                          POLL_TIME           = 250;

    public static int                                defaultNodeCapacity = 8;

    // The underlying executor
    private ExecutorService                          executor;

    // Number of active tasks
    private AtomicNumber                             activeTasks;
    // Cluster nodes
    private Cluster                                  cluster;
    // Number of tasks that can be run per node
    private int                                      nodeCapacity;

    // Tasks waiting to be sent to the executor
    private BlockingQueue<DistributedProcessingTask> queuedTasks;

    // Thread that executes queued tasks when there is capacity available
    private boolean                                  running;
    private Thread                                   execThread;

    public QueuedDistributedExecutor(String executorName) {
        this(executorName, defaultNodeCapacity);
    }

    public QueuedDistributedExecutor(String executorName, int nodeCapacity) {
        this.executor = Hazelcast.getExecutorService(executorName);
        this.activeTasks = Hazelcast.getAtomicNumber(executorName
                                                     + DistributedProcessingTask.ACTIVE_SUFFIX);
        this.cluster = Hazelcast.getCluster();
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

    public void shutdown() {
        running = false;
        execThread.interrupt();
    }

    /**
     * Pulls from the queue when there is capacity available
     */
    @Override
    public void run() {
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