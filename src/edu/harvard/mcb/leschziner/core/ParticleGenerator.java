package edu.harvard.mcb.leschziner.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * Given a single seed particle, generates many particles that are somehow
 * variations of the seed
 * 
 * @author spartango
 * 
 */
public interface ParticleGenerator extends Serializable {
    /**
     * Generate a series of particles from a seed
     * 
     * @param seed
     *            particle to derive new particles from
     * @return new particles
     */
    public Collection<Particle> generate(Particle seed);

    /**
     * Generate a series of particles from many seed particles
     * 
     * @param seed
     *            particles to be used to generate new ones
     * @return new particles generated from the seeds
     */
    public Collection<Particle> generate(Collection<Particle> seeds);
}
