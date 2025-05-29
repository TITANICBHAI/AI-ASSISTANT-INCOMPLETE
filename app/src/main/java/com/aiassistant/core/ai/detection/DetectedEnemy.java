package com.aiassistant.core.ai.detection;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Class representing a detected enemy in the game
 */
public class DetectedEnemy {
    private long id;
    private String type;
    private float x;
    private float y;
    private float width;
    private float height;
    private float confidence;
    private boolean visible;
    private long detectionTime;
    private Rect boundingBox;
    
    /**
     * Constructor
     * @param id ID
     * @param type Type
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     */
    public DetectedEnemy(long id, String type, float x, float y, float width, float height) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.confidence = 0.0f;
        this.visible = true;
        this.detectionTime = System.currentTimeMillis();
        this.boundingBox = new Rect(
            (int)x,
            (int)y,
            (int)(x + width),
            (int)(y + height)
        );
    }
    
    /**
     * Get ID
     * @return ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set ID
     * @param id ID
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get type
     * @return Type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set type
     * @param type Type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get X coordinate
     * @return X coordinate
     */
    public float getX() {
        return x;
    }
    
    /**
     * Set X coordinate
     * @param x X coordinate
     */
    public void setX(float x) {
        this.x = x;
        updateBoundingBox();
    }
    
    /**
     * Get Y coordinate
     * @return Y coordinate
     */
    public float getY() {
        return y;
    }
    
    /**
     * Set Y coordinate
     * @param y Y coordinate
     */
    public void setY(float y) {
        this.y = y;
        updateBoundingBox();
    }
    
    /**
     * Get width
     * @return Width
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Set width
     * @param width Width
     */
    public void setWidth(float width) {
        this.width = width;
        updateBoundingBox();
    }
    
    /**
     * Get height
     * @return Height
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Set height
     * @param height Height
     */
    public void setHeight(float height) {
        this.height = height;
        updateBoundingBox();
    }
    
    /**
     * Get confidence
     * @return Confidence
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set confidence
     * @param confidence Confidence
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Check if visible
     * @return True if visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Set visibility
     * @param visible Visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Get detection time
     * @return Detection time
     */
    public long getDetectionTime() {
        return detectionTime;
    }
    
    /**
     * Set detection time
     * @param detectionTime Detection time
     */
    public void setDetectionTime(long detectionTime) {
        this.detectionTime = detectionTime;
    }
    
    /**
     * Get bounding box
     * @return Bounding box
     */
    public Rect getBoundingBox() {
        return boundingBox;
    }
    
    /**
     * Get bounding box as RectF
     * @return Bounding box as RectF
     */
    public RectF getBoundingBoxF() {
        return new RectF(x, y, x + width, y + height);
    }
    
    /**
     * Check if point is inside enemy
     * @param pointX Point X coordinate
     * @param pointY Point Y coordinate
     * @return True if point is inside
     */
    public boolean containsPoint(float pointX, float pointY) {
        return pointX >= x && pointX <= (x + width) && pointY >= y && pointY <= (y + height);
    }
    
    /**
     * Update bounding box based on coordinates
     */
    private void updateBoundingBox() {
        this.boundingBox = new Rect(
            (int)x,
            (int)y,
            (int)(x + width),
            (int)(y + height)
        );
    }
}
