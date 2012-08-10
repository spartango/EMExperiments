package edu.harvard.mcb.leschziner.particlesource;

import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticlePicker;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public abstract class DistributedParticlePicker extends
                                               DistributedParticleConsumer implements
                                                                          ParticlePicker {

    // Size of area picked around particle
    protected final int                     boxSize;

    // Queue of particles produced
    protected final String                  particleQueueName;
    protected final BlockingQueue<Particle> extractedParticles;

    public DistributedParticlePicker(int boxSize) {
        super();
        this.boxSize = boxSize;
        // Pull up output queue
        particleQueueName = "ExtractedParticles_" + this.hashCode();

        extractedParticles = DefaultStorageEngine.getStorageEngine()
                                                 .getQueue(particleQueueName);
    }

    public String getParticleQueueName() {
        return particleQueueName;
    }

    @Override public BlockingQueue<Particle> getParticleQueue() {
        return extractedParticles;
    }

    @Override public void processParticle(Particle particle) {
        processMicrograph(particle);
    }
}
