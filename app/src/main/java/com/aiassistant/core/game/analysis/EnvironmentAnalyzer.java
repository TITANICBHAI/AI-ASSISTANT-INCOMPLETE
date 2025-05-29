package com.aiassistant.core.game.analysis;

/**
 * Analyzes game environment conditions to understand the current game state.
 * This class helps translate raw game data into environment models for analysis.
 */
public class EnvironmentAnalyzer {
    
    /**
     * Analysis result class for environment analysis
     */
    public static class EnvironmentAnalysisResult {
        public String environmentType;
        public int visibility; // 0-100
        public int danger; // 0-100
        public int resources; // 0-100
        public int complexity; // 0-100
        
        public String description;
    }
}
