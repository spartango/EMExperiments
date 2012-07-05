package edu.harvard.mcb.leschziner.particlefilter;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
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
        CvMat kernel = opencv_imgproc.getGaussianKernel(radius, -1,
                                                        opencv_core.CV_32F);

        opencv_imgproc.sepFilter2D(target.getImage(), result.getImage(), 3,
                                   kernel, kernel, new CvPoint(-1, 1), 0.0,
                                   opencv_imgproc.BORDER_DEFAULT);

        return result;
    }
}
