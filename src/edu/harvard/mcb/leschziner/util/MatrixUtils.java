package edu.harvard.mcb.leschziner.util;

public class MatrixUtils {
    public static float[] flatten(float[][] matrix) {
        int matrixHeight = matrix.length;
        int matrixWidth = matrix[0].length;

        float[] flattened = new float[matrixWidth * matrixHeight];
        // Copy over the kernel contents
        for (int i = 0; i < matrixHeight; i++) {
            System.arraycopy(matrix[i], 0, flattened, i * matrixWidth,
                             matrixWidth);
        }
        return flattened;
    }
    
    public static int[] flatten(int[][] matrix) {
        int matrixHeight = matrix.length;
        int matrixWidth = matrix[0].length;

        int[] flattened = new int[matrixWidth * matrixHeight];
        // Copy over the kernel contents
        for (int i = 0; i < matrixHeight; i++) {
            System.arraycopy(matrix[i], 0, flattened, i * matrixWidth,
                             matrixWidth);
        }
        return flattened;
    }

    public static float[][] unflatten(float[] matrix, int width, int height) {
        float[][] unflattened = new float[height][width];
        for(int i = 0; i < height; i++) {
            System.arraycopy(matrix, i*width, unflattened[i], 0, width);
        }
        return unflattened;
    }
    
    public static int[][] unflatten(int[] matrix, int width, int height) {
        int[][] unflattened = new int[height][width];
        for(int i = 0; i < height; i++) {
            System.arraycopy(matrix, i*width, unflattened[i], 0, width);
        }
        return unflattened;
    }
    
    public static int sum(int[] array) {
        int sum = 0;
        for(int i = 0; i<array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

}
