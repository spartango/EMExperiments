package edu.harvard.mcb.leschziner.particlesource;

import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ImageLoaderTask extends DistributedProcessingTask {
    private final String targetPath;
    private final String imageQueueName;

    public ImageLoaderTask(String target,
                           String imageQueueName,
                           String executorName) {
        super(executorName);
        this.targetPath = target;
        this.imageQueueName = imageQueueName;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2452347990745320758L;

    @Override public void process() {
        BlockingQueue<Particle> loadedImages = DefaultStorageEngine.getStorageEngine()
                                                                   .getQueue(imageQueueName);

        // Fetch the image
        // Need to GET the URL, and write it to file
        // Make it a particle

        // Put it on the queue
    }

}
