package edu.harvard.mcb.leschziner.core;

import java.util.Collection;

public interface TemplateClassifier extends ParticleConsumer {

    public Collection<Particle> getClassForTemplate(Particle template);

    public Particle getAverageForTemplate(Particle template);

    public Collection<Particle> getTemplates();

    public void processParticle(Particle target);

    public void addTemplate(Particle template);

    public void addTemplates(Collection<Particle> templates);
}
