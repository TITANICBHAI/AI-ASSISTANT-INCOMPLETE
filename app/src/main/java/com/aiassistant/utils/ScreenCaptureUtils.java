package com.aiassistant.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for screen capture.
 * This allows the AI to access visual information from the screen.
 */
public class ScreenCaptureUtils {
    private static final String TAG = Constants.TAG_PREFIX + "ScreenCaptureUtils";
    
    // Singleton instance
    private static volatile ScreenCaptureUtils instance;
    
    // Context
    private Context context;
    
    // Media projection manager
    private MediaProjectionManager mediaProjectionManager;
    
    // Media projection
    private MediaProjection mediaProjection;
    
    // Virtual display
    private VirtualDisplay virtualDisplay;
    
    // Image reader
    private ImageReader imageReader;
    
    // Screenshot directory
    private File screenshotDir;
    
    // Display metrics
    private DisplayMetrics displayMetrics;
    
    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    
    // Handlers
    private Handler handler;
    private ExecutorService executor;
    
    // Flags
    private boolean initialized = false;
    private boolean captureEnabled = false;
    
    /**
     * Get singleton instance
     * @param context Context
     * @return ScreenCaptureUtils instance
     */
    public static ScreenCaptureUtils getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (ScreenCaptureUtils.class) {
                if (instance == null) {
                    instance = new ScreenCaptureUtils(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Context
     */
    private ScreenCaptureUtils(Context context) {
        this.context = context.getApplicationContext();
        
        // Get display metrics
        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            display.getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
            screenDensity = displayMetrics.densityDpi;
        }
        
        // Create handlers
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        
        // Create screenshot directory
        screenshotDir = new File(context.getFilesDir(), Constants.SCREENSHOTS_DIR);
        if (!screenshotDir.exists()) {
            boolean success = screenshotDir.mkdirs();
            if (!success) {
                Log.e(TAG, "Failed to create screenshot directory");
            }
        }
        
        // Get media projection manager
        mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }
    
    /**
     * Initialize media projection with result from permission request
     * @param resultCode Result code from permission request
     * @param data Intent from permission request
     * @return True if initialization successful
     */
    public boolean initialize(int resultCode, Intent data) {
        if (mediaProjectionManager == null) {
            Log.e(TAG, "MediaProjectionManager is null");
            return false;
        }
        
        // Create media projection
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null");
            return false;
        }
        
        // Create image reader
        imageReader = ImageReader.newInstance(
                screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
        
        // Create virtual display
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "AI Assistant Screen Capture",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
        
        // Mark as initialized
        initialized = true;
        captureEnabled = true;
        
        Log.i(TAG, "Screen capture initialized with resolution " + screenWidth + "x" + screenHeight);
        return true;
    }
    
    /**
     * Check if screen capture is initialized
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Check if capture is enabled
     * @return True if enabled
     */
    public boolean isCaptureEnabled() {
        return captureEnabled;
    }
    
    /**
     * Set capture enabled
     * @param enabled True to enable
     */
    public void setCaptureEnabled(boolean enabled) {
        captureEnabled = enabled;
    }
    
    /**
     * Get screen width
     * @return Screen width
     */
    public int getScreenWidth() {
        return screenWidth;
    }
    
    /**
     * Get screen height
     * @return Screen height
     */
    public int getScreenHeight() {
        return screenHeight;
    }
    
    /**
     * Get screen density
     * @return Screen density
     */
    public int getScreenDensity() {
        return screenDensity;
    }
    
    /**
     * Capture a screenshot
     * @param callback Callback for capture result
     */
    public void captureScreenshot(CaptureCallback callback) {
        if (!initialized || !captureEnabled || imageReader == null) {
            if (callback != null) {
                callback.onError("Screen capture not initialized or disabled");
            }
            return;
        }
        
        try {
            // Get image from image reader
            Image image = imageReader.acquireLatestImage();
            if (image == null) {
                if (callback != null) {
                    callback.onError("Failed to acquire image");
                }
                return;
            }
            
            // Process image in background
            executor.execute(() -> {
                Bitmap bitmap = null;
                String path = null;
                
                try {
                    // Convert image to bitmap
                    bitmap = imageToBitmap(image);
                    
                    // Save bitmap to file if needed
                    path = saveBitmapToFile(bitmap);
                    
                    // Call callback on main thread
                    final Bitmap finalBitmap = bitmap;
                    final String finalPath = path;
                    handler.post(() -> {
                        if (callback != null) {
                            callback.onCaptured(finalBitmap, finalPath);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error processing screenshot", e);
                    
                    // Call callback with error
                    handler.post(() -> {
                        if (callback != null) {
                            callback.onError("Error processing screenshot: " + e.getMessage());
                        }
                    });
                } finally {
                    // Close image
                    image.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error capturing screenshot", e);
            
            if (callback != null) {
                callback.onError("Error capturing screenshot: " + e.getMessage());
            }
        }
    }
    
    /**
     * Convert image to bitmap
     * @param image Image
     * @return Bitmap
     */
    private Bitmap imageToBitmap(Image image) {
        // Get image planes
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        
        // Calculate row stride
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        
        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888);
        
        // Copy buffer to bitmap
        bitmap.copyPixelsFromBuffer(buffer);
        
        // Crop to correct size if needed
        if (bitmap.getWidth() > image.getWidth() || bitmap.getHeight() > image.getHeight()) {
            Bitmap croppedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, image.getWidth(), image.getHeight());
            bitmap.recycle();
            return croppedBitmap;
        }
        
        return bitmap;
    }
    
    /**
     * Save bitmap to file
     * @param bitmap Bitmap
     * @return File path
     */
    private String saveBitmapToFile(Bitmap bitmap) {
        // Generate file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "screenshot_" + timestamp + ".png";
        File file = new File(screenshotDir, fileName);
        
        // Save bitmap
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return null;
        }
    }
    
    /**
     * Release resources
     */
    public void release() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        
        initialized = false;
        captureEnabled = false;
        
        Log.i(TAG, "Screen capture resources released");
    }
    
    /**
     * Delete all screenshots
     */
    public void deleteAllScreenshots() {
        if (screenshotDir.exists()) {
            File[] files = screenshotDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete screenshot: " + file.getName());
                    }
                }
            }
            
            Log.i(TAG, "All screenshots deleted");
        }
    }
    
    /**
     * Delete old screenshots
     * @param maxAgeMs Maximum age in milliseconds
     */
    public void deleteOldScreenshots(long maxAgeMs) {
        if (screenshotDir.exists()) {
            long now = System.currentTimeMillis();
            File[] files = screenshotDir.listFiles();
            if (files != null) {
                int count = 0;
                for (File file : files) {
                    if (now - file.lastModified() > maxAgeMs) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            count++;
                        } else {
                            Log.w(TAG, "Failed to delete old screenshot: " + file.getName());
                        }
                    }
                }
                
                if (count > 0) {
                    Log.i(TAG, "Deleted " + count + " old screenshots");
                }
            }
        }
    }
    
    /**
     * Screenshot capture callback
     */
    public interface CaptureCallback {
        /**
         * Called when screenshot is captured
         * @param bitmap Bitmap
         * @param path File path or null
         */
        void onCaptured(Bitmap bitmap, String path);
        
        /**
         * Called when error occurs
         * @param message Error message
         */
        void onError(String message);
    }
}