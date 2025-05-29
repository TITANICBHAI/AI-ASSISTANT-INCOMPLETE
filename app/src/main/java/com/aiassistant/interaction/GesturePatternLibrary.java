package com.aiassistant.interaction;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library for gesture pattern recognition and management.
 * Handles the detection, learning, and matching of touch gesture patterns.
 */
public class GesturePatternLibrary {
    private static final String TAG = "GesturePatternLibrary";
    
    // Gesture types
    public static final String GESTURE_TAP = "TAP";
    public static final String GESTURE_DOUBLE_TAP = "DOUBLE_TAP";
    public static final String GESTURE_LONG_PRESS = "LONG_PRESS";
    public static final String GESTURE_SWIPE_UP = "SWIPE_UP";
    public static final String GESTURE_SWIPE_DOWN = "SWIPE_DOWN";
    public static final String GESTURE_SWIPE_LEFT = "SWIPE_LEFT";
    public static final String GESTURE_SWIPE_RIGHT = "SWIPE_RIGHT";
    public static final String GESTURE_PINCH = "PINCH";
    public static final String GESTURE_SPREAD = "SPREAD";
    public static final String GESTURE_DRAG = "DRAG";
    
    // Gesture detection configuration
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    
    // Context
    private Context context;
    private boolean isRunning = false;
    
    // Gesture detection
    private GestureDetector gestureDetector;
    private String lastRecognizedGestureType = null;
    private float lastRecognizedGestureConfidence = 0.0f;
    
    // Gesture pattern storage
    private Map<String, List<GesturePattern>> storedPatterns;
    
    /**
     * Inner class to represent a gesture pattern
     */
    private static class GesturePattern {
        String type;
        List<MotionEvent> sampleEvents;
        float confidence;
        
        GesturePattern(String type) {
            this.type = type;
            this.sampleEvents = new ArrayList<>();
            this.confidence = 1.0f;
        }
    }
    
    /**
     * Custom gesture detector listener
     */
    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            lastRecognizedGestureType = GESTURE_TAP;
            lastRecognizedGestureConfidence = 1.0f;
            Log.d(TAG, "Gesture detected: TAP");
            return true;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            lastRecognizedGestureType = GESTURE_DOUBLE_TAP;
            lastRecognizedGestureConfidence = 1.0f;
            Log.d(TAG, "Gesture detected: DOUBLE_TAP");
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            lastRecognizedGestureType = GESTURE_LONG_PRESS;
            lastRecognizedGestureConfidence = 1.0f;
            Log.d(TAG, "Gesture detected: LONG_PRESS");
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // More horizontal than vertical movement
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Right swipe
                            lastRecognizedGestureType = GESTURE_SWIPE_RIGHT;
                        } else {
                            // Left swipe
                            lastRecognizedGestureType = GESTURE_SWIPE_LEFT;
                        }
                        lastRecognizedGestureConfidence = calculateConfidence(diffX, velocityX);
                        result = true;
                    }
                } else {
                    // More vertical than horizontal movement
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // Down swipe
                            lastRecognizedGestureType = GESTURE_SWIPE_DOWN;
                        } else {
                            // Up swipe
                            lastRecognizedGestureType = GESTURE_SWIPE_UP;
                        }
                        lastRecognizedGestureConfidence = calculateConfidence(diffY, velocityY);
                        result = true;
                    }
                }
                
                if (result) {
                    Log.d(TAG, "Gesture detected: " + lastRecognizedGestureType + 
                          " (confidence: " + lastRecognizedGestureConfidence + ")");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error detecting swipe gesture", e);
            }
            return result;
        }
        
        /**
         * Calculate confidence for a swipe gesture based on distance and velocity
         */
        private float calculateConfidence(float distance, float velocity) {
            // Scale confidence based on distance and velocity
            float distanceConfidence = Math.min(1.0f, Math.abs(distance) / (SWIPE_THRESHOLD * 2));
            float velocityConfidence = Math.min(1.0f, Math.abs(velocity) / (SWIPE_VELOCITY_THRESHOLD * 2));
            
            // Combine the two factors with more weight on distance
            return (distanceConfidence * 0.7f) + (velocityConfidence * 0.3f);
        }
    }
    
    /**
     * Constructor
     */
    public GesturePatternLibrary() {
        storedPatterns = new HashMap<>();
        
        // Initialize patterns collections for each type
        storedPatterns.put(GESTURE_TAP, new ArrayList<>());
        storedPatterns.put(GESTURE_DOUBLE_TAP, new ArrayList<>());
        storedPatterns.put(GESTURE_LONG_PRESS, new ArrayList<>());
        storedPatterns.put(GESTURE_SWIPE_UP, new ArrayList<>());
        storedPatterns.put(GESTURE_SWIPE_DOWN, new ArrayList<>());
        storedPatterns.put(GESTURE_SWIPE_LEFT, new ArrayList<>());
        storedPatterns.put(GESTURE_SWIPE_RIGHT, new ArrayList<>());
        storedPatterns.put(GESTURE_PINCH, new ArrayList<>());
        storedPatterns.put(GESTURE_SPREAD, new ArrayList<>());
        storedPatterns.put(GESTURE_DRAG, new ArrayList<>());
    }
    
    /**
     * Initializes the gesture pattern library
     * @param context Application context
     */
    public void initialize(Context context) {
        this.context = context;
        this.gestureDetector = new GestureDetector(context, new CustomGestureListener());
        
        // Configure gesture detector
        gestureDetector.setIsLongpressEnabled(true);
        
        Log.i(TAG, "GesturePatternLibrary initialized");
    }
    
    /**
     * Starts the gesture pattern library
     */
    public void start() {
        if (isRunning) {
            Log.w(TAG, "GesturePatternLibrary already running");
            return;
        }
        
        isRunning = true;
        Log.i(TAG, "GesturePatternLibrary started");
    }
    
    /**
     * Stops the gesture pattern library
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        Log.i(TAG, "GesturePatternLibrary stopped");
    }
    
    /**
     * Processes a touch event for gesture recognition
     * @param event The touch event to process
     * @return true if the event was recognized as a gesture, false otherwise
     */
    public boolean processTouchEvent(MotionEvent event) {
        if (!isRunning) {
            return false;
        }
        
        // Reset last recognition on new gesture start
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastRecognizedGestureType = null;
            lastRecognizedGestureConfidence = 0.0f;
        }
        
        // Let the gesture detector process the event
        boolean handled = gestureDetector.onTouchEvent(event);
        
        // Handle multi-touch gestures separately
        if (event.getPointerCount() > 1) {
            handled = handleMultiTouchGesture(event) || handled;
        }
        
        return handled;
    }
    
    /**
     * Handle multi-touch gestures like pinch and spread
     * @param event The motion event
     * @return true if a multi-touch gesture was recognized
     */
    private boolean handleMultiTouchGesture(MotionEvent event) {
        // This is a simplified implementation - a real one would track
        // the full gesture sequence to detect pinch/spread/rotate
        
        // For this example, we'll just detect the start of a multi-touch
        if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            lastRecognizedGestureType = GESTURE_PINCH; // Simplified - would need to determine if pinch or spread
            lastRecognizedGestureConfidence = 0.7f;
            Log.d(TAG, "Multi-touch gesture detected: " + lastRecognizedGestureType);
            return true;
        }
        
        return false;
    }
    
    /**
     * Adds a new pattern for recognition
     * @param type The gesture type
     * @param sampleEvents The sample motion events that define the pattern
     */
    public void addGesturePattern(String type, List<MotionEvent> sampleEvents) {
        if (!storedPatterns.containsKey(type)) {
            Log.w(TAG, "Unknown gesture type: " + type);
            return;
        }
        
        GesturePattern pattern = new GesturePattern(type);
        pattern.sampleEvents.addAll(sampleEvents);
        storedPatterns.get(type).add(pattern);
        
        Log.i(TAG, "Added new gesture pattern for: " + type);
    }
    
    /**
     * Gets the type of the last recognized gesture
     * @return The gesture type, or null if none recognized
     */
    public String getLastRecognizedGestureType() {
        return lastRecognizedGestureType;
    }
    
    /**
     * Gets the confidence of the last recognized gesture
     * @return The confidence level (0.0-1.0)
     */
    public float getLastRecognizedGestureConfidence() {
        return lastRecognizedGestureConfidence;
    }
    
    /**
     * Gets the number of stored patterns for a gesture type
     * @param type The gesture type
     * @return The number of patterns stored
     */
    public int getPatternCount(String type) {
        if (!storedPatterns.containsKey(type)) {
            return 0;
        }
        return storedPatterns.get(type).size();
    }
    
    /**
     * Clears all stored patterns for a gesture type
     * @param type The gesture type to clear
     */
    public void clearPatterns(String type) {
        if (storedPatterns.containsKey(type)) {
            storedPatterns.get(type).clear();
            Log.i(TAG, "Cleared patterns for gesture type: " + type);
        }
    }
}
