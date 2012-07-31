package edu.harvard.mcb.leschziner.particlesource;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleSource;
import edu.harvard.mcb.leschziner.distributed.DistributedTaskHandler;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ImageLoader extends DistributedTaskHandler implements
                                                       ParticleSource {
    private final Vector<String>            imagePaths;

    // Queue of particles produced
    protected final String                  imageQueueName;
    protected final BlockingQueue<Particle> loadedImages;

    public ImageLoader() {
        super();

        imagePaths = new Vector<>();

        imageQueueName = "LoadedImages_" + this.hashCode();
        loadedImages = DefaultStorageEngine.getStorageEngine()
                                           .getQueue(imageQueueName);

    }

    public void addImagePath(String imagePath) {
        imagePaths.add(imagePath);
    }

    public void start() {
        for (String path : imagePaths) {
            execute(new ImageLoaderTask(path, imageQueueName, executorName));
        }
    }

    @Override public BlockingQueue<Particle> getParticleQueue() {
        return loadedImages;
    }
}
