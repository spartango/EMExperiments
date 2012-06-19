package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;

public class ThresholdFilter implements ParticleFilter {

    private int threshold;

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
                int pixel = ColorUtils.extractRed(filteredParticle.getPixel(x,
                                                                            y));
                // Threshold Check
                if (pixel >= threshold) {
                    filteredParticle.setPixel(x, y, ColorUtils.WHITE);
                } else {
                    filteredParticle.setPixel(x, y, ColorUtils.BLACK);
                }
            }
        }
        return filteredParticle;
    }
}
