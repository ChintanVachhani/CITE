package main.classes.v1;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;

public class EyeTracking {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    String cascadesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\cascades\\";
    String imagesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\images\\";
    CascadeClassifier faceCascade = new CascadeClassifier();
    CascadeClassifier eyeCascade = new CascadeClassifier();

    Mat frame = new Mat();
    Mat eyeTemplate = new Mat();
    Rect eyeBoundingBox = new Rect();

    JFrame jFrame = new JFrame();
    JLabel lbl = new JLabel();
    int index = 1;

    EyeTracking() {
        faceCascade.load(cascadesDirPath + "haarcascade_frontalface_alt2.xml");
        eyeCascade.load(cascadesDirPath + "haarcascade_eye.xml");
        jFrame.setLayout(new FlowLayout());
        jFrame.add(lbl);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    EyeTracking(int index) {
        this.index = index;
        faceCascade.load(cascadesDirPath + "haarcascade_frontalface_alt2.xml");
        eyeCascade.load(cascadesDirPath + "haarcascade_eye.xml");
        frame = Imgcodecs.imread(imagesDirPath + "testImage" + "1" + ".jpg", Imgcodecs.CV_LOAD_IMAGE_COLOR);
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
        //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
        ImageIcon icon = new ImageIcon(img);
        jFrame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
        lbl.setIcon(icon);
    }

    public int start() {

        // Open webcam
        /*VideoCapture videoCapture = new VideoCapture(0);

        // Check if everything is ok
        if (!videoCapture.isOpened()) {
            System.out.println("Error starting the Camera !");
            return 0;
        }
        if (faceCascade.empty()) {
            System.out.println("Error loading Face Cascade !");
            return 0;
        }

        // Set video to 320x240
        videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 800);
        videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 600);

*/
        //while (true) {
            /*videoCapture.read(frame);

            if (frame.empty()) {
                System.out.println("Error loading Frame !");
                break;
            }

            // Flip the frame horizontally, Windows users might need this
            Core.flip(frame, frame, 1)*/
        ;

        // Convert to grayscale and
        // adjust the image contrast using histogram equalization
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);


        //if (eyeBoundingBox.width == 0 && eyeBoundingBox.height == 0) {
        // Detection stage
        // Try to detect the face and the eye of the user
        System.out.println("Detecting Eye");

        // Detects faces and puts it into MatOfRect
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

        // Converts MatOfRect to Rect Array
        Rect[] facesArray = faces.toArray();

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0));
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "face" + ".jpg", frame);
            // Each face is cropped to Region of Interest (ROI)
            Mat faceROI = frame.submat(facesArray[i]);

            // Detects eyes and puts it into MatOfRect
            MatOfRect eyes = new MatOfRect();
            eyeCascade.detectMultiScale(faceROI, eyes, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());
            Rect[] eyesArray = eyes.toArray();

                /*if (eyesArray.length > 0) {
                    System.out.println("Eye Detected");
                    // Augment face coordinates to eye ROI
                        *//*eyesArray[0].x += facesArray[i].x;
                        eyesArray[0].y += facesArray[i].y;*//*

                    // Cropped eye ROI put into template
                    eyeBoundingBox = eyesArray[0];
                    eyeTemplate = faceROI.submat(eyeBoundingBox);
                }*/
            //for (int j = 0; j < eyesArray.length; j++) {
            Imgproc.rectangle(faceROI, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0, 0, 255));
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "faceROI" + ".jpg", faceROI);

            // Pupil Detection
            Mat eyesROI = faceROI.submat(eyesArray[0]);
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROI" + ".jpg", eyesROI);

            Mat grayEyesROI = eyesROI.clone();
            //Core.bitwise_not(eyesROI, grayEyesROI);
            Mat invertcolormatrix = new Mat(grayEyesROI.rows(), grayEyesROI.cols(), grayEyesROI.type(), new Scalar(255, 255, 255));
            Core.subtract(invertcolormatrix, grayEyesROI, grayEyesROI);
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIInverted" + ".jpg", grayEyesROI);
            Imgproc.cvtColor(grayEyesROI, grayEyesROI, Imgproc.COLOR_RGB2GRAY);
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIGrayscaled" + ".jpg", grayEyesROI);
            Imgproc.GaussianBlur(grayEyesROI, grayEyesROI, new Size(9, 9), 2, 2);
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIGaussianBlur" + ".jpg", grayEyesROI);
            //Imgproc.threshold(grayEyesROI, grayEyesROI, 183, 255, Imgproc.THRESH_BINARY);
            Core.inRange(grayEyesROI, new Scalar(0, 0, 0), new Scalar(179, 50, 150), grayEyesROI);
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIThreshold" + ".jpg", grayEyesROI);

            //Hough Circles

            Mat circles = new Mat();
           /* Imgproc.HoughCircles(grayEyesROI, circles, Imgproc.HOUGH_GRADIENT, 1, grayEyesROI.rows() / 8, 100, 300, 20, 400);
            if (circles.cols() > 0)
                for (int x = 0; x < circles.cols(); x++) {
                    double vCircle[] = circles.get(0, x);
                    System.out.println("Finding Circles");
                    if (vCircle == null)
                        break;

                    Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                    int radius = (int) Math.round(vCircle[2]);

                    // draw the found circle
                    Imgproc.circle(grayEyesROI, pt, radius, new Scalar(0, 255, 0), 10);
                    Imgproc.circle(grayEyesROI, pt, 3, new Scalar(0, 0, 255), 10);
                }
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIGrayscaledWithCircles" + ".jpg", eyesROI);

*/
            //Hough 2

            //System.out.println("1 Hough :" + circles.size());
            float circle[] = new float[3];
            for (int j = 0; j < circles.cols(); j++) {
                circles.get(0, j, circle);
                Point center = new Point();
                center.x = circle[0];
                center.y = circle[1];
                Imgproc.circle(grayEyesROI, center, (int) circle[2], new Scalar(255, 255, 100, 1), 4);
            }


            Imgproc.Canny(grayEyesROI, grayEyesROI, 200, 10, 3, false);

        /*Imgproc.HoughCircles( gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 100, 80, 10, 10, 50 );
        System.out.println("2 Hough:" +circles.size());

        for (int i = 0; i < circles.cols(); i++)
        {
            circles.get(i, 0, circle);
            org.opencv.core.Point center = new org.opencv.core.Point();
            center.x = circle[0];
            center.y = circle[1];
            Core.circle(gray, center, (int) circle[2], new Scalar(255,255,100,1), 4);
        }
        Imgproc.Canny( gray, gray, 200, 10, 3,false);
        */
            Imgproc.HoughCircles(grayEyesROI, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 100, 80, 10, 10, 50);
            //System.out.println("3 Hough" + circles.size());

            //float circle[] = new float[3];

            if(circles.cols() > 0){
                System.out.println("Eyes Open !");
            } else{
                System.out.println("Eyes Closed !");
            }

            for (int j = 0; j < circles.cols(); j++) {
                circles.get(j, 0, circle);
                //System.out.println("in hough");
                Point center = new Point();
                center.x = circle[0];
                center.y = circle[1];
                Imgproc.circle(grayEyesROI, center, (int) circle[2], new Scalar(255, 255, 100, 1), 4);
            }
            Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "hough" + ".jpg", grayEyesROI);
        }

/*
        // Find all contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayEyesROI, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        // Fill holes in each contour
        Imgproc.drawContours(grayEyesROI, contours, -1, new Scalar(255, 255, 255), -1);
        for (int k = 0; k < contours.size(); k++) {
            double area = Imgproc.contourArea(contours.get(k)); // Blob area
            Rect rect = Imgproc.boundingRect(contours.get(k));  // Bounding box
            int radius = rect.width / 2;                        // Approximate radius

            // Look for round shaped blob
            if (area >= 0 &&
                    Math.abs(1 - ((double) rect.width / (double) rect.height)) <= 0.2 &&
                    Math.abs(1 - (area / (Math.PI * Math.pow(radius, 2)))) <= 0.2) {
                Imgproc.rectangle(eyesROI, rect.tl(), rect.br(), new Scalar(0, 255, 0));
                Imgproc.circle(eyesROI, new Point(rect.x + radius, rect.y + radius), radius, new Scalar(0, 255, 0), 2);
            }
            frame = eyesROI;
        }
        Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "eyesROIWithCircles" + ".jpg", eyesROI);

    }*/

        //}
            /*} else {
                // Tracking stage with template matching
                System.out.println("Tracking Eye");
                Size size = new Size(eyeBoundingBox.width * 2, eyeBoundingBox.height * 2);
                Point point = new Point(size.width / 2, size.height / 2);
                eyeBoundingBox.width += size.width;
                eyeBoundingBox.height += size.height;
                eyeBoundingBox.x -= point.x;
                eyeBoundingBox.y -= point.y;
                Rect temp = new Rect(0, 0, frame.cols(), frame.rows());
                eyeBoundingBox.x &= temp.x;
                eyeBoundingBox.y &= temp.y;
                eyeBoundingBox.width &= temp.width;
                eyeBoundingBox.height &= temp.height;

                Mat dst = new Mat(eyeBoundingBox.width - eyeTemplate.rows() + 1, eyeBoundingBox.height - eyeTemplate.cols() + 1, CvType.CV_32FC1);
                Imgproc.matchTemplate(new Mat(frame, eyeBoundingBox), eyeTemplate, dst, Imgproc.TM_SQDIFF_NORMED);

                Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(dst);
                if (minMaxLocResult.minVal <= 0.2) {
                    eyeBoundingBox.x = (int) (eyeBoundingBox.x + minMaxLocResult.minLoc.x);
                    eyeBoundingBox.y = (int) (eyeBoundingBox.y + minMaxLocResult.minLoc.y);
                } else {
                    eyeBoundingBox.x = eyeBoundingBox.y = eyeBoundingBox.width = eyeBoundingBox.height = 0;
                }

                // Draw bounding rectangle for the eye
                Scalar color = new Scalar(0, 255, 0); //Green
                Imgproc.rectangle(frame, new Point(eyeBoundingBox.x, eyeBoundingBox.y), new Point(eyeBoundingBox.x + eyeBoundingBox.width, eyeBoundingBox.y + eyeBoundingBox.height), color);
            }
*/
        // To display image on the GUI
        // Draw bounding rectangle for the eye
        Scalar color = new Scalar(0, 255, 0); //Green
        Imgproc.rectangle(frame, new

                        Point(eyeBoundingBox.x, eyeBoundingBox.y),

                new

                        Point(eyeBoundingBox.x + eyeBoundingBox.width, eyeBoundingBox.y + eyeBoundingBox.height), color

        );

            /*System.out.println("Showing");
            displayImage(toBufferedImage(frame));*/


        Imgcodecs.imwrite(imagesDirPath + "testImageResult" + index + ".jpg", frame);

        //}

        return 1;
    }
}