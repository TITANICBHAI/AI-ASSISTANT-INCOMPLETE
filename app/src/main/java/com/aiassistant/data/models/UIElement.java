package com.aiassistant.data.models;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Represents a UI element in the game
 */
public class UIElement {
    
    private long id;
    private String type;
    private String text;
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean interactive;
    private boolean visible;
    private String imageContentDescription;
    private Rect boundingBox;
    
    /**
     * Default constructor
     */
    public UIElement() {
        this.interactive = false;
        this.visible = true;
    }
    
    /**
     * Constructor with position and size
     * 
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param width The width
     * @param height The height
     */
    public UIElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.interactive = false;
        this.visible = true;
        this.boundingBox = new Rect(
            (int)x,
            (int)y,
            (int)(x + width),
            (int)(y + height)
        );
    }
    
    /**
     * Get ID
     * 
     * @return The ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set ID
     * 
     * @param id The ID
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get type
     * 
     * @return The type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set type
     * 
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get text
     * 
     * @return The text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Set text
     * 
     * @param text The text
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Get X coordinate
     * 
     * @return The X coordinate
     */
    public float getX() {
        return x;
    }
    
    /**
     * Set X coordinate
     * 
     * @param x The X coordinate
     */
    public void setX(float x) {
        this.x = x;
        updateBoundingBox();
    }
    
    /**
     * Get Y coordinate
     * 
     * @return The Y coordinate
     */
    public float getY() {
        return y;
    }
    
    /**
     * Set Y coordinate
     * 
     * @param y The Y coordinate
     */
    public void setY(float y) {
        this.y = y;
        updateBoundingBox();
    }
    
    /**
     * Get width
     * 
     * @return The width
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Set width
     * 
     * @param width The width
     */
    public void setWidth(float width) {
        this.width = width;
        updateBoundingBox();
    }
    
    /**
     * Get height
     * 
     * @return The height
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Set height
     * 
     * @param height The height
     */
    public void setHeight(float height) {
        this.height = height;
        updateBoundingBox();
    }
    
    /**
     * Check if interactive
     * 
     * @return True if interactive
     */
    public boolean isInteractive() {
        return interactive;
    }
    
    /**
     * Set interactive
     * 
     * @param interactive Whether interactive
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
    
    /**
     * Check if visible
     * 
     * @return True if visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Set visible
     * 
     * @param visible Whether visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Get image content description
     * 
     * @return The image content description
     */
    public String getImageContentDescription() {
        return imageContentDescription;
    }
    
    /**
     * Set image content description
     * 
     * @param imageContentDescription The image content description
     */
    public void setImageContentDescription(String imageContentDescription) {
        this.imageContentDescription = imageContentDescription;
    }
    
    /**
     * Get bounding box
     * 
     * @return The bounding box
     */
    public Rect getBoundingBox() {
        if (boundingBox == null) {
            updateBoundingBox();
        }
        return boundingBox;
    }
    
    /**
     * Get center X coordinate
     * 
     * @return The center X coordinate
     */
    public float getCenterX() {
        return x + (width / 2);
    }
    
    /**
     * Get center Y coordinate
     * 
     * @return The center Y coordinate
     */
    public float getCenterY() {
        return y + (height / 2);
    }
    
    /**
     * Check if point is inside element
     * 
     * @param pointX The X coordinate of the point
     * @param pointY The Y coordinate of the point
     * @return True if point is inside
     */
    public boolean containsPoint(float pointX, float pointY) {
        return pointX >= x && pointX <= (x + width) && 
               pointY >= y && pointY <= (y + height);
    }
    
    /**
     * Check if bounding box overlaps with this element
     * 
     * @param rect The bounding box to check
     * @return True if overlaps
     */
    public boolean overlaps(Rect rect) {
        if (boundingBox == null) {
            updateBoundingBox();
        }
        
        return Rect.intersects(boundingBox, rect);
    }
    
    /**
     * Check if bounding box overlaps with this element
     * 
     * @param rect The bounding box to check
     * @return True if overlaps
     */
    public boolean overlaps(RectF rect) {
        return rect.intersects(x, y, x + width, y + height);
    }
    
    /**
     * Update bounding box
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
