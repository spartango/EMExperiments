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
            CvMat targetMat = CvMat.create(particleSize * particleSize,
                                           targets.size(), opencv_core.CV_32FC1);

            // Reshape the targets for use in the matrix
            for (int i = 0; i < targets.size(); i++) {
                IplImage targetImage = targets.get(i).getImage();
                CvMat row = CvMat.create(particleSize * particleSize, 1,
                                         targetImage.depth(),
                                         targetImage.nChannels());
                opencv_core.cvReshape(targetImage, row, 0, 0);
                // Copy into the target matrix
                opencv_core.cvConvertScale(row, targetMat.rows(i), 1 / 255.0, 0);
            }

            // Run PCA on the target matrix
            CvMat eigenValues = CvMat.create(principalComponentCount, 1);
            CvMat eigenVectors = CvMat.create(principalComponentCount,
                                              particleSize * particleSize,
                                              opencv_core.CV_32FC1);
            opencv_core.cvCalcPCA(targetMat, new CvMat(), eigenValues,
                                  eigenVectors, opencv_core.CV_PCA_DATA_AS_ROW);

            CvMat subspace = CvMat.create(targets.size(),
                                          principalComponentCount);
            // Project onto a subspace
            opencv_core.cvProjectPCA(targetMat, new CvMat(), eigenVectors,
                                     subspace);

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
