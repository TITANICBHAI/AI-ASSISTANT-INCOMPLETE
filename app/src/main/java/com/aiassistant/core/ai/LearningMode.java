package com.aiassistant.core.ai;

/**
 * Constants and utilities for AI learning modes
 */
public class LearningMode {
    // Learning mode constants
    public static final int CONSERVATIVE = 0;  // Slow learning, low exploration
    public static final int BALANCED = 1;      // Default balanced approach
    public static final int AGGRESSIVE = 2;    // Fast learning, high exploration
    
    // Default mode
    public static final int DEFAULT = BALANCED;
    
    /**
     * Get a human-readable string for a learning mode
     */
    public static String toString(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return "Conservative";
            case AGGRESSIVE:
                return "Aggressive";
            case BALANCED:
            default:
                return "Balanced";
        }
    }
    
    /**
     * Get the exploration rate for a learning mode
     */
    public static double getExplorationRate(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return 0.1;  // 10% exploration
            case AGGRESSIVE:
                return 0.3;  // 30% exploration
            case BALANCED:
            default:
                return 0.2;  // 20% exploration
        }
    }
    
    /**
     * Get the learning rate for a learning mode
     */
    public static double getLearningRate(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return 0.05;  // Slow learning rate
            case AGGRESSIVE:
                return 0.2;   // Fast learning rate
            case BALANCED:
            default:
                return 0.1;   // Moderate learning rate
        }
    }
    
    /**
     * Get the repetition count for a learning mode
     * (how many times to repeat training examples)
     */
    public static int getRepetitionCount(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return 1;  // Less repetition
            case AGGRESSIVE:
                return 5;  // More repetition
            case BALANCED:
            default:
                return 3;  // Moderate repetition
        }
    }
    
    /**
     * Get the reward discount factor for a learning mode
     */
    public static double getDiscountFactor(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return 0.9;  // Higher focus on long-term rewards
            case AGGRESSIVE:
                return 0.7;  // More focus on immediate rewards
            case BALANCED:
            default:
                return 0.8;  // Balanced approach
        }
    }
    
    /**
     * Get the model update frequency for a learning mode
     * (in milliseconds)
     */
    public static long getModelUpdateFrequency(int mode) {
        switch (mode) {
            case CONSERVATIVE:
                return 60000;  // Update every minute
            case AGGRESSIVE:
                return 15000;  // Update every 15 seconds
            case BALANCED:
            default:
                return 30000;  // Update every 30 seconds
        }
    }
}
