package main.classes.v1;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        /*for (int i = 1; i <= 1; i++) {
            EyeTracking obj = new EyeTracking(i);
            obj.start();
        }*/
        FaceTracking obj = null;
        try {
            obj = new FaceTracking();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        obj.start();
        /*EyeTrackingFirstAttempt obj = new EyeTrackingFirstAttempt();
        obj.start();*/
    }


}