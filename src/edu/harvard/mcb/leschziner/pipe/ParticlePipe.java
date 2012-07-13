package edu.harvard.mcb.leschziner.pipe;

import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public abstract class ParticlePipe extends DistributedParticleConsumer
                                                                      implements
                                                                      ParticleSource {
    // Output queue for processed particles
    protected final String                  processedQueueName;
    protected final BlockingQueue<Particle> processedParticles;

    public ParticlePipe() {
        processedQueueName = this.getClass().getSimpleName() + "_Processed_"
                             + this.hashCode();
        processedParticles = Hazelcast.getQueue(processedQueueName);
    }

    @Override public BlockingQueue<Particle> getParticleQueue() {
        return processedParticles;
    }

    public String getParticleQueueName() {
        return processedQueueName;
    }

}
