package main.classes;

import org.opencv.core.*;

public class Helpers {
    public static boolean rectInImage(Rect rect, Mat image) {
        return rect.x > 0 && rect.y > 0 && rect.x + rect.width < image.cols() && rect.y + rect.height < image.rows();
    }

    public static boolean inMat(Point point, int rows, int cols) {
        return point.x >= 0 && point.x < cols && point.y >= 0 && point.y < rows;
    }

    public static Mat matrixMagnitude(Mat matX, Mat matY) {
        Mat mags = new Mat(matX.rows(), matX.cols(), CvType.CV_64F);
        for (int i = 0; i < matX.rows(); ++i) {
            double[] Xr = matX.get(i, 0);
            double[] Yr = matY.get(i, 0);
            double[] Mr = mags.get(i, 0);
            for (int j = 0; j < matX.cols(); ++j) {
                double gX = Xr[j];
                double gY = Yr[j];
                double magnitude = Math.sqrt((gX * gX) + (gY * gY));
                Mr[j] = magnitude;
            }
        }
        return mags;
    }

    public static double computeDynamicThreshold(Mat mat, double stdDevFactor) {
        MatOfDouble stdMagnGrad = new MatOfDouble();
        MatOfDouble meanMagnGrad = new MatOfDouble();
        Core.meanStdDev(mat, meanMagnGrad, stdMagnGrad);
        double stdDev = stdMagnGrad.get(0, 0)[0] / Math.sqrt(mat.rows() * mat.cols());
        return stdDevFactor * stdDev + meanMagnGrad.get(0, 0)[0];
    }
}
