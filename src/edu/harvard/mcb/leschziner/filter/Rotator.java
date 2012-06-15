package edu.harvard.mcb.leschziner.filter;

import java.awt.geom.AffineTransform;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class Rotator implements ParticleFilter {

    private double radianAngle;

    public Rotator(double angle) { // Degrees
        radianAngle = Math.toRadians(angle);
    }

    private static AffineTransform generateTransform(double angle, int size) {
        return AffineTransform.getRotateInstance(angle, size / 2.0, size / 2.0);
    }

    @Override
    public Particle filter(Particle target) {
        AffineTransform transform = generateTransform(radianAngle, target.getSize());
        return Particle.transform(target, transform);
    }

}
