package edu.harvard.mcb.leschziner.particlefilter;

import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

/**
 * Applies a gaussian filter to a particle
 * 
 * @author spartango
 * 
 */
public class GaussianFilter implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -5419594820002104375L;

    // Radius of the filtering kernel
    private final int         radius;

    /**
     * Builds a gaussian filter
     * 
     * @param radius
     */
    public GaussianFilter(int radius) {
        this.radius = radius;
    }

    /**
     * Applies a gaussian blur to the particle
     */
    @Override public Particle filter(Particle target) {
        Particle result = target.createCompatible();

        // The kernel is separable, so we'll use 1D kernels
        opencv_imgproc.cvSmooth(target.getImage(),
                                result.getImage(),
                                opencv_imgproc.CV_GAUSSIAN,
                                radius,
                                radius,
                                0,
                                0);

        return result;
    }
}
