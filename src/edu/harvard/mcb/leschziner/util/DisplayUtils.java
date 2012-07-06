package edu.harvard.mcb.leschziner.util;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.harvard.mcb.leschziner.core.Particle;

public class DisplayUtils {

    public static void displayMat(CvMat target) {
        final CanvasFrame canvas = new CanvasFrame("Mat " + target.hashCode(),
                                                   1);

        IplImage image = IplImage.create(target.cols(), target.rows(), 8,
                                         target.channels());

        opencv_core.cvConvertScale(target, image, 255, 0);
        canvas.showImage(image);
    }

    public static void displayParticle(Particle target) {
        displayImage(target.asBufferedImage());
    }

    public static void displayImage(BufferedImage target) {
        JFrame canvas = new JFrame("Image");
        JPanel panel = new JPanel();
        JLabel icon = new JLabel(new ImageIcon(target));
        panel.add(icon);
        canvas.add(panel);
        canvas.pack();
        canvas.setVisible(true);
    }

}
