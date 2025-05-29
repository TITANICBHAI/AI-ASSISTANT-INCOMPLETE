package com.aiassistant.utils;

import android.app.Activity;
import android.app.Service;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for capturing screen content
 */
public class ScreenCaptureService extends Service {
    
    private static final String TAG = "ScreenCaptureService";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private int width;
    private int height;
    private int density;
    private Handler handler;
    private AtomicBoolean isCapturing = new AtomicBoolean(false);
    
    // Callback interface
    public interface ScreenCaptureCallback {
        void onScreenCaptured(Bitmap bitmap);
    }
    
    // Static instance of the service for direct access
    private static ScreenCaptureService instance;
    
    /**
     * Get the service instance
     * 
     * @param context The context
     * @return The service instance
     */
    public static ScreenCaptureService getInstance(Context context) {
        if (instance == null) {
            // Start the service if not running
            Intent intent = new Intent(context, ScreenCaptureService.class);
            context.startService(intent);
        }
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        handler = new Handler();
        
        // Get screen dimensions
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        density = metrics.densityDpi;
        
        Log.d(TAG, "Screen dimensions: " + width + "x" + height);
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Screen capture service started");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCapture();
        instance = null;
        Log.d(TAG, "Screen capture service destroyed");
    }
    
    /**
     * Start media projection
     * 
     * @param resultCode The result code
     * @param data The intent data
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startProjection(int resultCode, Intent data) {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        
        if (mediaProjection != null) {
            createVirtualDisplay();
            Log.d(TAG, "Media projection started");
        } else {
            Log.e(TAG, "Failed to start media projection");
        }
    }
    
    /**
     * Create virtual display
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        if (mediaProjection == null) {
            Log.e(TAG, "Media projection is null");
            return;
        }
        
        try {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
            
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    width,
                    height,
                    density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null,
                    handler);
            
            Log.d(TAG, "Virtual display created");
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating virtual display: " + e.getMessage(), e);
        }
    }
    
    /**
     * Capture screen
     * 
     * @param callback The callback
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void captureScreen(final ScreenCaptureCallback callback) {
        if (mediaProjection == null || imageReader == null) {
            Log.e(TAG, "Media projection or image reader is null");
            callback.onScreenCaptured(null);
            return;
        }
        
        if (isCapturing.get()) {
            Log.d(TAG, "Already capturing, skipping");
            return;
        }
        
        isCapturing.set(true);
        
        try {
            // Set up image available listener
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    Bitmap bitmap = null;
                    
                    try {
                        image = reader.acquireLatestImage();
                        if (image != null) {
                            Image.Plane[] planes = image.getPlanes();
                            ByteBuffer buffer = planes[0].getBuffer();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * width;
                            
                            // Create bitmap
                            bitmap = Bitmap.createBitmap(
                                    width + rowPadding / pixelStride,
                                    height,
                                    Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buffer);
                            
                            // Crop bitmap to the correct size
                            if (bitmap.getWidth() > width) {
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                            }
                            
                            // Invoke callback
                            callback.onScreenCaptured(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error capturing screen: " + e.getMessage(), e);
                        callback.onScreenCaptured(null);
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                        isCapturing.set(false);
                        
                        // Remove listener to prevent memory leaks
                        imageReader.setOnImageAvailableListener(null, null);
                    }
                }
            }, handler);
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up image available listener: " + e.getMessage(), e);
            isCapturing.set(false);
            callback.onScreenCaptured(null);
        }
    }
    
    /**
     * Stop capture
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopCapture() {
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
        
        Log.d(TAG, "Screen capture stopped");
    }
    
    /**
     * Get result data from activity
     * 
     * @param activity The activity
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The intent data
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            ScreenCaptureService service = getInstance(activity);
            if (service != null) {
                service.startProjection(resultCode, data);
            }
        }
    }
}
