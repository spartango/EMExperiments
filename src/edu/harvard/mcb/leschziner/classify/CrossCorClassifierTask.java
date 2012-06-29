package edu.harvard.mcb.leschziner.classify;

import java.util.Map;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.PearsonCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;

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
    @Override
    public void process() {
        // Pull up distributed maps
        MultiMap<Particle, Particle> classes = Hazelcast.getMultiMap(classMapName);
        Map<Particle, Particle> classAverages = Hazelcast.getMap(averagesMapName);
        Set<Particle> templates = Hazelcast.getSet(templateSetName);

        // Iterate through the templates, scoring pearson correlation.
        double bestCorrelation = 0;
        Particle bestTemplate = null;
        for (Particle template : templates) {
            double score = PearsonCorrelator.compare(target, template);
            // Select best correlation
            if (score > bestCorrelation) {
                bestCorrelation = score;
                bestTemplate = template;
            }
        }

        // Add target particle to closest match, if its above the threshold
        if (bestTemplate != null && bestCorrelation >= matchThreshold) {
            // Add to class
            classes.put(bestTemplate, target);
            // Invalidate the class average cache
            classAverages.remove(bestTemplate);
        }

    }
}
