package edu.harvard.mcb.leschziner.particlesource;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

public class TemplateParticleSource implements ParticleSource {

    public static int                      CORE_POOL  = 2;
    public static int                      MAX_POOL   = 4;
    public static int                      KEEP_ALIVE = 1000;

    private int                            boxSize;

    private Vector<ParticleSourceListener> listeners;

    // Queue of micrographs to be processed
    private BlockingQueue<Runnable>        micrographTasks;
    private Vector<Particle>               templates;

    private ThreadPoolExecutor             threadPool;

    public TemplateParticleSource(int boxSize, int threshold) {
        this.boxSize = boxSize;

        micrographTasks = new LinkedBlockingQueue<Runnable>();
        listeners = new Vector<ParticleSourceListener>();

        // Spin up threadpool
        threadPool = new ThreadPoolExecutor(CORE_POOL, MAX_POOL, KEEP_ALIVE,
                                            TimeUnit.MILLISECONDS,
                                            micrographTasks);
    }

    public void addTemplate(Particle template) {
        templates.add(template);
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
        Particle micrograph = new Particle(target);

        // For each template
        for (Particle template : templates) {
            // Build a kernel for the template
            int templateSize = template.getSize();
            float[] basis = new float[templateSize * templateSize];
            System.arraycopy(template.getPixelBuffer(), 0, basis, 0,
                             templateSize);
            Kernel templateKernel = new Kernel(templateSize, templateSize,
                                               basis);
            // Convolve the micrograph with the template
            Particle convolved = Particle.convolve(micrograph, templateKernel);

            DisplayUtils.displayParticle(convolved);
            // Pick blobs

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
                System.out.println("[Template " + Thread.currentThread()
                                   + "]: Completed Processing in " + deltaTime
                                   + "ms");
            }
        });
    }

    public void stop() {
        threadPool.shutdown();
    }
}
