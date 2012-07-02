package edu.harvard.mcb.leschziner.core;

import java.util.Collection;

/**
 * Classifies particles into groups, each associated by similarity to a single
 * template
 * 
 * @author spartango
 * 
 */
public interface TemplateClassifier extends ParticleConsumer {

    public Collection<Particle> getClassForTemplate(long templateId);

    public Particle getAverageForTemplate(long templateId);

    public Collection<Particle> getTemplates();

    public Collection<Long> getTemplateIds();

    public void processParticle(Particle target);

    public void addTemplate(Particle template);

    public void addTemplates(Collection<Particle> templates);
}
