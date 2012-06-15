package edu.harvard.mcb.leschziner.deploy;

import java.io.IOException;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.filter.CircularMask;
import edu.harvard.mcb.leschziner.filter.GaussianFilter;
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

            System.out.println("[Main]: Complete");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Particle processParticle(Particle target) {
        // ParticleFilter shift = new Shifter(-32, -32);
        ParticleFilter mask = new CircularMask(80);
        ParticleFilter gauss = new GaussianFilter(3);
        ParticleFilter rotator = new Rotator(45);
        ParticleFilter reCenter = new MassCenterer();

        Particle processed = mask.filter(target);
        processed = gauss.filter(processed);
        processed = rotator.filter(processed);
        processed = reCenter.filter(processed);

        return processed;
    }

}
