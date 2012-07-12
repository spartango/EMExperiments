package edu.harvard.mcb.leschziner.analyze;

import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class PrincipalComponentAnalyzer {
    private final int principalComponentCount;

    public PrincipalComponentAnalyzer(int principalComponentCount) {
        this.principalComponentCount = principalComponentCount;
    }

    public PrincipalComponents analyze(Vector<Particle> targets) {
        if (targets.size() > 0) {
            int particleSize = targets.get(0).getSize();
            int particleArea = particleSize * particleSize;
            // Each row is an image, each column is a pixel
            CvMat targetMat = CvMat.create(targets.size(), particleArea,
                                           opencv_core.CV_32FC1);
            // Reshape the targets for use in the matrix
            for (int i = 0; i < targets.size(); i++) {
                IplImage targetImage = targets.get(i).getImage();
                // A single row containing all the pixels in the image
                CvMat row = CvMat.createHeader(1, particleArea,
                                               targetImage.depth(),
                                               targetImage.nChannels());
                opencv_core.cvReshape(targetImage, row,
                                      targetImage.nChannels(), row.rows());

                // A 32 bit float row from the pca matrix
                CvMat targetRow = CvMat.createHeader(1, particleArea,
                                                     opencv_core.CV_32FC1);
                opencv_core.cvGetRow(targetMat, targetRow, i);

                // Convert the image data row to 32 bit float, normalizing it to
                // 0-1 scale
                opencv_core.cvConvertScale(row, targetRow, 1 / 255.0, 0);
            }

            // Run PCA on the target matrix

            CvMat eigenValues = CvMat.create(principalComponentCount, 1);
            CvMat eigenVectors = CvMat.create(principalComponentCount,
                                              particleArea,
                                              opencv_core.CV_32FC1);
            CvMat averages = CvMat.create(1, particleArea, opencv_core.CV_32FC1);
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Running PCA");
            opencv_core.cvCalcPCA(targetMat, averages, eigenValues,
                                  eigenVectors, opencv_core.CV_PCA_DATA_AS_ROW);
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Eigenvalues: ");

            // Some info about the principal components
            for (int i = 0; i < principalComponentCount; i++) {
                System.out.print(eigenValues.get(i) + " ");
                // Get the eigenvector
                CvMat eigenVector = CvMat.createHeader(1, particleArea,
                                                       opencv_core.CV_32FC1);
                opencv_core.cvGetRow(eigenVectors, eigenVector, i);

                // Turn the eigenvector into an eigenimage
                CvMat eigenImage = CvMat.create(particleSize, particleSize,
                                                opencv_core.CV_32FC1);

                opencv_core.cvReshape(eigenVector, eigenImage, 1, particleSize);

                // Display the eigenImage
                DisplayUtils.displayMat(eigenImage, "EigenImage " + i);
            }
            System.out.println();

            CvMat subspace = CvMat.create(targets.size(),
                                          principalComponentCount);
            // Project onto a subspace
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Projecting Eigenvectors");

            opencv_core.cvProjectPCA(targetMat, averages, eigenVectors,
                                     subspace);

            // Display the subspace
            DisplayUtils.displayMat(subspace, "PCA Subspace");

            // Stuff everything into the principal components
            return new PrincipalComponents(eigenValues, eigenVectors, subspace,
                                           averages);
        } else {
            return null;
        }
    }
}
