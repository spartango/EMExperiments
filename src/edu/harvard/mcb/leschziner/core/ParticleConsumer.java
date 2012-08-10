package edu.harvard.mcb.leschziner.core;

/**
 * Consumes particles from a particle source continously
 * 
 * @author spartango
 * 
 */
public interface ParticleConsumer {
    // Attach a particle source for this consumer to take particles from
    public void addParticleSource(ParticleSource p);
}
