package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class ThresholdFilter implements ParticleFilter {

    private static final int BLACK = 0;
    private static final int WHITE = 0xFFFFFF;

    private int              threshold;

    public ThresholdFilter(int threshold) {
        this.threshold = threshold;
    }

    public Particle filter(Particle target) {
        // Copy the particle
        Particle filteredParticle = target.clone();
        // Goes across the particle, blacking out any pixel outside of the
        // circular radius
        int size = target.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int pixel = filteredParticle.getPixel(x, y);
                // Threshold Check
                if (pixel >= threshold) {
                    filteredParticle.setPixel(x, y, BLACK);
                } else {
                    filteredParticle.setPixel(x, y, WHITE);
                }
            }
        }
        return filteredParticle;
    }
}
