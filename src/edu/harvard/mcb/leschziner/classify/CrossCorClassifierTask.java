package edu.harvard.mcb.leschziner.classify;

import java.util.Map;

import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.CrossCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

/**
 * A task that classifies a single particle against many references using
 * pearson cross correlation
 * 
 * @author spartango
 * 
 */
public class CrossCorClassifierTask extends DistributedProcessingTask {

    private static final long serialVersionUID = -5350862097468663627L;

    // Particle to be processed
    private final Particle    target;

    // Minimum correlation necessary to include this particle in a class
    private final double      matchThreshold;

    // Name of the map of classes (distributed many-to-one map)
    private final String      classMapName;

    // Name of the cache of class averages (distributed map)
    private final String      averagesMapName;

    // Name of the set of templates (distributed set)
    private final String      templateSetName;

    /**
     * Builds a classification task to be executed in the future
     * 
     * @param target
     *            : particle to be classified
     * @param matchThreshold
     *            : minimum correlation necessary to allow classification
     * @param classMapName
     *            : name of map of classes (distributed)
     * @param averagesMapName
     *            : name of map of averages (distributed)
     * @param templateSetName
     *            : name of set of templates (distributed)
     * @param executorName
     *            : name of executor which will run this task (distributed)
     */
    public CrossCorClassifierTask(Particle target,
                                  double matchThreshold,
                                  String classMapName,
                                  String averagesMapName,
                                  String templateSetName,
                                  String executorName) {
        super(executorName);
        this.target = target;
        this.matchThreshold = matchThreshold;
        this.classMapName = classMapName;
        this.averagesMapName = averagesMapName;
        this.templateSetName = templateSetName;
    }

    /**
     * Do the classification
     */
    @Override public void process() {
        // Pull up distributed maps
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();

        MultiMap<Long, Particle> classes = storage.getMultiMap(classMapName);
        Map<Long, Particle> classAverages = storage.getMap(averagesMapName);
        Map<Long, Particle> templates = storage.getMap(templateSetName);

        // Iterate through the templates, scoring pearson correlation.
        double bestCorrelation = 0;
        Long bestTemplateId = null;
        for (long templateId : templates.keySet()) {
            double score = CrossCorrelator.compare(target,
                                                   templates.get(templateId));
            // Select best correlation
            if (score > bestCorrelation) {
                bestCorrelation = score;
                bestTemplateId = templateId;
            }
        }

        // Add target particle to closest match, if its above the threshold
        if (bestTemplateId != null && bestCorrelation >= matchThreshold) {
            // Add to class
            classes.put(bestTemplateId, target);
            // Invalidate the class average cache
            classAverages.remove(bestTemplateId);
        }

    }
}
