package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;

import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

public abstract class DistributedClassifier extends DistributedParticleConsumer implements
                                                                               ParticleClassifier {
    // A map of the templates -> classes keyed by template uuid
    protected final String                   classesMapName;
    protected final MultiMap<Long, Particle> classes;

    // This is a cache of calculated classAverages keyed by template uuid
    protected final String                   averagesMapName;
    protected final Map<Long, Particle>      classAverages;

    public DistributedClassifier() {
        StorageEngine storage = DefaultStorageEngine.getStorageEngine();

        classesMapName = this.getClass().getSimpleName()
                         + "_Classes_"
                         + this.hashCode();
        classes = storage.getMultiMap(classesMapName);

        averagesMapName = this.getClass().getSimpleName()
                          + "_Averages_"
                          + this.hashCode();
        classAverages = storage.getMap(averagesMapName);
    }

    /**
     * Gets the set of particles sorted into a class
     */
    @Override public Collection<Particle> getClass(long classId) {
        return classes.get(classId);
    }

    /**
     * Gets the average of particles sorted into a template's class. Will
     * utilize a cached average if one has already been calculated.
     */
    @Override public Particle getClassAverage(long classId) {
        // Checks the cache for a class average
        Particle average = classAverages.get(classId);
        if (average == null && classes.containsKey(classId)) {
            // Otherwise calculates a new one, which is a bit costly
            average = ClassAverager.average(classes.get(classId));
            if (average != null) {
                classAverages.put(classId, average);
            }
        }
        return average;
    }

    /**
     * Get the ids of the templates being used for classification
     */
    @Override public Collection<Long> getClassIds() {
        return classes.keySet();
    }

    public void clearClasses() {
        classAverages.clear();
        classes.clear();
    }
}
