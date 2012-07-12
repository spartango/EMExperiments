package edu.harvard.mcb.leschziner.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.core.Particle;

public class DisplayUtils {
    // Where to place the next window on the screen
    private static int windowX = 0;
    private static int windowY = 0;

    public static void displayMat(CvMat target) {
        displayMat(target, "Mat " + target.hashCode());
    }

    public static void displayMat(CvMat target, String label) {
        IplImage image = IplImage.create(target.cols(), target.rows(), 8,
                                         target.channels());

        opencv_core.cvConvertScale(target, image, 25500, 0);
        displayImage(image.getBufferedImage(), label);
    }

    public static void displayParticle(Particle target) {
        displayParticle(target, "Particle " + target.hashCode());
    }

    public static void displayParticle(Particle target, String label) {
        displayImage(target.asBufferedImage(), label);
    }

    public static void displayImage(BufferedImage target) {
        displayImage(target, "Image " + target.hashCode());
    }

    public static void displayImage(BufferedImage target, String label) {
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame canvas = new JFrame(label);
        JPanel panel = new JPanel();
        JLabel icon = new JLabel(new ImageIcon(target));
        panel.add(icon);
        canvas.add(panel);
        canvas.pack();

        // Move the window to a nice place
        canvas.setLocation(windowX, windowY);
        windowX += canvas.getWidth();
        if (windowX > screenDimension.width) {
            windowX = 0;
            windowY += canvas.getHeight();
        }
        if (windowY > screenDimension.height) {
            windowY = 0;
        }

        canvas.setVisible(true);
    }
}
