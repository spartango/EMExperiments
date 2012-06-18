package edu.harvard.mcb.leschziner.deploy;

import java.io.IOException;

import edu.harvard.mcb.leschziner.analyze.CrossCorrelator;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.filter.CircularMask;
import edu.harvard.mcb.leschziner.filter.LowPassFilter;
import edu.harvard.mcb.leschziner.filter.MassCenterer;
import edu.harvard.mcb.leschziner.filter.Rotator;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Load the particle
            System.out.println("[Main]: Loading particle");
            Particle testParticle = Particle.fromFile("particles/rib15.png");

            System.out.println("[Main]: Processing particle");

            // Start timing
            long startTime = System.currentTimeMillis();

            // Process the particle
            Particle newParticle = processParticle(testParticle);

            // Stop Timing
            long deltaTime = System.currentTimeMillis() - startTime;

            System.out.println("[Main]: Completed Processing in " + deltaTime
                               + "ms");

            System.out.println("[Main]: Writing particle");
            // Write the particle
            newParticle.toFile("processed/rib15_p.png");

            System.out.println("[Main]: Rotating particle");

            // Start timing
            startTime = System.currentTimeMillis();

            // Process the particle
            Particle[] rotated = rotateParticle(newParticle, 20);

            // Stop Timing
            deltaTime = System.currentTimeMillis() - startTime;

            System.out.println("[Main]: Completed Rotations in " + deltaTime
                               + "ms");

            System.out.println("[Main]: Writing rotated particles");
            // Write the particle
            for (int i = 0; i < rotated.length; i++) {
                rotated[i].toFile("processed/rib15r_" + i + ".png");
            }

            double correlation = CrossCorrelator.compare(rotated[0], rotated[4]);
            System.out.println("[Main]: Correlation " + correlation);

            System.out.println("[Main]: Complete");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Particle[] rotateParticle(Particle newParticle,
                                             double deltaTheta) {
        int rotations = (int) (360 / deltaTheta);
        Particle[] rotated = new Particle[rotations];

        // Seed the rotation set with the unrotated particle
        rotated[0] = newParticle.clone();

        for (int i = 1; i < rotations; i++) {
            // Rotate relative to prev
            ParticleFilter rotator = new Rotator(deltaTheta * i);
            rotated[i] = rotator.filter(newParticle);
        }
        return rotated;
    }

    private static Particle processParticle(Particle target) {
        // ParticleFilter shift = new Shifter(-32, -32);
        ParticleFilter mask = new CircularMask(80);
        ParticleFilter lowpass = new LowPassFilter(3);
        ParticleFilter reCenter = new MassCenterer();

        Particle processed = target;

        // Apply filters
        processed = mask.filter(processed);
        processed = lowpass.filter(processed);
        //processed = reCenter.filter(processed);

        return processed;
    }

}
