package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Set;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class TemplateParticlePicker extends DistributedParticlePicker {

    private final BlobExtractor blobExtractor;
    private final double        matchThreshold;

    private final String        templateSetName;
    private final Set<Particle> templates;

    public TemplateParticlePicker(int particleSize,
                                  int particleEpsillon,
                                  double matchThreshold,
                                  int boxSize) {
        super(boxSize);
        blobExtractor = new BlobExtractor(particleSize, particleEpsillon);
        templateSetName = "PickingTemplates_" + this.hashCode();
        templates = DefaultStorageEngine.getStorageEngine()
                                        .getSet(templateSetName);
        this.matchThreshold = matchThreshold;
    }

    @Override public void processMicrograph(final BufferedImage image) {
        Particle target = new Particle(image);
        processMicrograph(target);
    }

    @Override public void processMicrograph(final IplImage image) {
        Particle target = new Particle(image);
        processMicrograph(target);
    }

    public void addTemplate(Particle template) {
        templates.add(template);
    }

    public void addTemplates(Collection<Particle> templates) {
        this.templates.addAll(templates);
    }

    @Override public void processMicrograph(Particle target) {
        for (Particle template : templates) {
            execute(new TemplatePickingTask(target,
                                            template,
                                            boxSize,
                                            matchThreshold,
                                            blobExtractor,
                                            particleQueueName,
                                            executorName));
        }
    }
}
