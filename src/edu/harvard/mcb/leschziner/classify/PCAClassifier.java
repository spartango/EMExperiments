package edu.harvard.mcb.leschziner.classify;

import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;

import edu.harvard.mcb.leschziner.analyze.PrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public class PCAClassifier extends DistributedParticleConsumer {

    private static final int                 MAX_ITERATIONS = 250;
    private final int                        classCount;
    private final double                     epsilon;

    private final Vector<Particle>           targets;
    private final PrincipalComponentAnalyzer pcAnalyzer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        this.classCount = classCount;
        this.epsilon = classAccuracy;

        targets = new Vector<Particle>();
        pcAnalyzer = new PrincipalComponentAnalyzer(principalComponents);
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
    }

    public void classifyAll() {
        // Run PCA
        PrincipalComponents pComponents = pcAnalyzer.analyze(targets);
        if (pComponents != null) {

            // System.out.println("[" + this.getClass().getSimpleName()
            // + "]: Clustering classes");

            CvTermCriteria terminationCriteria = new CvTermCriteria(
                                                                    opencv_core.CV_TERMCRIT_EPS
                                                                            + opencv_core.CV_TERMCRIT_ITER,
                                                                    MAX_ITERATIONS,
                                                                    epsilon);

            CvArr clusterLabels = new CvMat(pComponents.getSubSpace().rows());

            // Run a clusterer on the eigenimages
            // opencv_core.cvKMeans2(subspace, classCount, clusterLabels,
            // terminationCriteria, 1, null, 0, null, null);
        }
    }
}
