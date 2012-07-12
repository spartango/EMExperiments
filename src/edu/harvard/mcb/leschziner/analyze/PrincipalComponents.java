package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class PrincipalComponents {
    private final CvMat eigenValues;
    private final CvMat eigenVectors;
    private final CvMat subSpace;
    private final CvMat averages;

    public PrincipalComponents(CvMat eigenValues,
                               CvMat eigenVectors,
                               CvMat subSpace,
                               CvMat averages) {
        this.eigenValues = eigenValues;
        this.eigenVectors = eigenVectors;
        this.subSpace = subSpace;
        this.averages = averages;
    }

    public CvMat getEigenValues() {
        return eigenValues;
    }

    public CvMat getEigenVectors() {
        return eigenVectors;
    }

    public CvMat getSubSpace() {
        return subSpace;
    }

    public CvMat getAverages() {
        return averages;
    }

}
