package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;

public class DoGParticleSource implements ParticleSource {

    private ParticleFilter                 lowFilter;
    private ParticleFilter                 highFilter;

    private Vector<ParticleSourceListener> listeners;

    // Queue of micrographs to be processed
    private BlockingQueue<Runnable>        micrographTasks;

    public DoGParticleSource(int lowRadius, int highRadius) {
        lowFilter = new GaussianFilter(lowRadius);
        highFilter = new GaussianFilter(highRadius);
        micrographTasks = new LinkedBlockingQueue<Runnable>();
        listeners = new Vector<ParticleSourceListener>();
    }

    @Override
    public void addListener(ParticleSourceListener p) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeListener(ParticleSourceListener p) {
        // TODO Auto-generated method stub

    }

    private void handleMicrograph(BufferedImage target) {
        // Filter the image with each gaussian

        // Threshold Filter the image

        // Find Blobs

        // Build blob bounding box

        // Extract Particle from target micrograph

        // Notify listeners
    }

    @Override
    public void processMicrograph(final BufferedImage image) {
        micrographTasks.add(new Runnable() {
            @Override
            public void run() {
                handleMicrograph(image);
            }
        });
    }
}
