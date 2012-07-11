package edu.harvard.mcb.leschziner.classify;

import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public class PCAClassifier extends DistributedParticleConsumer {

    private static final int       MAX_ITERATIONS = 250;
    private final int              principalComponentCount;
    private final int              classCount;
    private final double           epsilon;
    private final Vector<Particle> targets;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        targets = new Vector<Particle>();
        this.principalComponentCount = principalComponents;
        this.classCount = classCount;
        this.epsilon = classAccuracy;
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
    }

    public void classifyAll() {
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
                                  eigenVectors, opencv_core.CV_PCA_DATA_AS_ROW
                                                | opencv_core.CV_PCA_USE_AVG);
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Eigenvalues: ");
            // Some info about the principal components
            for (int i = 0; i < principalComponentCount; i++) {
                System.out.print(eigenValues.get(i) + " ");
            }
            System.out.println();

            CvMat subspace = CvMat.create(targets.size(),
                                          principalComponentCount);
            // Project onto a subspace
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Projecting Eigenvectors");

            opencv_core.cvProjectPCA(targetMat, averages, eigenVectors,
                                     subspace);

            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Clustering classes");

            CvTermCriteria terminationCriteria = new CvTermCriteria(
                                                                    opencv_core.CV_TERMCRIT_EPS
                                                                            + opencv_core.CV_TERMCRIT_ITER,
                                                                    MAX_ITERATIONS,
                                                                    epsilon);

            CvArr clusterLabels = new CvMat(subspace.rows());

            // Run a clusterer on the eigenimages
            opencv_core.cvKMeans2(subspace, classCount, clusterLabels,
                                  terminationCriteria, 1, null, 0, null, null);
        }
    }
}
