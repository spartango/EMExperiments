package edu.harvard.mcb.leschziner.filter;

import java.awt.image.Kernel;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class GaussianFilter implements ParticleFilter {

    private Kernel kernel;

    public GaussianFilter(int radius) {
        kernel = generateKernel(radius);
    }

    @Override
    public Particle filter(Particle target) {
        return Particle.convolve(target, kernel);
    }

    private Kernel generateKernel(int radius) {
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

        return new Kernel(rows, 1, basis);
    }
}
