package com.aiassistant.core.gaming;

import android.graphics.Bitmap;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.UUID;

/**
 * Represents the current state of a game
 */
public class GameState {
    // Game state components
    private Bitmap screenImage;
    private AccessibilityNodeInfo rootNode;
    private String packageName;
    private String stateKey;
    private long timestamp;
    
    /**
     * Constructor with screen image and accessibility node
     * @param screenImage Screen image
     * @param rootNode Accessibility root node
     * @param packageName Current package name
     */
    public GameState(Bitmap screenImage, AccessibilityNodeInfo rootNode, String packageName) {
        this.screenImage = screenImage;
        this.rootNode = rootNode;
        this.packageName = packageName;
        this.timestamp = System.currentTimeMillis();
        this.stateKey = generateStateKey();
    }
    
    /**
     * Generate a state key based on state characteristics
     * @return State key
     */
    private String generateStateKey() {
        // In a real implementation, this would generate a key based on various
        // state characteristics to uniquely identify similar states
        return UUID.randomUUID().toString();
    }
    
    /**
     * Get the state key
     * @return State key
     */
    public String getStateKey() {
        return stateKey;
    }
    
    /**
     * Get the screen image
     * @return Screen image
     */
    public Bitmap getScreenImage() {
        return screenImage;
    }
    
    /**
     * Get the accessibility root node
     * @return Accessibility root node
     */
    public AccessibilityNodeInfo getRootNode() {
        return rootNode;
    }
    
    /**
     * Get the package name
     * @return Package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Get the timestamp
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
