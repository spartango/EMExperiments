package edu.harvard.mcb.leschziner.core;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class Particle {

    // Image
    private BufferedImage image;

    // Dimensions (in px)

    // Constructor
    public Particle(BufferedImage image) {
        this.image = image;
    }

    public int getSize() {
        return image.getHeight();
    }

    // I/O methods

    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }

    public int[] getRow(int y) {
        return image.getRGB(0, y, getSize(), 1, null, 0, getSize());
    }

    public int[] getColumn(int x) {
        return image.getRGB(x, 0, 1, getSize(), null, 0, getSize());
    }

    public int[][] getRegion(int x, int y, int width, int height) {
        int[] flat = image.getRGB(x, y, width, height, null, 0, getSize());
        return MatrixUtils.unflatten(flat, width, height);
    }
    
    public int[][] getPixels() {
        // TODO make this efficient
        return getRegion(0, 0, getSize(), getSize());
    }

    public void setPixel(int x, int y, int value) {
        image.setRGB(x, y, value);
    }

    // Object handling
    public Particle clone() {
        BufferedImage newImage = new BufferedImage(getSize(), getSize(),
                                                   image.getType());
        image.copyData(newImage.getRaster());
        return new Particle(newImage);
    }

    // Serialization
    public void toFile(String filename) throws IOException {
        ImageIO.write(image, "png", new File(filename));
    }

    // File loading
    public static Particle fromFile(String filename) throws IOException {
        BufferedImage particleImage = ImageIO.read(new File(filename));
        return new Particle(particleImage);
    }

    // Primitive operations
    // -----------------------------------------------------------------

    public static Particle transform(Particle target, float[][] matrix) {
        float[] flatMatrix = MatrixUtils.flatten(matrix);

        AffineTransform transform = new AffineTransform(flatMatrix);
        return transform(target, transform);
    }

    public static Particle transform(Particle target, AffineTransform xform) {
        // Build an AffineTransformOp
        AffineTransformOp operation = new AffineTransformOp(
                                                            xform,
                                                            AffineTransformOp.TYPE_BICUBIC);

        return applyOperation(target, operation);
    }

    public static Particle convolve(Particle target, float[][] kernel) {
        int kernelHeight = kernel.length;
        int kernelWidth = kernel[0].length;

        float[] basisKernel = MatrixUtils.flatten(kernel);

        // Build a Kernel
        Kernel newKernel = new Kernel(kernelWidth, kernelHeight, basisKernel);
        return convolve(target, newKernel);
    }

    public static Particle convolve(Particle target, Kernel kernel) {
        // Build a ConvolveOp
        ConvolveOp operation = new ConvolveOp(kernel);

        return applyOperation(target, operation);
    }

    private static Particle applyOperation(Particle target,
                                           BufferedImageOp operation) {
        // Create a destination
        BufferedImage dest = new BufferedImage(target.getSize(),
                                               target.getSize(),
                                               target.image.getType());

        // Apply the op
        operation.filter(target.image, dest);
        return new Particle(dest);
    }
}
