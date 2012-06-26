package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.core.ParticleSource;

public class CrossCorClassifier implements ParticleClassifier,
                               ItemListener<Particle> {

    // A map of the templates -> classes
    private final String                       classesMapName;
    private final MultiMap<Particle, Particle> classes;

    private final Vector<ParticleSource>       particleSources;

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

    // Pending count
    private final AtomicNumber                 pendingCount;

    // Defaults to trying to classify all particles
    public CrossCorClassifier() {
        this(0.0);
    }

    public CrossCorClassifier(double minimumCorrelation) {
        matchThreshold = minimumCorrelation;

        particleSources = new Vector<ParticleSource>();

        classesMapName = "Classes_" + this.hashCode();
        classes = Hazelcast.getMultiMap(classesMapName);

        executorName = "Classifier_" + this.hashCode();
        executor = Hazelcast.getExecutorService(executorName);

        averagesMapName = "Averages_" + this.hashCode();
        classAverages = Hazelcast.getMap(averagesMapName);

        templateSetName = "ClassTemplates_" + this.hashCode();
        templates = Hazelcast.getSet(templateSetName);

        pendingCount = Hazelcast.getAtomicNumber(executorName);
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
        // Bump the pending counter
        pendingCount.incrementAndGet();
        // Do this asynchronously across the cluster
        executor.execute(new CrossCorClassifierTask(target, matchThreshold,
                                                    classesMapName,
                                                    averagesMapName,
                                                    templateSetName,
                                                    executorName));

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

    public void stop() {
        executor.shutdown();
    }

    public long getPendingCount() {
        return pendingCount.get();
    }

    public boolean isActive() {
        return pendingCount.get() > 0;
    }

    @Override
    public void addParticleSource(ParticleSource p) {
        particleSources.add(p);
        // Attach as a listener
        ICollection<Particle> sourceQueue = Hazelcast.getQueue(p.getParticleQueueName());
        sourceQueue.addItemListener(this, true);
    }

    @Override
    public void itemAdded(ItemEvent<Particle> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void itemRemoved(ItemEvent<Particle> arg0) {
        // Not interested in item removal events
    }
}
