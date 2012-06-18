package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.image.Kernel;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class LowPassFilter implements ParticleFilter {

    private Kernel kernel;

    public LowPassFilter(int size) {
        kernel = generateKernel(size);
    }

    private static Kernel generateKernel(int size) {
        float[] basis = new float[size * size];

        float value = 1.0f / (size * size);

        for (int i = 0; i < basis.length; i++) {
            basis[i] = value;
        }
        return new Kernel(size, size, basis);
    }

    @Override
    public Particle filter(Particle target) {
        return Particle.convolve(target, kernel);
    }

}
