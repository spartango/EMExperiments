package edu.harvard.mcb.leschziner.core;

import java.awt.image.BufferedImage;

public interface ParticlePicker extends ParticleSource {

    public void processMicrograph(BufferedImage image);
}
