package edu.harvard.mcb.leschziner.deploy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlefilter.LowPassFilter;
import edu.harvard.mcb.leschziner.particlefilter.MassCenterer;
import edu.harvard.mcb.leschziner.particlefilter.Rotator;
import edu.harvard.mcb.leschziner.particlesource.DoGParticleSource;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Load the particle
            System.out.println("[Main]: Loading Image");
            BufferedImage micrograph = ImageIO.read(new File(
                                                             "raw/sub_rib_10fold_49kx_15.png"));

            // Setup the Particle Builder
            DoGParticleSource picker = new DoGParticleSource(60, 20, 20, 30,
                                                             180, 200);

            picker.addListener(new ParticleSourceListener() {

                @Override
                public void onNewParticle(final Particle p) {
                    System.out.println("[ParticleListener]: New particle "
                                       + p.hashCode());
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            try {
                                Particle newParticle = processParticle(p);

                                newParticle.toFile("processed/rib_"
                                                   + newParticle.hashCode()
                                                   + ".png");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    t.start();
                }
            });

            System.out.println("[Main]: Processing Micrograph");

            // Process the Micrograph
            picker.processMicrograph(micrograph);

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
        ParticleFilter lowpass = new LowPassFilter(5);
        ParticleFilter reCenter = new MassCenterer();

        Particle processed = target;
        // Start timing
        long startTime = System.currentTimeMillis();

        // Apply filters
        processed = mask.filter(processed);
        processed = lowpass.filter(processed);
        // processed = reCenter.filter(processed);

        // Stop Timing
        long deltaTime = System.currentTimeMillis() - startTime;

        System.out.println("[Main " + Thread.currentThread()
                           + "]: Completed Processing in " + deltaTime + "ms");
        return processed;
    }

}
