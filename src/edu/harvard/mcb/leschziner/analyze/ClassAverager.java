package edu.harvard.mcb.leschziner.analyze;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.ColorUtils;

public class ClassAverager {

    private static final int RED_OFFSET   = 0;
    private static final int GREEN_OFFSET = 1;
    private static final int BLUE_OFFSET  = 2;

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
                int[] pixelBuffer = particle.getPixelBuffer();
                for (int i = 0; i < pixelBuffer.length; i++) {
                    // For each pixel
                    // Extract each color and add it to the sums
                    int pixel = pixelBuffer[i];
                    sums[i + RED_OFFSET] += ColorUtils.extractRed(pixel);
                    sums[i + GREEN_OFFSET] += ColorUtils.extractGreen(pixel);
                    sums[i + BLUE_OFFSET] += ColorUtils.extractBlue(pixel);
                }
            }

            // Divide sums by the number of images
            int[] avgBuffer = new int[size * size];
            for (int i = 0; i < sums.length; i += 3) {
                avgBuffer[i] = ColorUtils.buildColor((int) (sums[i + RED_OFFSET] / particleCount),
                                                     (int) (sums[i
                                                                 + GREEN_OFFSET] / particleCount),
                                                     (int) (sums[i
                                                                 + BLUE_OFFSET] / particleCount));
            }

            BufferedImage avgImage = new BufferedImage(
                                                       size,
                                                       size,
                                                       particle.asBufferedImage()
                                                               .getType());

            // Copy in a pixel buffer
            avgImage.setRGB(0, 0, size, size, avgBuffer, 0, size);

            Particle average = new Particle(avgImage);
            return average;
        } else {
            return null;
        }
    }
}
