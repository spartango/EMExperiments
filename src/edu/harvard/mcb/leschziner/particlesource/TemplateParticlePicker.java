package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.core.Particle;

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
        templates = Hazelcast.getSet(templateSetName);
        this.matchThreshold = matchThreshold;
    }

    @Override public void processMicrograph(BufferedImage image) {
        Particle target = new Particle(image);
        for (Particle template : templates) {
            execute(new TemplatePickingTask(target, template, boxSize,
                                            matchThreshold, blobExtractor,
                                            particleQueueName, executorName));
        }

    }

    public void addTemplate(Particle template) {
        templates.add(template);
    }

}
