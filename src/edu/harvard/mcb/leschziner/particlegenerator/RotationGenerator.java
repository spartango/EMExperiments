package edu.harvard.mcb.leschziner.particlegenerator;

import java.util.Collection;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleGenerator;
import edu.harvard.mcb.leschziner.particlefilter.Rotator;

public class RotationGenerator implements ParticleGenerator {

    private Vector<Rotator> rotators;

    public RotationGenerator(double deltaTheta) {
        rotators = new Vector<Rotator>();
        for (double i = 0; i < 360; i += deltaTheta) {
            rotators.add(new Rotator(deltaTheta * i));
        }
    }

    @Override public Collection<Particle> generate(Particle seed) {
        // Apply each rotator to the particle
        Vector<Particle> rotated = new Vector<Particle>(rotators.size());
        for (Rotator rotator : rotators) {
            rotated.add(rotator.filter(seed));
        }
        return rotated;
    }

    @Override public Collection<Particle> generate(Collection<Particle> seeds) {
        Vector<Particle> rotated = new Vector<Particle>(seeds.size()
                                                        * rotators.size());
        for (Particle seed : seeds) {
            rotated.addAll(generate(seed));
        }
        return rotated;
    }
}
