package edu.harvard.mcb.leschziner.util;

import java.awt.Color;

public class ColorUtils {
    public static final int WHITE = Color.WHITE.getRGB();
    public static final int BLACK = Color.BLACK.getRGB();

    public static int extractRed(int target) {
        // Protected against ARGB data
        return (target >> 16) & 0xFF;
    }

    public static int extractGreen(int target) {
        return ((target & 0x00FF00) >> 8);
    }

    public static int extractBlue(int target) {
        return target & 0x0000FF;
    }

    public static int[] extractRed(int[] target) {
        int[] extracted = new int[target.length];
        for (int i = 0; i < target.length; i++) {
            extracted[i] = extractRed(target[i]);
        }
        return extracted;
    }

    public static int[] extractBlue(int[] target) {
        int[] extracted = new int[target.length];
        for (int i = 0; i < target.length; i++) {
            extracted[i] = extractBlue(target[i]);
        }
        return extracted;
    }

    public static int[] extractGreen(int[] target) {
        int[] extracted = new int[target.length];
        for (int i = 0; i < target.length; i++) {
            extracted[i] = extractGreen(target[i]);
        }
        return extracted;
    }

}
