package edu.harvard.mcb.leschziner.analyze;

import java.io.Serializable;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRNG;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;

public class KMeansClusterer implements Serializable {
    public KMeansClusterer(int clusters,
                           double epsilon,
                           int iterations,
                           int attempts) {
        this.iterations = iterations;
        this.attempts = attempts;
        this.epsilon = epsilon;
        this.clusters = clusters;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4280771652047557377L;

    private final int         iterations;
    private final int         attempts;

    private final double      epsilon;
    private final int         clusters;

    public Clusters cluster(CvMat data) {
        CvTermCriteria terminationCriteria = new CvTermCriteria(opencv_core.CV_TERMCRIT_EPS
                                                                        + opencv_core.CV_TERMCRIT_ITER,
                                                                iterations,
                                                                epsilon);

        CvMat clusterLabels = CvMat.create(data.rows(), 1, opencv_core.CV_32SC1);
        CvMat clusterCenters = CvMat.create(clusters,
                                            data.cols(),
                                            opencv_core.CV_32FC1);
        double[] compactness = new double[attempts];
        // Run a clusterer on the eigenimages
        opencv_core.cvKMeans2(data,
                              clusters,
                              clusterLabels,
                              terminationCriteria,
                              attempts,
                              new CvRNG(null),
                              0,
                              clusterCenters,
                              compactness);
        return new Clusters(clusterLabels, clusterCenters, compactness);
    }
}
