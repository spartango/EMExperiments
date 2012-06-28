package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

public class CrossCorClassifier extends DistributedParticleConsumer implements
                                                                   ParticleClassifier {

    // A map of the templates -> classes
    private final String                       classesMapName;
    private final MultiMap<Particle, Particle> classes;

    // This is a cache of calculated classAverages
    private final String                       averagesMapName;
    private final Map<Particle, Particle>      classAverages;

    // The set of templates
    private final String                       templateSetName;
    private final Set<Particle>                templates;

    // Gates classification with a minimum correlation
    private final double                       matchThreshold;

    // Defaults to trying to classify all particles
    public CrossCorClassifier() {
        this(0.0);
    }

    public CrossCorClassifier(double minimumCorrelation) {
        super();
        matchThreshold = minimumCorrelation;

        classesMapName = "Classes_" + this.hashCode();
        classes = Hazelcast.getMultiMap(classesMapName);

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
    public void processParticle(final Particle target) {
        // Bump the pending counter
        // Do this asynchronously across the cluster

        execute(new CrossCorClassifierTask(target, matchThreshold,
                                           classesMapName, averagesMapName,
                                           templateSetName, executorName));

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
        return templates;
    }

}
