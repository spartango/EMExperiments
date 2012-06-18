package edu.harvard.mcb.leschziner.particlesource.extract;

import java.awt.Rectangle;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;

public class BlobExtractor {

    private int size;
    private int epsillon;

    public BlobExtractor(int size, int epsillon) {
        this.size = size;
        this.epsillon = epsillon;
    }

    public Rectangle[] extract(Particle target) {
        Vector<Rectangle> rects = new Vector<Rectangle>();

        return (Rectangle[]) rects.toArray();
    }

}
