package edu.harvard.mcb.leschziner.particlesource;

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

public class DoGParticleSource implements ParticleSource {

    public static int                      CORE_POOL  = 2;
    public static int                      MAX_POOL   = 4;
    public static int                      KEEP_ALIVE = 1000;

    private ParticleFilter                 lowFilter;
    private ParticleFilter                 highFilter;
    private ParticleFilter                 thresholdFilter;

    private BlobExtractor                  blobExtractor;

    private Vector<ParticleSourceListener> listeners;

    // Queue of micrographs to be processed
    private BlockingQueue<Runnable>        micrographTasks;

    private ThreadPoolExecutor             threadPool;

    public DoGParticleSource(int particleSize,
                             int lowRadius,
                             int highRadius,
                             int threshold) {

        lowFilter = new GaussianFilter(lowRadius);
        highFilter = new GaussianFilter(highRadius);
        thresholdFilter = new ThresholdFilter(threshold);
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
        // Wrap the target in a particle for easy processing
        Particle temp = new Particle(target);

        // Filter the image with each gaussian and then the threshold
        Particle filtered = thresholdFilter.filter(highFilter.filter(lowFilter.filter(temp)));

        // Find Blobs
        // TODO
        // Build blob bounding box
        // TODO
        // Extract Particles from target micrograph
        // TODO
        // Notify listeners
    }

    private void notifyListeners(Particle processed) {
        for (ParticleSourceListener listener : listeners) {
            listener.onNewParticle(processed);
        }
    }

    @Override
    public void processMicrograph(final BufferedImage image) {
        // Queuing a request to pick particles
        micrographTasks.add(new Runnable() {
            @Override
            public void run() {
                handleMicrograph(image);
            }
        });
    }
}
