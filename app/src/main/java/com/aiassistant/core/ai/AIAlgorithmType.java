package com.aiassistant.core.ai;

/**
 * Enum defining the different algorithm types available in the AI system.
 */
public enum AIAlgorithmType {
    /**
     * Meta-learning algorithm that selects the best algorithm based on context.
     */
    META_LEARNING, 
    
    /**
     * Proximal Policy Optimization - Best for continuous action spaces.
     */
    PPO, 
    
    /**
     * Deep Q-Network - Best for processing visual input.
     */
    DQN, 
    
    /**
     * State-Action-Reward-State-Action - Good for cooperative environments.
     */
    SARSA, 
    
    /**
     * Q-Learning - Efficient for discrete action spaces.
     */
    Q_LEARNING
}
