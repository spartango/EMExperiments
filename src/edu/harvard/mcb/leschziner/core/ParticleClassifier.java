package edu.harvard.mcb.leschziner.core;

import java.util.Collection;

public interface ParticleClassifier {

    public Collection<Particle> getClassForTemplate(Particle template);

    public Particle getAverageForTemplate(Particle template);

    public Collection<Particle> getTemplates();

    public void classify(Particle target);

    public void addTemplate(Particle template);
}
