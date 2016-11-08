package main.classes.v1;

import com.atul.JavaOpenCV.Imshow;
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

public class EyeTrackingFirstAttempt extends JFrame {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    String cascadesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\cascades\\";
    String imagesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\images\\";

    public CascadeClassifier faceCascade = new CascadeClassifier();
    public CascadeClassifier eyeCascade = new CascadeClassifier();

    JFrame jFrame = new JFrame();
    JLabel lbl = new JLabel();

    Mat frame = new Mat();
    Mat gray = new Mat();
    Mat eyeTemplate = new Mat();
    Rect eyeBoundingBox = new Rect();

    EyeTrackingFirstAttempt() {
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
        //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
        ImageIcon icon = new ImageIcon(img);
        jFrame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
        lbl.setIcon(icon);
    }


    private void detectEye() {

        MatOfRect faces = new MatOfRect();
        MatOfRect eyes = new MatOfRect();

        // Detects faces and puts it into MatOfRect
        faceCascade.detectMultiScale(frame, faces, 1.1, 2, 2, new Size(30, 30), new Size());

        // Converts MatOfRect to Rect Array
        Rect[] facesArray = faces.toArray();
        Rect[] eyesArray;

        for (int i = 0; i < facesArray.length; i++) {
            System.out.println("Face Detected");
            // Each face is cropped to Region of Interest (ROI)
            Mat faceROI = frame.submat(facesArray[i]);
            System.out.println(faceROI);
            // Detects eyes and puts it into MatOfRect
            eyeCascade.detectMultiScale(faceROI, eyes, 1.1, 2, 2, new Size(20, 20), new Size());
            eyesArray = eyes.toArray();
            System.out.println(eyesArray.length);
            if (eyesArray.length > 0) {
                System.out.println("Eye Detected");
                // Augment face coordinates to eye ROI
                eyesArray[0].x += facesArray[i].x;
                eyesArray[0].y += facesArray[i].y;

                // Cropped eye ROI put into template
                eyeBoundingBox = eyesArray[0];
                eyeTemplate = frame.submat(eyeBoundingBox);
                Imgcodecs.imwrite(imagesDirPath + "testImageResult" + ".jpg", eyeTemplate);
            }
        }
        System.out.println(eyeBoundingBox.width + " " + " " + eyeBoundingBox.height);
        System.out.println(eyeTemplate);
        //System.out.println("Eye Detected");
    }


    private void trackEye() {
        /*Size size = new Size(eyeBoundingBox.width * 2, eyeBoundingBox.height * 2);
        Point point = new Point(size.width / 2, size.height / 2);
        System.out.println(eyeBoundingBox.width + " " + " " + eyeBoundingBox.height);
        eyeBoundingBox.width += size.width;
        eyeBoundingBox.height += size.height;
        System.out.println(eyeBoundingBox.width + " " + " " + eyeBoundingBox.height);
        eyeBoundingBox.x -= point.x;
        eyeBoundingBox.y -= point.y;
        System.out.println(eyeBoundingBox.width + " " + " " + eyeBoundingBox.height);
        Rect temp = new Rect(0, 0, frame.cols(), frame.rows());
        eyeBoundingBox.x &= temp.x;
        eyeBoundingBox.y &= temp.y;
        eyeBoundingBox.width &= temp.width;
        eyeBoundingBox.height &= temp.height;
        System.out.println(eyeBoundingBox.width + " " + " " + eyeBoundingBox.height);
        System.out.println(eyeTemplate.rows() + " " + " " + eyeTemplate.cols());
        System.out.println(eyeBoundingBox.width - eyeTemplate.rows() + 1);
        System.out.println(eyeBoundingBox.height - eyeTemplate.cols() + 1);

        if (eyeBoundingBox.width - eyeTemplate.rows() + 1 > 0 && eyeBoundingBox.height - eyeTemplate.cols() + 1 > 0) {
            Mat dst = new Mat(eyeBoundingBox.width - eyeTemplate.rows() + 1, eyeBoundingBox.height - eyeTemplate.cols() + 1, CvType.CV_32FC1);
            Imgproc.matchTemplate(new Mat(frame, eyeBoundingBox), eyeTemplate, dst, Imgproc.TM_SQDIFF_NORMED);

            Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(dst);
            if (minMaxLocResult.minVal <= 0.2) {
                eyeBoundingBox.x = (int) (eyeBoundingBox.x + minMaxLocResult.minLoc.x);
                eyeBoundingBox.y = (int) (eyeBoundingBox.y + minMaxLocResult.minLoc.y);
            } else {
                eyeBoundingBox.x = eyeBoundingBox.y = eyeBoundingBox.width = eyeBoundingBox.height = 0;
            }
        } else {
            eyeBoundingBox.x = eyeBoundingBox.y = eyeBoundingBox.width = eyeBoundingBox.height = 0;
        }*/
        Mat tempFrame = new Mat();
        frame.copyTo(tempFrame);
        Point matchLoc;
        Mat dst = new Mat(frame.rows() - eyeTemplate.rows() + 1, frame.cols() - eyeTemplate.cols() + 1, CvType.CV_32FC1);
        Imgproc.matchTemplate(frame, eyeTemplate, dst, Imgproc.TM_SQDIFF);
        Core.normalize(dst, dst, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(dst);
        System.out.println(minMaxLocResult.minVal);
        if (minMaxLocResult.minVal <= 0.2) {
            matchLoc = minMaxLocResult.minLoc;
            System.out.println("Tracking");
            Imgproc.rectangle(tempFrame, matchLoc, new Point(matchLoc.x + eyeTemplate.cols(), matchLoc.y + eyeTemplate.rows()), new Scalar(0, 255, 0));
            displayImage(toBufferedImage(tempFrame));
        } else {
            eyeBoundingBox.x = eyeBoundingBox.y = eyeBoundingBox.width = eyeBoundingBox.height = 0;
        }

    }

    public void start() {

        // Load the cascade classifiers
        // Make sure you point the XML files to the right path, or
        // just copy the files from [OPENCV_DIR]/data/haarcascades directory
        /*faceCascade.load(EyeTrackingFirstAttempt.class.getResource("main/resources/cascades/haarcascade_eye.xml").getPath());
        faceCascade.load(EyeTrackingFirstAttempt.class.getResource("main/resources/cascades/haarcascade_frontalface_alt2.xml").getPath());*/
        String DirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\cascades\\";
        faceCascade.load(DirPath + "haarcascade_frontalface_alt2.xml");
        eyeCascade.load(DirPath + "haarcascade_lefteye_2splits.xml");

        System.out.println("Start");
        // Open webcam
        VideoCapture videoCapture = new VideoCapture(0);

        // Check if everything is ok
        if (faceCascade.empty() || eyeCascade.empty() || !videoCapture.isOpened()) {
            System.out.println(faceCascade.empty());
            System.out.println(eyeCascade.empty());
            System.out.println(!videoCapture.isOpened());
            return;
        }

        // Set video to 320x240
        videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 800);
        videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 600);

        System.out.println("Outside Loop");

        while (true) {
            System.out.println("Inside Loop");
            videoCapture.read(frame);
            if (frame.empty())
                break;

            // Flip the frame horizontally, Windows users might need this
            Core.flip(frame, frame, 1);

            // Convert to grayscale and
            // adjust the image contrast using histogram equalization
            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.equalizeHist(gray, gray);
            System.out.println("Frame Captured");
            //detectEye();
            //Scalar color = new Scalar(0, 255, 0);
            //Imgproc.rectangle(frame, new Point(eyeBoundingBox.x, eyeBoundingBox.y), new Point(eyeBoundingBox.x + eyeBoundingBox.width, eyeBoundingBox.y + eyeBoundingBox.height), color);
            if (eyeBoundingBox.width == 0 && eyeBoundingBox.height == 0) {
                // Detection stage
                // Try to detect the face and the eye of the user
                System.out.println("Detecting Eye");
                detectEye();
            } else {
                // Tracking stage with template matching
                System.out.println("Tracking Eye");
                trackEye();

                // Draw bounding rectangle for the eye
                Scalar color = new Scalar(0, 255, 0);
                Imgproc.rectangle(frame, new Point(eyeBoundingBox.x, eyeBoundingBox.y), new Point(eyeBoundingBox.x + eyeBoundingBox.width, eyeBoundingBox.y + eyeBoundingBox.height), color);
            }

            // To display image on the GUI
            // We use external jar from the github project (source: https://github.com/master-atul/ImShow-Java-OpenCV)
            System.out.println("Showing");
            displayImage(toBufferedImage(frame));

            /*Imshow imshow = new Imshow("video");
            imshow.showImage(frame);*/

        }

    }
}
