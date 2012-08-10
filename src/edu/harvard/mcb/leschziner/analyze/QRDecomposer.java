package edu.harvard.mcb.leschziner.analyze;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class QRDecomposer {
    public static QRFactors decompose(CvMat target) {
        /*
         * CvMat QR = target.clone();
         * 
         * int targetRows = target.rows(); int targetCols = target.cols(); int N
         * = Math.min(targetCols, targetRows); double[] gammas = new
         * double[targetCols];
         * 
         * for (int i = 0; i < N; i++) { CvMat A_small = CvMat.create(targetRows
         * - i, targetCols - i, opencv_core.CV_32FC1); CvMat A_mod =
         * CvMat.create(A_small.rows(), A_small.cols(), opencv_core.CV_32FC1);
         * CvMat v = CvMat.create(A_small.rows(), 1, opencv_core.CV_32FC1);
         * CvMat Q_k = CvMat.create(v.rows(), v.rows(), opencv_core.CV_32FC1);
         * 
         * // use extract matrix to get the column that is to be zeroed
         * CommonOps.extract(QR, i, targetRows, i, i + 1, v, 0, 0);
         * 
         * double[] maxArray = new double[1]; opencv_core.cvMinMaxLoc(v, new
         * double[1], maxArray); double max = maxArray[0];
         * 
         * if (max > 0 && v.rows() > 1) { // normalize to reduce overflow issues
         * CommonOps.divide(max, v);
         * 
         * // compute the magnitude of the vector double tau =
         * opencv_core.cvNorm(v);
         * 
         * if (v.get(0) < 0) tau *= -1.0;
         * 
         * double u_0 = v.get(0) + tau; double gamma = u_0 / tau;
         * 
         * CommonOps.divide(u_0, v); v.put(0, 1.0); // extract the submatrix of
         * A which is being operated on CommonOps.extract(QR, i, targetRows, i,
         * targetCols, A_small, 0, 0);
         * 
         * // A = (I - &gamma;*u*u<sup>T</sup>)A CommonOps.setIdentity(Q_k);
         * CommonOps.multAddTransB(-gamma, v, v, Q_k); CommonOps.mult(Q_k,
         * A_small, A_mod);
         * 
         * // save the results CommonOps.insert(A_mod, QR, i, i);
         * CommonOps.insert(v, QR, i, i); QR.unsafe_set(i, i, -tau * max);
         * 
         * // save gamma for recomputing Q later on gammas[i] = gamma; } }
         * 
         * // Calculate Q CvMat Q = CommonOps.identity(targetRows); CvMat Q_k =
         * CvMat.create(targetRows, targetRows); CvMat u =
         * CvMat.create(targetRows, 1);
         * 
         * CvMat temp = CvMat.create(targetRows, targetRows);
         * 
         * // compute Q by first extracting the householder vectors from the //
         * columns of QR and then applying it to Q for (int j = N - 1; j >= 0;
         * j--) { if (j + 1 < N) u.set(j + 1, 0);
         * 
         * CommonOps.extract(QR, j, targetRows, j, j + 1, u, j, 0); u.set(j,
         * 1.0);
         * 
         * // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
         * CommonOps.setIdentity(Q_k); CommonOps.multAddTransB(-gammas[j], u, u,
         * Q_k); CommonOps.mult(Q_k, Q, temp); Q.set(temp); }
         * 
         * // Calculate R CvMat R = CvMat.create(targetRows, targetCols);
         * 
         * for (int i = 0; i < N; i++) { for (int j = i; j < targetCols; j++) {
         * R.unsafe_set(i, j, QR.unsafe_get(i, j)); } }
         */
        return null;
    }
}
