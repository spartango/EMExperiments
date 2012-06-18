package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.ColorUtils;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class MassCenterer implements ParticleFilter {

    @Override
    public Particle filter(Particle target) {
        // Find center of mass
        // in X
        // Sum mass of each column, find greatest
        int massCenterX = 0;
        int massCenterY = 0;

        int maxMassX = 0;
        for (int i = 0; i < target.getSize(); i++) {
            int mass = MatrixUtils.sum(ColorUtils.extractRed(target.getColumn(i)) );
            if (mass > maxMassX) {
                maxMassX = mass;
                massCenterX = i;
                //System.out.println("[MassCenterer]: X Sum(" + i + ") = " + mass);
            }
        }

        // in Y
        // Sum mass of each row, find greatest
        int maxMassY = 0;
        for (int j = 0; j < target.getSize(); j++) {
            int mass = MatrixUtils.sum(ColorUtils.extractRed(target.getRow(j)));
            if (mass > maxMassY) {
                maxMassY = mass;
                massCenterY = j;
                //System.out.println("[MassCenterer]: Y Sum(" + j + ") = " + mass);
            }
        }

        System.out.println("[MassCenterer]: Center at (" + massCenterX + ", "
                           + massCenterY + ")");
        Shifter shift = new Shifter((target.getSize() / 2) - massCenterX,
                                    (target.getSize() / 2) - massCenterY);

        return shift.filter(target);
    }

}
