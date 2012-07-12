package edu.harvard.mcb.leschziner.particlefilter;

import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class Binner implements ParticleFilter {
    /**
     * 
     */
    private static final long serialVersionUID = 851119644112631863L;
    private final double      binningFactor;

    public Binner(double binningFactor) {
        super();
        this.binningFactor = binningFactor;
    }

    @Override public Particle filter(Particle target) {
        int newSize = (int) Math.round(target.getSize() / binningFactor);
        Particle result = new Particle(IplImage.create(new CvSize(newSize,
                                                                  newSize),
                                                       target.getDepth(),
                                                       target.getChannels()));
        opencv_imgproc.cvResize(target.getImage(), result.getImage());
        return result;
    }
}
