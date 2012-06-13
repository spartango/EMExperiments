package edu.harvard.mcb.leschziner.core;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Particle {

    // Image
    private BufferedImage image;

    // Dimensions (in px)
    private int           size;

    // Constructor
    public Particle(BufferedImage image) {
        this.image = image;
        size = image.getHeight();
    }

    public int getPixel(int x, int y) {
        // TODO caching layer
        return image.getRGB(x, y);
    }

    public void setPixel(int x, int y, int value) {
        // TODO buffering layer
        image.setRGB(x, y, value);
    }

    public Particle clone() {
        BufferedImage newImage = new BufferedImage(size, size, image.getType());
        image.copyData(newImage.getRaster());
        return new Particle(newImage);
    }

    public int getSize() {
        return size;
    }

    // Primitive operations
    // -----------------------------------------------------------------
    
    public static Particle convolve(Particle target, float[][] kernel) {
        int kernelHeight = kernel.length;
        int kernelWidth = kernel[0].length;

        float[] basisKernel = new float[kernelWidth * kernelHeight];
        // Copy over the kernel contents
        int index = 0;
        for (int i = 0; i < kernelHeight; i++) {
            for (int j = 0; j < kernelWidth; j++) {
                basisKernel[index] = kernel[i][j];
                index++;
            }
        }
        // Build a Kernel
        Kernel newKernel = new Kernel(kernelWidth, kernelHeight, basisKernel);
        return convolve(target, newKernel);
    }

    public static Particle convolve(Particle target, Kernel kernel) {
        // Build a ConvolveOp
        ConvolveOp operation = new ConvolveOp(kernel);

        // Create a destination
        BufferedImage dest = operation.createCompatibleDestImage(target.image,
                                                                 target.image.getColorModel());

        // Apply the convolve op
        operation.filter(target.image, dest);
        return new Particle(dest); 
    }
}
