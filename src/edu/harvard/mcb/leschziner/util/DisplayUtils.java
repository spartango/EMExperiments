package edu.harvard.mcb.leschziner.util;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DisplayUtils {
    
    public static void displayImage(BufferedImage target) {
        JFrame canvas = new JFrame("Image");
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        JLabel icon = new JLabel(new ImageIcon(target));
        panel.add(icon);
        canvas.add(panel);
        canvas.pack();
        canvas.setVisible(true);
    }
    
}
