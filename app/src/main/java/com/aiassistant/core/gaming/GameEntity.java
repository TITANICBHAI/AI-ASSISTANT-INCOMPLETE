package com.aiassistant.core.gaming;

import java.io.Serializable;

/**
 * Represents an entity in a game, such as a player, enemy, item, or environment element.
 */
public class GameEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Entity types
    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_ENEMY = 1;
    public static final int TYPE_ITEM = 2;
    public static final int TYPE_ENVIRONMENT = 3;
    
    // Entity identification
    private int id;
    private int type;
    
    // Position and dimensions
    private int x;
    private int y;
    private int width;
    private int height;
    
    // Movement
    private int velocityX;
    private int velocityY;
    
    // Detection confidence
    private float confidence;
    
    // Tracking metadata
    private long lastSeen;
    
    // Additional properties
    private String label;
    private int color;
    
    /**
     * Default constructor
     */
    public GameEntity() {
        this.type = TYPE_ENVIRONMENT; // Default to environment
        this.confidence = 1.0f;
        this.lastSeen = System.currentTimeMillis();
    }
    
    /**
     * Constructor with type
     * @param type Entity type
     */
    public GameEntity(int type) {
        this.type = type;
        this.confidence = 1.0f;
        this.lastSeen = System.currentTimeMillis();
    }
    
    /**
     * Constructor with position
     * @param type Entity type
     * @param x X coordinate
     * @param y Y coordinate
     */
    public GameEntity(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.confidence = 1.0f;
        this.lastSeen = System.currentTimeMillis();
    }
    
    /**
     * Get entity ID
     * @return Entity ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set entity ID
     * @param id Entity ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get entity type
     * @return Entity type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Set entity type
     * @param type Entity type
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Get X coordinate
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Set X coordinate
     * @param x X coordinate
     */
    public void setX(int x) {
        this.x = x;
    }
    
    /**
     * Get Y coordinate
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Set Y coordinate
     * @param y Y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Get width
     * @return Width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Set width
     * @param width Width
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * Get height
     * @return Height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Set height
     * @param height Height
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    /**
     * Get X velocity
     * @return X velocity
     */
    public int getVelocityX() {
        return velocityX;
    }
    
    /**
     * Set X velocity
     * @param velocityX X velocity
     */
    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }
    
    /**
     * Get Y velocity
     * @return Y velocity
     */
    public int getVelocityY() {
        return velocityY;
    }
    
    /**
     * Set Y velocity
     * @param velocityY Y velocity
     */
    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
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
     * Get last seen timestamp
     * @return Last seen timestamp
     */
    public long getLastSeen() {
        return lastSeen;
    }
    
    /**
     * Set last seen timestamp
     * @param lastSeen Last seen timestamp
     */
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    /**
     * Get label
     * @return Label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Set label
     * @param label Label
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Get color
     * @return Color
     */
    public int getColor() {
        return color;
    }
    
    /**
     * Set color
     * @param color Color
     */
    public void setColor(int color) {
        this.color = color;
    }
    
    /**
     * Check if this entity is colliding with another entity
     * @param other Other entity
     * @return True if colliding
     */
    public boolean isColliding(GameEntity other) {
        if (other == null) {
            return false;
        }
        
        // Check for intersection of bounding boxes
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }
    
    /**
     * Check if a point is within this entity
     * @param pointX Point X coordinate
     * @param pointY Point Y coordinate
     * @return True if point is within entity
     */
    public boolean containsPoint(int pointX, int pointY) {
        return pointX >= x && pointX < x + width &&
               pointY >= y && pointY < y + height;
    }
    
    /**
     * Calculate distance to another entity
     * @param other Other entity
     * @return Distance
     */
    public float distanceTo(GameEntity other) {
        if (other == null) {
            return Float.MAX_VALUE;
        }
        
        int centerX1 = x + width / 2;
        int centerY1 = y + height / 2;
        int centerX2 = other.x + other.width / 2;
        int centerY2 = other.y + other.height / 2;
        
        int dx = centerX2 - centerX1;
        int dy = centerY2 - centerY1;
        
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Predict next position based on velocity
     * @param timeStep Time step in milliseconds
     * @return Predicted entity with updated position
     */
    public GameEntity predictNextPosition(long timeStep) {
        GameEntity predicted = new GameEntity(type);
        predicted.setId(id);
        predicted.setWidth(width);
        predicted.setHeight(height);
        predicted.setConfidence(confidence * 0.9f); // Lower confidence for prediction
        
        // Calculate new position based on velocity
        float seconds = timeStep / 1000.0f;
        predicted.setX(Math.round(x + velocityX * seconds));
        predicted.setY(Math.round(y + velocityY * seconds));
        
        // Copy velocity
        predicted.setVelocityX(velocityX);
        predicted.setVelocityY(velocityY);
        
        return predicted;
    }
    
    /**
     * Get entity type name
     * @return Type name
     */
    public String getTypeName() {
        switch (type) {
            case TYPE_PLAYER:
                return "Player";
            case TYPE_ENEMY:
                return "Enemy";
            case TYPE_ITEM:
                return "Item";
            case TYPE_ENVIRONMENT:
                return "Environment";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return "GameEntity{" +
               "id=" + id +
               ", type=" + getTypeName() +
               ", pos=(" + x + "," + y + ")" +
               ", vel=(" + velocityX + "," + velocityY + ")" +
               ", size=" + width + "x" + height +
               '}';
    }
}
