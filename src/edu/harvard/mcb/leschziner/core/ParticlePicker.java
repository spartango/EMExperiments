package edu.harvard.mcb.leschziner.core;

import java.awt.image.BufferedImage;

/**
 * Selects particles from an image that conains many of them (e.g. a raw
 * micrograph), feeding those particles to consumers
 * 
 * @author spartango
 * 
 */
public interface ParticlePicker extends ParticleSource {

    /**
     * Isolate all the particles in the target image
     * 
     * @param micrograph
     *            to find particles in
     */
    public void processMicrograph(BufferedImage image);
}
