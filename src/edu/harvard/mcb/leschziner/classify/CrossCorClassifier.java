package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.analyze.PearsonCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;

public class CrossCorClassifier implements ParticleClassifier {
    public static int                                                    CORE_POOL  = 2;
    public static int                                                    MAX_POOL   = 8;
    public static int                                                    KEEP_ALIVE = 1000;

    private ConcurrentHashMap<Particle, ConcurrentLinkedQueue<Particle>> classes;

    // This is a cache of calculated classAverages
    private ConcurrentHashMap<Particle, Particle>                        classAverages;

    private ThreadPoolExecutor                                           threadPool;
    private BlockingQueue<Runnable>                                      classifyQueue;

    public CrossCorClassifier() {
        classes = new ConcurrentHashMap<Particle, ConcurrentLinkedQueue<Particle>>();
        classAverages = new ConcurrentHashMap<Particle, Particle>();
        classifyQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(CORE_POOL, MAX_POOL, KEEP_ALIVE,
                                            TimeUnit.MILLISECONDS,
                                            classifyQueue);
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
            classAverages.put(template, average);
            return average;
        }
    }

    private void handleParticle(Particle target) {
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
        // Add to closest match
        addToClass(bestTemplate, target);
    }

    @Override
    public void classify(final Particle target) {
        // Do this asynchronously
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                handleParticle(target);
            }
        });

    }

    private void addToClass(Particle template, Particle target) {
        classes.get(template).add(target);
        // Invalidates class average cache
        classAverages.remove(template);
    }

    @Override
    public void addTemplate(Particle template) {
        classes.put(template, new ConcurrentLinkedQueue<Particle>());
    }

    @Override
    public Collection<Particle> getTemplates() {
        return classes.keySet();
    }

}
