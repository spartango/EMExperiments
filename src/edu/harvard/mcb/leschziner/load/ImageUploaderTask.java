package edu.harvard.mcb.leschziner.load;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;

import edu.harvard.mcb.leschziner.aws.DefaultCredentials;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ImageUploaderTask extends DistributedProcessingTask {
    /**
     * 
     */
    private static final long serialVersionUID = 6858185730174280848L;

    private final Particle    target;
    private final String      targetBucket;
    private final String      imageQueueName;

    public ImageUploaderTask(Particle particle,
                             String targetBucket,
                             String imageQueueName,
                             String executorName) {
        super(executorName);
        target = particle;
        this.targetBucket = targetBucket;
        this.imageQueueName = imageQueueName;
    }

    @Override public void process() {
        // Generate a UUID for the object
        UUID uuid = UUID.randomUUID();

        // Get the output queue
        BlockingQueue<String> outputQueue = DefaultStorageEngine.getStorageEngine()
                                                                .getQueue(imageQueueName);

        // Prep the particle for putting
        try {
            String filename = uuid.toString() + ".png";
            S3Object object = new S3Object(filename, target.toPng());
            // Connect to S3
            S3Service s3Service = new RestS3Service(DefaultCredentials.getAwsCredentials());
            s3Service.putObject(targetBucket, object);
            outputQueue.add("https://s3.amazonaws.com/"
                            + targetBucket
                            + "/"
                            + filename);
        } catch (NoSuchAlgorithmException | IOException e) {
            markError("Unable to serialize particle", e, 37);
        } catch (S3ServiceException e) {
            markError("Unable to Connect to S3", e, 40);
        }

    }
}
