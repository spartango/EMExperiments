package edu.harvard.mcb.leschziner.deploy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.classify.CrossCorClassifier;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlegenerator.RotationGenerator;
import edu.harvard.mcb.leschziner.particlegenerator.ShiftGenerator;
import edu.harvard.mcb.leschziner.particlesource.DoGParticlePicker;
import edu.harvard.mcb.leschziner.pipe.ParticleProcessingPipe;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Load the particle
            System.out.println("[Main]: Preparing pipeline");
            RotationGenerator templateRotator = new RotationGenerator(10);
            ShiftGenerator templateShifter = new ShiftGenerator(5, 2);

            // Setup the Particle Builder
            DoGParticlePicker picker = new DoGParticlePicker(80, 20, 22, 30,
                                                             181, 200);

            ParticleProcessingPipe processor = new ParticleProcessingPipe();
            processor.addStage(new CircularMask(80));
            // processor.addStage(new LowPassFilter(3));
            // processor.addStage(new GaussianFilter(3));

            CrossCorClassifier classifier = new CrossCorClassifier(.95);

            processor.addParticleSource(picker);

            classifier.addParticleSource(processor);

            // Load up templates
            for (int i = 16; i <= 19; i++) {
                classifier.addTemplates(templateShifter.generate(templateRotator.generate(Particle.fromFile("templates/rib_"
                                                                                                            + i
                                                                                                            + ".png"))));
            }

            System.out.println("[Main]: Loading Images");
            for (int i = 1; i <= 20; i++) {
                BufferedImage micrograph = ImageIO.read(new File(
                                                                 "/Volumes/allab/agupta/Raw/rib_10fold_49kx_"
                                                                         + i
                                                                         + ".png"));
                System.out.println("[Main]: Processing Micrograph "
                                   + micrograph.hashCode());
                picker.processMicrograph(micrograph);
            }

            do {
                System.out.println("[Main]: " + picker.getPendingCount()
                                   + " micrographs and "
                                   + classifier.getPendingCount()
                                   + " unclassified particles waiting");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (picker.isActive() || classifier.isActive());

            System.out.println("[Main]: Writing Class Averages");
            for (Particle template : classifier.getTemplates()) {
                Particle average = classifier.getAverageForTemplate(template);
                if (average != null) {
                    int matches = classifier.getClassForTemplate(template)
                                            .size();
                    System.out.println("[Main]: Template "
                                       + template.hashCode() + " -> "
                                       + average.hashCode() + " with "
                                       + matches);
                    if (matches > 3)
                        average.toFile("processed/avg" + template.hashCode()
                                       + "_" + matches + ".png");
                }
            }

            System.out.println("[Main]: Complete");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
