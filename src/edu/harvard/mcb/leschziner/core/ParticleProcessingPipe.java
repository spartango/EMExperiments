package edu.harvard.mcb.leschziner.core;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

public class ParticleProcessingPipe implements ItemListener<Particle> {
    public static int                     CORE_POOL  = 4;
    public static int                     MAX_POOL   = 8;
    public static int                     KEEP_ALIVE = 1000;

    // Distributed Executor
    private final String                  executorName;
    private final ExecutorService         executor;
    private final AtomicNumber            pendingCount;

    // Input Queues being watched

    // Output queue for processed particles
    private final String                  processedQueueName;
    private final BlockingQueue<Particle> processedParticles;

    // Filters that are applied
    private final Vector<ParticleFilter>  stages;

    public ParticleProcessingPipe() {

        stages = new Vector<ParticleFilter>();

        executorName = "ProcessingPipe_" + this.hashCode();
        executor = Hazelcast.getExecutorService(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName);
    }

    public void addStage(ParticleFilter filter) {
        stages.add(filter);
    }

    private void handleParticle(Particle target) {
        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
    }

    public void processParticle(final Particle particle) {
        // Queuing a request
        executor.execute(new Runnable() {
            @Override
            public void run() {
                handleParticle(particle);
            }
        });
    }

    public void stop() {
        executor.shutdown();
    }

    @Override
    public void itemAdded(ItemEvent<Particle> e) {
        // A particle is ready for processing

        // Take from the queue and create a processing job

    }

    @Override
    public void itemRemoved(ItemEvent<Particle> arg0) {
        // Don't really care when items are removed
    }
}
