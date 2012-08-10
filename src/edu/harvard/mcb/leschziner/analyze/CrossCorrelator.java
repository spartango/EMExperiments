package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.core.Particle;

public class CrossCorrelator {

    /**
     * Performs a pearson cross correlation between two particles
     * 
     * @param first
     *            Particle
     * @param second
     *            Particle
     * @return the correlation between the two Particles, between 0.0 and 1.0
     */
    public static double
            compare(Particle firstParticle, Particle secondParticle) {
        CvMat result = matchTemplate(firstParticle, secondParticle);
        double[] maxValues = new double[1];
        opencv_core.cvMinMaxLoc(result, new double[1], maxValues);

        return maxValues[0];
    }

    public static CvMat matchTemplate(Particle target, Particle template) {
        int resultSize = target.getSize() - template.getSize() + 1;
        CvMat result = CvMat.create(resultSize,
                                    resultSize,
                                    opencv_core.CV_32FC1);
        opencv_imgproc.cvMatchTemplate(target.getImage(),
                                       template.getImage(),
                                       result,
                                       opencv_imgproc.CV_TM_CCOEFF_NORMED);
        return result;
    }
}
