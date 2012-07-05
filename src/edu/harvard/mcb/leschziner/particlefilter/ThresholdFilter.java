package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;

/**
 * Thresholds black and white particles, blacking out pixels below a certain
 * value, and marking the remaining particles white. Ignores color channels.
 * 
 * @author spartango
 * 
 */
public class ThresholdFilter implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -5385792577940651526L;

    // Value of pixel above which to allow
    private final int         threshold;

    /**
     * Build a new threshold filter
     * 
     * @param threshold
     */
    public ThresholdFilter(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Mark Particle pixels above the threshold value white, and the rest black,
     * generating a new particle that is the thresholded particle
     * 
     * @param target
     *            particle
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
                int pixel = filteredParticle.getPixelRed(x, y);
                // Threshold Check
                if (pixel >= threshold) {
                    // Mark pixels above threshold white
                    filteredParticle.setPixel(x, y, ColorUtils.WHITE);
                } else {
                    // Mark pixels below threshold black
                    filteredParticle.setPixel(x, y, ColorUtils.BLACK);
                }
            }
        }
        return filteredParticle;
    }
}
