package edu.harvard.mcb.leschziner.analyze;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class CrossCorrelator {

    public static double compare(Particle firstParticle, Particle secondParticle) {

        int[][] firstImage = firstParticle.getPixels();
        int[][] secondImage = secondParticle.getPixels();

        double imagesSum = 0;
        double firstImageSumN = 0;
        double secondImageSumN = 0;

        double firstImageAverage = MatrixUtils.average(firstImage);
        double secondImageAverage = MatrixUtils.average(secondImage);

        for (int y = 0; y < firstParticle.getSize(); y++) {
            for (int x = 0; x < firstParticle.getSize(); x++) {
                imagesSum = imagesSum + (firstImage[y][x] - firstImageAverage)
                            * (secondImage[y][x] - secondImageAverage);
                firstImageSumN = firstImageSumN
                                 + (firstImage[y][x] - firstImageAverage)
                                 * (firstImage[y][x] - firstImageAverage);
                secondImageSumN = secondImageSumN
                                  + (secondImage[y][x] - secondImageAverage)
                                  * (secondImage[y][x] - secondImageAverage);
            }
        }

        return imagesSum / Math.sqrt(firstImageSumN * secondImageSumN);
    }
}
