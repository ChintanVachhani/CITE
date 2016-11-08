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

public class FaceTracking {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    String cascadesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\cascades\\";
    String imagesDirPath = "F:\\Study\\Project-Final_Year\\Implementations\\CITE\\src\\main\\resources\\images\\";
    CascadeClassifier faceCascade = new CascadeClassifier();
    CascadeClassifier eyeCascade = new CascadeClassifier();

    Mat frame = new Mat();
    Mat faceTemplate = new Mat();
    Rect faceBoundingBox = new Rect();
    Rect eyeBoundingBox = new Rect();
    int videoCaptureWidth = 800;
    int videoCaptureHeight = 600;
    int screenWidth = 1366;
    int screenHeight = 768;
    double widthScalingFactor, heightScalingFactor;


    JFrame jFrame = new JFrame();
    JLabel lbl = new JLabel();
    int index = 1;

    Robot robot = new Robot();

    FaceTracking() throws AWTException {
        faceCascade.load(cascadesDirPath + "haarcascade_frontalface_alt2.xml");
        eyeCascade.load(cascadesDirPath + "haarcascade_righteye_2splits.xml");
        jFrame.setLayout(new FlowLayout());
        jFrame.add(lbl);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        widthScalingFactor = screenWidth / videoCaptureWidth;
        heightScalingFactor = screenHeight / videoCaptureHeight;
    }

    FaceTracking(int index) throws AWTException {
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
        ImageIcon icon = new ImageIcon(img);
        jFrame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
        lbl.setIcon(icon);
    }

    public void start() {
        VideoCapture videoCapture = new VideoCapture(0);

        if (!videoCapture.isOpened()) {
            System.out.println("Error starting the Camera !");
            return;
        }
        if (faceCascade.empty()) {
            System.out.println("Error loading Face Cascade !");
            return;
        }

        videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, videoCaptureWidth);
        videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, videoCaptureHeight);

        while (true) {

            videoCapture.read(frame);

            if (frame.empty()) {
                System.out.println("Error loading Frame !");
                break;
            }

            Core.flip(frame, frame, 1);

            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGB2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            System.out.println(faceBoundingBox.br());
            System.out.println(faceBoundingBox.tl());

//            if (faceBoundingBox.width == 0 && faceBoundingBox.height == 0) {

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

            Rect[] facesArray = faces.toArray();

            if (facesArray.length > 0) {
                faceTemplate = frame.submat(facesArray[0]);
                Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "FaceTemplate" + ".jpg", faceTemplate);
                faceBoundingBox = facesArray[0];
                System.out.println("Detecting");
                Imgproc.rectangle(frame, new Point(faceBoundingBox.x, faceBoundingBox.y), new Point(faceBoundingBox.x + faceBoundingBox.width, faceBoundingBox.y + faceBoundingBox.height), new Scalar(0, 255, 0));
                robot.mouseMove((int) ((faceBoundingBox.x + (faceBoundingBox.width / 2)) * widthScalingFactor), (int) ((faceBoundingBox.y + (faceBoundingBox.height / 2)) * heightScalingFactor));
                // Each face is cropped to Region of Interest (ROI)
                Mat faceROI = frame.submat(facesArray[0]);

                // Detects eyes and puts it into MatOfRect
                MatOfRect eyes = new MatOfRect();
                eyeCascade.detectMultiScale(frame, eyes, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());
                Rect[] eyesArray = eyes.toArray();
                if (eyesArray.length > 0) {

                    eyeBoundingBox = eyesArray[0];

                    Imgproc.rectangle(frame, eyesArray[0].tl(), eyesArray[0].br(), new Scalar(0, 0, 255));
                    //Imgproc.rectangle(frame, new Point(eyeBoundingBox.x, eyeBoundingBox.y), new Point(eyeBoundingBox.x + eyeBoundingBox.width, eyeBoundingBox.y + eyeBoundingBox.height), new Scalar(0, 255, 0));
                    //robot.mouseMove((int) ((eyeBoundingBox.x + (eyeBoundingBox.width / 2)) * widthScalingFactor), (int) ((eyeBoundingBox.y + (eyeBoundingBox.height / 2)) * heightScalingFactor));
                }
            }
  /*          } else {
                Mat tempFrame = new Mat();
                frame.copyTo(tempFrame);
                Point matchLoc;
                Mat dst = new Mat(frame.rows() - faceTemplate.rows() + 1, frame.cols() - faceTemplate.cols() + 1, CvType.CV_32FC1);
                Imgproc.matchTemplate(frame, faceTemplate, dst, Imgproc.TM_SQDIFF);
                Core.normalize(dst, dst, 0, 1, Core.NORM_MINMAX, -1, new Mat());
                Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(dst);
                System.out.println(minMaxLocResult.minVal);
                if (minMaxLocResult.minVal <= 0.2) {
                    matchLoc = minMaxLocResult.minLoc;
                    System.out.println("Tracking");
                    //Imgproc.rectangle(frame, matchLoc, new Point(matchLoc.x + faceTemplate.cols(), matchLoc.y + faceTemplate.rows()), new Scalar(0, 255, 0));
                    faceBoundingBox.x = (int) matchLoc.x;
                    faceBoundingBox.y = (int) matchLoc.y;
                    faceBoundingBox.width = faceTemplate.cols();
                    faceBoundingBox.height = faceTemplate.rows();
                    System.out.println(faceBoundingBox.br());
                    System.out.println(faceBoundingBox.tl());
                    System.out.println("Tracked");
                    Imgproc.rectangle(frame, new Point(faceBoundingBox.x, faceBoundingBox.y), new Point(faceBoundingBox.x + faceBoundingBox.width, faceBoundingBox.y + faceBoundingBox.height), new Scalar(0, 255, 0));
                    //Imgcodecs.imwrite(imagesDirPath + "testImageResult" + "TrackedFace" + ".jpg", tempFrame);
                    displayImage(toBufferedImage(frame));
                } else {
                    faceBoundingBox.x = faceBoundingBox.y = faceBoundingBox.width = faceBoundingBox.height = 0;
                }


            }
  */
            displayImage(toBufferedImage(frame));

        }

    }

}
 