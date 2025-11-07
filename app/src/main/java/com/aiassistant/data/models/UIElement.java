package com.aiassistant.data.models;

import android.graphics.Rect;
import android.graphics.RectF;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

/**
 * Represents a UI element in the game - Room entity
 */
@Entity(tableName = "ui_elements")
@TypeConverters(Converters.class)
public class UIElement {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String elementId;
    private String screenId;
    private String type;
    private String text;
    private String className;
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean interactive;
    private boolean visible;
    private boolean isClickable;
    private boolean isEditable;
    private String imageContentDescription;
    
    @Ignore
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
     * @return The ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set ID
     * @param id The ID
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get element ID
     * @return The element ID
     */
    public String getElementId() {
        return elementId;
    }
    
    /**
     * Set element ID
     * @param elementId The element ID
     */
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    /**
     * Get screen ID
     * @return The screen ID
     */
    public String getScreenId() {
        return screenId;
    }
    
    /**
     * Set screen ID
     * @param screenId The screen ID
     */
    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }
    
    /**
     * Get type
     * @return The type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set type
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get class name
     * @return The class name
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Set class name
     * @param className The class name
     */
    public void setClassName(String className) {
        this.className = className;
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
     * @param visible Whether visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Check if clickable
     * @return True if clickable
     */
    public boolean isClickable() {
        return isClickable;
    }
    
    /**
     * Set clickable
     * @param clickable Whether clickable
     */
    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
    
    /**
     * Check if editable
     * @return True if editable
     */
    public boolean isEditable() {
        return isEditable;
    }
    
    /**
     * Set editable
     * @param editable Whether editable
     */
    public void setEditable(boolean editable) {
        isEditable = editable;
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
