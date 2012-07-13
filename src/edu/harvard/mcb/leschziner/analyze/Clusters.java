package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core;
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

    public int size() {
        return clusterLabels.rows();
    }

    public int getClusterLabel(int index) {
        return (int) clusterLabels.get(index, 1);
    }

    public CvMat getClusterCenter(int clusterIndex) {
        CvMat center = CvMat.createHeader(1, clusterCenters.cols(),
                                          opencv_core.CV_32FC1);
        opencv_core.cvGetRow(clusterCenters, center, clusterIndex);
        return center;
    }

}
