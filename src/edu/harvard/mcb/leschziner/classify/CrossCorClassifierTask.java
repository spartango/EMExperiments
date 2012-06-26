package edu.harvard.mcb.leschziner.classify;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.PearsonCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;

public class CrossCorClassifierTask implements Serializable, Runnable {

    private static final long serialVersionUID = -5350862097468663627L;
    private final Particle    target;
    private final double      matchThreshold;

    private final String      classMapName;
    private final String      averagesMapName;
    private final String      templateSetName;
    private final String      executorName;

    public CrossCorClassifierTask(Particle target,
                                 double matchThreshold,
                                 String classMapName,
                                 String averagesMapName,
                                 String templateSetName,
                                 String executorName) {
        this.target = target;
        this.matchThreshold = matchThreshold;
        this.classMapName = classMapName;
        this.averagesMapName = averagesMapName;
        this.templateSetName = templateSetName;
        this.executorName = executorName;
    }

    @Override
    public void run() {
        // Pull up distributed maps
        MultiMap<Particle, Particle> classes = Hazelcast.getMultiMap(classMapName);
        Map<Particle, Particle> classAverages = Hazelcast.getMap(averagesMapName);
        Set<Particle> templates = Hazelcast.getSet(templateSetName);

        // Iterate through the templates, scoring pearson correlation.
        double bestCorrelation = 0;
        Particle bestTemplate = null;
        for (Particle template : templates) {
            double score = PearsonCorrelator.compare(target, template);
            if (score > bestCorrelation) {
                bestCorrelation = score;
                bestTemplate = template;
            }
        }

        // Add to closest match, if there is one at all
        if (bestTemplate != null && bestCorrelation >= matchThreshold) {
            // System.out.println("[ClassifierJob]: Classifying "
            // + target.hashCode() + " with "
            // + bestTemplate.hashCode() + " -> "
            // + bestCorrelation);
            // Add to class
            classes.put(bestTemplate, target);
            // Invalidate the class average cache
            classAverages.remove(bestTemplate);
        }

        Hazelcast.getAtomicNumber(executorName).decrementAndGet();
    }
}
