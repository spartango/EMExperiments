package edu.harvard.mcb.leschziner.deploy;

import java.io.IOException;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.filter.CircularMask;
import edu.harvard.mcb.leschziner.filter.GaussianFilter;
import edu.harvard.mcb.leschziner.filter.MassCenterer;

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
            // Process the particle
            Particle newParticle = processParticle(testParticle);

            System.out.println("[Main]: Writing particle");
            // Write the particle
            newParticle.toFile("processed/rib15_p.png");

            System.out.println("[Main]: Complete");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Particle processParticle(Particle testParticle) {
        ParticleFilter preprocess = new CircularMask(1);
        ParticleFilter process = new MassCenterer();
        return process.filter(preprocess.filter(testParticle));
    }

}
