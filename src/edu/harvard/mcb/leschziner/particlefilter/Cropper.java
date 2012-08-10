package edu.harvard.mcb.leschziner.particlefilter;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class Cropper implements ParticleFilter {
    /**
     * 
     */
    private static final long serialVersionUID = 5035625536774413392L;

    private final int         croppedSize;
    private final int         xOffset;
    private final int         yOffset;

    public Cropper(int croppedSize, int xOffset, int yOffset) {
        this.croppedSize = croppedSize;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override public Particle filter(Particle target) {
        return target.subParticle(xOffset, yOffset, croppedSize);
    }

}
