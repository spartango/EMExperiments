package edu.harvard.mcb.leschziner.classify;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.analyze.Clusters;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class PCAClassifier extends DistributedParticleConsumer {

    public static int                            iterations = 50;
    public static int                            attempts   = 1;

    private final Vector<Particle>               targets;
    private final Map<Integer, Vector<Particle>> classes;

    private final PrincipalComponentAnalyzer     pcAnalyzer;
    private final KMeansClusterer                clusterer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        // Allocate the classes
        classes = new HashMap<>();

        targets = new Vector<Particle>();
        pcAnalyzer = new PrincipalComponentAnalyzer(principalComponents);
        clusterer = new KMeansClusterer(classCount, classAccuracy, iterations,
                                        attempts);
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
    }

    public void classifyAll() {
        // Clear any existing clasess
        classes.clear();

        // Run PCA
        PrincipalComponents pComponents = pcAnalyzer.analyze(targets);
        if (pComponents != null) {
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Eigenvalues: ");

            // Some info about the principal components
            for (int i = 0; i < pComponents.componentCount(); i++) {
                System.out.print(pComponents.getEigenValue(i) + " ");
                IplImage eigenImage = pComponents.getEigenImage(i);

                // Display the eigenImage
                DisplayUtils.displayImage(eigenImage, "EigenImage " + i);
            }
            System.out.println();

            // Display the subspace
            DisplayUtils.displayMat(pComponents.getSubSpace(), "PCA Subspace");

            // Run Clustering
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Clustering classes");
            Clusters clusters = clusterer.cluster(pComponents.getSubSpace());

            // Group the original images
            for (int i = 0; i < targets.size(); i++) {
                // Get the particle
                Particle target = targets.get(i);

                // Get the cluster label for this particle
                int cluster = clusters.getClusterForSample(i);

                // Get the Particle Vector, or create on if its not there yet
                if (classes.containsKey(cluster)) {
                    classes.get(cluster).add(target);
                } else {
                    Vector<Particle> newClass = new Vector<>();
                    newClass.add(target);
                    classes.put(cluster, newClass);
                }
            }

            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Completed Clustering");

        }
    }

    public Map<Integer, Vector<Particle>> getClasses() {
        return classes;
    }

}
