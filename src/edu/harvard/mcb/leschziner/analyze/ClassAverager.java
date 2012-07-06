package edu.harvard.mcb.leschziner.analyze;

import java.util.Collection;
import java.util.Iterator;

import edu.harvard.mcb.leschziner.core.Particle;

public class ClassAverager {
    private static final int RED_OFFSET   = 0;
    private static final int GREEN_OFFSET = 1;
    private static final int BLUE_OFFSET  = 2;

    /**
     * Averages a set of particles to generate an average, summing each pixel
     * location across all particles, then dividing by the number of images.
     * Average does segregate colorchannels. Returns null if the particle set is
     * empty
     * 
     * @param particles
     *            : to be averaged
     * @return A single average particle
     */
    public static Particle average(Collection<Particle> particles) {
        Iterator<Particle> iter = particles.iterator();
        int particleCount = particles.size();

        if (iter.hasNext()) {
            Particle particle = iter.next();
            // Get the particle dimensions
            int size = particle.getSize();

            // Allocate a sum buffer
            long[] sums = new long[3 * size * size];
            for (; iter.hasNext(); particle = iter.next()) {
                // Get the pixels (RGB) from the image
                int i = 0;
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        sums[i + RED_OFFSET] += particle.getPixelRed(x, y);
                        sums[i + GREEN_OFFSET] += particle.getPixelGreen(x, y);
                        sums[i + BLUE_OFFSET] += particle.getPixelBlue(x, y);
                        i += 3;
                    }
                }
            }

            Particle average = particle.createCompatible();
            int i = 0;
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    average.setPixelRed(x,
                                        y,
                                        (int) (sums[i + RED_OFFSET] / particleCount));
                    average.setPixelGreen(x,
                                          y,
                                          (int) (sums[i + GREEN_OFFSET] / particleCount));
                    average.setPixelBlue(x,
                                         y,
                                         (int) (sums[i + BLUE_OFFSET] / particleCount));
                    i += 3;
                }
            }

            return average;
        } else {
            return null;
        }
    }
}
