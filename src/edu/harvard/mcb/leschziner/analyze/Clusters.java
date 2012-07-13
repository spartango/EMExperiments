package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class Clusters {
    private final CvMat    clusterLabels;
    private final CvMat    clusterCenters;
    private final double[] compactness;

    public Clusters(CvMat clusterLabels,
                    CvMat clusterCenters,
                    double[] compactness) {
        super();
        this.clusterLabels = clusterLabels;
        this.clusterCenters = clusterCenters;
        this.compactness = compactness;
    }

    public CvMat getClusterLabels() {
        return clusterLabels;
    }

    public CvMat getClusterCenters() {
        return clusterCenters;
    }

    public double[] getCompactness() {
        return compactness;
    }

}
