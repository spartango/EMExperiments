package edu.harvard.mcb.leschziner.particlesource;

import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class DoGPickingTask extends DistributedPickingTask {

    /**
     * 
     */
    private static final long    serialVersionUID = 1262983259390203636L;

    private final ParticleFilter lowFilter;
    private final ParticleFilter highFilter;
    private final ParticleFilter thresholdFilter;
    private final BlobExtractor  blobExtractor;

    public DoGPickingTask(Particle target,
                          ParticleFilter lowFilter,
                          ParticleFilter highFilter,
                          ParticleFilter thresholdFilter,
                          BlobExtractor blobExtractor,
                          int boxSize,
                          String particleQueueName,
                          String executorName) {
        super(target, boxSize, particleQueueName, executorName);

        this.lowFilter = lowFilter;
        this.highFilter = highFilter;
        this.thresholdFilter = thresholdFilter;
        this.blobExtractor = blobExtractor;

    }

    @Override public void process() {
        BlockingQueue<Particle> particleQueue = Hazelcast.getQueue(particleQueueName);

        // Filter the image with each gaussian and then the threshold
        Particle lowFiltered = lowFilter.filter(target);
        Particle highFiltered = highFilter.filter(lowFiltered);
        // Particle subtracted = Particle.subtract(lowFiltered, highFiltered);
        Particle thresholded = thresholdFilter.filter(highFiltered);

        // Debug visualization
        // DisplayUtils.displayParticle(highFiltered);
        DisplayUtils.displayParticle(thresholded);

        // Find Blobs
        Rectangle[] blobs = blobExtractor.extract(thresholded);
        System.out.println("[DoGParticleSource]: Extracted " + blobs.length
                           + " blobs from " + target.hashCode());
        // Extract Particles from target micrograph
        // Pad the particles with a box
        double padding = boxSize / 2.0;
        for (Rectangle boundingBox : blobs) {
            // Check that we can actually extract this particle (Bounds)
            int xOffset = (int) (boundingBox.getCenterX() - padding);
            int yOffset = (int) (boundingBox.getCenterY() - padding);
            if (xOffset + boxSize < target.getSize()
                && yOffset + boxSize < target.getSize() && xOffset > 0
                && yOffset > 0) {
                Particle extracted = target.subParticle(xOffset, yOffset,
                                                        boxSize);
                // Queue the particle
                particleQueue.add(extracted);

                // Mark completed
            }
        }
    }
}
