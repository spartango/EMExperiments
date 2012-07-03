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

    public static final int LOG_RATE = 5000; // ms

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
            RotationGenerator templateRotator = new RotationGenerator(90);
            ShiftGenerator templateShifter = new ShiftGenerator(3, 2);

            // Setup a pipe full of filters to be applied to picked particles
            ParticleProcessingPipe processor = new ParticleProcessingPipe();
            processor.addStage(new CircularMask(80));
            // processor.addStage(new LowPassFilter(3));
            // processor.addStage(new GaussianFilter(3));

            // Setup a classifier to sort the picked, filtered particles
            CrossCorClassifier classifier = new CrossCorClassifier(0);

            // Attach the processing pipe to the particle picker
            processor.addParticleSource(picker);
            // Have the classifier get particles from the processing pipe
            classifier.addParticleSource(processor);

            // Load up templates
            for (int i = 1; i <= 4; i++) {
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

            // Keep track of the last pending count to calculate rate
            long lastPick = 1;
            long lastProcessed = 1;
            long lastClassfied = 1;

            long prevUnpicked = 0;
            long prevUnprocessed = 0;
            long prevUnclassified = 0;

            do {
                long currentUnpicked = picker.getPendingCount();
                long currentUnprocessed = processor.getPendingCount();
                long currentUnclassified = classifier.getPendingCount();
                // Log pending
                System.out.println("[Main]: " + currentUnpicked
                                   + " micrographs,  " + currentUnprocessed
                                   + " unprocessed particles, and "
                                   + currentUnclassified
                                   + " unclassified particles");
                // Log rate
                if (prevUnpicked > currentUnpicked) {
                    System.out.println("[Main]: Picking at "
                                       + (.001 * lastPick / (prevUnpicked - currentUnpicked))
                                       + " s/micrograph");
                    lastPick = LOG_RATE;
                } else {
                    lastPick += LOG_RATE;
                }

                if (prevUnprocessed > currentUnprocessed) {
                    System.out.println("[Main]: Processing at "
                                       + (.001 * lastProcessed / (prevUnprocessed - currentUnprocessed))
                                       + " s/particle");
                    lastProcessed = LOG_RATE;
                } else {
                    lastProcessed += LOG_RATE;
                }

                if (prevUnclassified > currentUnclassified) {
                    System.out.println("[Main]: Classifying at "
                                       + (.001 * lastClassfied / (prevUnclassified - currentUnclassified))
                                       + " s/particle");
                    lastClassfied = LOG_RATE;
                } else {
                    lastClassfied += LOG_RATE;
                }

                prevUnpicked = currentUnpicked;
                prevUnprocessed = currentUnprocessed;
                prevUnclassified = currentUnclassified;

                try {
                    Thread.sleep(LOG_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (picker.isActive() || processor.isActive()
                     || classifier.isActive());

            // Get the class averages and write them to files
            System.out.println("[Main]: Writing Class Averages");
            for (long templateId : classifier.getTemplateIds()) {
                Particle average = classifier.getAverageForTemplate(templateId);
                if (average != null) {
                    int matches = classifier.getClassForTemplate(templateId)
                                            .size();
                    System.out.println("[Main]: Writing " + templateId
                                       + " with " + matches + " matches");
                    // Ignore extremely small classes
                    if (matches > 3)
                        average.toFile("processed/avg" + templateId + "_"
                                       + matches + ".png");
                }
            }

            System.out.println("[Main]: Complete");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
