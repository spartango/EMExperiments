package edu.harvard.mcb.leschziner.load;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
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
        Vector<S3Object> objects = new Vector<S3Object>(classifier.getClassIds()
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
                objects.add(object);

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
            simpleMulti.putObjects(targetBucket,
                                   objects.toArray(new S3Object[objects.size()]));
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        // Reuse the vector
        objects.clear();
        Vector<File> tempFiles = new Vector<>();
        for (Long classId : classifier.getClassIds()) {
            String filename = classId + ".zip";
            File zipFile = new File("download/" + filename);

            try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile)) {
                for (Particle target : classifier.getClass(classId)) {
                    // Give the particle a filename
                    UUID uuid = UUID.randomUUID();
                    String path = uuid + ".png";

                    ZipArchiveEntry zipEntry = new ZipArchiveEntry(path);
                    byte[] buffer = target.toPng();
                    zipEntry.setSize(buffer.length);
                    zos.putArchiveEntry(zipEntry);
                    zos.write(buffer);
                    zos.closeArchiveEntry();
                }
                // Keep track of the temporary file
                tempFiles.add(zipFile);
                // Upload the entire class
                S3Object object = new S3Object(zipFile);
                String key = pipelineId + "/" + filename;
                String url = "https://s3.amazonaws.com/"
                             + targetBucket
                             + "/"
                             + key;
                object.setKey(key);
                objects.add(object);

                uploadedClasses.put(classId, url);
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        try {
            simpleMulti.putObjects(targetBucket,
                                   objects.toArray(new S3Object[objects.size()]));
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        // Clean up the files
        for (File file : tempFiles) {
            file.delete();
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
            classResults.putNumber("count", classifier.getClass(classId).size());
            results.putObject(classId.toString(), classResults);
        }

        return results.encode();
    }
}
