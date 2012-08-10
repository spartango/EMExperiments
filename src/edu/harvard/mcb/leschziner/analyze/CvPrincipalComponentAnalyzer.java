package edu.harvard.mcb.leschziner.analyze;

import java.util.Collection;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.core.Particle;

public class CvPrincipalComponentAnalyzer implements PrincipalComponentAnalyzer {
    /**
     * 
     */
    private static final long serialVersionUID = -2016995095636434924L;
    private final int         principalComponentCount;

    public CvPrincipalComponentAnalyzer(int principalComponentCount) {
        this.principalComponentCount = principalComponentCount;
    }

    @Override public PrincipalComponents analyze(Collection<Particle> targets) {
        if (!targets.isEmpty()) {
            int particleSize = targets.iterator().next().getSize();
            int particleArea = particleSize * particleSize;
            // Each row is an image, each column is a pixel
            CvMat targetMat = CvMat.create(targets.size(),
                                           particleArea,
                                           opencv_core.CV_32FC1);
            int i = 0;
            // Reshape the targets for use in the matrix
            for (Particle target : targets) {
                IplImage targetImage = target.getImage();
                // A single row containing all the pixels in the image
                CvMat row = CvMat.createHeader(1,
                                               particleArea,
                                               targetImage.depth(),
                                               targetImage.nChannels());
                opencv_core.cvReshape(targetImage,
                                      row,
                                      targetImage.nChannels(),
                                      row.rows());

                // A 32 bit float row from the pca matrix
                CvMat targetRow = CvMat.createHeader(1,
                                                     particleArea,
                                                     opencv_core.CV_32FC1);
                opencv_core.cvGetRow(targetMat, targetRow, i);

                // Convert the image data row to 32 bit float, normalizing it to
                // 0-1 scale
                opencv_core.cvConvertScale(row, targetRow, 1 / 255.0, 0);
                i++;
            }

            // Run PCA on the target matrix

            CvMat eigenValues = CvMat.create(principalComponentCount, 1);
            CvMat eigenVectors = CvMat.create(principalComponentCount,
                                              particleArea,
                                              opencv_core.CV_32FC1);
            CvMat averages = CvMat.create(1, particleArea, opencv_core.CV_32FC1);
            System.out.println("["
                               + this.getClass().getSimpleName()
                               + "]: Running PCA");
            opencv_core.cvCalcPCA(targetMat,
                                  averages,
                                  eigenValues,
                                  eigenVectors,
                                  opencv_core.CV_PCA_DATA_AS_ROW);

            CvMat subspace = CvMat.create(targets.size(),
                                          principalComponentCount,
                                          opencv_core.CV_32FC1);
            // Project onto a subspace
            System.out.println("["
                               + this.getClass().getSimpleName()
                               + "]: Projecting Eigenvectors");

            opencv_core.cvProjectPCA(targetMat,
                                     averages,
                                     eigenVectors,
                                     subspace);

            PrincipalComponents components = new PrincipalComponents(eigenValues,
                                                                     eigenVectors,
                                                                     subspace,
                                                                     averages);

            // Stuff everything into the principal components
            return components;
        } else {
            return null;
        }
    }
}
