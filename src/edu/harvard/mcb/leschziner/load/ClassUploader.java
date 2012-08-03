package edu.harvard.mcb.leschziner.load;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.multi.SimpleThreadedStorageService;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.aws.DefaultCredentials;
import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ClassUploader {
    private final ParticleClassifier     classifier;

    private final String                 targetBucket;
    private final String                 pipelineId;
    private final String                 classMapName;
    private final MultiMap<Long, String> uploadedClasses;
    private final String                 averageMapName;
    private final Map<Long, String>      uploadedAverages;

    private S3Service                    s3Service;

    public ClassUploader(ParticleClassifier classifier,
                         String bucket,
                         String pipelineId) {
        super();
        this.classifier = classifier;
        classMapName = "UploadedClasses_" + this.hashCode();
        uploadedClasses = DefaultStorageEngine.getStorageEngine()
                                              .getMultiMap(classMapName);
        averageMapName = "UploadedAverages_" + this.hashCode();
        uploadedAverages = DefaultStorageEngine.getStorageEngine()
                                               .getMap(averageMapName);
        targetBucket = bucket;
        this.pipelineId = pipelineId;

        initS3Connection();
    }

    private boolean initS3Connection() {
        try {
            s3Service = new RestS3Service(DefaultCredentials.getAwsCredentials());
            return true;
        } catch (S3ServiceException e) {
            System.err.println("["
                               + this.getClass().getSimpleName()
                               + "]: Failed to connect to S3");
            s3Service = null;
            return false;
        }
    }

    public void uploadAll() {
        if (s3Service == null && !initS3Connection()) {
            return;
        }
        SimpleThreadedStorageService simpleMulti = new SimpleThreadedStorageService(s3Service);
        Vector<S3Object> averageObjects = new Vector<S3Object>(classifier.getClassIds()
                                                                         .size());
        // For each class
        for (Long classId : classifier.getClassIds()) {
            System.out.println("["
                               + this.getClass().getSimpleName()
                               + "]: Uploading class "
                               + classId);

            UUID uuid = UUID.randomUUID();

            // Prep the class average
            String filename = pipelineId + "/" + uuid.toString() + ".png";
            String url = "https://s3.amazonaws.com/"
                         + targetBucket
                         + "/"
                         + filename;
            S3Object object;
            try {
                object = new S3Object(filename,
                                      classifier.getClassAverage(classId)
                                                .toPng());
                averageObjects.add(object);

                uploadedAverages.put(classId, url);
                System.out.println("["
                                   + this.getClass().getSimpleName()
                                   + "]: Put average, "
                                   + url
                                   + " in S3");
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

        }
        try {
            StorageObject[] createdObjects = simpleMulti.putObjects(targetBucket,
                                                                    averageObjects.toArray(new S3Object[averageObjects.size()]));
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        for (Long classId : classifier.getClassIds()) {
            // Write the particles to disk in their own directory
            String folder = "upload/" + classId + "/";
            for (Particle target : classifier.getClass(classId)) {
                UUID uuid = UUID.randomUUID();
                try {
                    target.toFile(folder + uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Zip the directory
            // Upload the zip
            // Remove the directory
            // Upload each of the particles

        }
    }

    private String s3Put(Particle target) {
        // Generate a UUID for the object
        UUID uuid = UUID.randomUUID();

        // Prep the particle for putting
        try {
            String filename = pipelineId + "/" + uuid.toString() + ".png";
            String url = "https://s3.amazonaws.com/"
                         + targetBucket
                         + "/"
                         + filename;
            S3Object object = new S3Object(filename, target.toPng());
            // Connect to S3
            s3Service.putObject(targetBucket, object);
            return url;
        } catch (NoSuchAlgorithmException | IOException | S3ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTargetBucket() {
        return targetBucket;
    }

    public String getClassMapName() {
        return classMapName;
    }

    public MultiMap<Long, String> getUploadedClasses() {
        return uploadedClasses;
    }

    public String getAverageMapName() {
        return averageMapName;
    }

    public Map<Long, String> getUploadedAverages() {
        return uploadedAverages;
    }

    public String getResultsJson() {
        JsonObject results = new JsonObject();

        for (Long classId : uploadedClasses.keySet()) {
            JsonObject classResults = new JsonObject();
            JsonArray classParticles = new JsonArray();
            for (String url : uploadedClasses.get(classId)) {
                classParticles.addString(url);
            }
            classResults.putArray("particles", classParticles);
            classResults.putString("average", uploadedAverages.get(classId));
            classResults.putNumber("count", uploadedClasses.get(classId).size());
            results.putObject(classId.toString(), classResults);
        }

        return results.encode();
    }
}
