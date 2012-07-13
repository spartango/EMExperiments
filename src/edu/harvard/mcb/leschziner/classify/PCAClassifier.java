package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.analyze.Clusters;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Classifier;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class PCAClassifier extends DistributedParticleConsumer implements
                                                              Classifier {

    public static int                        iterations = 50;
    public static int                        attempts   = 1;

    private final Vector<Particle>           targets;

    // A map of the templates -> classes keyed by template uuid
    private final String                     classesMapName;
    private final MultiMap<Long, Particle>   classes;

    // This is a cache of calculated classAverages keyed by template uuid
    private final String                     averagesMapName;
    private final Map<Long, Particle>        classAverages;

    private final PrincipalComponentAnalyzer pcAnalyzer;
    private final KMeansClusterer            clusterer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        classesMapName = "Classes_" + this.hashCode();
        classes = Hazelcast.getMultiMap(classesMapName);

        averagesMapName = "Averages_" + this.hashCode();
        classAverages = Hazelcast.getMap(averagesMapName);

        targets = new Vector<Particle>();
        pcAnalyzer = new PrincipalComponentAnalyzer(principalComponents);
        clusterer = new KMeansClusterer(classCount, classAccuracy, iterations,
                                        attempts);
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
    }

    /**
     * Gets the set of particles sorted into a class
     */
    public Collection<Particle> getClass(long classId) {
        return classes.get(classId);
    }

    /**
     * Gets the average of particles sorted into a template's class. Will
     * utilize a cached average if one has already been calculated.
     */
    public Particle getClassAverage(long classId) {
        // Checks the cache for a class average
        Particle average = classAverages.get(classId);
        if (average == null && classes.containsKey(classId)) {
            // Otherwise calculates a new one, which is a bit costly
            average = ClassAverager.average(classes.get(classId));
            if (average != null) {
                classAverages.put(classId, average);
            }
        }
        return average;
    }

    /**
     * Get the ids of the templates being used for classification
     */
    public Collection<Long> getClassIds() {
        return classes.keySet();
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
                long cluster = clusters.getClusterForSample(i);

                // Put the particle in the right class
                classes.put(cluster, target);

            }

            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Completed Clustering");

        }
    }

}
