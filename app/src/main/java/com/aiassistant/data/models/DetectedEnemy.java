package com.aiassistant.data.models;

import android.graphics.Rect;

import java.io.Serializable;

/**
 * Represents a detected enemy in the game
 */
public class DetectedEnemy implements Serializable {
    
    private Rect bounds;
    private float confidence;
    private String type;
    private float threat;
    private int detectionMethod; // 1 = color, 2 = motion, 3 = UI, 4 = combined
    
    /**
     * Default constructor
     */
    public DetectedEnemy() {
        this.bounds = new Rect();
        this.confidence = 0.0f;
    }
    
    /**
     * Constructor with bounds and confidence
     * 
     * @param bounds The bounds
     * @param confidence The confidence
     */
    public DetectedEnemy(Rect bounds, float confidence) {
        this.bounds = bounds;
        this.confidence = confidence;
    }
    
    /**
     * Get the bounds
     * 
     * @return The bounds
     */
    public Rect getBounds() {
        return bounds;
    }
    
    /**
     * Set the bounds
     * 
     * @param bounds The bounds
     */
    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }
    
    /**
     * Get the confidence
     * 
     * @return The confidence
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set the confidence
     * 
     * @param confidence The confidence
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Get the type
     * 
     * @return The type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set the type
     * 
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the threat level
     * 
     * @return The threat level
     */
    public float getThreat() {
        return threat;
    }
    
    /**
     * Set the threat level
     * 
     * @param threat The threat level
     */
    public void setThreat(float threat) {
        this.threat = threat;
    }
    
    /**
     * Get the detection method
     * 
     * @return The detection method
     */
    public int getDetectionMethod() {
        return detectionMethod;
    }
    
    /**
     * Set the detection method
     * 
     * @param detectionMethod The detection method
     */
    public void setDetectionMethod(int detectionMethod) {
        this.detectionMethod = detectionMethod;
    }
    
    /**
     * Get the center X coordinate
     * 
     * @return The center X
     */
    public float getCenterX() {
        return bounds.exactCenterX();
    }
    
    /**
     * Get the center Y coordinate
     * 
     * @return The center Y
     */
    public float getCenterY() {
        return bounds.exactCenterY();
    }
    
    /**
     * Calculate distance to another enemy
     * 
     * @param other The other enemy
     * @return The distance
     */
    public float distanceTo(DetectedEnemy other) {
        float dx = getCenterX() - other.getCenterX();
        float dy = getCenterY() - other.getCenterY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate distance to a point
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The distance
     */
    public float distanceTo(float x, float y) {
        float dx = getCenterX() - x;
        float dy = getCenterY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Check if this enemy overlaps with another
     * 
     * @param other The other enemy
     * @return Whether they overlap
     */
    public boolean overlaps(DetectedEnemy other) {
        return Rect.intersects(this.bounds, other.bounds);
    }
}
