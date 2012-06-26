package edu.harvard.mcb.leschziner.pipe;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleConsumer;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticleSource;

public class ParticleProcessingPipe implements ItemListener<Particle>,
                                   ParticleSource, ParticleConsumer {

    // Distributed Executor
    private final String                  executorName;
    private final ExecutorService         executor;
    private final AtomicNumber            pendingCount;

    // Output queue for processed particles
    private final String                  processedQueueName;
    private final BlockingQueue<Particle> processedParticles;

    // Input Sources being watched
    private final Vector<ParticleSource>  particleSources;

    // Filters that are applied
    private final Vector<ParticleFilter>  stages;

    public ParticleProcessingPipe() {

        stages = new Vector<ParticleFilter>();

        particleSources = new Vector<ParticleSource>();

        processedQueueName = "Processed_" + this.hashCode();
        processedParticles = Hazelcast.getQueue(processedQueueName);

        executorName = "ProcessingPipe_" + this.hashCode();
        executor = Hazelcast.getExecutorService(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName);
    }

    public void addStage(ParticleFilter filter) {
        stages.add(filter);
    }

    public void processParticle(final Particle particle) {
        // Queuing a request
        executor.execute(new ProcessingPipeTask());
    }

    public void stop() {
        executor.shutdown();
    }

    @Override
    public void itemAdded(ItemEvent<Particle> e) {
        if (e.getSource() instanceof Queue) {
            Particle target = ((BlockingQueue<Particle>) e.getSource()).poll();
            if (target != null) {
                processParticle(target);
            }
        }
    }

    @Override
    public void itemRemoved(ItemEvent<Particle> arg0) {
        // Don't really care when items are removed
    }

    @Override
    public BlockingQueue<Particle> getParticleQueue() {
        return processedParticles;
    }

    public String getParticleQueueName() {
        return processedQueueName;
    }

    public boolean isActive() {
        return pendingCount.get() > 0;
    }

    public long getPendingCount() {
        return pendingCount.get();
    }

    @Override
    public void addParticleSource(ParticleSource p) {
        particleSources.add(p);
        // Attach as listener
        if (p.getParticleQueue() instanceof ICollection)
            ((ICollection<Particle>) p.getParticleQueue()).addItemListener(this,
                                                                           true);
    }
}
