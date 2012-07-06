package edu.harvard.mcb.leschziner.particlesource;

import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.analyze.CrossCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;

public class TemplatePickingTask extends DistributedPickingTask {
    /**
     * 
     */
    private static final long   serialVersionUID = 4675358215115778447L;

    private final Particle      template;
    private final double        matchThreshold;
    private final BlobExtractor blobExtractor;

    public TemplatePickingTask(Particle target,
                               Particle template,
                               int boxSize,
                               double matchThreshold,
                               BlobExtractor extractor,
                               String particleQueueName,
                               String executorName) {
        super(target, boxSize, particleQueueName, executorName);

        this.template = template;
        this.matchThreshold = matchThreshold;
        this.blobExtractor = extractor;
    }

    @Override public void process() {
        BlockingQueue<Particle> particleQueue = Hazelcast.getQueue(particleQueueName);

        // Match the template
        CvMat matchMat = CrossCorrelator.matchTemplate(target, template);

        // Filter out low correlation points
        CvMat filteredMat = CvMat.create(matchMat.rows(), matchMat.cols(),
                                         matchMat.type());

        opencv_imgproc.cvThreshold(matchMat, filteredMat, matchThreshold, 255,
                                   opencv_imgproc.CV_THRESH_TOZERO);

        // Find Blobs
        Rectangle[] blobs = blobExtractor.extract(filteredMat);
        for (Rectangle blob : blobs) {
            // Pull the match blob
            CvRect cvBlob = BlobExtractor.cvRectFromRectangle(blob);
            CvMat regionMat = CvMat.create(cvBlob.width(), cvBlob.height());
            opencv_core.cvGetSubRect(filteredMat, regionMat, cvBlob);

            // Find Blob Max

        }

        // Extract Boxes from original image

    }
}
