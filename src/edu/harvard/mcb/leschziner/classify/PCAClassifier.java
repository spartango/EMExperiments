package edu.harvard.mcb.leschziner.classify;

import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.analyze.CvPrincipalComponentAnalyzer;
import edu.harvard.mcb.leschziner.analyze.KMeansClusterer;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.event.CompletionEvent;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class PCAClassifier extends DistributedClassifier {

    public static int                          iterations = 100;
    public static int                          attempts   = 1;

    private final String                       targetQueueName;
    private final BlockingQueue<Particle>      targets;

    private final CvPrincipalComponentAnalyzer pcAnalyzer;
    private final KMeansClusterer              clusterer;

    public PCAClassifier(int principalComponents,
                         int classCount,
                         double classAccuracy) {
        super();
        targetQueueName = "ToClassify_" + this.hashCode();
        targets = DefaultStorageEngine.getStorageEngine()
                                      .getQueue(targetQueueName);
        pcAnalyzer = new CvPrincipalComponentAnalyzer(principalComponents);
        clusterer = new KMeansClusterer(classCount,
                                        classAccuracy,
                                        iterations,
                                        attempts);
    }

    @Override public void processParticle(Particle particle) {
        targets.add(particle);
        DefaultStorageEngine.getStorageEngine()
                            .getBufferedQueue(executorName
                                              + DistributedProcessingTask.EVENT_SUFFIX)
                            .add(new CompletionEvent(this.getClass().getName(),
                                                     0,
                                                     0));
    }

    @Override public void classifyAll() {
        execute(new PCAClassifierTask(targetQueueName,
                                      pcAnalyzer,
                                      clusterer,
                                      classesMapName,
                                      executorName));
    }

    @Override public void destroy() {
        super.destroy();
        DefaultStorageEngine.getStorageEngine()
                            .destroyBufferedQueue(executorName
                                                  + DistributedProcessingTask.EVENT_SUFFIX);
        DefaultStorageEngine.getStorageEngine().destroyQueue(targetQueueName);
    }
}
