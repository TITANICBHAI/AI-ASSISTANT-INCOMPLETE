package com.aiassistant.data.models;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Represents a game state with its UI elements.
 * This is a relation class for Room that combines a GameState with its UIElements.
 */
public class GameStateWithElements {
    @Embedded
    public GameState gameState;
    
    @Relation(
        parentColumn = "id",
        entityColumn = "gameStateId"
    )
    public List<UIElement> uiElements;
    
    /**
     * Default constructor
     */
    public GameStateWithElements() {
    }
    
    /**
     * Constructor with game state and UI elements
     * @param gameState Game state
     * @param uiElements UI elements
     */
    public GameStateWithElements(GameState gameState, List<UIElement> uiElements) {
        this.gameState = gameState;
        this.uiElements = uiElements;
    }
    
    /**
     * Find a UI element by ID
     * @param elementId Element ID
     * @return UI element or null
     */
    public UIElement findElementById(String elementId) {
        if (elementId == null || uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (elementId.equals(element.getElementId())) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find a UI element by text
     * @param text Text to find (case insensitive)
     * @return UI element or null
     */
    public UIElement findElementByText(String text) {
        if (text == null || uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (text.equalsIgnoreCase(element.getText())) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find a UI element by partial text
     * @param partialText Partial text to find (case insensitive)
     * @return UI element or null
     */
    public UIElement findElementByPartialText(String partialText) {
        if (partialText == null || uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (element.getText() != null && 
                element.getText().toLowerCase().contains(partialText.toLowerCase())) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find a UI element by content description
     * @param description Content description to find (case insensitive)
     * @return UI element or null
     */
    public UIElement findElementByContentDescription(String description) {
        if (description == null || uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (description.equalsIgnoreCase(element.getContentDescription())) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find a UI element by position
     * @param x X coordinate
     * @param y Y coordinate
     * @return UI element or null
     */
    public UIElement findElementByPosition(int x, int y) {
        if (uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (x >= element.getX() && x <= element.getX() + element.getWidth() &&
                y >= element.getY() && y <= element.getY() + element.getHeight()) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find a clickable UI element
     * @return UI element or null
     */
    public UIElement findClickableElement() {
        if (uiElements == null) {
            return null;
        }
        
        for (UIElement element : uiElements) {
            if (element.isClickable()) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Get the number of UI elements
     * @return Number of UI elements
     */
    public int getElementCount() {
        return uiElements != null ? uiElements.size() : 0;
    }
    
    @Override
    public String toString() {
        return "GameStateWithElements{" +
                "gameState=" + (gameState != null ? gameState.toString() : "null") +
                ", elementCount=" + getElementCount() +
                '}';
    }
}