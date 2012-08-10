package edu.harvard.mcb.leschziner.core;

import java.io.Serializable;

/**
 * Performs an alteration on a particle that generates a new, modified particle
 * 
 * @author spartango
 * 
 */
public interface ParticleFilter extends Serializable {

    /**
     * Apply the alteration to the target particle, generating a new, altered
     * particle
     * 
     * @param target
     *            to be altered
     * @return new particle that is the altered form of the target
     */
    public Particle filter(Particle target);
}
