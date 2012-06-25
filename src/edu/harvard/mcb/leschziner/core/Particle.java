package edu.harvard.mcb.leschziner.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Stack;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.util.MatrixUtils;

public class Particle implements Serializable {

    /**
     * 
     */
    private static final long      serialVersionUID = 8805574980503468420L;

    // Image
    private final BufferedImage    image;

    // Operation stack
    private Stack<AffineTransform> transforms;

    // Constructor
    public Particle(BufferedImage image) {
        this.image = image;
        transforms = new Stack<AffineTransform>();
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

    // Provides a flat region buffer
    public int[] getRegionBuffer(int x, int y, int width, int height) {
        return image.getRGB(x, y, width, height, null, 0, width);
    }

    public int[][] getPixels() {
        // TODO make this efficient
        return getRegion(0, 0, getSize(), getSize());
    }

    public int[] getPixelBuffer() {
        return getRegionBuffer(0, 0, getSize(), getSize());
    }

    public void setPixel(int x, int y, int value) {
        image.setRGB(x, y, value);
    }

    // Transform stack ops
    public void pushTransform(AffineTransform t) {
        // Push a transform that was just executed
        transforms.push(t);
    }

    public Particle untransformed() {
        Particle result = this.clone();

        // Pop off each transform
        Stack<AffineTransform> operationStack = (Stack<AffineTransform>) this.transforms.clone();
        while (!operationStack.isEmpty()) {
            // Invert it if possible,
            try {
                AffineTransform transform = operationStack.pop()
                                                          .createInverse();
                // Apply it to the target
                result = transform(result, transform);
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }

        // Wipe the target's history clean
        result.transforms = operationStack;
        return result;

    }

    // Object handling
    @Override
    public Particle clone() {
        BufferedImage newImage = new BufferedImage(getSize(), getSize(),
                                                   image.getType());
        image.copyData(newImage.getRaster());
        Particle result = new Particle(newImage);
        return result;
    }

    public Particle subParticle(int x, int y, int size) {
        return new Particle(image.getSubimage(x, y, size, size));
    }

    public BufferedImage asBufferedImage() {
        return image;
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
        Particle result = applyOperation(target, operation);
        result.pushTransform(xform);
        return result;
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

    public static Particle scale(Particle target, float scaleFactor) {
        RescaleOp operation = new RescaleOp(scaleFactor, 0, null);
        return applyOperation(target, operation);
    }

    public static Particle addScalar(Particle target, float offset) {
        RescaleOp operation = new RescaleOp(1, offset, null);
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
