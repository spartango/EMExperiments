package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

/**
 * Shifts the X, Y position of particles by a fixed amount
 * 
 * @author spartango
 * 
 */
public class Shifter implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -5825325037999113079L;

    // Shift Transformation to be applied to particles
    private AffineTransform   transform;

    /**
     * Build a new shifter that will move particles
     * 
     * @param x
     *            shift
     * @param y
     *            shift
     */
    public Shifter(double x, double y) { // Degrees
        transform = generateTransform(x, y);
    }

    private static AffineTransform generateTransform(double x, double y) {
        return AffineTransform.getTranslateInstance(x, y);
    }

    /**
     * Apply a shift to the particle, generating a new, shifted particle
     */
    @Override
    public Particle filter(Particle target) {
        return Particle.transform(target, transform);
    }

    /**
     * Undo the shift on a particle by shifting it the opposite direction
     * 
     * @param target
     *            to be unshifted
     * @return new, unshifted particle
     */
    public Particle reverseFilter(Particle target) {
        try {
            return Particle.transform(target, transform.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return target;
        }
    }

}
