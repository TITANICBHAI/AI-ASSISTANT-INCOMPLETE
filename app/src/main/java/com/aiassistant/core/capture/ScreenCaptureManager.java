package com.aiassistant.core.capture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Manager for screen capture
 */
public class ScreenCaptureManager {
    private static final String TAG = "ScreenCaptureManager";
    
    private Context context;
    private boolean isCapturing;
    private Bitmap latestScreenshot;
    
    /**
     * Constructor
     * @param context Application context
     */
    public ScreenCaptureManager(Context context) {
        this.context = context;
    }
    
    /**
     * Start capture
     */
    public void startCapture() {
        if (isCapturing) {
            return;
        }
        
        isCapturing = true;
        Log.d(TAG, "Screen capture started");
    }
    
    /**
     * Stop capture
     */
    public void stopCapture() {
        if (!isCapturing) {
            return;
        }
        
        isCapturing = false;
        Log.d(TAG, "Screen capture stopped");
    }
    
    /**
     * Get latest screenshot
     * @return Latest screenshot
     */
    public Bitmap getLatestScreenshot() {
        return latestScreenshot;
    }
    
    /**
     * Update latest screenshot
     * @param screenshot New screenshot
     */
    public void updateLatestScreenshot(Bitmap screenshot) {
        this.latestScreenshot = screenshot;
    }
    
    /**
     * Is capturing
     * @return True if capturing
     */
    public boolean isCapturing() {
        return isCapturing;
    }
}
