package edu.harvard.mcb.leschziner.core;

import java.io.Serializable;

public interface ParticleFilter extends Serializable {

    public Particle filter(Particle target);
}
