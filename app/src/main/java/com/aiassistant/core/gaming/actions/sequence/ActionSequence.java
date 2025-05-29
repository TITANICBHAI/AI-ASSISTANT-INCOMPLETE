package com.aiassistant.core.gaming.actions.sequence;

import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.ScreenActionEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A sequence of actions to be executed in order
 */
public class ActionSequence {
    
    private static final String TAG = "ActionSequence";
    
    private final List<ScreenActionEntity> actions;
    private int currentIndex;
    private boolean isComplete;
    private String name;
    private String description;
    private String gameId;
    private long startTime;
    private long endTime;
    
    /**
     * Default constructor
     */
    public ActionSequence() {
        this.actions = new ArrayList<>();
        this.currentIndex = 0;
        this.isComplete = false;
        this.startTime = 0;
        this.endTime = 0;
    }
    
    /**
     * Constructor with name
     * 
     * @param name The sequence name
     */
    public ActionSequence(String name) {
        this();
        this.name = name;
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The sequence name
     * @param description The sequence description
     */
    public ActionSequence(String name, String description) {
        this(name);
        this.description = description;
    }
    
    /**
     * Add an action to the sequence
     * 
     * @param action The action to add
     */
    public void addAction(ScreenActionEntity action) {
        actions.add(action);
    }
    
    /**
     * Add an AI action to the sequence
     * 
     * @param action The AI action to add
     */
    public void addAction(AIAction action) {
        if (action != null) {
            actions.add(action.toScreenAction());
        }
    }
    
    /**
     * Get the next action in the sequence
     * 
     * @return The next action, or null if sequence is complete
     */
    public ScreenActionEntity getNextAction() {
        if (isComplete || currentIndex >= actions.size()) {
            isComplete = true;
            return null;
        }
        
        return actions.get(currentIndex++);
    }
    
    /**
     * Start the sequence
     */
    public void start() {
        this.currentIndex = 0;
        this.isComplete = false;
        this.startTime = System.currentTimeMillis();
        Log.d(TAG, "Started action sequence: " + name);
    }
    
    /**
     * Complete the sequence
     */
    public void complete() {
        this.isComplete = true;
        this.endTime = System.currentTimeMillis();
        Log.d(TAG, "Completed action sequence: " + name + " in " + 
              (endTime - startTime) + "ms");
    }
    
    /**
     * Reset the sequence
     */
    public void reset() {
        this.currentIndex = 0;
        this.isComplete = false;
        this.startTime = 0;
        this.endTime = 0;
    }
    
    /**
     * Check if a game state matches the requirements for this sequence
     * 
     * @param state The game state
     * @return True if the sequence is applicable
     */
    public boolean matchesState(GameState state) {
        // Default implementation always matches
        // Override in subclasses for specific matching logic
        return true;
    }
    
    /**
     * Get the number of actions in the sequence
     * 
     * @return The action count
     */
    public int getActionCount() {
        return actions.size();
    }
    
    /**
     * Get the completion percentage
     * 
     * @return The completion percentage (0-100)
     */
    public int getCompletionPercentage() {
        if (actions.isEmpty()) {
            return 100;
        }
        
        return (int) ((float) currentIndex / actions.size() * 100);
    }
    
    // Getters and setters
    
    public List<ScreenActionEntity> getActions() {
        return actions;
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
    
    public boolean isComplete() {
        return isComplete;
    }
    
    public void setComplete(boolean complete) {
        isComplete = complete;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public long getDuration() {
        if (endTime == 0) {
            return System.currentTimeMillis() - startTime;
        }
        return endTime - startTime;
    }
}
