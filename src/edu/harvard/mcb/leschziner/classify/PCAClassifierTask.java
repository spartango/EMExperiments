package edu.harvard.mcb.leschziner.classify;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.Clusters;
import edu.harvard.mcb.leschziner.analyze.CvPrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.analyze.PrincipalComponents;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

public class PCAClassifierTask extends DistributedProcessingTask {
    private final String                       targetQueueName;
    private final CvPrincipalComponentAnalyzer pcAnalyzer;
    private final KMeansClusterer              clusterer;
    private final String                       classesMapName;

    public PCAClassifierTask(String targetQueueName,
                             CvPrincipalComponentAnalyzer pcAnalyzer,
                             KMeansClusterer clusterer,
                             String classesMapName,
                             String executorName) {
        super(executorName);
        this.targetQueueName = targetQueueName;
        this.pcAnalyzer = pcAnalyzer;
        this.clusterer = clusterer;
        this.classesMapName = classesMapName;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 868914623793845509L;

    @Override public void process() {
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();
        // Fetch the targets
        BlockingQueue<Particle> targets = storage.getQueue(targetQueueName);

        // Clear any existing classes
        MultiMap<Long, Particle> classes = storage.getMultiMap(classesMapName);
        classes.clear();

        // Run PCA
        PrincipalComponents pComponents = pcAnalyzer.analyze(targets);
        if (pComponents != null) {
            System.out.print("["
                             + this.getClass().getSimpleName()
                             + "]: Eigenvalues: ");

            // Some info about the principal components
            for (int i = 0; i < pComponents.componentCount(); i++) {
                System.out.print(pComponents.getEigenValue(i) + " ");
                Particle eigenParticle = new Particle(pComponents.getEigenImage(i));

                // write the eigenParticle
                try {
                    eigenParticle.toFile("processed/eigen_" + i + ".png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();

            // Run Clustering
            System.out.println("["
                               + this.getClass().getSimpleName()
                               + "]: Clustering classes");
            Clusters clusters = clusterer.cluster(pComponents.getSubSpace());

            int i = 0;
            // Group the original images
            for (Particle target : targets) {
                // Get the cluster label for this particle
                long cluster = clusters.getClusterForSample(i);

                // Put the particle in the right class
                classes.put(cluster, target);

                i++;
            }

            System.out.println("["
                               + this.getClass().getSimpleName()
                               + "]: Completed Clustering");

        }
    }

}
