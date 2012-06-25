package edu.harvard.mcb.leschziner.classify;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.harvard.mcb.leschziner.analyze.PearsonCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;

public class CrossCorClassifierJob implements Serializable, Runnable {

    private static final long                                                  serialVersionUID = -5350862097468663627L;
    private final Particle                                                     target;
    private final double                                                       matchThreshold;

    private final ConcurrentHashMap<Particle, ConcurrentLinkedQueue<Particle>> classes;
    private final ConcurrentHashMap<Particle, Particle>                        classAverages;

    public CrossCorClassifierJob(Particle target,
                                 double matchThreshold,
                                 ConcurrentHashMap<Particle, ConcurrentLinkedQueue<Particle>> classes,
                                 ConcurrentHashMap<Particle, Particle> classAverages) {
        super();
        this.target = target;
        this.matchThreshold = matchThreshold;
        this.classes = classes;
        this.classAverages = classAverages;
    }

    @Override
    public void run() {
        // Iterate through the templates, scoring pearson correlation.
        double bestCorrelation = 0;
        Particle bestTemplate = null;
        for (Particle template : classes.keySet()) {
            double score = PearsonCorrelator.compare(target, template);
            if (score > bestCorrelation) {
                bestCorrelation = score;
                bestTemplate = template;
            }
        }
        // Add to closest match, if there is one at all
        if (bestTemplate != null && bestCorrelation >= matchThreshold) {
            // System.out.println("[CrossCorClassifier " +
            // Thread.currentThread()
            // + "]: Classifying " + target.hashCode()
            // + " with " + bestTemplate.hashCode() + " -> "
            // + bestCorrelation);
            addToClass(bestTemplate, target);
        }
    }

    private void addToClass(Particle template, Particle target) {
        classes.get(template).add(target);
        // Invalidates class average cache
        classAverages.remove(template);
    }
}
