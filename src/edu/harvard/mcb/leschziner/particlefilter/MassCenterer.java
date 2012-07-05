package edu.harvard.mcb.leschziner.particlefilter;

import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

/**
 * A Filter that finds the center of mass of a particle, and moves that center
 * to the center of the particle's box
 * 
 * @author spartango
 * 
 */
public class MassCenterer implements ParticleFilter {

    /**
     * 
     */
    private static final long            serialVersionUID = 3903067451461061220L;

    // List of filters to be applied prior to center-finding
    private final Vector<ParticleFilter> preFilters;

    /**
     * Builds a new centerer, ready to center particles
     */
    public MassCenterer() {
        preFilters = new Vector<ParticleFilter>();
    }

    /**
     * Finds a particle's center of mass, and then generates a new particle by
     * shifting the original to center that point
     */
    @Override public Particle filter(Particle target) {
        // Find center of mass
        // in X
        // Sum mass of each column, find greatest
        int massCenterX = 0;
        int massCenterY = 0;

        // Low Pass filter the target to make centering easier
        Particle filtered = target;
        for (ParticleFilter filter : preFilters) {
            filtered = filter.filter(filtered);
        }

        int maxMassX = 0;
        for (int x = 0; x < target.getSize(); x++) {
            int mass = 0;

            // Sum mass
            for (int y = 0; y < target.getSize(); y++) {
                mass += target.getPixelRed(x, y);
            }

            if (mass > maxMassX) {
                maxMassX = mass;
                massCenterX = x;

            }
        }

        // in Y
        // Sum mass of each row, find greatest
        int maxMassY = 0;
        for (int y = 0; y < target.getSize(); y++) {
            int mass = 0;
            // Sum mass
            for (int x = 0; x < target.getSize(); x++) {
                mass += target.getPixelRed(x, y);
            }

            if (mass > maxMassY) {
                maxMassY = mass;
                massCenterY = y;
            }
        }

        System.out.println("[MassCenterer]: Center at (" + massCenterX + ", "
                           + massCenterY + ")");
        Shifter shift = new Shifter((target.getSize() / 2) - massCenterX,
                                    (target.getSize() / 2) - massCenterY);

        return shift.filter(target);
    }

    /**
     * Add filter to be applied to particles that will be centered, so as to
     * reduce noise/error in center of mass finding
     * 
     * @param particle
     *            filter
     */
    public void addPreFilter(ParticleFilter p) {
        preFilters.add(p);
    }
}
