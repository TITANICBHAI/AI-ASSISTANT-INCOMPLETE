package com.aiassistant.core.ai.video;

import android.graphics.Rect;

/**
 * Represents a text element detected in a video frame.
 */
public class TextElement {
    // Text content
    private final String text;
    
    // Bounding rectangle
    private final Rect bounds;
    
    // Additional properties
    private float confidence = 1.0f;
    private String language = "en";
    private boolean isTitle = false;
    
    /**
     * Constructor
     * @param text Text content
     * @param bounds Bounding rectangle
     */
    public TextElement(String text, Rect bounds) {
        this.text = text;
        this.bounds = new Rect(bounds);
    }
    
    /**
     * Get text content
     * @return Text string
     */
    public String getText() {
        return text;
    }
    
    /**
     * Get text bounds
     * @return Bounding rectangle
     */
    public Rect getBounds() {
        return bounds;
    }
    
    /**
     * Get OCR confidence
     * @return Confidence score (0.0 - 1.0)
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set OCR confidence
     * @param confidence Confidence score
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Get detected language
     * @return Language code
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Set detected language
     * @param language Language code
     */
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * Check if this is a title element
     * @return True if title
     */
    public boolean isTitle() {
        return isTitle;
    }
    
    /**
     * Set title flag
     * @param isTitle Title flag
     */
    public void setTitle(boolean isTitle) {
        this.isTitle = isTitle;
    }
    
    /**
     * Get horizontal center position
     * @return Center X coordinate
     */
    public int getCenterX() {
        return bounds.centerX();
    }
    
    /**
     * Get vertical center position
     * @return Center Y coordinate
     */
    public int getCenterY() {
        return bounds.centerY();
    }
    
    /**
     * Check if the text contains a substring
     * @param substring The substring to search for
     * @return True if the substring is found
     */
    public boolean contains(String substring) {
        return text != null && text.contains(substring);
    }
    
    /**
     * Get element width
     * @return Width in pixels
     */
    public int getWidth() {
        return bounds.width();
    }
    
    /**
     * Get element height
     * @return Height in pixels
     */
    public int getHeight() {
        return bounds.height();
    }
    
    /**
     * Calculate distance to another text element
     * @param other The other element
     * @return Distance in pixels
     */
    public float distanceTo(TextElement other) {
        float dx = other.getCenterX() - getCenterX();
        float dy = other.getCenterY() - getCenterY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return "TextElement{" +
                "text='" + text + '\'' +
                ", bounds=" + bounds +
                ", confidence=" + confidence +
                ", language='" + language + '\'' +
                ", isTitle=" + isTitle +
                '}';
    }
}
