package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.image.BufferedImage;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class Binner implements ParticleFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -2597307690489788311L;
    private int               binSize;

    public Binner(int size) {
        binSize = size;
    }

    @Override
    public Particle filter(Particle target) {
        int targetSize = target.getSize();
        int resultSize = targetSize / binSize;
        Particle result = new Particle(
                                       new BufferedImage(
                                                         resultSize,
                                                         resultSize,
                                                         target.asBufferedImage()
                                                               .getType()));
        // For each bin
        for (int y = 0; y < targetSize - binSize; y += binSize) {
            for (int x = 0; x < targetSize - binSize; x += binSize) {
                // Get all the pixels in the bin
                int[] binPixels = ColorUtils.extractRed(result.getRegionBuffer(x,
                                                                               y,
                                                                               binSize,
                                                                               binSize));
                // Average them
                int average = (int) MatrixUtils.average(binPixels);
                // Assign them to a pixel
                result.setPixel(x, y, average);
            }
        }

        return result;
    }
}
