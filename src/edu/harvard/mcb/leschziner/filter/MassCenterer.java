package edu.harvard.mcb.leschziner.filter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class MassCenterer implements ParticleFilter {

    @Override
    public Particle filter(Particle target) {
        // Find center of mass
        // in X
        // Sum mass of each column, find greatest
        // in Y
        // Sum mass of each row, find greatest

        int massCenterX = 0;
        int maxMassX = 0;
        int massCenterY = 0;
        int maxMassY = 0;

        for (int i = 0; i < target.getSize(); i++) {
            int mass = MatrixUtils.sum(target.getColumn(i));
            if (mass > maxMassX) {
                maxMassX = mass;
                massCenterX = i;
            }
        }

        for (int j = 0; j < target.getSize(); j++) {
            int mass = MatrixUtils.sum(target.getColumn(j));
            if (mass > maxMassY) {
                maxMassY = mass;
                massCenterX = j;
            }
        }

        Shifter shift = new Shifter((target.getSize() / 2) - massCenterX,
                                    (target.getSize() / 2) - massCenterY);

        return shift.filter(target);
    }

}
