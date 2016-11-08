package main.classes;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import main.classes.Helpers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;

import static main.classes.Constants.*;
import static main.classes.Helpers.computeDynamicThreshold;
import static main.classes.Helpers.inMat;
import static main.classes.Helpers.matrixMagnitude;

public class FindEyeCentre {

    String imagesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\images\\";
    JFrame jFrame = new JFrame();
    JLabel lbl = new JLabel();

    FindEyeCentre() {
        jFrame.setLayout(new FlowLayout());
        jFrame.add(lbl);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /*
     * Function to convert Mat to Image for displaying it in a window.
     * @param m the source mat object
     */
    public Image toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    /*
     * Function to display image in a window.
     * @param img the source img object
     */
    public void displayImage(Image img) {
        ImageIcon icon = new ImageIcon(img);
        jFrame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
        lbl.setIcon(icon);
    }

    Point unscalePoint(Point point, Rect origSize) {
        float ratio = (((float) kFastEyeWidth) / origSize.width);
        int x = (int) Math.round(point.x / ratio);
        int y = (int) Math.round(point.y / ratio);
        return new Point(x, y);
    }

    void scaleToFastSize(Mat src, Mat dst) {
        Imgproc.resize(src, dst, new Size(kFastEyeWidth, (((float) kFastEyeWidth) / src.cols()) * src.rows()));
    }

    Mat computeMatXGradient(Mat mat) {
        Mat out = new Mat(mat.rows(), mat.cols(), CvType.CV_64F);

        for (int i = 0; i < mat.rows(); ++i) {
            double[] Mr = mat.get(i, 0);
            double[] Or = out.get(i, 0);
            Or[0] = Mr[1] - Mr[0];
            for (int j = 1; j < mat.cols() - 1; ++j) {
                Or[j] = (Mr[j + 1] - Mr[j - 1]) / 2.0;
            }
            Or[mat.cols() - 1] = Mr[mat.cols() - 1] - Mr[mat.cols() - 2];
        }
        return out;
    }

    void testPossibleCentersFormula(int x, int y, Mat weight, double gx, double gy, Mat out) {
        // for all possible centers
        for (int cy = 0; cy < out.rows(); ++cy) {
            double[] Or = out.get(cy, 0);
            double[] Wr = weight.get(cy, 0);
            for (int cx = 0; cx < out.cols(); ++cx) {
                if (x == cx && y == cy) {
                    continue;
                }
                // create a vector from the possible center to the gradient origin
                double dx = x - cx;
                double dy = y - cy;
                // normalize d
                double magnitude = Math.sqrt((dx * dx) + (dy * dy));
                dx = dx / magnitude;
                dy = dy / magnitude;
                double dotProduct = dx * gx + dy * gy;
                dotProduct = Math.max(0.0, dotProduct);
                // square and multiply by the weight
                if (kEnableWeight) {
                    Or[cx] += dotProduct * dotProduct * (Wr[cx] / kWeightDivisor);
                } else {
                    Or[cx] += dotProduct * dotProduct;
                }
            }
        }
    }

    Point findEyeCenter(Mat face, Rect eye, String debugWindow) {
        Mat eyeROIUnscaled = new Mat(face, eye);
        Mat eyeROI = new Mat();
        scaleToFastSize(eyeROIUnscaled, eyeROI);
        // draw eye region
        Imgproc.rectangle(face, eye.tl(), eye.br(), new Scalar(0, 255, 0));
        //-- Find the gradient
        Mat gradientX = computeMatXGradient(eyeROI);
        Mat gradientY = computeMatXGradient(eyeROI.t()).t();
        //-- Normalize and threshold the gradient
        // compute all the magnitudes
        Mat mags = matrixMagnitude(gradientX, gradientY);
        //compute the threshold
        double gradientThresh = computeDynamicThreshold(mags, kGradientThreshold);
        //double gradientThresh = kGradientThreshold;
        //double gradientThresh = 0;
        //normalize
        for (int i = 0; i < eyeROI.rows(); ++i) {
            double[] Xr = gradientX.get(i, 0);
            double[] Yr = gradientY.get(i, 0);
            double[] Mr = mags.get(i, 0);
            for (int j = 0; j < eyeROI.cols(); ++j) {
                double gX = Xr[j], gY = Yr[j];
                double magnitude = Mr[j];
                if (magnitude > gradientThresh) {
                    Xr[j] = gX / magnitude;
                    Yr[j] = gY / magnitude;
                } else {
                    Xr[j] = 0.0;
                    Yr[j] = 0.0;
                }
            }
        }

        displayImage(toBufferedImage(gradientX));

        //-- Create a blurred and inverted image for weighting
        Mat weight = new Mat();
        Imgproc.GaussianBlur(eyeROI, weight, new Size(kWeightBlurSize, kWeightBlurSize), 0, 0);
        for (int i = 0; i < weight.rows(); ++i) {
            double[] row = weight.get(i, 0);
            for (int j = 0; j < weight.cols(); ++j) {
                row[j] = (255 - row[j]);
            }
        }
        //displayImage(toBufferedImage(weight));;
        //-- Run the algorithm!
        Mat outSum = Mat.zeros(eyeROI.rows(), eyeROI.cols(), CvType.CV_64F);
        // for each possible gradient location
        // Note: these loops are reversed from the way the paper does them
        // it evaluates every possible center for each gradient location instead of
        // every possible gradient location for every center.
        System.out.println("Eye Size: " + outSum.cols() + "x" + outSum.rows() + "\n");
        for (int y = 0; y < weight.rows(); ++y) {
            double[] Xr = gradientX.get(y, 0);
            double[] Yr = gradientY.get(y, 0);
            for (int x = 0; x < weight.cols(); ++x) {
                double gX = Xr[x], gY = Yr[x];
                if (gX == 0.0 && gY == 0.0) {
                    continue;
                }
                testPossibleCentersFormula(x, y, weight, gX, gY, outSum);
            }
        }

        // scale all the values down, basically averaging them
        double numGradients = (weight.rows() * weight.cols());
        Mat out = new Mat();
        outSum.convertTo(out, CvType.CV_32F, 1.0 / numGradients);
        //displayImage(toBufferedImage(out));
        //-- Find the maximum point
        Point maxP = new Point();
        double maxVal;
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(out);
        //-- Flood fill the edges
        if (kEnablePostProcess) {
            Mat floodClone = new Mat();
            //double floodThresh = computeDynamicThreshold(out, 1.5);
            double floodThresh = minMaxLocResult.maxVal * kPostProcessThreshold;
            Imgproc.threshold(out, floodClone, floodThresh, 0.0f, Imgproc.THRESH_TOZERO);
            if (kPlotVectorField) {
                //plotVecField(gradientX, gradientY, floodClone);
                Imgcodecs.imwrite(imagesDirPath + "eyeFrame.png", eyeROIUnscaled);
            }
            Mat mask = floodKillEdges(floodClone);
            //displayImage(toBufferedImage(mask));
            //displayImage(toBufferedImage(out));
            // redo max
            minMaxLocResult = Core.minMaxLoc(out, mask);
        }
        return unscalePoint(maxP, eye);
    }

    boolean floodShouldPushPoint(Point np, Mat mat) {
        return inMat(np, mat.rows(), mat.cols());
    }

    // returns a mask
    Mat floodKillEdges(Mat mat) {
        Rect tempRect = new Rect(0, 0, mat.cols(), mat.rows());
        Imgproc.rectangle(mat, tempRect.tl(), tempRect.br(), new Scalar(0, 255, 0));

        Mat mask = new Mat(mat.rows(), mat.cols(), CvType.CV_8U);
        Queue<Point> toDo = new LinkedList<Point>();
        toDo.add(new Point(0, 0));
        while (!toDo.isEmpty()) {
            Point p = toDo.element();
            toDo.remove();
            if (mat.get((int) p.x, (int) p.y)[0] == 0.0f) {
                continue;
            }
            // add in every direction
            Point np = new Point(p.x + 1, p.y); // right
            if (floodShouldPushPoint(np, mat)) toDo.add(np);
            np.x = p.x - 1;
            np.y = p.y; // left
            if (floodShouldPushPoint(np, mat)) toDo.add(np);
            np.x = p.x;
            np.y = p.y + 1; // down
            if (floodShouldPushPoint(np, mat)) toDo.add(np);
            np.x = p.x;
            np.y = p.y - 1; // up
            if (floodShouldPushPoint(np, mat)) toDo.add(np);
            // kill it
            mat.put((int) p.x, (int) p.y, new double[]{0.0});
            mask.put((int) p.x, (int) p.y, new double[]{0});
        }
        return mask;
    }

}
