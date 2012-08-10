package edu.harvard.mcb.leschziner.load;

import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedParticleConsumer;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ImageUploader extends DistributedParticleConsumer {

    protected final String                bucket;
    protected final String                imageQueueName;
    protected final BlockingQueue<String> uploadedUrls;

    public ImageUploader(String bucket, String imageQueueName) {
        super();
        this.bucket = bucket;
        this.imageQueueName = imageQueueName;
        this.uploadedUrls = DefaultStorageEngine.getStorageEngine()
                                                .getQueue(imageQueueName);
    }

    @Override public void processParticle(Particle particle) {
        execute(new ImageUploaderTask(particle,
                                      bucket,
                                      imageQueueName,
                                      executorName));
    }
}
