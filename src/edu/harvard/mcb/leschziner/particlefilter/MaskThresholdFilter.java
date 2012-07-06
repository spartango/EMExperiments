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
public class MaskThresholdFilter implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 4463626671980599698L;
    // Value of pixel above which to allow
    private final int         threshold;

    /**
     * Build a new threshold filter
     * 
     * @param threshold
     */
    public MaskThresholdFilter(int threshold) {
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
                                   opencv_imgproc.CV_THRESH_TOZERO);

        return filteredParticle;
    }
}
