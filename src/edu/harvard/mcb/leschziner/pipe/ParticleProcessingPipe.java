package edu.harvard.mcb.leschziner.pipe;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public class ParticleProcessingPipe extends DistributedParticleConsumer
                                                                       implements
                                                                       ParticleSource {

    // Output queue for processed particles
    private final String                  processedQueueName;
    private final BlockingQueue<Particle> processedParticles;

    // Filters that are applied
    private final Vector<ParticleFilter>  stages;

    public ParticleProcessingPipe() {
        super();
        stages = new Vector<ParticleFilter>();
        processedQueueName = "Processed_" + this.hashCode();
        processedParticles = Hazelcast.getQueue(processedQueueName);

    }

    public void addStage(ParticleFilter filter) {
        stages.add(filter);
    }

    @Override public void processParticle(final Particle particle) {
        // Queuing a request
        execute(new ProcessingPipeTask(particle, stages, processedQueueName,
                                       executorName));
    }

    @Override public BlockingQueue<Particle> getParticleQueue() {
        return processedParticles;
    }

    public String getParticleQueueName() {
        return processedQueueName;
    }

}
