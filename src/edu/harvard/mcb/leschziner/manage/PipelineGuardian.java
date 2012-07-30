package edu.harvard.mcb.leschziner.manage;

import java.util.UUID;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;

/**
 * An agent responsible for all the elements of a particular pipeline
 * 
 * @author spartango
 * 
 */
public class PipelineGuardian {
    private final UUID uuid;

    public PipelineGuardian() {
        uuid = UUID.randomUUID();
    }

    /**
     * 
     * @param parameters
     * @return
     */
    public boolean initialize(String parameters) {
        JsonObject json = new JsonObject(parameters);
        // Check for the appropriate sections (validation)
        if (json.getFieldNames().contains("images")
            && json.getFieldNames().contains("picker")
            && json.getFieldNames().contains("filter")
            && json.getFieldNames().contains("generation")
            && json.getFieldNames().contains("classifier")) {
            // Images

            // Picker

            // Filters

            // Generation

            // Classification

        }
        return false;
    }

    public String getStatusJSON() {
        // TODO Auto-generated method stub
        return null;
    }

    public Buffer getResultsJSON() {
        // TODO Auto-generated method stub
        return null;
    }

    public UUID getUUID() {
        return uuid;
    }

}
