package edu.harvard.mcb.leschziner.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Stack;

import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;
import com.googlecode.javacv.cpp.opencv_imgproc;

import edu.harvard.mcb.leschziner.util.ColorUtils;
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
    private static final long      serialVersionUID = 8805574980503468420L;

    // Channel offsets
    private static final int       GREEN            = 1;
    private static final int       BLUE             = 2;
    private static final int       RED              = 0;

    // Image can't be serialized, will be transferred manually
    private transient IplImage     image;

    // Stack of reversible transformation operations performed on this particle
    private Stack<AffineTransform> transforms;

    /**
     * Builds a particle from a square image
     * 
     * @param image
     */
    public Particle(BufferedImage image) {
        this.image = IplImage.createFrom(image);
        transforms = new Stack<AffineTransform>();
    }

    /**
     * Builds a particle from a square image
     * 
     * @param image
     */
    public Particle(IplImage image) {
        this.image = image;
        transforms = new Stack<AffineTransform>();
    }

    /**
     * Gets the size of the particle's box
     * 
     * @return particle box size in pixels
     */
    public int getSize() {
        return image.width();
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
        return (getPixelRed(x, y) << 16) & (getPixelGreen(x, y) << 8)
               & getPixelBlue(x, y);
    }

    public int getPixelRed(int x, int y) {
        return getPixelChannel(x, y, RED);
    }

    public int getPixelBlue(int x, int y) {
        return getPixelChannel(x, y, BLUE);
    }

    public int getPixelGreen(int x, int y) {
        return getPixelChannel(x, y, GREEN);
    }

    public int getPixelChannel(int x, int y, int channel) {
        // Find row, go to channel byte, compensate for unsigned value
        return image.getByteBuffer().get(y * image.widthStep()
                                                 + image.nChannels() * x
                                                 + channel) & 0xFF;
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
    public ByteBuffer getPixelBuffer() {
        return image.asByteBuffer();
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
        setPixelRed(x, y, ColorUtils.extractRed(value));
        setPixelGreen(x, y, ColorUtils.extractGreen(value));
        setPixelBlue(x, y, ColorUtils.extractBlue(value));
    }

    public void setPixelRed(int x, int y, int value) {
        setPixelChannel(x, y, RED, value);
    }

    public void setPixelGreen(int x, int y, int value) {
        setPixelChannel(x, y, GREEN, value);
    }

    public void setPixelBlue(int x, int y, int value) {
        setPixelChannel(x, y, BLUE, value);
    }

    public void setPixelChannel(int x, int y, int channel, int value) {
        image.getByteBuffer().put(y * image.widthStep() + image.nChannels() * x
                                          + channel, (byte) (value));
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
        Particle result = new Particle(image.clone());
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
        ImageIO.write(asBufferedImage(), "png", out);
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
        image = IplImage.createFrom(ImageIO.read(in));
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
        return new Particle(image.getBufferedImage().getSubimage(x, y, size,
                                                                 size));
    }

    /**
     * Provides a buffered image representation of this particle (for drawing
     * etc)
     * 
     * @return a buffered image
     */
    public BufferedImage asBufferedImage() {
        return image.getBufferedImage();
    }

    public IplImage getImage() {
        return image;
    }

    /**
     * Writes this particle to a PNG file
     * 
     * @param filename
     * @throws IOException
     */
    public void toFile(String filename) throws IOException {
        opencv_highgui.cvSaveImage(filename, image);
    }

    /**
     * Creates a new particle from an image file
     * 
     * @param filename
     * @return new particle from image file
     * @throws IOException
     */
    public static Particle fromFile(String filename) throws IOException {
        return new Particle(opencv_highgui.cvLoadImage(filename));
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
    public static Particle transform(Particle target, double[][] matrix) {
        // Reformat the matrix
        double[] kernel = MatrixUtils.flatten(matrix);
        // TODO validate
        CvMat kernelMat = CvMat.create(matrix.length, matrix[0].length);
        kernelMat.put(0, kernel, 0, kernel.length);

        // Apply the transformation
        return transform(target, kernelMat);
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
        // Grab data from the transform
        double[] matrix = new double[6];
        // stored as { m00 m10 m01 m11 m02 m12 }
        xform.getMatrix(matrix);

        // Build an appropriate CvMat
        CvMat kernelMat = CvMat.create(2, 3);
        kernelMat.put(0, 0, matrix[0]);
        kernelMat.put(1, 0, matrix[1]);
        kernelMat.put(0, 1, matrix[2]);
        kernelMat.put(1, 1, matrix[3]);
        kernelMat.put(0, 2, matrix[4]);
        kernelMat.put(1, 2, matrix[5]);

        Particle result = transform(target, kernelMat);
        result.pushTransform(xform);
        return result;
    }

    public static Particle transform(Particle target, CvMat kernel) {
        IplImage dst = IplImage.createCompatible(target.image);

        // Apply the transform
        opencv_imgproc.cvWarpAffine(target.image, dst, kernel);
        return new Particle(dst);
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

        double[] basisKernel = MatrixUtils.upConvertArray(MatrixUtils.flatten(kernel));

        // Build a Kernel
        CvMat kernelMat = CvMat.create(kernelHeight, kernelWidth);
        kernelMat.put(0, basisKernel, 0, basisKernel.length);

        return convolve(target, kernelMat);
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
        double[] kernelData = MatrixUtils.upConvertArray(kernel.getKernelData(null));

        // Build a CvMat
        CvMat kernelMat = CvMat.create(kernel.getHeight(), kernel.getWidth());

        // Copy in kernel data
        kernelMat.put(0, kernelData, 0, kernelData.length);
        return convolve(target, kernelMat);
    }

    public static Particle convolve(Particle target, CvMat kernel) {
        IplImage dst = IplImage.createCompatible(target.image);
        opencv_imgproc.cvFilter2D(target.image, dst, kernel,
                                  new CvPoint(-1, -1));
        return new Particle(dst);
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
        IplImage dst = IplImage.createCompatible(target.image);
        opencv_core.cvScale(target.image, dst, scaleFactor, 0);
        return new Particle(dst);
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
        IplImage dst = IplImage.createCompatible(target.image);
        opencv_core.cvScale(target.image, dst, 1.0, offset);
        return new Particle(dst);
    }

}
