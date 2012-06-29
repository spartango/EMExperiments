package edu.harvard.mcb.leschziner.classify;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.analyze.ClassAverager;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.TemplateClassifier;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;

/**
 * A Classifier that compares particles to a set of template particles, sorting
 * them into classes by greatest similarity (determined by Pearson cross
 * correlation).
 * 
 * @author spartango
 * 
 */
public class CrossCorClassifier extends DistributedParticleConsumer implements
                                                                   TemplateClassifier {

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

        classesMapName = "Classes_" + this.hashCode();
        classes = Hazelcast.getMultiMap(classesMapName);

        averagesMapName = "Averages_" + this.hashCode();
        classAverages = Hazelcast.getMap(averagesMapName);

        templateSetName = "ClassTemplates_" + this.hashCode();
        templates = Hazelcast.getSet(templateSetName);
    }

    /**
     * Gets the set of particles sorted into a class due to similarity to a
     * given template
     */
    @Override
    public Collection<Particle> getClassForTemplate(Particle template) {
        return classes.get(template);
    }

    /**
     * Gets the average of particles sorted into a template's class. Will
     * utilize a cached average if one has already been calculated.
     */
    @Override
    public Particle getAverageForTemplate(Particle template) {
        // Checks the cache for a class average
        Particle average = classAverages.get(template);
        if (average == null && classes.containsKey(template)) {
            // Otherwise calculates a new one, which is a bit costly
            average = ClassAverager.average(classes.get(template));
            if (average != null) {
                classAverages.put(template, average);
            }
        }
        return average;
    }

    /**
     * Classifies a particle
     */
    @Override
    public void processParticle(final Particle target) {
        // Classify the particle asynchronously in a distributed way
        execute(new CrossCorClassifierTask(target, matchThreshold,
                                           classesMapName, averagesMapName,
                                           templateSetName, executorName));

    }

    /**
     * Adds a template to compare particles against
     */
    @Override
    public void addTemplate(Particle template) {
        // System.out.println("[CrossCorClassifier]: Added Template "
        // + template.hashCode());
        templates.add(template);
    }

    /**
     * Add a bunch of templates to compare particles against
     * 
     * @param templates
     */
    @Override
    public void addTemplates(Collection<Particle> templates) {
        for (Particle template : templates) {
            addTemplate(template);
        }
    }

    /**
     * Get all the templates being used for classification
     */
    @Override
    public Collection<Particle> getTemplates() {
        return templates;
    }

}
