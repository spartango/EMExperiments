package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;

/**
 * Creates a circular mask that blacks out everything outside of a certain
 * radius in a particle
 * 
 * @author spartango
 * 
 */
public class CircularMask implements ParticleFilter {
    /**
     * 
     */
    private static final long serialVersionUID = -3268811096169325679L;

    // Radius of pixels to preserve
    private int               radius;

    // Amount to offset the circle from the Particle center
    private int               xOffset;
    private int               yOffset;

    /**
     * Build a new mask that masks out everything outside of a centered circle
     * 
     * @param radius
     *            of allowed circle
     */
    public CircularMask(int radius) {
        this(radius, 0, 0);
    }

    /**
     * Build a new mask that masks out everything outside of an offset circle
     * 
     * @param radius
     *            of allowed circle
     * @param x
     *            offset of the circle
     * @param y
     *            offset of the circle
     */
    public CircularMask(int radius, int xOffset, int yOffset) {
        this.radius = radius;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    /**
     * Generate a new particle by masking the target
     */
    @Override
    public Particle filter(Particle target) {
        // Copy the particle
        Particle filteredParticle = target.clone();
        // Goes across the particle, blacking out any pixel outside of the
        // circular radius
        int size = target.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // Radius Check
                double xShift = (size / 2.0) + xOffset;
                double yShift = (size / 2.0) + yOffset;
                double distanceSquared = (x - xShift) * (x - xShift)
                                         + (y - yShift) * (y - yShift);
                if (distanceSquared > (radius * radius)) {
                    filteredParticle.setPixel(x, y, ColorUtils.BLACK);
                }
            }
        }
        return filteredParticle;
    }

}
