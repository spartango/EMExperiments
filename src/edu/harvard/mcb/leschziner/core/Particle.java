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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Stack;

import javax.imageio.ImageIO;

import edu.harvard.mcb.leschziner.util.MatrixUtils;

/**
 * An image of a single particle from an EM image, generally a single
 * protein/object. The particle is enclosed in a square box, with some of the
 * surround.
 * 
 * @author spartango
 * 
 */
public class Particle implements Serializable {

    /**
     * 
     */
    private static final long       serialVersionUID = 8805574980503468420L;

    // Image can't be serialized, will be transferred manually
    private transient BufferedImage image;

    // Stack of reversible transformation operations performed on this particle
    private Stack<AffineTransform>  transforms;

    /**
     * Builds a particle from a square image
     * 
     * @param image
     */
    public Particle(BufferedImage image) {
        this.image = image;
        transforms = new Stack<AffineTransform>();
    }

    /**
     * Gets the size of the particle's box
     * 
     * @return particle box size in pixels
     */
    public int getSize() {
        return image.getHeight();
    }

    // I/O methods

    /**
     * Gets the RGB value of a single pixel
     * 
     * @param x
     *            position
     * @param y
     *            position
     * @return RGB pixel value
     */
    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }

    /**
     * Gets a single row of pixels
     * 
     * @param y
     *            coordinate of the row
     * @return the row of RGB pixels
     */
    public int[] getRow(int y) {
        return image.getRGB(0, y, getSize(), 1, null, 0, getSize());
    }

    /**
     * Gets a single column of pixels
     * 
     * @param x
     *            coordinate of the column
     * @return
     */
    public int[] getColumn(int x) {
        return image.getRGB(x, 0, 1, getSize(), null, 0, getSize());
    }

    /**
     * Gets the pixels in a bounded region
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return Array of Arrays (2D) of RGB pixels
     */
    public int[][] getRegion(int x, int y, int width, int height) {
        int[] flat = image.getRGB(x, y, width, height, null, 0, getSize());
        return MatrixUtils.unflatten(flat, width, height);
    }

    /**
     * Provides a buffer with all the RGB pixels in a region
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return An Array of RGB pixels, in row major order
     */
    public int[] getRegionBuffer(int x, int y, int width, int height) {
        return image.getRGB(x, y, width, height, null, 0, width);
    }

    /**
     * Gets all the pixels in the particle
     * 
     * @return A 2D array of the RGB pixels in the particle
     */
    public int[][] getPixels() {
        // TODO make this efficient
        return getRegion(0, 0, getSize(), getSize());
    }

    /**
     * Provides a buffer with all the RGB pixels in the particle
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return An Array of RGB pixels, in row major order
     */
    public int[] getPixelBuffer() {
        return getRegionBuffer(0, 0, getSize(), getSize());
    }

    /**
     * Sets the value of a pixel at a given location
     * 
     * @param x
     *            position
     * @param y
     *            position
     * @param RGB
     *            value
     */
    public void setPixel(int x, int y, int value) {
        image.setRGB(x, y, value);
    }

    /**
     * Records a transformation in this particle's history of transformations
     * 
     * @param affine
     *            transform
     */
    public void pushTransform(AffineTransform t) {
        // Push a transform that was just executed
        transforms.push(t);
    }

    /**
     * Sequentially undoes all the transformations previously performed on this
     * particle, returning a new particle in the original state
     * 
     * @return new particle in untransformed (inverse transformed) state
     */
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

    /**
     * Provides a clone of this particle
     */
    @Override
    public Particle clone() {
        BufferedImage newImage = new BufferedImage(getSize(), getSize(),
                                                   image.getType());
        image.copyData(newImage.getRaster());
        Particle result = new Particle(newImage);
        return result;
    }

    /**
     * Writes this particle to a stream (for serialization)
     * 
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // write buff with imageIO to out
        ImageIO.write(image, "png", out);
    }

    /**
     * Reads this particle from a stream (for deserialization)
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException,
                                                 ClassNotFoundException {
        in.defaultReadObject();
        // read buff with imageIO from in
        image = ImageIO.read(in);
    }

    /**
     * Returns a new particle that is a smaller area of this particle
     * 
     * @param x
     * @param y
     * @param size
     * @return new subparticle
     */
    public Particle subParticle(int x, int y, int size) {
        return new Particle(image.getSubimage(x, y, size, size));
    }

    /**
     * Provides a buffered image representation of this particle (for drawing
     * etc)
     * 
     * @return a buffered image
     */
    public BufferedImage asBufferedImage() {
        return image;
    }

    /**
     * Writes this particle to a PNG file
     * 
     * @param filename
     * @throws IOException
     */
    public void toFile(String filename) throws IOException {
        ImageIO.write(image, "png", new File(filename));
    }

    /**
     * Creates a new particle from an image file
     * 
     * @param filename
     * @return new particle from image file
     * @throws IOException
     */
    public static Particle fromFile(String filename) throws IOException {
        BufferedImage particleImage = ImageIO.read(new File(filename));
        return new Particle(particleImage);
    }

    // Primitive operations
    // -----------------------------------------------------------------
    //
    // All operations on a particle generate a new particle rather than
    // operating in place

    /**
     * Transforms a particle given a transformation matrix (affine transform)
     * 
     * @param target
     *            particle
     * @param matrix
     *            (2x2 or 3x3)
     * @return new, transformed particle
     */
    public static Particle transform(Particle target, float[][] matrix) {
        float[] flatMatrix = MatrixUtils.flatten(matrix);

        AffineTransform transform = new AffineTransform(flatMatrix);
        return transform(target, transform);
    }

    /**
     * Transforms a particle given an affine transform
     * 
     * @param target
     *            particle
     * @param xform
     *            , an affine transform
     * @return a new, transformed particle
     */
    public static Particle transform(Particle target, AffineTransform xform) {
        // Build an AffineTransformOp
        AffineTransformOp operation = new AffineTransformOp(
                                                            xform,
                                                            AffineTransformOp.TYPE_BICUBIC);
        Particle result = applyOperation(target, operation);
        result.pushTransform(xform);
        return result;
    }

    /**
     * Convolves a particle with a matrix
     * 
     * @param target
     *            particle
     * @param kernel
     *            , the matrix to be convolved with the particle
     * @return a new, convolved particle
     */
    public static Particle convolve(Particle target, float[][] kernel) {
        int kernelHeight = kernel.length;
        int kernelWidth = kernel[0].length;

        float[] basisKernel = MatrixUtils.flatten(kernel);

        // Build a Kernel
        Kernel newKernel = new Kernel(kernelWidth, kernelHeight, basisKernel);
        return convolve(target, newKernel);
    }

    /**
     * Convolves a kernel with a particle
     * 
     * @param target
     *            particle
     * @param kernel
     *            , to be convolved with the particle
     * @return a new, convolved particle
     */
    public static Particle convolve(Particle target, Kernel kernel) {
        // Build a ConvolveOp
        ConvolveOp operation = new ConvolveOp(kernel);

        return applyOperation(target, operation);
    }

    /**
     * Scales the values of a particle (scalar multiplication)
     * 
     * @param target
     *            particle
     * @param scaleFactor
     *            to be multiplied at each pixel
     * @return new, scaled particle
     */
    public static Particle scale(Particle target, float scaleFactor) {
        RescaleOp operation = new RescaleOp(scaleFactor, 0, null);
        return applyOperation(target, operation);
    }

    /**
     * Adds a scalar value to each pixel
     * 
     * @param target
     *            particle
     * @param offset
     *            to be added to each pixel
     * @return a new particle with each pixel increased by the scalar
     */
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
