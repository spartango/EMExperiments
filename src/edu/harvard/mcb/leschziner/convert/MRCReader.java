package edu.harvard.mcb.leschziner.convert;

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileOpener;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MRCReader {
    // Borrowed from Albert Cardona
    public static final int HEADER_BYTES = 136;

    /**
     * Expects path as argument, or will ask for it and then open the image.
     * 
     * @throws IOException
     */
    public static BufferedImage read(String filename) throws IOException {
        InputStream is;
        byte[] buf = new byte[HEADER_BYTES];
        is = new FileInputStream(filename);
        is.read(buf, 0, HEADER_BYTES);
        is.close();

        int w = readIntLittleEndian(buf, 0);
        int h = readIntLittleEndian(buf, 4);
        int n = readIntLittleEndian(buf, 8);
        int dtype = getType(readIntLittleEndian(buf, 12));

        ImagePlus imp = openRaw(dtype, "", filename, w, h, 1024L, n, 0, true, // little-endian
                                false);
        return imp.getBufferedImage();

    }

    private static int getType(int datatype) {
        switch (datatype) {
        case 0:
            return FileInfo.GRAY8;
        case 1:
            return FileInfo.GRAY16_SIGNED;
        case 2:
            return FileInfo.GRAY32_FLOAT;
        case 6:
            return FileInfo.GRAY16_UNSIGNED;
        default:
            // else, error:
            return -1;
        }
    }

    private static int readIntLittleEndian(byte[] buf, int start) {
        return (buf[start]) + (buf[start + 1] << 8) + (buf[start + 2] << 12)
               + (buf[start + 3] << 24);
    }

    /**
     * Copied and modified from ij.io.ImportDialog. @param imageType must be a
     * static field from FileInfo class.
     */
    private static ImagePlus openRaw(int imageType, String directory,
                                     String fileName, int width, int height,
                                     long offset, int nImages,
                                     int gapBetweenImages,
                                     boolean intelByteOrder, boolean whiteIsZero) {
        FileInfo fi = new FileInfo();
        fi.fileType = imageType;
        fi.fileFormat = FileInfo.RAW;
        fi.fileName = fileName;
        fi.directory = directory;
        fi.width = width;
        fi.height = height;
        if (offset > 2147483647)
            fi.longOffset = offset;
        else
            fi.offset = (int) offset;
        fi.nImages = nImages;
        fi.gapBetweenImages = gapBetweenImages;
        fi.intelByteOrder = intelByteOrder;
        fi.whiteIsZero = whiteIsZero;
        FileOpener fo = new FileOpener(fi);

        return fo.open(false);

    }
}
