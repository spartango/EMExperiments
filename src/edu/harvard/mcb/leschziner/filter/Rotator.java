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
        double cos = Math.cos(radianAngle);
        double sin = Math.sin(radianAngle);
        return new AffineTransform(cos, sin, -sin, cos, 0, 0);
    }

    @Override
    public Particle filter(Particle target) {
        return Particle.transform(target, transform);
    }

}
