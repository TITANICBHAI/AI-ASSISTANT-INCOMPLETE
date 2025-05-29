
package com.aiassistant.core.gaming.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.gaming.GameEntity;
import com.aiassistant.core.gaming.GameState;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Vision enhancement manager that coordinates all vision-related improvements:
 * - Frame buffer analysis for temporal continuity
 * - Event-based capture for critical moments
 * - Predictive vision for entity movement forecasting
 * - High FPS capture optimization
 * - Multi-frame analysis for enhanced detection
 */
public class VisionEnhancementManager implements 
        HighFPSCaptureManager.CaptureListener,
        FrameBufferAnalyzer.FrameBufferListener,
        EventBasedCapture.CaptureEventListener {
    
    private static final String TAG = "VisionEnhancementManager";
    
    // Vision components
    private final HighFPSCaptureManager captureManager;
    private final FrameBufferAnalyzer frameBufferAnalyzer;
    private final EventBasedCapture eventBasedCapture;
    private final PredictiveVisionModel predictiveVisionModel;
    private final MultiFrameAnalyzer multiFrameAnalyzer;
    
    // Processing
    private final ExecutorService processingExecutor;
    private final Handler mainHandler;
    
    // State
    private boolean isRunning = false;
    private int frameCount = 0;
    private GameState currentGameState;
    
    // Context
    private final Context context;
    
    // Listeners
    private final List<VisionEnhancementListener> listeners = new ArrayList<>();
    
    /**
     * Interface for vision enhancement events
     */
    public interface VisionEnhancementListener {
        void onGameStateUpdated(GameState enhancedState, Bitmap currentFrame);
        void onEntityPrediction(String entityId, float predictedX, float predictedY, long predictionTimeMs);
        void onCriticalEvent(String eventType, Bitmap eventFrame);
        void onFpsChanged(float currentFps, int captureMode);
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public VisionEnhancementManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Initialize components
        this.captureManager = new HighFPSCaptureManager(context);
        this.frameBufferAnalyzer = new FrameBufferAnalyzer(10);
        this.eventBasedCapture = new EventBasedCapture(context);
        this.predictiveVisionModel = new PredictiveVisionModel();
        this.multiFrameAnalyzer = new MultiFrameAnalyzer();
        
        // Initialize processing
        this.processingExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Set up listeners
        this.captureManager.addListener(this);
        this.frameBufferAnalyzer.setListener(this);
        this.eventBasedCapture.addListener(this);
        
        // Initialize state
        this.currentGameState = new GameState();
    }
    
    /**
     * Add vision enhancement listener
     * @param listener Listener to add
     */
    public void addListener(VisionEnhancementListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove vision enhancement listener
     * @param listener Listener to remove
     */
    public void removeListener(VisionEnhancementListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Start vision enhancement
     * @param mediaProjection Media projection for screen capture
     */
    public void start(MediaProjection mediaProjection) {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        
        // Start high FPS capture
        captureManager.startCapture(mediaProjection);
        
        Log.d(TAG, "Vision enhancement started");
    }
    
    /**
     * Stop vision enhancement
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Stop capture
        captureManager.stopCapture();
        
        // Clean up resources
        frameBufferAnalyzer.cleanup();
        multiFrameAnalyzer.cleanup();
        
        Log.d(TAG, "Vision enhancement stopped");
    }
    
    /**
     * Set capture priority mode
     * @param mode Capture mode
     */
    public void setCaptureMode(int mode) {
        captureManager.setCaptureMode(mode);
        
        // Notify listeners of FPS change
        for (VisionEnhancementListener listener : listeners) {
            listener.onFpsChanged(captureManager.getCurrentFps(), mode);
        }
    }
    
    /**
     * Get current capture mode
     * @return Capture mode
     */
    public int getCaptureMode() {
        return captureManager.getCaptureMode();
    }
    
    /**
     * Get current FPS
     * @return Current FPS
     */
    public float getCurrentFps() {
        return captureManager.getCurrentFps();
    }
    
    /**
     * Set game state
     * @param gameState Current game state
     */
    public void setGameState(GameState gameState) {
        this.currentGameState = gameState;
    }
    
    /**
     * Implementation of HighFPSCaptureManager.CaptureListener
     */
    @Override
    public void onFrameCaptured(Bitmap bitmap, long timestamp, float fps) {
        if (!isRunning || bitmap == null || bitmap.isRecycled()) {
            return;
        }
        
        // Create a copy for processing
        final Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
        final GameState gameStateCopy = currentGameState.copy();
        final long captureTime = timestamp;
        
        // Process frame on background thread
        processingExecutor.execute(() -> {
            try {
                // Add to buffer
                frameBufferAnalyzer.addFrame(bitmapCopy, captureTime, frameCount++);
                
                // Process for event detection
                eventBasedCapture.processFrame(bitmapCopy, gameStateCopy, captureTime);
                
                // Add to multi-frame analyzer
                multiFrameAnalyzer.addFrame(bitmapCopy, gameStateCopy, captureTime);
                
                // Process every 3rd frame for predictions (to reduce processing load)
                if (frameCount % 3 == 0) {
                    processFrameForPredictions(bitmapCopy, gameStateCopy, captureTime);
                }
                
                // Every 5th frame, generate enhanced game state
                if (frameCount % 5 == 0) {
                    GameState enhancedState = multiFrameAnalyzer.analyzeSequence();
                    
                    if (enhancedState != null) {
                        mainHandler.post(() -> {
                            for (VisionEnhancementListener listener : listeners) {
                                listener.onGameStateUpdated(enhancedState, bitmapCopy);
                            }
                        });
                    }
                }
                
                // Every 30th frame, update FPS info
                if (frameCount % 30 == 0) {
                    mainHandler.post(() -> {
                        for (VisionEnhancementListener listener : listeners) {
                            listener.onFpsChanged(fps, captureManager.getCaptureMode());
                        }
                    });
                }
                
                // Don't recycle bitmap here - it's passed to listeners
            } catch (Exception e) {
                Log.e(TAG, "Error processing frame: " + e.getMessage());
                
                // Ensure bitmap is recycled on error
                if (bitmapCopy != null && !bitmapCopy.isRecycled()) {
                    bitmapCopy.recycle();
                }
            }
        });
    }
    
    /**
     * Process frame for entity predictions
     * @param bitmap Frame bitmap
     * @param gameState Game state
     * @param timestamp Timestamp
     */
    private void processFrameForPredictions(Bitmap bitmap, GameState gameState, long timestamp) {
        // Track entities for prediction
        if (gameState.getEntities() != null) {
            for (GameEntity entity : gameState.getEntities()) {
                if (entity.getType() == GameEntity.TYPE_ENEMY) {
                    // Track position for prediction
                    android.graphics.PointF position = new android.graphics.PointF(
                        entity.getX() + entity.getWidth() / 2,
                        entity.getY() + entity.getHeight() / 2
                    );
                    
                    predictiveVisionModel.trackEntityPosition(
                        entity.getId(), position, timestamp, entity.getConfidence());
                    
                    // Generate prediction for 200ms in the future
                    android.graphics.PointF prediction = predictiveVisionModel.predictPosition(
                        entity.getId(), 200);
                    
                    if (prediction != null) {
                        final String entityId = entity.getId();
                        final float predictedX = prediction.x;
                        final float predictedY = prediction.y;
                        
                        mainHandler.post(() -> {
                            for (VisionEnhancementListener listener : listeners) {
                                listener.onEntityPrediction(entityId, predictedX, predictedY, 200);
                            }
                        });
                    }
                }
            }
        }
    }
    
    /**
     * Implementation of HighFPSCaptureManager.CaptureListener
     */
    @Override
    public void onCaptureError(String errorMessage) {
        Log.e(TAG, "Capture error: " + errorMessage);
    }
    
    /**
     * Implementation of FrameBufferAnalyzer.FrameBufferListener
     */
    @Override
    public void onSignificantEvent(FrameBufferAnalyzer.BufferedFrame[] relevantFrames, String eventType) {
        // Temporarily increase capture rate for significant events
        int previousMode = captureManager.getCaptureMode();
        
        // Set high priority mode
        captureManager.setCaptureMode(HighFPSCaptureManager.MODE_HIGH_PRIORITY);
        
        // Schedule return to previous mode after 1 second
        mainHandler.postDelayed(() -> 
            captureManager.setCaptureMode(previousMode), 1000);
        
        // Notify listeners
        if (relevantFrames.length > 0) {
            FrameBufferAnalyzer.BufferedFrame keyFrame = relevantFrames[relevantFrames.length / 2];
            Bitmap eventBitmap = keyFrame.getBitmap().copy(keyFrame.getBitmap().getConfig(), false);
            
            mainHandler.post(() -> {
                for (VisionEnhancementListener listener : listeners) {
                    listener.onCriticalEvent(eventType, eventBitmap);
                }
            });
        }
    }
    
    /**
     * Implementation of EventBasedCapture.CaptureEventListener
     */
    @Override
    public void onEventDetected(String eventType, Bitmap capturedFrame, long timestamp) {
        // Increase capture rate temporarily
        int previousMode = captureManager.getCaptureMode();
        captureManager.setCaptureMode(HighFPSCaptureManager.MODE_HIGH_PRIORITY);
        
        // Schedule return to previous mode
        mainHandler.postDelayed(() -> 
            captureManager.setCaptureMode(previousMode), 1000);
        
        // Notify listeners
        if (capturedFrame != null && !capturedFrame.isRecycled()) {
            Bitmap eventBitmap = capturedFrame.copy(capturedFrame.getConfig(), false);
            
            mainHandler.post(() -> {
                for (VisionEnhancementListener listener : listeners) {
                    listener.onCriticalEvent(eventType, eventBitmap);
                }
            });
        }
    }
    
    /**
     * Implementation of EventBasedCapture.CaptureEventListener
     */
    @Override
    public void onBurstCaptureStarted(String eventType) {
        // Already handled in onEventDetected
    }
    
    /**
     * Implementation of EventBasedCapture.CaptureEventListener
     */
    @Override
    public void onBurstCaptureCompleted(String eventType, List<Bitmap> frames) {
        // Return to normal capture rate
        captureManager.setCaptureMode(HighFPSCaptureManager.MODE_ACTIVE);
    }
}
