package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticlePicker;
import edu.harvard.mcb.leschziner.distributed.DistributedTaskHandler;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;
import edu.harvard.mcb.leschziner.particlefilter.ThresholdFilter;

public class DoGParticlePicker extends DistributedTaskHandler implements
                                                             ParticlePicker {

    // Size of area picked around particle
    private final int                     boxSize;

    // Filters for picking
    private final ParticleFilter          lowFilter;
    private final ParticleFilter          highFilter;
    private final ParticleFilter          thresholdFilter;

    private final BlobExtractor           blobExtractor;

    // Queue of particles produced
    private final String                  particleQueueName;
    private final BlockingQueue<Particle> extractedParticles;

    public DoGParticlePicker(int particleSize,
                             int particleEpsillon,
                             int lowRadius,
                             int highRadius,
                             int threshold,
                             int boxSize) {
        super();
        this.boxSize = boxSize;

        lowFilter = new GaussianFilter(lowRadius);
        highFilter = new GaussianFilter(highRadius);
        thresholdFilter = new ThresholdFilter(threshold);

        blobExtractor = new BlobExtractor(particleSize, particleEpsillon);

        // Pull up output queue
        particleQueueName = "ExtractedParticles_" + this.hashCode();
        extractedParticles = Hazelcast.getQueue(particleQueueName);
    }

    @Override
    public void processMicrograph(final BufferedImage image) {
        // Queuing a request to pick particles
        Particle target = new Particle(image);
        execute(new DoGPickingTask(target, lowFilter, highFilter,
                                   thresholdFilter, blobExtractor, boxSize,
                                   particleQueueName, executorName));
    }

    public String getParticleQueueName() {
        return particleQueueName;
    }

    @Override
    public BlockingQueue<Particle> getParticleQueue() {
        return extractedParticles;
    }
}
