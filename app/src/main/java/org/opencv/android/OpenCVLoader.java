package org.opencv.android;

import android.content.Context;
import android.util.Log;

/**
 * Stub implementation of OpenCVLoader
 */
public class OpenCVLoader {
    private static final String TAG = "OpenCVLoader";
    
    public static final String OPENCV_VERSION_2_4_2 = "2.4.2";
    public static final String OPENCV_VERSION_2_4_3 = "2.4.3";
    public static final String OPENCV_VERSION_2_4_4 = "2.4.4";
    public static final String OPENCV_VERSION_2_4_5 = "2.4.5";
    public static final String OPENCV_VERSION_2_4_6 = "2.4.6";
    public static final String OPENCV_VERSION_2_4_7 = "2.4.7";
    public static final String OPENCV_VERSION_2_4_8 = "2.4.8";
    public static final String OPENCV_VERSION_2_4_9 = "2.4.9";
    public static final String OPENCV_VERSION_2_4_10 = "2.4.10";
    public static final String OPENCV_VERSION_2_4_11 = "2.4.11";
    public static final String OPENCV_VERSION_3_0_0 = "3.0.0";
    public static final String OPENCV_VERSION_3_1_0 = "3.1.0";
    public static final String OPENCV_VERSION_3_2_0 = "3.2.0";
    public static final String OPENCV_VERSION_3_3_0 = "3.3.0";
    public static final String OPENCV_VERSION_3_4_0 = "3.4.0";
    public static final String OPENCV_VERSION_4_5_5 = "4.5.5";
    
    /**
     * Stub method for loading OpenCV libraries
     */
    public static boolean initDebug() {
        Log.w(TAG, "OpenCV is not available - using stub implementation");
        return true;
    }
    
    /**
     * Stub method for loading OpenCV libraries
     */
    public static boolean initDebug(boolean initCuda) {
        Log.w(TAG, "OpenCV is not available - using stub implementation");
        return true;
    }
    
    /**
     * Stub method for loading OpenCV libraries asynchronously
     */
    public static boolean initAsync(String version, Context context, LoaderCallbackInterface callback) {
        Log.w(TAG, "OpenCV is not available - using stub implementation");
        if (callback != null) {
            callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        return true;
    }
    
    /**
     * Stub interface for loader callbacks
     */
    public interface LoaderCallbackInterface {
        int SUCCESS = 0;
        int MARKET_ERROR = 1;
        int INSTALL_CANCELED = 2;
        int INCOMPATIBLE_MANAGER_VERSION = 3;
        int INIT_FAILED = 4;
        int INVALID_VERSION = 5;
        
        void onManagerConnected(int status);
        void onPackageInstall(int operation, InstallCallbackInterface callback);
    }
    
    /**
     * Stub interface for installation callbacks
     */
    public interface InstallCallbackInterface {
        int INSTALL_STARTED = 0;
        int INSTALLED = 1;
        
        void install();
        void cancel();
        void wait_install();
    }
}