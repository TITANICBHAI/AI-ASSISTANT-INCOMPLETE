
package com.aiassistant.core.gaming.vision;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains a rolling buffer of frames for temporal analysis
 * Allows analysis of events that occur across multiple frames
 */
public class FrameBufferAnalyzer {
    private static final String TAG = "FrameBufferAnalyzer";
    
    // Frame buffer configuration
    private static final int DEFAULT_BUFFER_SIZE = 15; // Default buffer size (frames)
    private int bufferSize; // Current buffer size
    
    // Frame buffer
    private final Queue<BufferedFrame> frameBuffer;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    
    // Listeners
    private FrameBufferListener listener;
    
    /**
     * Represents a frame in the buffer
     */
    public static class BufferedFrame {
        private final Bitmap bitmap;
        private final long timestamp;
        private final int frameIndex;
        
        public BufferedFrame(Bitmap bitmap, long timestamp, int frameIndex) {
            this.bitmap = bitmap;
            this.timestamp = timestamp;
            this.frameIndex = frameIndex;
        }
        
        public Bitmap getBitmap() {
            return bitmap;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public int getFrameIndex() {
            return frameIndex;
        }
    }
    
    /**
     * Interface for buffer events
     */
    public interface FrameBufferListener {
        void onSignificantEvent(BufferedFrame[] relevantFrames, String eventType);
    }
    
    /**
     * Constructor
     */
    public FrameBufferAnalyzer() {
        this(DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Constructor with custom buffer size
     * @param bufferSize Number of frames to keep in buffer
     */
    public FrameBufferAnalyzer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.frameBuffer = new LinkedList<>();
    }
    
    /**
     * Set listener
     * @param listener Listener for buffer events
     */
    public void setListener(FrameBufferListener listener) {
        this.listener = listener;
    }
    
    /**
     * Add frame to buffer
     * @param bitmap Frame bitmap
     * @param timestamp Frame timestamp
     * @param frameIndex Frame index
     */
    public void addFrame(Bitmap bitmap, long timestamp, int frameIndex) {
        // Create a copy of the bitmap to ensure it's not recycled elsewhere
        Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
        
        // Add to buffer
        synchronized (frameBuffer) {
            // Remove oldest frame if buffer is full
            if (frameBuffer.size() >= bufferSize) {
                BufferedFrame oldestFrame = frameBuffer.poll();
                if (oldestFrame != null && !oldestFrame.getBitmap().isRecycled()) {
                    oldestFrame.getBitmap().recycle();
                }
            }
            
            // Add new frame
            frameBuffer.add(new BufferedFrame(bitmapCopy, timestamp, frameIndex));
        }
        
        // Analyze buffer if not already processing
        if (!isProcessing.getAndSet(true)) {
            analyzeBuffer();
        }
    }
    
    /**
     * Analyze buffer for significant events
     */
    private void analyzeBuffer() {
        try {
            // Get frames from buffer
            BufferedFrame[] frames;
            synchronized (frameBuffer) {
                frames = frameBuffer.toArray(new BufferedFrame[0]);
            }
            
            if (frames.length < 2) {
                return;
            }
            
            // Check for rapid motion
            if (detectRapidMotion(frames)) {
                if (listener != null) {
                    listener.onSignificantEvent(frames, "rapid_motion");
                }
            }
            
            // Check for sudden brightness change (flash/explosion)
            if (detectBrightnessChange(frames)) {
                if (listener != null) {
                    listener.onSignificantEvent(frames, "brightness_change");
                }
            }
            
            // Check for HP/health bar changes
            if (detectHealthChange(frames)) {
                if (listener != null) {
                    listener.onSignificantEvent(frames, "health_change");
                }
            }
        } finally {
            // Reset processing flag
            isProcessing.set(false);
        }
    }
    
    /**
     * Detect rapid motion between frames
     * @param frames Array of frames
     * @return True if rapid motion detected
     */
    private boolean detectRapidMotion(BufferedFrame[] frames) {
        if (frames.length < 3) {
            return false;
        }
        
        // Calculate motion metrics between consecutive frames
        // Simple implementation looks at pixel differences between frames
        for (int i = 0; i < frames.length - 2; i++) {
            Bitmap current = frames[i].getBitmap();
            Bitmap next = frames[i + 1].getBitmap();
            
            // Skip if either bitmap is recycled
            if (current.isRecycled() || next.isRecycled()) {
                continue;
            }
            
            // Calculate difference in key areas (center of screen)
            int width = current.getWidth();
            int height = current.getHeight();
            
            int centerX = width / 2;
            int centerY = height / 2;
            int sampleRadius = Math.min(width, height) / 4;
            
            int diffCount = 0;
            int totalSamples = 0;
            
            // Sample pixels in center region
            for (int x = centerX - sampleRadius; x < centerX + sampleRadius; x += 4) {
                for (int y = centerY - sampleRadius; y < centerY + sampleRadius; y += 4) {
                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        int pixel1 = current.getPixel(x, y);
                        int pixel2 = next.getPixel(x, y);
                        
                        // Calculate color difference
                        int rDiff = Math.abs(((pixel1 >> 16) & 0xFF) - ((pixel2 >> 16) & 0xFF));
                        int gDiff = Math.abs(((pixel1 >> 8) & 0xFF) - ((pixel2 >> 8) & 0xFF));
                        int bDiff = Math.abs((pixel1 & 0xFF) - (pixel2 & 0xFF));
                        
                        int avgDiff = (rDiff + gDiff + bDiff) / 3;
                        
                        if (avgDiff > 30) { // Threshold for significant change
                            diffCount++;
                        }
                        
                        totalSamples++;
                    }
                }
            }
            
            // Calculate percentage of changed pixels
            double changePercentage = (double)diffCount / totalSamples;
            
            // If more than 20% pixels changed significantly, consider it rapid motion
            if (changePercentage > 0.2) {
                Log.d(TAG, "Detected rapid motion: " + changePercentage + " changed");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detect sudden brightness changes (flashes, explosions)
     * @param frames Array of frames
     * @return True if brightness change detected
     */
    private boolean detectBrightnessChange(BufferedFrame[] frames) {
        if (frames.length < 3) {
            return false;
        }
        
        // Calculate average brightness for each frame
        double[] brightness = new double[frames.length];
        
        for (int i = 0; i < frames.length; i++) {
            Bitmap bitmap = frames[i].getBitmap();
            
            if (bitmap.isRecycled()) {
                continue;
            }
            
            long totalBrightness = 0;
            int pixelCount = 0;
            
            // Sample pixels (skip every few pixels for efficiency)
            for (int x = 0; x < bitmap.getWidth(); x += 8) {
                for (int y = 0; y < bitmap.getHeight(); y += 8) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    
                    // Use relative luminance formula
                    int luminance = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
                    totalBrightness += luminance;
                    pixelCount++;
                }
            }
            
            brightness[i] = (double)totalBrightness / pixelCount;
        }
        
        // Check for significant brightness changes
        for (int i = 0; i < brightness.length - 1; i++) {
            double change = Math.abs(brightness[i + 1] - brightness[i]);
            double changePercentage = change / brightness[i];
            
            // If brightness changed by more than 30%, consider it significant
            if (changePercentage > 0.3) {
                Log.d(TAG, "Detected brightness change: " + changePercentage + " change");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detect health/HP bar changes
     * @param frames Array of frames
     * @return True if health change detected
     */
    private boolean detectHealthChange(BufferedFrame[] frames) {
        // This would use game-specific knowledge of where health bars are located
        // For demonstration purposes, we'll check for red/green changes in the lower part
        // of the screen where health bars are commonly located
        
        if (frames.length < 3) {
            return false;
        }
        
        for (int i = 0; i < frames.length - 1; i++) {
            Bitmap current = frames[i].getBitmap();
            Bitmap next = frames[i + 1].getBitmap();
            
            if (current.isRecycled() || next.isRecycled()) {
                continue;
            }
            
            int width = current.getWidth();
            int height = current.getHeight();
            
            // Health bars are typically in lower portions of the screen
            int startY = (int)(height * 0.8);
            int endY = height;
            
            int redGreenChanges = 0;
            int sampledPixels = 0;
            
            for (int x = 0; x < width; x += 4) {
                for (int y = startY; y < endY; y += 4) {
                    int pixel1 = current.getPixel(x, y);
                    int pixel2 = next.getPixel(x, y);
                    
                    // Extract red and green channels
                    int r1 = (pixel1 >> 16) & 0xFF;
                    int g1 = (pixel1 >> 8) & 0xFF;
                    int r2 = (pixel2 >> 16) & 0xFF;
                    int g2 = (pixel2 >> 8) & 0xFF;
                    
                    // Check for significant changes in red or green
                    if (Math.abs(r1 - r2) > 30 || Math.abs(g1 - g2) > 30) {
                        redGreenChanges++;
                    }
                    
                    sampledPixels++;
                }
            }
            
            double changePercentage = (double)redGreenChanges / sampledPixels;
            
            // If more than 5% of pixels in health bar region changed, consider it significant
            if (changePercentage > 0.05) {
                Log.d(TAG, "Detected potential health change: " + changePercentage + " change");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        synchronized (frameBuffer) {
            for (BufferedFrame frame : frameBuffer) {
                if (frame.getBitmap() != null && !frame.getBitmap().isRecycled()) {
                    frame.getBitmap().recycle();
                }
            }
            frameBuffer.clear();
        }
    }
}
