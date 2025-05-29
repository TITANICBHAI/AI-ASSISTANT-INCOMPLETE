package com.aiassistant.core.interaction;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import com.aiassistant.core.ai.recognition.ContextRecognitionManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls complex interactions with adaptive behavior based on context
 */
public class AdaptiveInteractionController {
    private static final String TAG = "AdptInteractionCtrl";
    
    // Components
    private final Context context;
    private final ContextRecognitionManager contextRecognitionManager;
    
    // Gesture storage
    private Map<String, ComplexGesture> savedGestures = new HashMap<>();
    private static final String GESTURE_FILE = "saved_gestures.dat";
    
    // Interaction patterns
    private Map<String, List<InteractionPattern>> interactionPatterns = new HashMap<>();
    
    // Statistics
    private Map<String, Integer> gestureUsageCount = new HashMap<>();
    private Map<String, Float> gestureSuccessRate = new HashMap<>();
    
    /**
     * Constructor
     * @param context Application context
     * @param contextRecognitionManager Context recognition manager
     */
    public AdaptiveInteractionController(Context context, ContextRecognitionManager contextRecognitionManager) {
        this.context = context;
        this.contextRecognitionManager = contextRecognitionManager;
        
        // Load saved gestures
        loadGestures();
    }
    
    /**
     * Load saved gestures
     */
    private void loadGestures() {
        try {
            FileInputStream fis = context.openFileInput(GESTURE_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            savedGestures = (Map<String, ComplexGesture>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            Log.w(TAG, "Could not load saved gestures", e);
            // No saved gestures yet
        }
    }
    
    /**
     * Save gestures
     */
    private void saveGestures() {
        try {
            FileOutputStream fos = context.openFileOutput(GESTURE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(savedGestures);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not save gestures", e);
        }
    }
    
    /**
     * Add a complex gesture
     * @param name Gesture name
     * @param gesture Complex gesture
     */
    public void addGesture(String name, ComplexGesture gesture) {
        savedGestures.put(name, gesture);
        saveGestures();
    }
    
    /**
     * Get a saved gesture
     * @param name Gesture name
     * @return Complex gesture or null
     */
    public ComplexGesture getGesture(String name) {
        return savedGestures.get(name);
    }
    
    /**
     * Get all saved gestures
     * @return Map of saved gestures
     */
    public Map<String, ComplexGesture> getAllGestures() {
        return new HashMap<>(savedGestures);
    }
    
    /**
     * Record an interaction pattern
     * @param contextId Context ID
     * @param pattern Interaction pattern
     */
    public void recordInteractionPattern(String contextId, InteractionPattern pattern) {
        if (!interactionPatterns.containsKey(contextId)) {
            interactionPatterns.put(contextId, new ArrayList<>());
        }
        
        interactionPatterns.get(contextId).add(pattern);
    }
    
    /**
     * Get recommended interaction for context
     * @param contextId Context ID
     * @param nodeInfo Accessibility node info
     * @return Recommended interaction or null
     */
    public InteractionPattern getRecommendedInteraction(String contextId, AccessibilityNodeInfo nodeInfo) {
        if (!interactionPatterns.containsKey(contextId)) {
            return null;
        }
        
        // Find most successful pattern for this context
        List<InteractionPattern> patterns = interactionPatterns.get(contextId);
        InteractionPattern bestPattern = null;
        float bestSuccessRate = 0;
        
        for (InteractionPattern pattern : patterns) {
            float successRate = getPatternSuccessRate(pattern);
            if (successRate > bestSuccessRate) {
                bestSuccessRate = successRate;
                bestPattern = pattern;
            }
        }
        
        return bestPattern;
    }
    
    /**
     * Get pattern success rate
     * @param pattern Interaction pattern
     * @return Success rate
     */
    private float getPatternSuccessRate(InteractionPattern pattern) {
        String patternId = pattern.getId();
        if (!gestureSuccessRate.containsKey(patternId)) {
            return 0.5f; // Default 50% success rate
        }
        
        return gestureSuccessRate.get(patternId);
    }
    
    /**
     * Record interaction result
     * @param patternId Pattern ID
     * @param success Whether interaction was successful
     */
    public void recordInteractionResult(String patternId, boolean success) {
        // Update usage count
        int usageCount = gestureUsageCount.getOrDefault(patternId, 0) + 1;
        gestureUsageCount.put(patternId, usageCount);
        
        // Update success rate
        float currentRate = gestureSuccessRate.getOrDefault(patternId, 0.5f);
        float alpha = 1.0f / usageCount; // Learning rate decreases with usage
        float newRate = (1 - alpha) * currentRate + alpha * (success ? 1 : 0);
        gestureSuccessRate.put(patternId, newRate);
    }
    
    /**
     * Find UI element by description
     * @param nodeInfo Root node
     * @param description Description to find
     * @return Matching node or null
     */
    public AccessibilityNodeInfo findElementByDescription(AccessibilityNodeInfo nodeInfo, String description) {
        if (nodeInfo == null) {
            return null;
        }
        
        if (nodeInfo.getContentDescription() != null && 
                nodeInfo.getContentDescription().toString().contains(description)) {
            return AccessibilityNodeInfo.obtain(nodeInfo);
        }
        
        // Recursively search children
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            AccessibilityNodeInfo result = findElementByDescription(child, description);
            if (result != null) {
                return result;
            }
            child.recycle();
        }
        
        return null;
    }
    
    /**
     * Find UI element by text
     * @param nodeInfo Root node
     * @param text Text to find
     * @return Matching node or null
     */
    public AccessibilityNodeInfo findElementByText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return null;
        }
        
        if (nodeInfo.getText() != null && 
                nodeInfo.getText().toString().contains(text)) {
            return AccessibilityNodeInfo.obtain(nodeInfo);
        }
        
        // Recursively search children
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            AccessibilityNodeInfo result = findElementByText(child, text);
            if (result != null) {
                return result;
            }
            child.recycle();
        }
        
        return null;
    }
    
    /**
     * Reset controller
     */
    public void reset() {
        interactionPatterns.clear();
        gestureUsageCount.clear();
        gestureSuccessRate.clear();
    }
    
    /**
     * Complex gesture class
     */
    public static class ComplexGesture implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private final List<GestureStep> steps;
        private final String description;
        
        public ComplexGesture(List<GestureStep> steps, String description) {
            this.steps = steps;
            this.description = description;
        }
        
        public List<GestureStep> getSteps() {
            return steps;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Gesture step class
     */
    public static class GestureStep implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private final GestureType type;
        private final int x;
        private final int y;
        private final int duration;
        private final String text;
        
        public GestureStep(GestureType type, int x, int y, int duration) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.duration = duration;
            this.text = null;
        }
        
        public GestureStep(String text) {
            this.type = GestureType.TEXT;
            this.x = 0;
            this.y = 0;
            this.duration = 0;
            this.text = text;
        }
        
        public GestureType getType() {
            return type;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getDuration() {
            return duration;
        }
        
        public String getText() {
            return text;
        }
    }
    
    /**
     * Gesture type enum
     */
    public enum GestureType {
        TAP,
        LONG_PRESS,
        SWIPE,
        TEXT,
        WAIT
    }
    
    /**
     * Interaction pattern class
     */
    public static class InteractionPattern implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String id;
        private final String contextId;
        private final List<ComplexGesture> gestures;
        
        public InteractionPattern(String id, String contextId, List<ComplexGesture> gestures) {
            this.id = id;
            this.contextId = contextId;
            this.gestures = gestures;
        }
        
        public String getId() {
            return id;
        }
        
        public String getContextId() {
            return contextId;
        }
        
        public List<ComplexGesture> getGestures() {
            return gestures;
        }
    }
}
