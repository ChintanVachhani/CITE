package main.classes;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import static main.classes.Constants.*;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    String cascadesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\cascades\\";
    String imagesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\images\\";
    JFrame jFrame = new JFrame();
    JLabel lbl = new JLabel();

    CascadeClassifier faceCascade = new CascadeClassifier();
    Mat debugImage = new Mat();
    //Mat skinCrCbHist = Mat.zeros(new Size(256, 256), CvType.CV_8UC1);


    Main() {
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

    int main() {
        Mat frame = new Mat();

        if (!faceCascade.load(cascadesDirPath + "haarcascade_frontalface_alt2.xml")) {
            System.out.println("Error loading face cascade, please change face_cascade_name in source code.");
            return -1;
        }

        //createCornerKernels();

        //Imgproc.ellipse(skinCrCbHist, new Point(133, 155.6), new Size(23.4, 15.2), 43.0, 0.0, 360.0, new Scalar(255, 255, 255), -1);

        VideoCapture capture = new VideoCapture(0);
        if (capture.isOpened()) {
            while (true) {
                capture.read(frame);

                Core.flip(frame, frame, 1);
                frame.copyTo(debugImage);

                if (!frame.empty()) {
                    detectAndDisplay(frame);
                } else {
                    System.out.println("No captured frame -- Break!");
                    break;
                }
                displayImage(toBufferedImage(debugImage));

            /*int c = waitKey(10);
            if( (char)c == 'c' ) { break; }
            if( (char)c == 'f' ) {
                imwrite("frame.png",frame);
            }*/
            }
        }

        //releaseCornerKernels();

        return 0;
    }

    void findEyes(Mat frameGray, Rect face) {
        Mat faceROI = new Mat(frameGray, face);
        Mat debugFace = faceROI;

        if (kSmoothFaceImage) {
            double sigma = kSmoothFaceFactor * face.width;
            Imgproc.GaussianBlur(faceROI, faceROI, new Size(0, 0), sigma);
        }
        //-- Find eye regions and draw them
        int eye_region_width = (int) (face.width * (kEyePercentWidth / 100.0));
        int eye_region_height = (int) (face.width * (kEyePercentHeight / 100.0));
        int eye_region_top = (int) (face.height * (kEyePercentTop / 100.0));
        Rect leftEyeRegion = new Rect((int) (face.width * (kEyePercentSide / 100.0)),
                eye_region_top, eye_region_width, eye_region_height);
        Rect rightEyeRegion = new Rect((int) (face.width - eye_region_width - face.width * (kEyePercentSide / 100.0)),
                eye_region_top, eye_region_width, eye_region_height);

        //-- Find Eye Centers
        FindEyeCentre f = new FindEyeCentre();
        Point leftPupil = f.findEyeCenter(faceROI, leftEyeRegion, "Left Eye");
        Point rightPupil = f.findEyeCenter(faceROI, rightEyeRegion, "Right Eye");
        // get corner regions
        Rect leftRightCornerRegion = leftEyeRegion;
        leftRightCornerRegion.width -= leftPupil.x;
        leftRightCornerRegion.x += leftPupil.x;
        leftRightCornerRegion.height /= 2;
        leftRightCornerRegion.y += leftRightCornerRegion.height / 2;
        Rect leftLeftCornerRegion = leftEyeRegion;
        leftLeftCornerRegion.width = (int) leftPupil.x;
        leftLeftCornerRegion.height /= 2;
        leftLeftCornerRegion.y += leftLeftCornerRegion.height / 2;
        Rect rightLeftCornerRegion = rightEyeRegion;
        rightLeftCornerRegion.width = (int) rightPupil.x;
        rightLeftCornerRegion.height /= 2;
        rightLeftCornerRegion.y += rightLeftCornerRegion.height / 2;
        Rect rightRightCornerRegion = rightEyeRegion;
        rightRightCornerRegion.width -= rightPupil.x;
        rightRightCornerRegion.x += rightPupil.x;
        rightRightCornerRegion.height /= 2;
        rightRightCornerRegion.y += rightRightCornerRegion.height / 2;
        Imgproc.rectangle(debugFace, leftRightCornerRegion.tl(), leftRightCornerRegion.br(), new Scalar(0, 200, 0));
        Imgproc.rectangle(debugFace, leftLeftCornerRegion.tl(), leftLeftCornerRegion.br(), new Scalar(0, 200, 0));
        Imgproc.rectangle(debugFace, rightLeftCornerRegion.tl(), rightLeftCornerRegion.br(), new Scalar(0, 200, 0));
        Imgproc.rectangle(debugFace, rightRightCornerRegion.tl(), rightRightCornerRegion.br(), new Scalar(0, 200, 0));
        // change eye centers to face coordinates
        rightPupil.x += rightEyeRegion.x;
        rightPupil.y += rightEyeRegion.y;
        leftPupil.x += leftEyeRegion.x;
        leftPupil.y += leftEyeRegion.y;
        // draw eye centers

        Imgproc.circle(debugFace, rightPupil, 3, new Scalar(0, 1234, 0));
        Imgproc.circle(debugFace, leftPupil, 3, new Scalar(0, 1234, 0));

        //-- Find Eye Corners
        /*if (kEnableEyeCorner) {
            Point leftRightCorner = findEyeCorner(faceROI(leftRightCornerRegion), true, false);
            leftRightCorner.x += leftRightCornerRegion.x;
            leftRightCorner.y += leftRightCornerRegion.y;
            Point leftLeftCorner = findEyeCorner(faceROI(leftLeftCornerRegion), true, true);
            leftLeftCorner.x += leftLeftCornerRegion.x;
            leftLeftCorner.y += leftLeftCornerRegion.y;
            Point rightLeftCorner = findEyeCorner(faceROI(rightLeftCornerRegion), false, true);
            rightLeftCorner.x += rightLeftCornerRegion.x;
            rightLeftCorner.y += rightLeftCornerRegion.y;
            Point rightRightCorner = findEyeCorner(faceROI(rightRightCornerRegion), false, false);
            rightRightCorner.x += rightRightCornerRegion.x;
            rightRightCorner.y += rightRightCornerRegion.y;
            Imgproc.circle(faceROI, leftRightCorner, 3, new Scalar(0, 200, 0));
            Imgproc.circle(faceROI, leftLeftCorner, 3, new Scalar(0, 200, 0));
            Imgproc.circle(faceROI, rightLeftCorner, 3, new Scalar(0, 200, 0));
            Imgproc.circle(faceROI, rightRightCorner, 3, new Scalar(0, 200, 0));
        }*/

        displayImage(toBufferedImage(faceROI));
        //  Rect roi( Point( 0, 0 ), faceROI.size());
        //  Mat destinationROI = debugImage( roi );
        //  faceROI.copyTo( destinationROI );
    }

    /*cv::Mat findSkin (cv::Mat &frame) {
        cv::Mat input;
        cv::Mat output = cv::Mat(frame.rows,frame.cols, CV_8U);

        cvtColor(frame, input, CV_BGR2YCrCb);

        for (int y = 0; y < input.rows; ++y) {
            const cv::Vec3b *Mr = input.ptr<cv::Vec3b>(y);
//    uchar *Or = output.ptr<uchar>(y);
            cv::Vec3b *Or = frame.ptr<cv::Vec3b>(y);
            for (int x = 0; x < input.cols; ++x) {
                cv::Vec3b ycrcb = Mr[x];
//      Or[x] = (skinCrCbHist.at<uchar>(ycrcb[1], ycrcb[2]) > 0) ? 255 : 0;
                if(skinCrCbHist.at<uchar>(ycrcb[1], ycrcb[2]) == 0) {
                    Or[x] = cv::Vec3b(0,0,0);
                }
            }
        }
        return output;
    }*/


    void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        //cv::Mat frame_gray;

        List<Mat> rgbChannels = new ArrayList<Mat>(3);
        Core.split(frame, rgbChannels);
        Mat frameGray = rgbChannels.get(2);

        //cvtColor( frame, frame_gray, CV_BGR2GRAY );
        //equalizeHist( frame_gray, frame_gray );
        //cv::pow(frame_gray, CV_64F, frame_gray);
        //-- Detect faces
        faceCascade.detectMultiScale(frameGray, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());
//  findSkin(debugImage);

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(debugImage, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 1234, 0));
        }
        //-- Show what you got
        if (facesArray.length > 0) {
            findEyes(frameGray, facesArray[0]);
        }
    }
}
