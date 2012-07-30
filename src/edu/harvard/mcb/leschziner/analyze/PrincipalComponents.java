package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

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

    public double getEigenValue(int componentIndex) {
        return eigenValues.get(componentIndex, 0);
    }

    public CvMat getEigenVector(int componentIndex) {
        CvMat eigenVector = CvMat.createHeader(1,
                                               eigenVectors.cols(),
                                               opencv_core.CV_32FC1);
        opencv_core.cvGetRow(eigenVectors, eigenVector, componentIndex);
        return eigenVector;
    }

    public IplImage getEigenImage(int componentIndex) {
        CvMat eigenVector = getEigenVector(componentIndex);
        // Turn the eigenvector into an eigenimage
        int particleSize = (int) Math.round(Math.sqrt(eigenVector.cols()));

        // Reshape the columns into an image
        CvMat target = CvMat.create(particleSize,
                                    particleSize,
                                    opencv_core.CV_32FC1);
        opencv_core.cvReshape(eigenVector, target, 1, particleSize);

        // Normalize it by its minimum and max
        CvMat normalized = CvMat.create(target.rows(),
                                        target.cols(),
                                        target.type());
        opencv_core.cvNormalize(target,
                                normalized,
                                0,
                                1,
                                opencv_core.CV_MINMAX,
                                null);

        // Convert the scale to 8 bit integer
        IplImage image = IplImage.create(normalized.cols(),
                                         normalized.rows(),
                                         8,
                                         normalized.channels());
        opencv_core.cvConvertScale(normalized, image, 255, 0);

        return image;
    }

    public double getAverage(int componentIndex) {
        return averages.get(componentIndex, 0);
    }

    public int componentCount() {
        return eigenVectors.rows();
    }
}
