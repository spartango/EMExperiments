package edu.harvard.mcb.leschziner.particlesource.extract;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.util.ColorUtils;

public class BlobExtractor {
    private static final int UNLABELED = 0;

    // Kernel Sectors
    private static final int SECTOR_A  = 0;
    private static final int SECTOR_B  = 1;
    private static final int SECTOR_C  = 2;
    private static final int SECTOR_X  = 3;
    private static final int SECTOR_D  = 4;

    // The expected size of the particle we're finding in pixels
    private int              targetSize;
    // Amount of variability allowed in the particles chosen (+/- epsillon pixels)
    private int              epsillon;

    public BlobExtractor(int size, int epsillon) {
        this.targetSize = size;
        this.epsillon = epsillon;
    }

    // 2-Pass Connected components algorithm
    public Rectangle[] extract(Particle target) {
        int size = target.getSize();

        // This will hold labels as they're generated
        Particle labeled = new Particle(
                                        new BufferedImage(
                                                          size,
                                                          size,
                                                          BufferedImage.TYPE_INT_RGB));

        int currentLabel = 1; // Labeling starts at 1.

        // Pass 1: Region labeling
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int xPixel = target.getPixel(x, y);

                // Pixel is in foreground
                if (xPixel != ColorUtils.BLACK) {
                    int[] labelKernel;
                    // If we aren't at a border
                    if (x > 0 && x < size - 1 && y > 0 && y < size - 1) {
                        // Pull out the 8-connectivity kernel region
                        labelKernel = labeled.getRegionBuffer(x, y, 3, 2);
                    } else {
                        // Edge region case
                        labelKernel = new int[6];
                        labelKernel[SECTOR_A] = (x > 0 && y > 0 ? labeled.getPixel(x - 1,
                                                                                   y - 1)
                                                               : UNLABELED);
                        labelKernel[SECTOR_B] = (y > 0 ? labeled.getPixel(x,
                                                                          y - 1)
                                                      : UNLABELED);
                        labelKernel[SECTOR_C] = (x < size - 1 && y > 0 ? labeled.getPixel(x + 1,
                                                                                          y - 1)
                                                                      : UNLABELED);
                        labelKernel[SECTOR_D] = (x > 0 ? labeled.getPixel(x - 1,
                                                                          y)
                                                      : UNLABELED);

                    }

                    if (labelKernel[SECTOR_A] == UNLABELED
                        && labelKernel[SECTOR_B] == UNLABELED
                        && labelKernel[SECTOR_C] == UNLABELED
                        && labelKernel[SECTOR_D] == UNLABELED) {
                        // Assign a new label
                        labeled.setPixel(x, y, currentLabel);
                        currentLabel++;
                    } else {
                        // Find the lowest label > 0
                        // Assign all sectors that value
                        int minLabel = currentLabel;
                        for (int i = SECTOR_A; i < SECTOR_X; i++) {
                            if (labelKernel[i] != UNLABELED
                                && labelKernel[i] < minLabel) {
                                minLabel = labelKernel[i];
                            }
                        }

                        // Label the target
                        labeled.setPixel(x, y, minLabel);

                        if (labelKernel[SECTOR_A] != UNLABELED) {
                            labeled.setPixel(x - 1, y - 1, minLabel);
                        }
                        if (labelKernel[SECTOR_B] != UNLABELED) {
                            labeled.setPixel(x, y - 1, minLabel);

                        }
                        if (labelKernel[SECTOR_C] != UNLABELED) {
                            labeled.setPixel(x + 1, y - 1, minLabel);

                        }
                        if (labelKernel[SECTOR_D] != UNLABELED) {
                            labeled.setPixel(x - 1, y, minLabel);
                        }
                    }
                }
            }
        }

        // Pass 2: Region aggregation & Bounds

        // Preallocate a vector to hold our rectangles
        Rectangle[] rects = new Rectangle[currentLabel - 1];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int labelPixel = labeled.getPixel(x, y);
                if (labelPixel != UNLABELED) {
                    if (rects[labelPixel] == null) {
                        // Create a new bounding box if none exists
                        rects[labelPixel] = new Rectangle(x, y, 1, 1);
                    } else if (!rects[labelPixel].contains(x, y)) {
                        // Adjust the bounding box to include this pixel
                        rects[labelPixel].add(x, y);
                    }
                }
            }
        }
        // Filtering by size
        Vector<Rectangle> filteredBounds = new Vector<Rectangle>();
        for (Rectangle rect : rects) {
            // Make the rectangle a square, as we use square boxes
            if (rect.getWidth() != rect.getHeight()) {
                int maxBound = (int) Math.max(rect.getWidth(), rect.getHeight());
                rect.setSize(maxBound, maxBound);
            }

            // Check the side-length against the target side length +/- epsillon
            if (Math.abs(rect.getWidth() - targetSize) <= epsillon) {
                filteredBounds.add(rect);
            }
        }

        return (Rectangle[]) filteredBounds.toArray();
    }
}
