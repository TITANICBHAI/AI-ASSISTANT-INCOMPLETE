package com.aiassistant.core.ai.model;

import java.io.Serializable;

/**
 * Model class for touch actions
 */
public class TouchAction implements Serializable {
    
    public enum Type {
        TAP,
        SWIPE,
        LONG_PRESS,
        DOUBLE_TAP,
        PINCH,
        ZOOM,
        DRAG,
        TEXT_INPUT
    }
    
    private Type type;
    private int x;
    private int y;
    private int endX;
    private int endY;
    private int duration;
    private String targetElementId;
    private String inputText;
    
    /**
     * Constructor
     */
    public TouchAction() {
        this.type = Type.TAP;
        this.duration = 100;
    }
    
    // Getters and Setters
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getEndX() {
        return endX;
    }
    
    public void setEndX(int endX) {
        this.endX = endX;
    }
    
    public int getEndY() {
        return endY;
    }
    
    public void setEndY(int endY) {
        this.endY = endY;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public String getTargetElementId() {
        return targetElementId;
    }
    
    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }
    
    public String getInputText() {
        return inputText;
    }
    
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}
