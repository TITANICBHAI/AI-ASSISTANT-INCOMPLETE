package com.aiassistant.data.models;

/**
 * Represents a touch action performed on the screen
 */
public class TouchAction {
    
    // Action types
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 1;
    public static final int ACTION_UP = 2;
    
    private long id;
    private String gameId;
    private int actionType;
    private float x;
    private float y;
    private long timestamp;
    private int pointerId;
    private float pressure;
    private float size;
    
    /**
     * Default constructor
     */
    public TouchAction() {
        this.timestamp = System.currentTimeMillis();
        this.pressure = 1.0f;
        this.size = 0.5f;
    }
    
    /**
     * Constructor with action type and coordinates
     * 
     * @param actionType The action type
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public TouchAction(int actionType, float x, float y) {
        this();
        this.actionType = actionType;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Constructor with all parameters
     * 
     * @param id The ID
     * @param gameId The game ID
     * @param actionType The action type
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param timestamp The timestamp
     * @param pointerId The pointer ID
     * @param pressure The pressure
     * @param size The size
     */
    public TouchAction(long id, String gameId, int actionType, float x, float y,
                      long timestamp, int pointerId, float pressure, float size) {
        this.id = id;
        this.gameId = gameId;
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
        this.pointerId = pointerId;
        this.pressure = pressure;
        this.size = size;
    }
    
    /**
     * Create a TouchAction from a ScreenAction
     * 
     * @param screenAction The screen action
     * @return The touch action
     */
    public static TouchAction fromScreenAction(ScreenActionEntity screenAction) {
        if (screenAction == null) {
            return null;
        }
        
        TouchAction touchAction = new TouchAction();
        touchAction.setX(screenAction.getX());
        touchAction.setY(screenAction.getY());
        
        // Map ScreenActionEntity type to TouchAction type
        switch (screenAction.getActionType()) {
            case ScreenActionEntity.ACTION_TAP:
                touchAction.setActionType(ACTION_DOWN); // For tap, we use DOWN
                break;
                
            case ScreenActionEntity.ACTION_SWIPE:
                touchAction.setActionType(ACTION_MOVE); // For swipe, we use MOVE
                break;
                
            case ScreenActionEntity.ACTION_LONG_PRESS:
                touchAction.setActionType(ACTION_DOWN); // For long press, we use DOWN
                break;
                
            default:
                touchAction.setActionType(ACTION_DOWN); // Default to DOWN
                break;
        }
        
        return touchAction;
    }
    
    /**
     * Convert this TouchAction to a TouchPath
     * 
     * @return The touch path
     */
    public TouchPath toTouchPath() {
        TouchPath path = new TouchPath();
        path.setGameId(gameId);
        path.setStartX(x);
        path.setStartY(y);
        path.setStartTime(timestamp);
        
        // For MOVE actions (like swipes), set end point
        if (actionType == ACTION_MOVE) {
            path.setEndX(x); // This will be updated later
            path.setEndY(y); // This will be updated later
            path.setEndTime(timestamp + 300); // Default duration
        } else {
            path.setEndX(x);
            path.setEndY(y);
            path.setEndTime(timestamp + 50); // Short duration for taps
        }
        
        return path;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public int getActionType() {
        return actionType;
    }
    
    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getPointerId() {
        return pointerId;
    }
    
    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }
    
    public float getPressure() {
        return pressure;
    }
    
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }
    
    public float getSize() {
        return size;
    }
    
    public void setSize(float size) {
        this.size = size;
    }
    
    @Override
    public String toString() {
        String actionString;
        switch (actionType) {
            case ACTION_DOWN:
                actionString = "DOWN";
                break;
            case ACTION_MOVE:
                actionString = "MOVE";
                break;
            case ACTION_UP:
                actionString = "UP";
                break;
            default:
                actionString = "UNKNOWN";
                break;
        }
        
        return "TouchAction{" +
                "type=" + actionString +
                ", x=" + x +
                ", y=" + y +
                ", time=" + timestamp +
                '}';
    }
}
