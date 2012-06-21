package edu.harvard.mcb.leschziner.deploy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleProcessingPipe;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;
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
                                                             "raw/rib_10fold_49kx_15.png"));

            // Setup the Particle Builder
            DoGParticleSource picker = new DoGParticleSource(60, 20, 20, 30,
                                                             180, 200);
            ParticleProcessingPipe processor = new ParticleProcessingPipe();
            processor.addStage(new CircularMask(80));
            processor.addStage(new GaussianFilter(5));
            picker.addListener(processor);

            processor.addListener(new ParticleSourceListener() {

                @Override
                public void onNewParticle(final Particle p) {
                    System.out.println("[ParticleListener]: New particle "
                                       + p.hashCode());

                    try {
                        p.toFile("processed/rib_" + p.hashCode() + ".png");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            System.out.println("[Main]: Processing Micrograph");

            // Process the Micrograph
            picker.processMicrograph(micrograph);

            System.out.println("[Main]: Complete");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
