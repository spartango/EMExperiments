package edu.harvard.mcb.leschziner.particlefilter;

import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class MassCenterer implements ParticleFilter {

    /**
     * 
     */
    private static final long      serialVersionUID = 3903067451461061220L;
    private Vector<ParticleFilter> preFilters;

    public MassCenterer() {
        preFilters = new Vector<ParticleFilter>();
    }

    @Override
    public Particle filter(Particle target) {
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
        for (int i = 0; i < target.getSize(); i++) {
            int mass = MatrixUtils.sum(ColorUtils.extractRed(filtered.getColumn(i)));
            if (mass > maxMassX) {
                maxMassX = mass;
                massCenterX = i;
                // System.out.println("[MassCenterer]: X Sum(" + i + ") = " +
                // mass);
            }
        }

        // in Y
        // Sum mass of each row, find greatest
        int maxMassY = 0;
        for (int j = 0; j < target.getSize(); j++) {
            int mass = MatrixUtils.sum(ColorUtils.extractRed(filtered.getRow(j)));
            if (mass > maxMassY) {
                maxMassY = mass;
                massCenterY = j;
                // System.out.println("[MassCenterer]: Y Sum(" + j + ") = " +
                // mass);
            }
        }

        System.out.println("[MassCenterer]: Center at (" + massCenterX + ", "
                           + massCenterY + ")");
        Shifter shift = new Shifter((target.getSize() / 2) - massCenterX,
                                    (target.getSize() / 2) - massCenterY);

        return shift.filter(target);
    }

    public void addPreFilter(ParticleFilter p) {
        preFilters.add(p);
    }
}
