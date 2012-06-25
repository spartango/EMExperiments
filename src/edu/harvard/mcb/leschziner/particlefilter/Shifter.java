package edu.harvard.mcb.leschziner.particlefilter;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class Shifter implements ParticleFilter {

    private AffineTransform transform;

    public Shifter(double x, double y) { // Degrees
        transform = generateTransform(x, y);
    }

    private static AffineTransform generateTransform(double x, double y) {
        return AffineTransform.getTranslateInstance(x, y);
    }

    @Override
    public Particle filter(Particle target) {
        return Particle.transform(target, transform);
    }

    public Particle reverseFilter(Particle target) {
        try {
            return Particle.transform(target, transform.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return target;
        }
    }

}
