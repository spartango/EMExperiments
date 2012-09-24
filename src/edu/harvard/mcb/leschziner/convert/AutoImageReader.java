package edu.harvard.mcb.leschziner.convert;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;

public class AutoImageReader {

    public static Collection<BufferedImage> readStack(String filename) {
        Vector<BufferedImage> images = null;
        try (ImageReader reader = new ImageReader();
             BufferedImageReader imageReader = new BufferedImageReader(reader)) {
            reader.setId(filename);

            images = new Vector<>(reader.getImageCount());

            for (int i = 0; i < reader.getImageCount(); i++) {
                images.add(imageReader.openImage(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return images;

    }

    public static BufferedImage readImage(String filename) {
        BufferedImage image = null;
        try (ImageReader reader = new ImageReader();
             BufferedImageReader imageReader = new BufferedImageReader(reader)) {
            reader.setId(filename);

            if (imageReader.getImageCount() > 0)
                image = imageReader.openImage(0);

        } catch (FormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

}
