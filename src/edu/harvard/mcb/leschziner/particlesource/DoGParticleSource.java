package edu.harvard.mcb.leschziner.particlesource;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;
import edu.harvard.mcb.leschziner.particlefilter.ThresholdFilter;
import edu.harvard.mcb.leschziner.particlesource.extract.BlobExtractor;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class DoGParticleSource implements ParticleSource {

    public static int                      CORE_POOL  = 2;
    public static int                      MAX_POOL   = 8;
    public static int                      KEEP_ALIVE = 1000;

    private int                            boxSize;

    private ParticleFilter                 lowFilter;
    private ParticleFilter                 highFilter;
    private ParticleFilter                 thresholdFilter;

    private BlobExtractor                  blobExtractor;

    private Vector<ParticleSourceListener> listeners;

    // Queue of micrographs to be processed
    private BlockingQueue<Runnable>        micrographTasks;

    private ThreadPoolExecutor             threadPool;

    public DoGParticleSource(int particleSize,
                             int particleEpsillon,
                             int lowRadius,
                             int highRadius,
                             int threshold,
                             int boxSize) {

        this.boxSize = boxSize;

        lowFilter = new GaussianFilter(lowRadius);
        highFilter = new GaussianFilter(highRadius);
        thresholdFilter = new ThresholdFilter(threshold);

        blobExtractor = new BlobExtractor(particleSize, particleEpsillon);

        micrographTasks = new LinkedBlockingQueue<Runnable>();
        listeners = new Vector<ParticleSourceListener>();

        // Spin up threadpool
        threadPool = new ThreadPoolExecutor(CORE_POOL, MAX_POOL, KEEP_ALIVE,
                                            TimeUnit.MILLISECONDS,
                                            micrographTasks);
    }

    @Override
    public void addListener(ParticleSourceListener p) {
        listeners.add(p);
    }

    @Override
    public void removeListener(ParticleSourceListener p) {
        listeners.remove(p);
    }

    private void handleMicrograph(BufferedImage target) {
        // TODO square crop target

        // Wrap the target in a particle for easy processing
        Particle temp = new Particle(target);

        // Filter the image with each gaussian and then the threshold
        Particle filtered = highFilter.filter(lowFilter.filter(temp));
        Particle thresholded = thresholdFilter.filter(filtered);

        // Debug visualization
        // DisplayUtils.displayParticle(filtered);
        // DisplayUtils.displayParticle(thresholded);

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
            if (xOffset + boxSize < temp.getSize()
                && yOffset + boxSize < temp.getSize() && xOffset > 0
                && yOffset > 0) {
                Particle extracted = temp.subParticle(xOffset, yOffset, boxSize);
                // Notify listeners
                notifyListeners(extracted);
            }
        }
    }

    private void notifyListeners(Particle processed) {
        for (ParticleSourceListener listener : listeners) {
            listener.onNewParticle(processed);
        }
    }

    @Override
    public void processMicrograph(final BufferedImage image) {
        // Queuing a request to pick particles
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                // Start timing
                long startTime = System.currentTimeMillis();
                handleMicrograph(image);
                // Stop Timing
                long deltaTime = System.currentTimeMillis() - startTime;
                System.out.println("[DoG " + Thread.currentThread()
                                   + "]: Completed Processing in " + deltaTime
                                   + "ms");
            }
        });
    }

    public void stop() {
        threadPool.shutdown();
    }
}
