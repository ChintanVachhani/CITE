package main.classes;

public class Constants {
    // Debugging
    public static final boolean kPlotVectorField = false;

    // Size constants
    public static final int kEyePercentTop = 25;
    public static final int kEyePercentSide = 13;
    public static final int kEyePercentHeight = 30;
    public static final int kEyePercentWidth = 35;

    // Pre-processing
    public static final boolean kSmoothFaceImage = false;
    public static final double kSmoothFaceFactor = 0.005;

    // Algorithm Parameters
    public static final int kFastEyeWidth = 50;
    public static final int kWeightBlurSize = 5;
    public static final boolean kEnableWeight = true;
    public static final double kWeightDivisor = 1.0;
    public static final double kGradientThreshold = 50.0;

    // Postprocessing
    public static final boolean kEnablePostProcess = true;
    public static final double kPostProcessThreshold = 0.97;

    // Eye Corner
    public static final boolean kEnableEyeCorner = false;
}
