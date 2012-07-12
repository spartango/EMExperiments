package edu.harvard.mcb.leschziner.pipe;

import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleGenerator;

public class ParticleGeneratingPipe extends ParticlePipe {

    private final Vector<ParticleGenerator> stages;

    public ParticleGeneratingPipe() {
        super();
        stages = new Vector<ParticleGenerator>();
    }

    @Override public void processParticle(Particle particle) {
        execute(new GeneratingPipeTask(particle, stages, processedQueueName,
                                       executorName));
    }

    public void addStage(ParticleGenerator generator) {
        stages.add(generator);
    }
}
