package edu.harvard.mcb.leschziner.classify;

import java.util.Vector;

import edu.harvard.mcb.leschziner.analyze.Clusters;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public class PCAClassifier extends DistributedParticleConsumer {

    public static int                        iterations = 50;
    public static int                        attempts   = 1;

    private final Vector<Particle>           targets;
    private final PrincipalComponentAnalyzer pcAnalyzer;
    private final KMeansClusterer            clusterer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {

        targets = new Vector<Particle>();
        pcAnalyzer = new PrincipalComponentAnalyzer(principalComponents);
        clusterer = new KMeansClusterer(classCount, classAccuracy, iterations,
                                        attempts);
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
    }

    public void classifyAll() {
        // Run PCA
        PrincipalComponents pComponents = pcAnalyzer.analyze(targets);
        if (pComponents != null) {
            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Clustering classes");
            Clusters clusters = clusterer.cluster(pComponents.getSubSpace());
            // Group the original images

            System.out.println("[" + this.getClass().getSimpleName()
                               + "]: Completed Clustering");
        }
    }
}
