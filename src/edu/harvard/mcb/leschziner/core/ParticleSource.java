package edu.harvard.mcb.leschziner.core;

import java.util.concurrent.BlockingQueue;

public interface ParticleSource {

    // Get Queue in which particles are entered
    public BlockingQueue<Particle> getParticleQueue();
}
