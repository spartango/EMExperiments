package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.particlefilter.BinaryThresholdFilter;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;

public class DoGParticlePicker extends DistributedParticlePicker {

    // Filters for picking
    private final ParticleFilter lowFilter;
    private final ParticleFilter highFilter;
    private final ParticleFilter thresholdFilter;

    private final BlobExtractor  blobExtractor;

    public DoGParticlePicker(int particleSize,
                             int particleEpsillon,
                             int lowRadius,
                             int highRadius,
                             int threshold,
                             int boxSize) {
        super(boxSize);

        lowFilter = new GaussianFilter(lowRadius);
        highFilter = new GaussianFilter(highRadius);
        thresholdFilter = new BinaryThresholdFilter(threshold);

        blobExtractor = new BlobExtractor(particleSize, particleEpsillon);

    }

    @Override public void processMicrograph(final BufferedImage image) {
        // Queuing a request to pick particles
        Particle target = new Particle(image);
        processMicrograph(target);
    }

    @Override public void processMicrograph(final IplImage image) {
        Particle target = new Particle(image);
        processMicrograph(target);
    }

    @Override public void processMicrograph(Particle target) {
        execute(new DoGPickingTask(target,
                                   lowFilter,
                                   highFilter,
                                   thresholdFilter,
                                   blobExtractor,
                                   boxSize,
                                   particleQueueName,
                                   executorName));
    }

}
