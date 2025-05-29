package com.aiassistant.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aiassistant.services.AIAccessibilityService;

/**
 * Utility class for taking screenshots and screen capture
 */
public class ScreenshotUtils {
    private static final String TAG = "ScreenshotUtils";
    
    /**
     * Take a screenshot using the accessibility service
     * @param context Application context
     * @return Bitmap of the screenshot or null if failed
     */
    public static Bitmap takeScreenshot(Context context) {
        AIAccessibilityService service = AIAccessibilityService.getInstance();
        if (service == null) {
            Log.e(TAG, "Cannot take screenshot: accessibility service not running");
            return null;
        }
        
        return service.takeScreenshot();
    }
    
    /**
     * Save a screenshot to file
     * @param bitmap Screenshot bitmap
     * @param context Application context
     * @param filename Filename for the screenshot
     * @return True if successful
     */
    public static boolean saveScreenshot(Bitmap bitmap, Context context, String filename) {
        if (bitmap == null) {
            return false;
        }
        
        try {
            java.io.FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving screenshot: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crop a region from a screenshot
     * @param screenshot Full screenshot
     * @param x Left coordinate
     * @param y Top coordinate
     * @param width Width of region
     * @param height Height of region
     * @return Cropped bitmap or null if failed
     */
    public static Bitmap cropScreenshot(Bitmap screenshot, int x, int y, int width, int height) {
        if (screenshot == null) {
            return null;
        }
        
        // Ensure coordinates are within bounds
        int bmpWidth = screenshot.getWidth();
        int bmpHeight = screenshot.getHeight();
        
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > bmpWidth) width = bmpWidth - x;
        if (y + height > bmpHeight) height = bmpHeight - y;
        
        // Check if crop dimensions are valid
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid crop dimensions: " + width + "x" + height);
            return null;
        }
        
        try {
            return Bitmap.createBitmap(screenshot, x, y, width, height);
        } catch (Exception e) {
            Log.e(TAG, "Error cropping screenshot: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Scale a bitmap to a specific size
     * @param bitmap Original bitmap
     * @param newWidth New width
     * @param newHeight New height
     * @return Scaled bitmap or null if failed
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
        
        try {
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        } catch (Exception e) {
            Log.e(TAG, "Error scaling bitmap: " + e.getMessage());
            return null;
        }
    }
}
