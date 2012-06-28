package edu.harvard.mcb.leschziner.analyze;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.ColorUtils;

public class BlobExtractor implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8979444781712887357L;

    private static final int  UNLABELED        = 0;

    // Kernel Sectors
    private static final int  SECTOR_A         = 0;
    private static final int  SECTOR_B         = 1;
    private static final int  SECTOR_C         = 2;
    private static final int  SECTOR_X         = 3;
    private static final int  SECTOR_D         = 4;

    // The expected size of the particle we're finding in pixels
    private final int         targetSize;
    // Amount of variability allowed in the particles chosen (+/- epsillon
    // pixels)
    private final int         epsillon;

    public BlobExtractor(int size, int epsillon) {
        this.targetSize = size;
        this.epsillon = epsillon;
    }

    // 2-Pass Connected components algorithm
    public Rectangle[] extract(Particle target) {
        int size = target.getSize();

        // This will hold labels as they're generated
        int[][] labelBuffer = new int[size][size];

        int currentLabel = 1; // Labeling starts at 1.

        // Pass 1: Region labeling
        System.out.println("[BlobExtractor]: Starting Blob Extraction Pass 1");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int xPixel = target.getPixel(x, y);
                // System.out.print(xPixel + " ");

                // Pixel is in foreground
                if (xPixel != ColorUtils.BLACK) {
                    int[] labelKernel;

                    labelKernel = new int[6];
                    labelKernel[SECTOR_A] = (x > 0 && y > 0 ? labelBuffer[y - 1][x - 1]
                                                           : UNLABELED);
                    labelKernel[SECTOR_B] = (y > 0 ? labelBuffer[y - 1][x]

                    : UNLABELED);
                    labelKernel[SECTOR_C] = (x < size - 1 && y > 0 ? labelBuffer[y - 1][x + 1]

                                                                  : UNLABELED);
                    labelKernel[SECTOR_D] = (x > 0 ? labelBuffer[y][x - 1]
                                                  : UNLABELED);

                    if (labelKernel[SECTOR_A] == UNLABELED
                        && labelKernel[SECTOR_B] == UNLABELED
                        && labelKernel[SECTOR_C] == UNLABELED
                        && labelKernel[SECTOR_D] == UNLABELED) {
                        // Assign a new label
                        labelBuffer[y][x] = currentLabel;
                        currentLabel++;
                    } else {

                        // Find the lowest label > 0
                        // Assign all sectors that value
                        int minLabel = currentLabel;
                        for (int i = SECTOR_A; i <= SECTOR_D; i++) {
                            if (labelKernel[i] > UNLABELED
                                && labelKernel[i] < minLabel) {
                                minLabel = labelKernel[i];
                            }
                        }

                        // Label the target
                        labelBuffer[y][x] = minLabel;

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
                }
            }

        }

        // Pass 2: Region aggregation & Bounds
        System.out.println("[BlobExtractor]: Starting Blob Extraction Pass 2, finding "
                           + currentLabel + " labels");
        // Preallocate a vector to hold our rectangles
        Rectangle[] rects = new Rectangle[currentLabel - 1];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int labelPixel = labelBuffer[y][x];
                if (labelPixel != UNLABELED) {
                    if (rects[labelPixel - 1] == null) {
                        // Create a new bounding box if none exists
                        rects[labelPixel - 1] = new Rectangle(x, y, 1, 1);
                    } else if (!rects[labelPixel - 1].contains(x, y)) {
                        // Adjust the bounding box to include this pixel
                        rects[labelPixel - 1].add(x, y);
                    }
                }
            }

        }
        // Filtering by size
        System.out.println("[BlobExtractor]: Filtering Blobs from "
                           + rects.length + " Candidate regions");
        Vector<Rectangle> filteredBounds = new Vector<Rectangle>();
        for (Rectangle rect : rects) {
            // Make the rectangle a square, as we use square boxes
            if (rect != null && rect.getWidth() != rect.getHeight()) {
                int maxBound = (int) Math.max(rect.getWidth(), rect.getHeight());
                rect.setSize(maxBound, maxBound);

            }
            // Check the side-length against the target side length +/-
            // epsillon
            // Also excludes particles extending beyond the frame {TODO}
            if (rect != null
                && Math.abs(rect.getWidth() - targetSize) <= epsillon) {
                filteredBounds.add(rect);
            }
        }

        return filteredBounds.toArray(new Rectangle[filteredBounds.size()]);
    }
}
