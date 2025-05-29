package com.aiassistant.core.ai.learning;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.interfaces.RLAlgorithm;
import com.aiassistant.core.ai.models.ScreenActionWrapper;
import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.ScreenActionEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for reinforcement learning agents
 */
public abstract class BaseReinforcementLearningAgent implements Serializable {
    
    private static final String TAG = "RLAgent";
    
    protected Context context;
    protected float learningRate = 0.01f;
    protected float discountFactor = 0.95f;
    protected float explorationRate = 0.1f;
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public BaseReinforcementLearningAgent(Context context) {
        this.context = context;
    }
    
    /**
     * Learning experience for reinforcement learning
     */
    public static class LearningExperience implements Serializable {
        private GameState previousState;
        private ScreenActionEntity action;
        private Reward reward;
        private GameState currentState;
        
        public LearningExperience(GameState previousState, ScreenActionEntity action, Reward reward, GameState currentState) {
            this.previousState = previousState;
            this.action = action;
            this.reward = reward;
            this.currentState = currentState;
        }
        
        public GameState getPreviousState() {
            return previousState;
        }
        
        public ScreenActionEntity getAction() {
            return action;
        }
        
        public Reward getReward() {
            return reward;
        }
        
        public GameState getCurrentState() {
            return currentState;
        }
    }
    
    /**
     * Reward class for reinforcement learning
     */
    public static class Reward implements Serializable {
        private float value;
        private String reason;
        
        public Reward(float value, String reason) {
            this.value = value;
            this.reason = reason;
        }
        
        public float getValue() {
            return value;
        }
        
        public String getReason() {
            return reason;
        }
    }
    
    /**
     * Get the algorithm name
     * 
     * @return The algorithm name
     */
    public abstract String getAlgorithmName();
    
    /**
     * Select the best action for a given state
     * 
     * @param currentState The current game state
     * @param possibleActions List of possible actions
     * @return The best action to take
     */
    protected abstract ScreenActionEntity selectBestAction(GameState currentState, List<ScreenActionEntity> possibleActions);
    
    /**
     * Update the model with a new learning experience
     * 
     * @param experience The learning experience
     */
    protected abstract void updateModel(LearningExperience experience);
    
    /**
     * Batch update the model with multiple learning experiences
     * 
     * @param experiences List of learning experiences
     */
    protected abstract void batchUpdateModel(List<LearningExperience> experiences);
    
    /**
     * Create a snapshot of the current model
     * 
     * @return A serializable snapshot of the model
     */
    protected abstract Object createModelSnapshot();
    
    /**
     * Restore the model from a snapshot
     * 
     * @param modelSnapshot The model snapshot
     */
    protected abstract void restoreModelSnapshot(Object modelSnapshot);
    
    /**
     * Reset the model to initial state
     */
    protected abstract void resetModel();
    
    /**
     * Set the learning rate
     * 
     * @param learningRate The learning rate (0.0-1.0)
     */
    public void setLearningRate(float learningRate) {
        if (learningRate >= 0 && learningRate <= 1) {
            this.learningRate = learningRate;
            Log.d(TAG, "Learning rate set to " + learningRate);
        }
    }
    
    /**
     * Get the current learning rate
     * 
     * @return The learning rate
     */
    public float getLearningRate() {
        return learningRate;
    }
    
    /**
     * Set the discount factor
     * 
     * @param discountFactor The discount factor (0.0-1.0)
     */
    public void setDiscountFactor(float discountFactor) {
        if (discountFactor >= 0 && discountFactor <= 1) {
            this.discountFactor = discountFactor;
            Log.d(TAG, "Discount factor set to " + discountFactor);
        }
    }
    
    /**
     * Get the current discount factor
     * 
     * @return The discount factor
     */
    public float getDiscountFactor() {
        return discountFactor;
    }
    
    /**
     * Set the exploration rate (epsilon)
     * 
     * @param explorationRate The exploration rate (0.0-1.0)
     */
    public void setExplorationRate(float explorationRate) {
        if (explorationRate >= 0 && explorationRate <= 1) {
            this.explorationRate = explorationRate;
            Log.d(TAG, "Exploration rate set to " + explorationRate);
        }
    }
    
    /**
     * Get the current exploration rate
     * 
     * @return The exploration rate
     */
    public float getExplorationRate() {
        return explorationRate;
    }
}
