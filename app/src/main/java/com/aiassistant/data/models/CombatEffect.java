package com.aiassistant.data.models;

import android.graphics.Rect;

/**
 * Represents a combat visual effect detected in the game
 */
public class CombatEffect {
    
    // Effect type constants
    public static final int TYPE_EXPLOSION = 0;
    public static final int TYPE_GUNFIRE = 1;
    public static final int TYPE_PROJECTILE = 2;
    public static final int TYPE_IMPACT = 3;
    public static final int TYPE_SMOKE = 4;
    public static final int TYPE_FLAME = 5;
    
    // Effect properties
    private int type;
    private Rect boundingBox;
    private long timestamp;
    private float intensity; // 0.0-1.0
    private float confidence; // Detection confidence 0.0-1.0
    
    /**
     * Default constructor
     */
    public CombatEffect() {
        this.timestamp = System.currentTimeMillis();
        this.intensity = 0.5f;
        this.confidence = 0.5f;
    }
    
    /**
     * Constructor with type and bounding box
     * @param type The effect type
     * @param boundingBox The effect bounding box
     */
    public CombatEffect(int type, Rect boundingBox) {
        this();
        this.type = type;
        this.boundingBox = boundingBox;
    }
    
    /**
     * Constructor with all parameters
     * @param type The effect type
     * @param boundingBox The effect bounding box
     * @param intensity The effect intensity
     * @param confidence The detection confidence
     */
    public CombatEffect(int type, Rect boundingBox, float intensity, float confidence) {
        this(type, boundingBox);
        this.intensity = intensity;
        this.confidence = confidence;
    }

    /**
     * Get effect type
     * @return The effect type
     */
    public int getType() {
        return type;
    }

    /**
     * Set effect type
     * @param type The effect type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Get bounding box
     * @return The effect bounding box
     */
    public Rect getBoundingBox() {
        return boundingBox;
    }

    /**
     * Set bounding box
     * @param boundingBox The effect bounding box
     */
    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Get timestamp
     * @return The effect timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp
     * @param timestamp The effect timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get intensity
     * @return The effect intensity (0.0-1.0)
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * Set intensity
     * @param intensity The effect intensity (0.0-1.0)
     */
    public void setIntensity(float intensity) {
        this.intensity = Math.min(1.0f, Math.max(0.0f, intensity));
    }

    /**
     * Get confidence
     * @return The detection confidence (0.0-1.0)
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * Set confidence
     * @param confidence The detection confidence (0.0-1.0)
     */
    public void setConfidence(float confidence) {
        this.confidence = Math.min(1.0f, Math.max(0.0f, confidence));
    }
    
    /**
     * Get effect age in milliseconds
     * @return The effect age
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Check if the effect is fresh (less than 500ms old)
     * @return True if the effect is fresh
     */
    public boolean isFresh() {
        return getAge() < 500;
    }
    
    /**
     * Get a string representation of the effect type
     * @return The effect type as a string
     */
    public String getTypeString() {
        switch (type) {
            case TYPE_EXPLOSION:
                return "Explosion";
            case TYPE_GUNFIRE:
                return "Gunfire";
            case TYPE_PROJECTILE:
                return "Projectile";
            case TYPE_IMPACT:
                return "Impact";
            case TYPE_SMOKE:
                return "Smoke";
            case TYPE_FLAME:
                return "Flame";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return getTypeString() + " at (" + 
                (boundingBox != null ? boundingBox.centerX() : "?") + ", " + 
                (boundingBox != null ? boundingBox.centerY() : "?") + "), " +
                "intensity=" + String.format("%.2f", intensity) + ", " +
                "confidence=" + String.format("%.2f", confidence);
    }
}
