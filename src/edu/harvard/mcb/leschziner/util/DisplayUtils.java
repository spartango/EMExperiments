package edu.harvard.mcb.leschziner.util;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.harvard.mcb.leschziner.core.Particle;

public class DisplayUtils {

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
