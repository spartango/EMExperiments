package edu.harvard.mcb.leschziner.particlesource;

import java.util.concurrent.BlockingQueue;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.analyze.CrossCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;

public class TemplatePickingTask extends DistributedPickingTask {
    /**
     * 
     */
    private static final long serialVersionUID = 4675358215115778447L;

    private final Particle    template;
    private final int         minSeparation;
    private final double      matchThreshold;

    public TemplatePickingTask(Particle target,
                               Particle template,
                               int boxSize,
                               int minSeparation,
                               double matchThreshold,
                               String particleQueueName,
                               String executorName) {
        super(target, boxSize, particleQueueName, executorName);

        this.template = template;
        this.minSeparation = minSeparation;
        this.matchThreshold = matchThreshold;
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

        // Find Blob Max

        // Extract Boxes from original image
    }
}
