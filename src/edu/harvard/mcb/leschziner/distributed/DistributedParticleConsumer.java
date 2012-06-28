package edu.harvard.mcb.leschziner.distributed;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleConsumer;
import edu.harvard.mcb.leschziner.core.ParticleSource;

public abstract class DistributedParticleConsumer extends
        DistributedTaskHandler implements ParticleConsumer,
                              ItemListener<Particle> {

    // Input Sources being watched
    protected final Vector<ParticleSource> particleSources;

    public DistributedParticleConsumer() {
        super();
        particleSources = new Vector<ParticleSource>();
    }

    @Override
    public void addParticleSource(ParticleSource p) {
        particleSources.add(p);
        // Attach as listener
        ((ICollection<Particle>) p.getParticleQueue()).addItemListener(this,
                                                                       true);
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

}
