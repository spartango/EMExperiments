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
            System.out.println("[Main]: Preparing pipeline");

            // Setup the Particle picker
            DoGParticlePicker picker = new DoGParticlePicker(80, 20, 22, 30,
                                                             181, 200);
            // Setup some template generators
            RotationGenerator templateRotator = new RotationGenerator(10);
            ShiftGenerator templateShifter = new ShiftGenerator(5, 2);

            // Setup a pipe full of filters to be applied to picked particles
            ParticleProcessingPipe processor = new ParticleProcessingPipe();
            processor.addStage(new CircularMask(80));
            // processor.addStage(new LowPassFilter(3));
            // processor.addStage(new GaussianFilter(3));

            // Setup a classifier to sort the picked, filtered particles
            CrossCorClassifier classifier = new CrossCorClassifier(.5);

            // Attach the processing pipe to the particle picker
            processor.addParticleSource(picker);
            // Have the classifier get particles from the processing pipe
            classifier.addParticleSource(processor);

            // Load up templates
            for (int i = 1; i <= 2; i++) {
                // Generate many templates that are rotations and shifts from
                // each template
                classifier.addTemplates(templateShifter.generate(templateRotator.generate(Particle.fromFile("templates/template_"
                                                                                                            + i
                                                                                                            + ".png"))));
            }

            System.out.println("[Main]: Loading Images");
            for (int i = 1; i <= 2; i++) {
                BufferedImage micrograph = ImageIO.read(new File(
                                                                 "raw/rib_10fold_49kx_"
                                                                         + i
                                                                         + ".png"));
                System.out.println("[Main]: Processing Micrograph "
                                   + micrograph.hashCode());
                // Pick particles
                picker.processMicrograph(micrograph);
            }

            // Wait for the particles to be picked, processed, and finally
            // classified.
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

            // Get the class averages and write them to files
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
                    // Ignore extremely small classes
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
