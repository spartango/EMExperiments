package edu.harvard.mcb.leschziner.core;

import java.util.Collection;

public interface ParticleGenerator {
    public Collection<Particle> generate(Particle seed);
}