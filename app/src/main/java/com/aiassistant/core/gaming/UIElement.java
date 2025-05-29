package com.aiassistant.core.gaming;

import android.graphics.Rect;

/**
 * Represents a UI element in a game
 */
public class UIElement {
    // Element properties
    private String id;
    private String type;
    private Rect bounds;
    private String text;
    private boolean clickable;
    private float confidence;
    private String className;
    
    /**
     * Constructor
     * @param id Element ID
     * @param type Element type
     * @param bounds Element bounds
     */
    public UIElement(String id, String type, Rect bounds) {
        this.id = id;
        this.type = type;
        this.bounds = bounds;
        this.clickable = true;
        this.confidence = 1.0f;
    }
    
    /**
     * Get element ID
     * @return Element ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get element type
     * @return Element type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get element bounds
     * @return Element bounds
     */
    public Rect getBounds() {
        return bounds;
    }
    
    /**
     * Get element text
     * @return Element text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Set element text
     * @param text Element text
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Is element clickable
     * @return True if clickable
     */
    public boolean isClickable() {
        return clickable;
    }
    
    /**
     * Set element clickable
     * @param clickable Clickable state
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    
    /**
     * Get detection confidence
     * @return Confidence
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set detection confidence
     * @param confidence Confidence
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Get element class name
     * @return Class name
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Set element class name
     * @param className Class name
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * Get center X coordinate
     * @return Center X
     */
    public int getCenterX() {
        return bounds.centerX();
    }
    
    /**
     * Get center Y coordinate
     * @return Center Y
     */
    public int getCenterY() {
        return bounds.centerY();
    }
    
    /**
     * Check if element contains point
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if contains point
     */
    public boolean containsPoint(int x, int y) {
        return bounds.contains(x, y);
    }
    
    /**
     * Check if element has text
     * @param text Text to check
     * @return True if element has text
     */
    public boolean hasText(String text) {
        return this.text != null && this.text.contains(text);
    }
    
    /**
     * Check if element matches type
     * @param type Type to match
     * @return True if matches
     */
    public boolean isType(String type) {
        return this.type != null && this.type.equals(type);
    }
    
    @Override
    public String toString() {
        return "UIElement{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", bounds=" + bounds +
                ", text='" + text + '\'' +
                '}';
    }
}
