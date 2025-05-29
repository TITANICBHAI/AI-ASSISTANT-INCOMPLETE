package com.aiassistant.models;

/**
 * Enum defining the different types of games that the AI assistant can recognize and support.
 * Each game type has different characteristics and optimal interaction patterns.
 */
public enum GameType {
    /**
     * Action games focus on physical challenges, hand-eye coordination, reaction time,
     * and often involve combat or platforming elements.
     */
    ACTION,
    
    /**
     * Racing games focus on competitive driving or riding, typically with vehicles.
     * They often involve speed, obstacles, and time trials.
     */
    RACING,
    
    /**
     * Puzzle games focus on problem-solving, logic, pattern recognition, 
     * or sequence solving. They typically require critical thinking.
     */
    PUZZLE,
    
    /**
     * Unknown game type when the system cannot determine the game category.
     * This is the default value until a game type is recognized.
     */
    UNKNOWN
}
