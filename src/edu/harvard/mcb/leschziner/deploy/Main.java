package edu.harvard.mcb.leschziner.deploy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.classify.CrossCorClassifier;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleProcessingPipe;
import edu.harvard.mcb.leschziner.core.ParticleSourceListener;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlefilter.GaussianFilter;
import edu.harvard.mcb.leschziner.particlefilter.LowPassFilter;
import edu.harvard.mcb.leschziner.particlefilter.MassCenterer;
import edu.harvard.mcb.leschziner.particlefilter.ThresholdFilter;
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
            DoGParticleSource picker = new DoGParticleSource(60, 20, 22, 30,
                                                             181, 200);

            ParticleProcessingPipe processor = new ParticleProcessingPipe();
            processor.addStage(new CircularMask(80));
            processor.addStage(new LowPassFilter(3));
            //processor.addStage(new GaussianFilter(5));

            CrossCorClassifier classifier = new CrossCorClassifier();

            // Load up templates
            for (int i = 15; i <= 20; i++) {
                classifier.addTemplate(Particle.fromFile("templates/rib_" + i
                                                         + ".png"));
            }

            picker.addListener(processor);
            processor.addListener(classifier);
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

            try {
                Thread.sleep(5500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("[Main]: Writing Class Averages");
            for (Particle template : classifier.getTemplates()) {
                Particle average = classifier.getAverageForTemplate(template);
                if (average != null)
                    average.toFile("processed/avg"
                                   + average.hashCode()
                                   + "_"
                                   + classifier.getClassForTemplate(template)
                                               .size() + ".png");
            }

            System.out.println("[Main]: Complete");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
