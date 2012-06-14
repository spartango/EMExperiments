package edu.harvard.mcb.leschziner.core;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import edu.harvard.mcb.leschziner.util.MatrixUtils;

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
    
    public int getSize() {
        return size;
    }

    // I/O methods
    
    public int getPixel(int x, int y) {
        // TODO caching layer
        return image.getRGB(x, y);
    }
    

    public int[] getRow(int y) {
        int[] row = new int[size]; 
        image.getRGB(0, y, size, 1, row, 0, size);
        return row;
    }
    
    public int[] getColumn(int x) {
        int[] column = new int[size]; 
        image.getRGB(x, 0, 1, size, column, 0, size);
        return column;
    }

    public int[][] getRegion(int x, int y, int width, int height) {
        int[] flat = new int[width * height];
        image.getRGB(x, y, width, height, flat, 0, size);
        return MatrixUtils.unflatten(flat, width, height);
    }
    
    public void setPixel(int x, int y, int value) {
        // TODO buffering layer
        image.setRGB(x, y, value);
    }

    // Object handling
    
    public Particle clone() {
        BufferedImage newImage = new BufferedImage(size, size, image.getType());
        image.copyData(newImage.getRaster());
        return new Particle(newImage);
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
        AffineTransformOp operation = new AffineTransformOp(xform, AffineTransformOp.TYPE_BICUBIC);
        
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
    
    private static Particle applyOperation(Particle target, BufferedImageOp operation) {
        // Create a destination
        BufferedImage dest = operation.createCompatibleDestImage(target.image,
                                                                 target.image.getColorModel());

        // Apply the op
        operation.filter(target.image, dest);
        return new Particle(dest); 
    }
}
