package com.aiassistant.core.ai.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of video frame analysis.
 * Contains detected UI elements, text, features, and metadata.
 */
public class AnalysisResult {
    // Success flag and message
    private boolean success;
    private String message;
    
    // Analysis components
    private List<UIElement> uiElements = new ArrayList<>();
    private List<TextElement> textElements = new ArrayList<>();
    private List<GameElement> gameElements = new ArrayList<>();
    private float[] visualFeatures = new float[0];
    
    // Additional metadata
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * Constructor
     * @param success Whether analysis was successful
     * @param message Message describing the result
     */
    public AnalysisResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    /**
     * Check if analysis was successful
     * @return Success flag
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Set success flag
     * @param success New success value
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Get result message
     * @return Message describing the result
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Set result message
     * @param message New message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Get detected UI elements
     * @return List of UI elements
     */
    public List<UIElement> getUiElements() {
        return uiElements;
    }
    
    /**
     * Set UI elements
     * @param uiElements List of UI elements
     */
    public void setUiElements(List<UIElement> uiElements) {
        this.uiElements = uiElements != null ? uiElements : new ArrayList<>();
    }
    
    /**
     * Get detected text elements
     * @return List of text elements
     */
    public List<TextElement> getTextElements() {
        return textElements;
    }
    
    /**
     * Set text elements
     * @param textElements List of text elements
     */
    public void setTextElements(List<TextElement> textElements) {
        this.textElements = textElements != null ? textElements : new ArrayList<>();
    }
    
    /**
     * Get detected game elements
     * @return List of game elements
     */
    public List<GameElement> getGameElements() {
        return gameElements;
    }
    
    /**
     * Set game elements
     * @param gameElements List of game elements
     */
    public void setGameElements(List<GameElement> gameElements) {
        this.gameElements = gameElements != null ? gameElements : new ArrayList<>();
    }
    
    /**
     * Get extracted visual features
     * @return Feature vector
     */
    public float[] getVisualFeatures() {
        return visualFeatures;
    }
    
    /**
     * Set visual features
     * @param visualFeatures Feature vector
     */
    public void setVisualFeatures(float[] visualFeatures) {
        this.visualFeatures = visualFeatures != null ? visualFeatures : new float[0];
    }
    
    /**
     * Get metadata for a key
     * @param key Metadata key
     * @return Metadata value, or null if not found
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Set metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void setMetadata(String key, String value) {
        if (key != null && value != null) {
            metadata.put(key, value);
        }
    }
    
    /**
     * Get all metadata
     * @return Map of metadata key-value pairs
     */
    public Map<String, String> getAllMetadata() {
        return metadata;
    }
    
    /**
     * Check if a specific UI element type exists
     * @param elementType Element type to check for
     * @return True if elements of this type exist
     */
    public boolean hasElementType(String elementType) {
        for (UIElement element : uiElements) {
            if (element.getType().equals(elementType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Count elements of a specific type
     * @param elementType Element type to count
     * @return Number of elements of this type
     */
    public int countElementType(String elementType) {
        int count = 0;
        for (UIElement element : uiElements) {
            if (element.getType().equals(elementType)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Check if a specific text string is present
     * @param text Text to search for
     * @return True if the text is found
     */
    public boolean containsText(String text) {
        if (text == null) {
            return false;
        }
        
        for (TextElement element : textElements) {
            if (element.getText().contains(text)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "AnalysisResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", uiElements=" + uiElements.size() +
                ", textElements=" + textElements.size() +
                ", gameElements=" + gameElements.size() +
                ", metadata=" + metadata.size() +
                '}';
    }
}
