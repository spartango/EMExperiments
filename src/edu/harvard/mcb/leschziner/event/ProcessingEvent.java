package edu.harvard.mcb.leschziner.event;

import java.io.Serializable;
import java.util.Date;

/**
 * An event emitted by a processing stage when something notable occurs
 * 
 * @author spartango
 * 
 */
public abstract class ProcessingEvent implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3383566140051748055L;
    protected final Date      time;
    protected final String    className;

    public ProcessingEvent(String className) {
        time = new Date();
        this.className = className;
    }

    public Date getTime() {
        return time;
    }

    public String getClassName() {
        return className;
    }
}
