package edu.harvard.mcb.leschziner.filter;

import java.awt.geom.AffineTransform;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class Rotator implements ParticleFilter {

    private AffineTransform transform;

    public Rotator(double angle) { // Degrees
        transform = generateTransform(angle);
    }

    private static AffineTransform generateTransform(double angle) {
        double radianAngle = Math.toRadians(angle);
        return AffineTransform.getRotateInstance(radianAngle);
    }

    @Override
    public Particle filter(Particle target) {
        return Particle.transform(target, transform);
    }

}
