package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core;

import edu.harvard.mcb.leschziner.core.Particle;

public class PearsonCorrelator {

    /**
     * Performs a pearson cross correlation between two particles
     * 
     * @param first
     *            Particle
     * @param second
     *            Particle
     * @return the correlation between the two Particles, between 0.0 and 1.0
     */
    public static double compare(Particle firstParticle, Particle secondParticle) {

        double imagesSum = 0;
        double firstImageSumN = 0;
        double secondImageSumN = 0;

        double firstImageAverage = opencv_core.cvAvg(firstParticle.getImage(),
                                                     null).red();
        double secondImageAverage = opencv_core.cvAvg(secondParticle.getImage(),
                                                      null).red();

        for (int y = 0; y < firstParticle.getSize(); y++) {
            for (int x = 0; x < firstParticle.getSize(); x++) {
                int firstImagePixel = firstParticle.getPixelRed(x, y);
                int secondImagePixel = secondParticle.getPixelRed(x, y);

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
