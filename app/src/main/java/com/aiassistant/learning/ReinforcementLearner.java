package com.aiassistant.learning;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;

import java.util.Map;

/**
 * Interface for reinforcement learning algorithms
 */
public class ReinforcementLearner {
    
    /**
     * Enumeration of supported reinforcement learning algorithms
     */
    public enum Algorithm {
        Q_LEARNING("Q-Learning"),
        SARSA("SARSA"),
        DQN("Deep Q-Network"),
        PPO("Proximal Policy Optimization"),
        A2C("Advantage Actor-Critic");
        
        private final String displayName;
        
        Algorithm(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Interface for reinforcement learning algorithms
     */
    public interface LearningAlgorithm {
        /**
         * Initialize the algorithm with parameters
         * @param parameters Map of parameters
         */
        void initialize(Map<String, Object> parameters);
        
        /**
         * Select the best action for the current state
         * @param state Current game state
         * @param explorationRate Probability of exploring (0.0-1.0)
         * @return Selected action
         */
        AIAction selectAction(GameState state, float explorationRate);
        
        /**
         * Update algorithm with results of previous action
         * @param previousState Previous game state
         * @param action Action that was taken
         * @param currentState Current game state after action
         * @param reward Reward received for the action
         * @param terminal Whether this is a terminal state
         */
        void update(GameState previousState, AIAction action, GameState currentState, 
                   float reward, boolean terminal);
        
        /**
         * Save the algorithm's state
         * @return Serialized state as byte array
         */
        byte[] saveState();
        
        /**
         * Load the algorithm's state
         * @param state Serialized state
         */
        void loadState(byte[] state);
        
        /**
         * Get the algorithm type
         * @return Algorithm type name
         */
        String getAlgorithmType();
        
        /**
         * Set the learning rate
         * @param learningRate New learning rate
         */
        void setLearningRate(float learningRate);
        
        /**
         * Get the current learning rate
         * @return Current learning rate
         */
        float getLearningRate();
        
        /**
         * Set the discount factor
         * @param discountFactor New discount factor
         */
        void setDiscountFactor(float discountFactor);
        
        /**
         * Get the current discount factor
         * @return Current discount factor
         */
        float getDiscountFactor();
    }
}
