package edu.harvard.mcb.leschziner.core;

import java.util.Collection;

/**
 * Classifies particles into groups, each associated by similarity to a single
 * template
 * 
 * @author spartango
 * 
 */
public interface ParticleClassifier extends ParticleConsumer {

    public Collection<Particle> getClass(long classId);

    public Particle getClassAverage(long templateId);

    public Collection<Long> getClassIds();

    public void processParticle(Particle target);

    public void classifyAll();

}
