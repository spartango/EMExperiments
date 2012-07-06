package edu.harvard.mcb.leschziner.analyze;

import java.util.Collection;
import java.util.Iterator;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.core.Particle;

public class ClassAverager {

    /**
     * Averages a set of particles to generate an average, summing each pixel
     * location across all particles, then dividing by the number of images.
     * Average does segregate colorchannels. Returns null if the particle set is
     * empty
     * 
     * @param particles
     *            : to be averaged
     * @return A single average particle
     */
    public static Particle average(Collection<Particle> particles) {
        Iterator<Particle> iter = particles.iterator();
        int particleCount = particles.size();

        if (iter.hasNext()) {
            Particle particle = iter.next();
            // Get the particle dimensions
            int size = particle.getSize();

            // Allocate a sum buffer with greater depth
            IplImage sumBuffer = IplImage.create(new CvSize(size, size), 32,
                                                 particle.getImage()
                                                         .nChannels());

            for (; iter.hasNext(); particle = iter.next()) {
                // Accumulate the images
                opencv_imgproc.cvAcc(particle.getImage(), sumBuffer, null);
            }

            IplImage mean = IplImage.createCompatible(particle.getImage());

            // Rescale
            opencv_core.cvScale(sumBuffer, mean, 1.0 / particleCount, 0);

            Particle average = new Particle(mean);
            return average;
        } else {
            return null;
        }
    }
}
