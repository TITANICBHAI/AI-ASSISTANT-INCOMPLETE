package com.aiassistant.core.ai.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of game states, actions, rewards, and whether the state is terminal.
 * Used for training reinforcement learning algorithms.
 */
public class Trajectory {
    private List<GameState> states;
    private List<GameAction> actions;
    private List<Float> rewards;
    private List<Boolean> terminals;
    
    /**
     * Create a new empty trajectory
     */
    public Trajectory() {
        states = new ArrayList<>();
        actions = new ArrayList<>();
        rewards = new ArrayList<>();
        terminals = new ArrayList<>();
    }
    
    /**
     * Add a transition to the trajectory
     * 
     * @param state The current state
     * @param action The action taken
     * @param reward The reward received
     * @param isTerminal Whether this is a terminal state
     */
    public void addTransition(GameState state, GameAction action, float reward, boolean isTerminal) {
        states.add(state);
        actions.add(action);
        rewards.add(reward);
        terminals.add(isTerminal);
    }
    
    /**
     * Get the length of the trajectory
     * 
     * @return The number of transitions in the trajectory
     */
    public int size() {
        return states.size();
    }
    
    /**
     * Calculate the discounted return for each step in the trajectory
     * 
     * @param gamma The discount factor
     * @return An array of discounted returns
     */
    public float[] calculateReturns(float gamma) {
        int length = rewards.size();
        float[] returns = new float[length];
        
        float cumulativeReturn = 0;
        for (int i = length - 1; i >= 0; i--) {
            cumulativeReturn = rewards.get(i) + gamma * cumulativeReturn * (terminals.get(i) ? 0 : 1);
            returns[i] = cumulativeReturn;
        }
        
        return returns;
    }
    
    /**
     * Get the states in the trajectory
     * 
     * @return List of states
     */
    public List<GameState> getStates() {
        return states;
    }
    
    /**
     * Get the actions in the trajectory
     * 
     * @return List of actions
     */
    public List<GameAction> getActions() {
        return actions;
    }
    
    /**
     * Get the rewards in the trajectory
     * 
     * @return List of rewards
     */
    public List<Float> getRewards() {
        return rewards;
    }
    
    /**
     * Get the terminal flags in the trajectory
     * 
     * @return List of terminal flags
     */
    public List<Boolean> getTerminals() {
        return terminals;
    }
    
    /**
     * Check if the trajectory is valid (all lists have the same size)
     * 
     * @return True if valid, false otherwise
     */
    public boolean isValid() {
        int size = states.size();
        return actions.size() == size && rewards.size() == size && terminals.size() == size;
    }
    
    /**
     * Clear the trajectory
     */
    public void clear() {
        states.clear();
        actions.clear();
        rewards.clear();
        terminals.clear();
    }
}
