package edu.harvard.mcb.leschziner.particlesource;

import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.analyze.BlobExtractor;
import edu.harvard.mcb.leschziner.analyze.CrossCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;
import edu.harvard.mcb.leschziner.util.DisplayUtils;

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
        BlockingQueue<Particle> particleQueue = DefaultStorageEngine.getStorageEngine()
                                                                    .getQueue(particleQueueName);

        // Match the template
        CvMat matchMat = CrossCorrelator.matchTemplate(target, template);
        System.out.println("["
                           + this.getClass().getSimpleName()
                           + "]: Matched Template");

        // Filter out low correlation points
        CvMat filteredMat = CvMat.create(matchMat.rows(),
                                         matchMat.cols(),
                                         matchMat.type());

        opencv_imgproc.cvThreshold(matchMat,
                                   filteredMat,
                                   matchThreshold,
                                   255,
                                   opencv_imgproc.CV_THRESH_TOZERO);
        DisplayUtils.displayMat(filteredMat);

        double padding = boxSize / 2.0;

        // Find Blobs
        Rectangle[] blobs = blobExtractor.extract(filteredMat);
        System.out.println("["
                           + this.getClass().getSimpleName()
                           + "]: "
                           + blobs.length
                           + " blobs");
        for (Rectangle blob : blobs) {
            // Pull the match blob
            if (blob.x + blob.width < filteredMat.cols()
                && blob.y + blob.height < filteredMat.rows()) {
                CvRect cvBlob = BlobExtractor.cvRectFromRectangle(blob);
                CvMat regionMat = CvMat.create(cvBlob.width(), cvBlob.height());
                opencv_core.cvGetSubRect(filteredMat, regionMat, cvBlob);

                // Find Blob Max
                CvPoint max = new CvPoint();
                opencv_core.cvMinMaxLoc(regionMat,
                                        new double[1],
                                        new double[1],
                                        new CvPoint(),
                                        max,
                                        null);

                // Offset that point by the original rect coordinates
                int xPick = blob.x + max.x();
                int yPick = blob.y + max.y();

                // Extract Boxes from original image
                // Check that the boxes are fully in bounds
                if (xPick + padding < target.getSize()
                    && yPick + padding < target.getSize()
                    && xPick - padding > 0
                    && yPick - padding > 0) {

                    Particle extracted = target.subParticle((int) (xPick - padding),
                                                            (int) (yPick - padding),
                                                            boxSize);
                    // Queue the particle
                    particleQueue.add(extracted);
                }
            }
        }

    }
}
