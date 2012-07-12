package edu.harvard.mcb.leschziner.pipe;

import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class ParticleFilteringPipe extends ParticlePipe {

    // Filters that are applied
    private final Vector<ParticleFilter> stages;

    public ParticleFilteringPipe() {
        super();
        stages = new Vector<ParticleFilter>();
    }

    public void addStage(ParticleFilter filter) {
        stages.add(filter);
    }

    @Override public void processParticle(final Particle particle) {
        // Queuing a request
        execute(new FilteringPipeTask(particle, stages, processedQueueName,
                                       executorName));
    }

}
