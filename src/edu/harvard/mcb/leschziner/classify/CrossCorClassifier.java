package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;

public class CrossCorClassifier implements ParticleClassifier,
                               ParticleSourceListener {
    public static int                          CORE_POOL  = 8;
    public static int                          MAX_POOL   = 8;
    public static int                          KEEP_ALIVE = 1000;

    // A map of the templates -> classes
    private final String                       classesMapName;
    private final MultiMap<Particle, Particle> classes;

    // This is a cache of calculated classAverages
    private final String                       averagesMapName;
    private final Map<Particle, Particle>      classAverages;

    // The set of templates
    private final String                       templateSetName;
    private final Set<Particle>                templates;

    // The Cluster Distributed Executor
    private final String                       executorName;
    private final ExecutorService              executor;

    // Gates classification with a minimum correlation
    private final double                       matchThreshold;

    // Defaults to trying to classify all particles
    public CrossCorClassifier() {
        this(0.0);
    }

    public CrossCorClassifier(double minimumCorrelation) {
        matchThreshold = minimumCorrelation;
        classesMapName = "Classes_" + this.hashCode();
        classes = Hazelcast.getMultiMap(classesMapName);

        executorName = "Classifier_" + this.hashCode();
        executor = Hazelcast.getExecutorService(executorName);

        averagesMapName = "Averages_" + this.hashCode();
        classAverages = Hazelcast.getMap(averagesMapName);

        templateSetName = "ClassTemplates_" + this.hashCode();
        templates = Hazelcast.getSet(templateSetName);
    }

    @Override
    public Collection<Particle> getClassForTemplate(Particle template) {
        return classes.get(template);
    }

    @Override
    public Particle getAverageForTemplate(Particle template) {
        // Checks the cache for a class average
        if (classAverages.containsKey(template)) {
            return classAverages.get(template);
        } else {
            // Otherwise calculates a new one, which is a bit costly
            Particle average = ClassAverager.average(classes.get(template));
            if (average != null) {
                classAverages.put(template, average);
            }
            return average;
        }
    }

    @Override
    public void classify(final Particle target) {
        // Do this asynchronously across the cluster
        executor.execute(new CrossCorClassifierJob(target, matchThreshold,
                                                   classesMapName,
                                                   averagesMapName,
                                                   templateSetName));

    }

    @Override
    public void addTemplate(Particle template) {
        // System.out.println("[CrossCorClassifier]: Added Template "
        // + template.hashCode());
        templates.add(template);
    }

    public void addTemplates(Collection<Particle> templates) {
        for (Particle template : templates) {
            addTemplate(template);
        }
    }

    @Override
    public Collection<Particle> getTemplates() {
        return classes.keySet();
    }

    @Override
    public void onNewParticle(Particle p) {
        // Try to classify incoming particles
        classify(p);
    }

    public void stop() {
        executor.shutdown();
    }

    public int getPendingCount() {
        return -1;
    }

    public boolean isActive() {
        return !executor.isTerminated();
    }
}
