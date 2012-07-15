package edu.harvard.mcb.leschziner.classify;

import java.io.IOException;
import java.util.Vector;

import edu.harvard.mcb.leschziner.analyze.Clusters;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.analyze.CvPrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Particle;

public class PCAClassifier extends DistributedClassifier {

    public static int                        iterations = 50;
    public static int                        attempts   = 1;

    private final Vector<Particle>           targets;

    private final CvPrincipalComponentAnalyzer pcAnalyzer;
    private final KMeansClusterer            clusterer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        super();
        targets = new Vector<Particle>();
        pcAnalyzer = new CvPrincipalComponentAnalyzer(principalComponents);
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
            System.out.print("[" + this.getClass().getSimpleName()
                             + "]: Eigenvalues: ");

            // Some info about the principal components
            for (int i = 0; i < pComponents.componentCount(); i++) {
                System.out.print(pComponents.getEigenValue(i) + " ");
                Particle eigenParticle = new Particle(
                                                      pComponents.getEigenImage(i));

                // write the eigenParticle
                try {
                    eigenParticle.toFile("processed/eigen_" + i + ".png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();

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
