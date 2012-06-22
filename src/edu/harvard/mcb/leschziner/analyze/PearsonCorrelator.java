package edu.harvard.mcb.leschziner.analyze;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.ColorUtils;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class PearsonCorrelator {

    public static double compare(Particle firstParticle, Particle secondParticle) {

        int[][] firstImage = firstParticle.getPixels();
        int[][] secondImage = secondParticle.getPixels();

        double imagesSum = 0;
        double firstImageSumN = 0;
        double secondImageSumN = 0;

        double firstImageAverage = MatrixUtils.average(ColorUtils.extractRed(firstImage));
        double secondImageAverage = MatrixUtils.average(ColorUtils.extractRed(secondImage));

        for (int y = 0; y < firstParticle.getSize(); y++) {
            for (int x = 0; x < firstParticle.getSize(); x++) {
                int firstImagePixel = ColorUtils.extractRed(firstImage[y][x]);
                int secondImagePixel = ColorUtils.extractRed(secondImage[y][x]);

                imagesSum = imagesSum + (firstImagePixel - firstImageAverage)
                            * (secondImagePixel - secondImageAverage);
                firstImageSumN = firstImageSumN
                                 + (firstImagePixel - firstImageAverage)
                                 * (firstImagePixel - firstImageAverage);
                secondImageSumN = secondImageSumN
                                  + (secondImagePixel - secondImageAverage)
                                  * (secondImagePixel - secondImageAverage);
            }
        }

        return imagesSum / Math.sqrt(firstImageSumN * secondImageSumN);
    }
}
