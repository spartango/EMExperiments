package edu.harvard.mcb.leschziner.manage;

import java.util.UUID;

import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import edu.harvard.mcb.leschziner.classify.DistributedClassifier;
import edu.harvard.mcb.leschziner.classify.PCAClassifier;
import edu.harvard.mcb.leschziner.particlefilter.Binner;
import edu.harvard.mcb.leschziner.particlefilter.CircularMask;
import edu.harvard.mcb.leschziner.particlefilter.LowPassFilter;
import edu.harvard.mcb.leschziner.particlegenerator.RotationGenerator;
import edu.harvard.mcb.leschziner.particlegenerator.ShiftGenerator;
import edu.harvard.mcb.leschziner.particlesource.DistributedParticlePicker;
import edu.harvard.mcb.leschziner.particlesource.DoGParticlePicker;
import edu.harvard.mcb.leschziner.particlesource.ImageLoader;
import edu.harvard.mcb.leschziner.pipe.ParticleFilteringPipe;
import edu.harvard.mcb.leschziner.pipe.ParticleGeneratingPipe;

/**
 * An agent responsible for all the elements of a particular pipeline
 * 
 * @author spartango
 * 
 */
public class PipelineGuardian {
    private final UUID                uuid;

    private ImageLoader               loader;
    private DistributedParticlePicker picker;
    private ParticleFilteringPipe     filter;
    private ParticleGeneratingPipe    generator;
    private DistributedClassifier     classifier;

    public PipelineGuardian() {
        uuid = UUID.randomUUID();
    }

    /**
     * 
     * @param parameters
     * @return
     */
    public boolean initialize(String parameters) {
        try {
            JsonObject json = new JsonObject(parameters);

            // Check for the appropriate sections (validation)
            if (validateParameters(json)) {
                System.out.println("[Guardian "
                                   + this.uuid.toString()
                                   + "]: Building pipeline");
                // Images
                JsonArray images = json.getArray("images");
                // Build an ImageLoader
                initLoader(images);

                // Picker
                JsonObject pickerParams = json.getObject("picker");
                // Build a Picker
                initPicker(pickerParams);

                // Filters
                JsonObject filterParams = json.getObject("filter");
                // Build a filter pipe
                initFilterPipe(filterParams);

                // Generation
                JsonObject generationParams = json.getObject("generation");
                // Build a generator pipe
                initGeneratorPipe(generationParams);

                // Classification
                JsonObject classifierParams = json.getObject("classifier");
                // Build a classifier
                initClassifier(classifierParams);

                // Check that everything got built
                if (pipelineBuilt()) {
                    System.out.println("[Guardian "
                                       + this.uuid.toString()
                                       + "]: Starting pipeline");
                    // Wire the pipes together
                    linkPipes();
                    // Start the loader
                    loader.start();
                    return true;
                }

            }
        } catch (DecodeException e) {
            System.err.println("["
                               + this.getClass().getSimpleName()
                               + "]: Failed to decode body");
        }
        return false;

    }

    private void linkPipes() {
        picker.addParticleSource(loader);
        filter.addParticleSource(picker);
        generator.addParticleSource(filter);
        classifier.addParticleSource(generator);
    }

    private static boolean validatePickerParams(JsonObject pickerParams) {
        return pickerParams.getInteger("particleSize") != null
               && pickerParams.getInteger("particleEpsilon") != null
               && pickerParams.getInteger("boxSize") != null
               && pickerParams.getInteger("firstFilter") != null
               && pickerParams.getInteger("secondFilter") != null
               && pickerParams.getInteger("threshold") != null;
    }

    private void initPicker(JsonObject pickerParams) {
        if (validatePickerParams(pickerParams)) {
            picker = new DoGParticlePicker(pickerParams.getInteger("particleSize"),
                                           pickerParams.getInteger("particleEpsilon"),
                                           pickerParams.getInteger("firstFilter"),
                                           pickerParams.getInteger("secondFilter"),
                                           pickerParams.getInteger("threshold"),
                                           pickerParams.getInteger("boxSize"));
        }

    }

    private static boolean validateGeneratorParams(JsonObject generatorParams) {
        return generatorParams.getNumber("rotationAngle") != null;
    }

    private void initGeneratorPipe(JsonObject generationParams) {
        if (validateGeneratorParams(generationParams)) {
            generator = new ParticleGeneratingPipe();
            // Rotation generator (if needed)
            double rotationAngle = generationParams.getNumber("rotationAngle")
                                                   .doubleValue();
            if (rotationAngle > 0) {
                generator.addStage(new RotationGenerator(rotationAngle));
            }

            // Shift generator (if needed)
            Integer shiftDistance = generationParams.getInteger("shiftDistance");
            Integer shiftIncrement = generationParams.getInteger("shiftIncrement");

            if (shiftDistance != null
                && shiftIncrement != null
                && shiftDistance > 0
                && shiftIncrement > 0) {
                generator.addStage(new ShiftGenerator(shiftDistance,
                                                      shiftIncrement));
            }
        }
    }

    private static boolean validateFilterParams(JsonObject filterParams) {
        return filterParams.getInteger("maskSize") != null
               && filterParams.getInteger("lowPassFilter") != null
               && filterParams.getInteger("binning") != null;
    }

    private void initFilterPipe(JsonObject filterParams) {
        if (validateFilterParams(filterParams)) {
            filter = new ParticleFilteringPipe();

            // Circular Mask
            filter.addStage(new CircularMask(filterParams.getInteger("maskSize")));

            // Low Pass Filter (if needed)
            int lowPassFilter = filterParams.getInteger("lowPassFilter");
            if (lowPassFilter > 1) {
                filter.addStage(new LowPassFilter(lowPassFilter));
            }
            // Binner (if needed)
            int binning = filterParams.getInteger("binning");
            if (binning > 1) {
                filter.addStage(new Binner(binning));
            }
        }
    }

    private static boolean
            validateClassifierParams(JsonObject classifierParams) {
        return classifierParams.getNumber("classAccuracy") != null
               && classifierParams.getInteger("classCount") != null
               && classifierParams.getInteger("principalComponents") != null;
    }

    private void initClassifier(JsonObject classifierParams) {
        if (validateClassifierParams(classifierParams)) {
            classifier = new PCAClassifier(classifierParams.getInteger("principalComponents"),
                                           classifierParams.getInteger("classCount"),
                                           classifierParams.getNumber("classAccuracy")
                                                           .doubleValue());
        }
    }

    private void initLoader(JsonArray images) {
        loader = new ImageLoader();
        for (Object o : images) {
            // Hope this is a real string? else we'll choke later
            loader.addImagePath(o.toString());
        }
    }

    public String getStatusJSON() {
        // TODO Auto-generated method stub
        JsonObject status = new JsonObject();

        JsonObject loaderStatus = new JsonObject();
        loaderStatus.putNumber("pending", loader.getPendingCount());
        loaderStatus.putNumber("requests", loader.getTotalRequests());
        status.putObject("loader", loaderStatus);

        JsonObject pickerStatus = new JsonObject();
        pickerStatus.putNumber("pending", picker.getPendingCount());
        pickerStatus.putNumber("requests", picker.getTotalRequests());
        status.putObject("picker", pickerStatus);

        JsonObject filterStatus = new JsonObject();
        filterStatus.putNumber("pending", filter.getPendingCount());
        filterStatus.putNumber("requests", filter.getTotalRequests());
        status.putObject("filter", filterStatus);

        JsonObject generatorStatus = new JsonObject();
        generatorStatus.putNumber("pending", generator.getPendingCount());
        generatorStatus.putNumber("requests", generator.getTotalRequests());
        status.putObject("generation", generatorStatus);

        JsonObject classfierStatus = new JsonObject();
        classfierStatus.putNumber("consumed", classifier.getParticlesConsumed());
        classfierStatus.putNumber("classes", classifier.getClassIds().size());
        status.putObject("classifier", classfierStatus);
        return status.encode();
    }

    public String getResultsJSON() {
        // TODO Auto-generated method stub
        JsonObject results = new JsonObject();
        JsonObject classes = new JsonObject();
        for (Long id : classifier.getClassIds()) {
            classes.putNumber(id.toString(), classifier.getClass(id).size());
        }
        results.putObject("classes", classes);
        return results.encode();
    }

    public UUID getUUID() {
        return uuid;
    }

    private static boolean validateParameters(JsonObject json) {
        return json.getArray("images") != null
               && json.getObject("picker") != null
               && json.getObject("filter") != null
               && json.getObject("generation") != null
               && json.getObject("classifier") != null;
    }

    private boolean pipelineBuilt() {
        return loader != null
               && picker != null
               && filter != null
               && generator != null
               && classifier != null;
    }

}