package com.aiassistant.learning;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements meta-learning to optimize learning rates and other hyperparameters
 * for reinforcement learning algorithms based on observed performance.
 */
public class MetaLearner {
    private static final String TAG = "MetaLearner";
    
    // Learning algorithm types
    public enum LearningAlgorithm {
        PPO,        // Proximal Policy Optimization
        DQN,        // Deep Q Network
        SARSA,      // State-Action-Reward-State-Action
        Q_LEARNING, // Q-Learning
        RANDOM      // Random baseline for comparison
    }
    
    // Learning parameters
    private Map<LearningAlgorithm, Float> learningRates;
    private Map<LearningAlgorithm, Float> discountFactors;
    private Map<LearningAlgorithm, Float> explorationRates;
    
    // Performance tracking
    private Map<LearningAlgorithm, Float> avgRewards;
    private Map<LearningAlgorithm, Integer> successCounts;
    private Map<LearningAlgorithm, Integer> totalTrials;
    
    // Learning rate optimization
    private float metaLearningRate = 0.001f;
    private boolean adaptiveParametersEnabled = true;
    
    /**
     * Constructor initializes default parameters for all algorithms
     */
    public MetaLearner() {
        initializeParameters();
    }
    
    /**
     * Initialize default parameters for all learning algorithms
     */
    private void initializeParameters() {
        learningRates = new HashMap<>();
        discountFactors = new HashMap<>();
        explorationRates = new HashMap<>();
        avgRewards = new HashMap<>();
        successCounts = new HashMap<>();
        totalTrials = new HashMap<>();
        
        // Set default learning rates
        learningRates.put(LearningAlgorithm.PPO, 0.0003f);
        learningRates.put(LearningAlgorithm.DQN, 0.001f);
        learningRates.put(LearningAlgorithm.SARSA, 0.1f);
        learningRates.put(LearningAlgorithm.Q_LEARNING, 0.1f);
        learningRates.put(LearningAlgorithm.RANDOM, 0.0f);
        
        // Set default discount factors (gamma)
        discountFactors.put(LearningAlgorithm.PPO, 0.99f);
        discountFactors.put(LearningAlgorithm.DQN, 0.99f);
        discountFactors.put(LearningAlgorithm.SARSA, 0.95f);
        discountFactors.put(LearningAlgorithm.Q_LEARNING, 0.95f);
        discountFactors.put(LearningAlgorithm.RANDOM, 0.0f);
        
        // Set default exploration rates (epsilon)
        explorationRates.put(LearningAlgorithm.PPO, 0.2f);
        explorationRates.put(LearningAlgorithm.DQN, 0.1f);
        explorationRates.put(LearningAlgorithm.SARSA, 0.1f);
        explorationRates.put(LearningAlgorithm.Q_LEARNING, 0.1f);
        explorationRates.put(LearningAlgorithm.RANDOM, 1.0f);
        
        // Initialize performance tracking
        for (LearningAlgorithm algo : LearningAlgorithm.values()) {
            avgRewards.put(algo, 0.0f);
            successCounts.put(algo, 0);
            totalTrials.put(algo, 0);
        }
        
        Log.i(TAG, "MetaLearner parameters initialized");
    }
    
    /**
     * Reset all learning parameters to defaults
     */
    public void reset() {
        initializeParameters();
        Log.i(TAG, "MetaLearner reset to default parameters");
    }
    
    /**
     * Get the learning rate for a specific algorithm
     * @param algorithm The learning algorithm
     * @return The current learning rate
     */
    public float getLearningRate(LearningAlgorithm algorithm) {
        return learningRates.getOrDefault(algorithm, 0.01f);
    }
    
    /**
     * Get the discount factor for a specific algorithm
     * @param algorithm The learning algorithm
     * @return The current discount factor
     */
    public float getDiscountFactor(LearningAlgorithm algorithm) {
        return discountFactors.getOrDefault(algorithm, 0.95f);
    }
    
    /**
     * Get the exploration rate for a specific algorithm
     * @param algorithm The learning algorithm
     * @return The current exploration rate
     */
    public float getExplorationRate(LearningAlgorithm algorithm) {
        return explorationRates.getOrDefault(algorithm, 0.1f);
    }
    
    /**
     * Update parameters based on observed performance
     * @param algorithm The learning algorithm that was used
     * @param reward The reward received
     * @param success Whether the task was completed successfully
     */
    public void updateFromPerformance(LearningAlgorithm algorithm, float reward, boolean success) {
        if (!adaptiveParametersEnabled) {
            return;
        }
        
        // Update performance tracking
        int trials = totalTrials.getOrDefault(algorithm, 0) + 1;
        totalTrials.put(algorithm, trials);
        
        int successes = successCounts.getOrDefault(algorithm, 0);
        if (success) {
            successes++;
            successCounts.put(algorithm, successes);
        }
        
        // Update average reward using incremental formula
        float oldAvg = avgRewards.getOrDefault(algorithm, 0.0f);
        float newAvg = oldAvg + (reward - oldAvg) / trials;
        avgRewards.put(algorithm, newAvg);
        
        // Adapt learning rate based on performance
        if (trials % 10 == 0) { // Only adapt every 10 trials
            adaptParameters(algorithm);
        }
        
        Log.d(TAG, "Performance update for " + algorithm + ": reward=" + reward + 
                ", success=" + success + ", avgReward=" + newAvg + 
                ", successRate=" + (float)successes/trials);
    }
    
    /**
     * Adapt learning parameters based on performance history
     * @param algorithm The learning algorithm to adapt
     */
    private void adaptParameters(LearningAlgorithm algorithm) {
        if (algorithm == LearningAlgorithm.RANDOM) {
            return; // Don't adapt random baseline
        }
        
        float currentAvgReward = avgRewards.getOrDefault(algorithm, 0.0f);
        int trials = totalTrials.getOrDefault(algorithm, 0);
        
        if (trials < 10) {
            return; // Not enough data to adapt
        }
        
        // Get current parameters
        float currentLR = learningRates.getOrDefault(algorithm, 0.01f);
        float currentDF = discountFactors.getOrDefault(algorithm, 0.95f);
        float currentER = explorationRates.getOrDefault(algorithm, 0.1f);
        
        // Simple adaptation rules (in a real system, this would be more sophisticated)
        
        // If rewards are consistently high, slightly reduce learning rate for stability
        if (currentAvgReward > 0.8f) {
            float newLR = Math.max(0.0001f, currentLR * 0.95f);
            learningRates.put(algorithm, newLR);
            Log.d(TAG, "Reduced learning rate for " + algorithm + " to " + newLR);
        }
        // If rewards are low, try increasing learning rate
        else if (currentAvgReward < 0.3f) {
            float newLR = Math.min(0.5f, currentLR * 1.05f);
            learningRates.put(algorithm, newLR);
            Log.d(TAG, "Increased learning rate for " + algorithm + " to " + newLR);
        }
        
        // Adapt exploration rate - decrease over time for exploitation
        float successRate = (float)successCounts.getOrDefault(algorithm, 0) / trials;
        if (successRate > 0.7f) {
            // If successful, reduce exploration gradually
            float newER = Math.max(0.01f, currentER * 0.95f);
            explorationRates.put(algorithm, newER);
            Log.d(TAG, "Reduced exploration rate for " + algorithm + " to " + newER);
        } 
        else if (successRate < 0.3f) {
            // If not succeeding, increase exploration
            float newER = Math.min(0.9f, currentER * 1.05f);
            explorationRates.put(algorithm, newER);
            Log.d(TAG, "Increased exploration rate for " + algorithm + " to " + newER);
        }
        
        // Discount factor usually stays more stable, but can be tuned slightly
        if (algorithm == LearningAlgorithm.PPO || algorithm == LearningAlgorithm.DQN) {
            // These algorithms typically work better with high discount factors
            float newDF = Math.min(0.999f, currentDF + metaLearningRate * 0.01f);
            discountFactors.put(algorithm, newDF);
        }
    }
    
    /**
     * Get the current best performing algorithm
     * @return The algorithm with highest average reward
     */
    public LearningAlgorithm getBestAlgorithm() {
        LearningAlgorithm best = LearningAlgorithm.Q_LEARNING; // Default
        float bestReward = -Float.MAX_VALUE;
        
        for (Map.Entry<LearningAlgorithm, Float> entry : avgRewards.entrySet()) {
            // Require at least 10 trials before considering
            if (totalTrials.getOrDefault(entry.getKey(), 0) >= 10 && entry.getValue() > bestReward) {
                bestReward = entry.getValue();
                best = entry.getKey();
            }
        }
        
        return best;
    }
    
    /**
     * Get the success rate for a specific algorithm
     * @param algorithm The learning algorithm
     * @return The success rate (0.0 to 1.0)
     */
    public float getSuccessRate(LearningAlgorithm algorithm) {
        int trials = totalTrials.getOrDefault(algorithm, 0);
        if (trials == 0) {
            return 0.0f;
        }
        return (float)successCounts.getOrDefault(algorithm, 0) / trials;
    }
    
    /**
     * Get the average reward for a specific algorithm
     * @param algorithm The learning algorithm
     * @return The average reward
     */
    public float getAverageReward(LearningAlgorithm algorithm) {
        return avgRewards.getOrDefault(algorithm, 0.0f);
    }
    
    /**
     * Set whether adaptive parameters are enabled
     * @param enabled Whether to enable adaptive parameters
     */
    public void setAdaptiveParametersEnabled(boolean enabled) {
        this.adaptiveParametersEnabled = enabled;
    }
    
    /**
     * Check if adaptive parameters are enabled
     * @return true if adaptive parameters are enabled, false otherwise
     */
    public boolean isAdaptiveParametersEnabled() {
        return adaptiveParametersEnabled;
    }
    
    /**
     * Set the meta learning rate (how quickly parameters are adapted)
     * @param rate The meta learning rate
     */
    public void setMetaLearningRate(float rate) {
        this.metaLearningRate = Math.max(0.0001f, Math.min(0.1f, rate));
    }
    
    /**
     * Get the current meta learning rate
     * @return The meta learning rate
     */
    public float getMetaLearningRate() {
        return metaLearningRate;
    }
    
    /**
     * Gets a summary of all learning algorithm performances
     * @return A string summarizing algorithm performances
     */
    public String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Learning Algorithm Performance Summary:\n");
        
        for (LearningAlgorithm algo : LearningAlgorithm.values()) {
            int trials = totalTrials.getOrDefault(algo, 0);
            if (trials > 0) {
                float successRate = getSuccessRate(algo);
                float avgReward = getAverageReward(algo);
                
                summary.append(algo.name()).append(": ")
                       .append("Trials=").append(trials)
                       .append(", Success=").append(String.format("%.2f", successRate * 100)).append("%")
                       .append(", AvgReward=").append(String.format("%.4f", avgReward))
                       .append("\n");
            }
        }
        
        // Add the current best algorithm
        summary.append("Best algorithm: ").append(getBestAlgorithm().name());
        
        return summary.toString();
    }
}