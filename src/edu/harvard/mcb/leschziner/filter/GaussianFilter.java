package edu.harvard.mcb.leschziner.filter;

import java.awt.image.Kernel;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class GaussianFilter implements ParticleFilter {

    private Kernel xKernel;
    private Kernel yKernel;

    public GaussianFilter(int radius) {
        xKernel = generateXKernel(radius);
        yKernel = generateYKernel(radius);
    }

    @Override
    public Particle filter(Particle target) {
        // Apply each 1D filter
        Particle filtered = Particle.convolve(target, xKernel);
        filtered = Particle.convolve(filtered, yKernel);
        return filtered;
    }

    private static Kernel generateXKernel(int radius) {
        float[] xDistribution = generateGaussian(radius);
        return new Kernel(xDistribution.length, 1, xDistribution);
    }

    private static Kernel generateYKernel(int radius) {
        float[] yDistribution = generateGaussian(radius);
        return new Kernel(1, yDistribution.length, yDistribution);
    }

    private static float[] generateGaussian(int radius) {
        // Build kernel
        int rCeil = (int) Math.ceil(radius);
        int rows = rCeil * 2 + 1;
        float[] basis = new float[rows];
        float sigma = radius / 3;
        float sigma2sq = 2 * sigma * sigma;
        float sigmaPi2 = (float) (2 * Math.PI * sigma);
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radiusSq = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -rCeil; row <= rCeil; row++) {
            float distance = row * row;
            if (distance > radiusSq) {
                basis[index] = 0;
            } else {
                basis[index] = (float) Math.exp(-distance / sigma2sq)
                               / sqrtSigmaPi2;
            }
            total += basis[index];
            index++;
        }
        for (int i = 0; i < rows; i++) {
            basis[i] /= total;
        }

        return basis;
    }
}
