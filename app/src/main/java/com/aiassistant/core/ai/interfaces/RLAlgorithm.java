package com.aiassistant.core.ai.interfaces;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;

/**
 * Interface for reinforcement learning algorithms
 */
public interface RLAlgorithm {
    
    /**
     * Process a game state and select an action
     * 
     * @param gameState The current game state
     * @return The selected action
     */
    AIAction processState(GameState gameState);
    
    /**
     * Update the model with a reward
     * 
     * @param gameState The previous game state
     * @param action The action taken
     * @param reward The reward received
     * @param nextState The resulting game state
     */
    void updateWithReward(GameState gameState, AIAction action, float reward, GameState nextState);
    
    /**
     * Save the model
     * 
     * @return True if successful
     */
    boolean save();
    
    /**
     * Load the model
     * 
     * @return True if successful
     */
    boolean load();
    
    /**
     * Reset the model
     */
    void reset();
    
    /**
     * Set the number of state dimensions
     * 
     * @param stateSize The state size
     */
    void setStateSize(int stateSize);
    
    /**
     * Get the number of state dimensions
     * 
     * @return The state size
     */
    int getStateSize();
    
    /**
     * Set the number of possible actions
     * 
     * @param actionSize The action size
     */
    void setActionSize(int actionSize);
    
    /**
     * Get the number of possible actions
     * 
     * @return The action size
     */
    int getActionSize();
    
    /**
     * Set the learning rate
     * 
     * @param learningRate The learning rate
     */
    void setLearningRate(float learningRate);
    
    /**
     * Get the learning rate
     * 
     * @return The learning rate
     */
    float getLearningRate();
    
    /**
     * Set the discount factor
     * 
     * @param discountFactor The discount factor
     */
    void setDiscountFactor(float discountFactor);
    
    /**
     * Get the discount factor
     * 
     * @return The discount factor
     */
    float getDiscountFactor();
    
    /**
     * Set the exploration rate
     * 
     * @param epsilon The exploration rate
     */
    void setEpsilon(float epsilon);
    
    /**
     * Get the exploration rate
     * 
     * @return The exploration rate
     */
    float getEpsilon();
    
    /**
     * Set the game ID
     * 
     * @param gameId The game ID
     */
    void setGameId(String gameId);
    
    /**
     * Get the game ID
     * 
     * @return The game ID
     */
    String getGameId();
}
