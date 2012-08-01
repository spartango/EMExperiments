package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;

import com.hazelcast.core.AtomicNumber;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.storage.StorageEngine;

/**
 * A Classifier that compares particles to a set of template particles, sorting
 * them into classes by greatest similarity (determined by Pearson cross
 * correlation).
 * 
 * @author spartango
 * 
 */
public class CrossCorClassifier extends DistributedClassifier {

    // The set of templates keyed by UUID
    private final String              templateSetName;
    private final AtomicNumber        currentTemplateId;
    private final Map<Long, Particle> templates;

    // Gates classification with a minimum correlation
    private final double              matchThreshold;

    /**
     * Builds a Cross Correlating Classifier with no minimum correlation
     * required for sorting
     */
    public CrossCorClassifier() {
        this(0.0);
    }

    /**
     * Builds a Cross Correlating Classifier that only adds a particle to a
     * class when its correlation to the template is above a certain value
     * 
     * @param minimumCorrelation
     *            : minimum score necessary to sort a particle
     */
    public CrossCorClassifier(double minimumCorrelation) {
        super();
        matchThreshold = minimumCorrelation;

        templateSetName = "ClassTemplates_" + this.hashCode();

        StorageEngine storage = DefaultStorageEngine.getStorageEngine();
        currentTemplateId = storage.getAtomicNumber(templateSetName);
        templates = storage.getMap(templateSetName);
    }

    /**
     * Classifies a particle
     */
    @Override public void processParticle(final Particle target) {
        // Classify the particle asynchronously in a distributed way
        execute(new CrossCorClassifierTask(target,
                                           matchThreshold,
                                           classesMapName,
                                           averagesMapName,
                                           templateSetName,
                                           executorName));

    }

    /**
     * Adds a template to compare particles against
     */
    public void addTemplate(Particle template) {
        long id = currentTemplateId.incrementAndGet();
        templates.put(id, template);
    }

    /**
     * Add a bunch of templates to compare particles against
     * 
     * @param templates
     */
    public void addTemplates(Collection<Particle> templates) {
        for (Particle template : templates) {
            addTemplate(template);
        }
    }

    /**
     * Get all the templates being used for classification
     */
    public Collection<Particle> getTemplates() {
        return templates.values();
    }

    @Override public void classifyAll() {
        // Doesn't do anything, due to streaming classification
    }

}
