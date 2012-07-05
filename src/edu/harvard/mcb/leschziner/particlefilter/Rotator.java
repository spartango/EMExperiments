package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.geom.AffineTransform;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

/**
 * Rotates a particle by a fixed amount
 * 
 * @author spartango
 * 
 */
public class Rotator implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -6677740995143021586L;

    // The angle of the rotation to be performed in radians
    private double            radianAngle;

    /**
     * Build a rotator that rotates particles by a certain number of degrees
     * 
     * @param angle
     *            in degrees
     */
    public Rotator(double angle) { // Degrees
        radianAngle = Math.toRadians(angle);
    }

    private static AffineTransform generateTransform(double angle, int size) {
        return AffineTransform.getRotateInstance(angle, size / 2.0, size / 2.0);
    }

    /**
     * Rotate a particle, generating a new, rotated particle
     */
    @Override public Particle filter(Particle target) {
        AffineTransform transform = generateTransform(radianAngle,
                                                      target.getSize());
        return Particle.transform(target, transform);
    }

    /**
     * Un-rotate a particle by inverting the rotation angle
     * 
     * @param target
     *            particle
     * @return rotated particle
     */
    public Particle reverseFilter(Particle target) {
        AffineTransform transform = generateTransform(-radianAngle,
                                                      target.getSize());
        return Particle.transform(target, transform);
    }

}
