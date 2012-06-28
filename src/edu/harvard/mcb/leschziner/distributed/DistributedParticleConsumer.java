package edu.harvard.mcb.leschziner.distributed;

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
import edu.harvard.mcb.leschziner.core.ParticleSource;

public abstract class DistributedParticleConsumer implements ParticleConsumer,
                                                 ItemListener<Particle> {
    // Distributed Executor
    protected final String                 executorName;
    protected final ExecutorService        executor;
    protected final AtomicNumber           pendingCount;

    // Input Sources being watched
    protected final Vector<ParticleSource> particleSources;

    public DistributedParticleConsumer() {
        particleSources = new Vector<ParticleSource>();

        executorName = this.getClass().getName() + "_" + this.hashCode();
        executor = Hazelcast.getExecutorService(executorName);
        pendingCount = Hazelcast.getAtomicNumber(executorName);
    }

    @Override
    public void addParticleSource(ParticleSource p) {
        particleSources.add(p);
        // Attach as listener
        ((ICollection<Particle>) p.getParticleQueue()).addItemListener(this,
                                                                       true);
    }

    public boolean isActive() {
        return pendingCount.get() > 0;
    }

    public long getPendingCount() {
        return pendingCount.get();
    }

    public abstract void processParticle(final Particle particle);

    private static BlockingQueue<Particle> queueNameFromEvent(ItemEvent<Particle> event) {
        String sourceName = event.getSource().toString();
        if (sourceName.startsWith("q:")) {

            String queueName = sourceName.substring(2);
            return Hazelcast.getQueue(queueName);
        } else {
            return null;
        }
    }

    @Override
    public void itemAdded(ItemEvent<Particle> e) {
        BlockingQueue<Particle> queue = queueNameFromEvent(e);
        if (queue != null) {
            // Queue Draining
            Particle target = queue.poll();

            if (target != null) {
                processParticle(target);
            }
        }
    }

    @Override
    public void itemRemoved(ItemEvent<Particle> arg0) {
        // Don't really care when items are removed
    }

    public void stop() {
        executor.shutdown();
    }

}
