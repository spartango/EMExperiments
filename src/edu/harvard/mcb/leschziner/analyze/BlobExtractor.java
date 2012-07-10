package edu.harvard.mcb.leschziner.analyze;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;

import edu.harvard.mcb.leschziner.core.Particle;

/**
 * Extracts blobs from a thresholded (targets are white, everything else is
 * black) image. Utilizes connected components, and selects only blobs within
 * epsilon of the targeted size. Provides square regions surrounding each blob.
 * 
 * @author spartango
 * 
 */
public class BlobExtractor implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8979444781712887357L;

    // Value of an unlabeled pixel in the labeled blob regions
    private static final int  UNLABELED        = 0;

    // Kernel Sectors for an 8-connected kernel (Connected Components)
    private static final int  SECTOR_A         = 0;
    private static final int  SECTOR_B         = 1;
    private static final int  SECTOR_C         = 2;
    // private static final int SECTOR_X = 3;
    private static final int  SECTOR_D         = 4;

    private static final int  BLACK            = 0;

    // The expected size of the particle we're finding in pixels
    private final int         targetSize;
    // Amount of variability allowed in the particles chosen (+/- epsilon
    // pixels)
    private final int         epsilon;

    /**
     * Constructs a blob extractor
     * 
     * @param size
     *            : expected side length of blobs
     * @param epsilon
     *            : size variability in selected blobs, +/- pixels from size
     */
    public BlobExtractor(int size, int epsilon) {
        this.targetSize = size;
        this.epsilon = epsilon;
    }

    /**
     * Extracts blobs from the selected Particle using a 2-Pass Connected
     * components approach. Blobs are described by their bounding squares
     * 
     * @param target
     *            : particle from which blobs are to be extracted
     * @return Array of squares containing blobs
     */
    public Rectangle[] extract(Particle target) {
        int size = target.getSize();

        // Pass 1: labeling
        int[][] labelBuffer = new int[size][size];

        int labels = labelPass(target, labelBuffer, size);

        // Pass 2: Region aggregation & Bounds
        Rectangle[] rects = extractPass(labelBuffer, labels, size);

        // Filtering by size
        Vector<Rectangle> filteredBounds = selectionPass(rects);

        return filteredBounds.toArray(new Rectangle[filteredBounds.size()]);
    }

    public Rectangle[] extract(CvMat target) {
        int size = target.cols();

        // Pass 1: labeling
        int[][] labelBuffer = new int[size][size];

        int labels = labelPass(target, labelBuffer, size);

        // Pass 2: Region aggregation & Bounds
        Rectangle[] rects = extractPass(labelBuffer, labels, size);

        // Filtering by size
        Vector<Rectangle> filteredBounds = selectionPass(rects);

        return filteredBounds.toArray(new Rectangle[filteredBounds.size()]);
    }

    public static int labelPass(CvMat target, int[][] labelBuffer, int size) {

        // Labels are generated to identify each individual blob
        int currentLabel = 1; // Labeling starts at 1.

        // This will hold labels as they're generated

        // Pass 1: Region labeling
        int[] labelKernel = new int[6];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Get the pixel value from the particle
                double xPixel = target.get(x, y);

                // Pixel is in foreground (is a blob)
                if (xPixel != BLACK) {
                    currentLabel = labelFgPoint(labelBuffer, labelKernel,
                                                currentLabel, x, y, size);
                }
            }

        }
        return currentLabel;
    }

    private static int labelPass(Particle target, int[][] labelBuffer, int size) {

        // Labels are generated to identify each individual blob
        int currentLabel = 1; // Labeling starts at 1.

        // This will hold labels as they're generated

        // Pass 1: Region labeling
        int[] labelKernel = new int[6];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Get the pixel value from the particle
                int xPixel = target.getPixel(x, y);

                // Pixel is in foreground (is a blob)
                if (xPixel != BLACK) {
                    currentLabel = labelFgPoint(labelBuffer, labelKernel,
                                                currentLabel, x, y, size);
                }
            }

        }
        return currentLabel;
    }

    private static int labelFgPoint(int[][] labelBuffer, int[] labelKernel,
                                    int currentLabel, int x, int y, int size) {
        // Check for pre-existing labels around the target pixel, if
        // those pixels exist
        labelKernel[SECTOR_A] = (x > 0 && y > 0 ? labelBuffer[y - 1][x - 1]
                                               : UNLABELED);
        labelKernel[SECTOR_B] = (y > 0 ? labelBuffer[y - 1][x]

        : UNLABELED);
        labelKernel[SECTOR_C] = (x < size - 1 && y > 0 ? labelBuffer[y - 1][x + 1]

                                                      : UNLABELED);
        labelKernel[SECTOR_D] = (x > 0 ? labelBuffer[y][x - 1] : UNLABELED);

        // If none of the surrounding pixels are part of blobs
        if (labelKernel[SECTOR_A] == UNLABELED
            && labelKernel[SECTOR_B] == UNLABELED
            && labelKernel[SECTOR_C] == UNLABELED
            && labelKernel[SECTOR_D] == UNLABELED) {
            // Assign a new label
            labelBuffer[y][x] = currentLabel;
            currentLabel++;
        } else {
            // At least one of the surrounding pixels is already in
            // a labeled blob
            // Find the lowest label > 0
            int minLabel = currentLabel;
            for (int i = SECTOR_A; i <= SECTOR_D; i++) {
                if (labelKernel[i] > UNLABELED && labelKernel[i] < minLabel) {
                    minLabel = labelKernel[i];
                }
            }

            // Label the target
            labelBuffer[y][x] = minLabel;

            // Assign all sectors that value
            if (labelKernel[SECTOR_A] != UNLABELED) {
                labelBuffer[y - 1][x - 1] = minLabel;
            }
            if (labelKernel[SECTOR_B] != UNLABELED) {
                labelBuffer[y - 1][x] = minLabel;
            }
            if (labelKernel[SECTOR_C] != UNLABELED) {
                labelBuffer[y - 1][x + 1] = minLabel;
            }
            if (labelKernel[SECTOR_D] != UNLABELED) {
                labelBuffer[y][x - 1] = minLabel;
            }
        }
        return currentLabel;
    }

    private static Rectangle[] extractPass(int[][] labelBuffer,
                                           int currentLabel, int size) {
        // Preallocate a vector to hold our rectangles
        Rectangle[] rects = new Rectangle[currentLabel - 1];

        // Scan across the label buffer
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int labelPixel = labelBuffer[y][x];
                // Check if the pixel is labeled
                if (labelPixel != UNLABELED) {
                    if (rects[labelPixel - 1] == null) {
                        // Create a new bounding box for this label if none
                        // exists
                        rects[labelPixel - 1] = new Rectangle(x, y, 1, 1);
                    } else if (!rects[labelPixel - 1].contains(x, y)) {
                        // Adjust the bounding box of this label to include this
                        // pixel
                        rects[labelPixel - 1].add(x, y);
                    }
                }
            }

        }
        return rects;
    }

    private Vector<Rectangle> selectionPass(Rectangle[] rects) {
        Vector<Rectangle> filteredBounds = new Vector<Rectangle>();
        for (Rectangle rect : rects) {
            // Make the rectangle a square, as we use square boxes
            if (rect != null && rect.getWidth() != rect.getHeight()) {
                int maxBound = (int) Math.max(rect.getWidth(), rect.getHeight());
                rect.setSize(maxBound, maxBound);

            }
            // Check the side-length against the target side length +/-
            // epsilon
            // Also excludes particles extending beyond the frame
            if (rect != null
                && Math.abs(rect.getWidth() - targetSize) <= epsilon) {
                filteredBounds.add(rect);
            }
        }
        return filteredBounds;

    }

    public static CvRect cvRectFromRectangle(Rectangle target) {
        return new CvRect(target.x, target.y, target.width, target.height);
    }

    public static Rectangle RectangleFromCvRect(CvRect target) {
        return new Rectangle(target.x(), target.y(), target.width(),
                             target.height());
    }
}
