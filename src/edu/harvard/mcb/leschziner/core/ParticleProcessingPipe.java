package edu.harvard.mcb.leschziner.core;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ParticleProcessingPipe implements ParticleSourceListener {
    public static int                      CORE_POOL  = 4;
    public static int                      MAX_POOL   = 8;
    public static int                      KEEP_ALIVE = 1000;

    private ThreadPoolExecutor             threadPool;
    private BlockingQueue<Runnable>        particleQueue;

    private Vector<ParticleFilter>         stages;

    private Vector<ParticleSourceListener> listeners;

    public ParticleProcessingPipe() {
        stages = new Vector<ParticleFilter>();
        listeners = new Vector<ParticleSourceListener>();
        particleQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(CORE_POOL, MAX_POOL, KEEP_ALIVE,
                                            TimeUnit.MILLISECONDS,
                                            particleQueue);
    }

    public void addStage(ParticleFilter filter) {
        stages.add(filter);
    }

    private void handleParticle(Particle target) {
        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
        notifyListeners(processed);
    }

    @Override
    public void onNewParticle(Particle p) {
        processParticle(p);
    }

    public void addListener(ParticleSourceListener p) {
        listeners.add(p);
    }

    public void removeListener(ParticleSourceListener p) {
        listeners.remove(p);
    }

    private void notifyListeners(Particle processed) {
        for (ParticleSourceListener listener : listeners) {
            listener.onNewParticle(processed);
        }
    }

    public void processParticle(final Particle particle) {
        // Queuing a request 
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                handleParticle(particle);
            }
        });
    }

    public void stop() {
        threadPool.shutdown();
    }
}
