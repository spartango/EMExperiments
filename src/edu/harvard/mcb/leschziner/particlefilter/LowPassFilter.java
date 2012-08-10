package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.image.Kernel;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

/**
 * A simple low-pass filter that utilizes a 1/size kernel
 * 
 * @author spartango
 * 
 */
public class LowPassFilter implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 5558259888147858661L;

    // Size of the filter
    private int               size;

    // Low pass kernel to be convolved (cannot be serialized)
    private transient Kernel  kernel;

    /**
     * Builds a low pass filter of a given size
     * 
     * @param size
     */
    public LowPassFilter(int size) {
        this.size = size;
    }

    private static Kernel generateKernel(int size) {
        float[] basis = new float[size * size];

        float value = 1.0f / (size * size);

        for (int i = 0; i < basis.length; i++) {
            basis[i] = value;
        }
        return new Kernel(size, size, basis);
    }

    /**
     * Applies the low pass filter to a particle, generating a new, filtered
     * particle
     */
    @Override public Particle filter(Particle target) {
        if (kernel == null) {
            kernel = generateKernel(size);
        }
        return Particle.convolve(target, kernel);
    }

}
