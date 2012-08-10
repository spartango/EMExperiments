package edu.harvard.mcb.leschziner.core;

import java.util.concurrent.BlockingQueue;

/**
 * Generates particles to be processed by consumers
 * 
 * @author spartango
 * 
 */
public interface ParticleSource {

    /**
     * Get the queue in which particles that are built can be found
     * 
     * @return the particle queue from which the consumer should take
     */
    public BlockingQueue<Particle> getParticleQueue();
}
