package edu.harvard.mcb.leschziner.core;

import java.awt.image.BufferedImage;

public interface ParticleSource {

    public void addListener(ParticleSourceListener p);

    public void removeListener(ParticleSourceListener p);

    public void processMicrograph(BufferedImage image);
}
