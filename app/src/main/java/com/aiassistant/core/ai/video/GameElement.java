package com.aiassistant.core.ai.video;

import android.graphics.Rect;

/**
 * Represents a game element detected in a video frame.
 * This could be an enemy, a player, an item, or a UI element.
 */
public class GameElement {
    // Element types
    public static final String TYPE_ENEMY = "ENEMY";
    public static final String TYPE_PLAYER = "PLAYER";
    public static final String TYPE_WEAPON = "WEAPON";
    public static final String TYPE_ITEM = "ITEM";
    public static final String TYPE_HEALTH = "HEALTH";
    public static final String TYPE_BUTTON = "BUTTON";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_MINIMAP = "MINIMAP";
    public static final String TYPE_OBJECTIVE = "OBJECTIVE";
    public static final String TYPE_INTERACTIVE = "INTERACTIVE";
    public static final String TYPE_UNKNOWN = "UNKNOWN";
    
    // Element properties
    private final String type;
    private final Rect bounds;
    private final float confidence;
    
    /**
     * Constructor
     * @param type The type of element
     * @param bounds The bounding rectangle of the element
     * @param confidence The confidence score (0.0 - 1.0)
     */
    public GameElement(String type, Rect bounds, float confidence) {
        this.type = type;
        this.bounds = bounds;
        this.confidence = confidence;
    }
    
    /**
     * Get the element type
     * @return Element type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the element bounds
     * @return Bounding rectangle
     */
    public Rect getBounds() {
        return bounds;
    }
    
    /**
     * Get the detection confidence
     * @return Confidence score (0.0 - 1.0)
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Get the center X coordinate
     * @return Center X
     */
    public int getCenterX() {
        return bounds.centerX();
    }
    
    /**
     * Get the center Y coordinate
     * @return Center Y
     */
    public int getCenterY() {
        return bounds.centerY();
    }
    
    /**
     * Get the width of the element
     * @return Width in pixels
     */
    public int getWidth() {
        return bounds.width();
    }
    
    /**
     * Get the height of the element
     * @return Height in pixels
     */
    public int getHeight() {
        return bounds.height();
    }
    
    /**
     * Check if this element overlaps with another
     * @param other The other element
     * @return True if they overlap
     */
    public boolean overlaps(GameElement other) {
        return Rect.intersects(bounds, other.bounds);
    }
    
    /**
     * Calculate the distance to another element
     * @param other The other element
     * @return Pixel distance between centers
     */
    public float distanceTo(GameElement other) {
        float dx = other.getCenterX() - getCenterX();
        float dy = other.getCenterY() - getCenterY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return "GameElement{" +
                "type='" + type + '\'' +
                ", bounds=" + bounds +
                ", confidence=" + confidence +
                '}';
    }
}
