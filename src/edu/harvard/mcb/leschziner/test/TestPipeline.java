package edu.harvard.mcb.leschziner.test;

import java.io.IOException;

import edu.harvard.mcb.leschziner.classify.PCAClassifier;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.load.ImageDownloader;
import edu.harvard.mcb.leschziner.particlefilter.Binner;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlefilter.Cropper;
import edu.harvard.mcb.leschziner.particlefilter.LowPassFilter;
import edu.harvard.mcb.leschziner.particlegenerator.RotationGenerator;
import edu.harvard.mcb.leschziner.particlesource.DoGParticlePicker;
import edu.harvard.mcb.leschziner.pipe.ParticleFilteringPipe;
import edu.harvard.mcb.leschziner.pipe.ParticleGeneratingPipe;

public class TestPipeline {

    public static final int               POLL_RATE = 2000; // ms

    private static ImageDownloader        loader;
    private static DoGParticlePicker      picker;
    private static ParticleFilteringPipe  processor;
    private static ParticleGeneratingPipe generator;
    private static PCAClassifier          classifier;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Setup the pipeline
            initPipeline();

            // Wait for the particles to be picked, processed, and finally
            // classified.
            awaitCompletion();

            classifyParticles();

            // Get the class averages and write them to files
            writeClassAverages();

            System.out.println("[Main]: Complete");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initPipeline() throws IOException {
        System.out.println("[Main]: Preparing pipeline");

        // Setup the image Loader
        loader = new ImageDownloader();

        // Setup the Particle picker
        picker = new DoGParticlePicker(80, 20, 45, 71, 120, 200);

        generator = new ParticleGeneratingPipe();
        // Setup some particle generators
        generator.addStage(new RotationGenerator(90));
        // generator.addStage(new ShiftGenerator(6, 6));

        // Setup a pipe full of filters to be applied to picked particles
        processor = new ParticleFilteringPipe();
        processor.addStage(new CircularMask(80));
        processor.addStage(new LowPassFilter(3));
        processor.addStage(new Cropper(160, 20, 20));
        processor.addStage(new Binner(2));

        // Setup a classifier to sort the picked, filtered particles
        classifier = new PCAClassifier(12, 8, .001);

        // Attach the picker to the loader
        picker.addParticleSource(loader);

        // Attach the generator to the picker
        generator.addParticleSource(picker);

        // Attach the processing pipe to the particle generator
        processor.addParticleSource(generator);

        // Have the classifier get particles from the processing pipe
        classifier.addParticleSource(processor);

        System.out.println("[Main]: Loading Images");
        loader.addImagePath("http://www.filepicker.io/api/file/JIHhJ1NSR9SqOe2bAIGt");

        loader.start();
    }

    private static void awaitCompletion() {
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
            System.out.println("[Main]: "
                               + currentUnpicked
                               + " micrographs,  "
                               + currentUnprocessed
                               + " unprocessed particles, and "
                               + currentUnclassified
                               + " unclassified particles");
            // Log rate
            if (prevUnpicked > currentUnpicked) {
                System.out.println("[Main]: Picking at "
                                   + (.001 * lastPick / (prevUnpicked - currentUnpicked))
                                   + " s/micrograph");
                lastPick = POLL_RATE;
            } else {
                lastPick += POLL_RATE;
            }

            if (prevUnprocessed > currentUnprocessed) {
                System.out.println("[Main]: Processing at "
                                   + (.001 * lastProcessed / (prevUnprocessed - currentUnprocessed))
                                   + " s/particle");
                lastProcessed = POLL_RATE;
            } else {
                lastProcessed += POLL_RATE;
            }

            if (prevUnclassified > currentUnclassified) {
                System.out.println("[Main]: Classifying at "
                                   + (.001 * lastClassfied / (prevUnclassified - currentUnclassified))
                                   + " s/particle");
                lastClassfied = POLL_RATE;
            } else {
                lastClassfied += POLL_RATE;
            }

            prevUnpicked = currentUnpicked;
            prevUnprocessed = currentUnprocessed;
            prevUnclassified = currentUnclassified;

            try {
                Thread.sleep(POLL_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (loader.isActive()
                 || picker.isActive()
                 || processor.isActive()
                 || generator.isActive()
                 || classifier.isActive());
    }

    private static void classifyParticles() {
        System.out.println("[Main]: "
                           + classifier.getParticlesConsumed()
                           + " particles consumed");
        System.out.println("[Main]: Classifying...");
        long startTime = System.currentTimeMillis();
        // Execute the Mass Classification
        classifier.classifyAll();
        long runTime = System.currentTimeMillis() - startTime;
        System.out.println("[Main]: Completed Classification in "
                           + (runTime / 1000.0)
                           + " s");

    }

    private static void writeClassAverages() throws IOException {
        System.out.println("[Main]: Writing Class Averages");
        for (long classId : classifier.getClassIds()) {
            Particle average = classifier.getClassAverage(classId);
            if (average != null) {
                int matches = classifier.getClass(classId).size();
                System.out.println("[Main]: Writing "
                                   + classId
                                   + " with "
                                   + matches
                                   + " matches");
                average.toFile("processed/avg"
                               + classId
                               + "_"
                               + matches
                               + ".png");

            }
        }
    }
}
