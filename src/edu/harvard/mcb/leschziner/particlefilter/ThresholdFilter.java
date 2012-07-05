package edu.harvard.mcb.leschziner.particlefilter;

import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

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
    @Override public Particle filter(Particle target) {
        // Copy the particle
        Particle filteredParticle = target.createCompatible();

        opencv_imgproc.cvThreshold(target.getImage(),
                                   filteredParticle.getImage(), threshold, 255,
                                   opencv_imgproc.CV_THRESH_BINARY);

        return filteredParticle;
    }
}
