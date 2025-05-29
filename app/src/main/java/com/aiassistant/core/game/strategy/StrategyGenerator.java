package com.aiassistant.core.game.strategy;

import com.aiassistant.core.ai.neural.GamePatternModel;
import com.aiassistant.core.ai.neural.SpatialReasoningModel;
import com.aiassistant.core.ai.neural.TacticalAnalysisModel;

/**
 * Generates strategic recommendations based on comprehensive analysis
 */
public class StrategyGenerator {
    
    /**
     * Generate a comprehensive strategy based on various analysis results
     * @param patternResult Pattern analysis result
     * @param spatialResult Spatial analysis result
     * @param tacticalResult Tactical analysis result
     * @return Comprehensive strategy recommendation
     */
    public String generateStrategy(GamePatternModel.PatternRecognitionResult patternResult,
                                  SpatialReasoningModel.SpatialAnalysisResult spatialResult,
                                  TacticalAnalysisModel.TacticalAnalysisResult tacticalResult) {
        StringBuilder strategy = new StringBuilder();
        strategy.append("Strategic Recommendation:\n\n");
        
        // Determine overall strategic approach
        StrategicApproach approach = determineStrategicApproach(
                patternResult, spatialResult, tacticalResult);
        
        // Add overall strategic direction
        strategy.append("Recommended Approach: ").append(approach.name()).append("\n\n");
        strategy.append(getApproachDescription(approach)).append("\n\n");
        
        // Add pattern-based recommendations
        if (patternResult != null) {
            strategy.append("Pattern Strategy:\n");
            strategy.append(generatePatternStrategy(patternResult)).append("\n\n");
        }
        
        // Add tactical recommendations
        if (tacticalResult != null && tacticalResult.tacticalRecommendations != null
                && !tacticalResult.tacticalRecommendations.isEmpty()) {
            strategy.append("Tactical Actions:\n");
            
            // List top 3 recommendations
            int count = Math.min(3, tacticalResult.tacticalRecommendations.size());
            for (int i = 0; i < count; i++) {
                TacticalAnalysisModel.TacticalRecommendation rec = 
                        tacticalResult.tacticalRecommendations.get(i);
                strategy.append(i+1).append(". ").append(rec.description).append("\n");
            }
            strategy.append("\n");
        }
        
        // Add spatial recommendations
        if (spatialResult != null && spatialResult.keyPoints != null 
                && !spatialResult.keyPoints.isEmpty()) {
            strategy.append("Spatial Strategy:\n");
            strategy.append("Focus on controlling these key positions:\n");
            
            // List key positions
            int count = Math.min(3, spatialResult.keyPoints.size());
            for (int i = 0; i < count; i++) {
                SpatialReasoningModel.KeyPosition pos = spatialResult.keyPoints.get(i);
                strategy.append("- ").append(pos.type).append(" position at (")
                       .append(Math.round(pos.x)).append(", ")
                       .append(Math.round(pos.y)).append(", ")
                       .append(Math.round(pos.z)).append(")\n");
            }
            strategy.append("\n");
        }
        
        // Add resource management advice
        strategy.append("Resource Strategy:\n");
        if (tacticalResult != null) {
            if (tacticalResult.resourceAdvantage > 0.2f) {
                strategy.append("You have a resource advantage. Leverage this for expansion and pressure.\n");
            } else if (tacticalResult.resourceAdvantage < -0.2f) {
                strategy.append("You have a resource disadvantage. Focus on efficient usage and securing more resources.\n");
            } else {
                strategy.append("Resources are balanced. Seek efficiency and deny resources to opponent.\n");
            }
        } else {
            strategy.append("Maintain balanced resource allocation between offense, defense, and development.\n");
        }
        
        return strategy.toString();
    }
    
    /**
     * Determine the overall strategic approach based on all analysis results
     * @param patternResult Pattern analysis result
     * @param spatialResult Spatial analysis result
     * @param tacticalResult Tactical analysis result
     * @return Recommended strategic approach
     */
    private StrategicApproach determineStrategicApproach(
            GamePatternModel.PatternRecognitionResult patternResult,
            SpatialReasoningModel.SpatialAnalysisResult spatialResult,
            TacticalAnalysisModel.TacticalAnalysisResult tacticalResult) {
        
        // Default to balanced approach
        StrategicApproach approach = StrategicApproach.BALANCED;
        
        // Adjust based on tactical situation if available
        if (tacticalResult != null) {
            float overallAdvantage = tacticalResult.overallAdvantage;
            float offensiveOpportunity = tacticalResult.offensiveOpportunity;
            float defensivePositionQuality = tacticalResult.defensivePositionQuality;
            float escapeViability = tacticalResult.escapeViability;
            
            // Significant advantage -> aggressive
            if (overallAdvantage > 0.3f && offensiveOpportunity > 0.6f) {
                approach = StrategicApproach.AGGRESSIVE;
            }
            // Significant disadvantage -> defensive or evasive
            else if (overallAdvantage < -0.3f) {
                if (defensivePositionQuality > 0.7f) {
                    approach = StrategicApproach.DEFENSIVE;
                } else if (escapeViability > 0.7f) {
                    approach = StrategicApproach.EVASIVE;
                } else {
                    approach = StrategicApproach.DEFENSIVE;
                }
            }
            // Slight advantage -> probing
            else if (overallAdvantage > 0.1f) {
                approach = StrategicApproach.PROBING;
            }
            // Slight disadvantage -> cautious
            else if (overallAdvantage < -0.1f) {
                approach = StrategicApproach.CAUTIOUS;
            }
            // Balanced situation -> balanced or development
            else {
                approach = StrategicApproach.BALANCED;
            }
        }
        
        // Adjust based on pattern recognition if available
        if (patternResult != null && patternResult.patternConfidence > 0.6f) {
            GamePatternModel.PatternType patternType = patternResult.dominantPattern;
            
            switch (patternType) {
                case REPETITIVE:
                case CYCLIC:
                    // If opponent is predictable, can be more aggressive
                    if (approach == StrategicApproach.BALANCED || approach == StrategicApproach.CAUTIOUS) {
                        approach = StrategicApproach.PROBING;
                    }
                    break;
                    
                case ADAPTIVE:
                    // If opponent is adaptive, need to be more adaptive ourselves
                    if (approach == StrategicApproach.AGGRESSIVE) {
                        approach = StrategicApproach.PROBING;
                    }
                    break;
                    
                case ESCALATING:
                    // If opponent is escalating, match with appropriate response
                    if (tacticalResult != null && tacticalResult.overallAdvantage > 0) {
                        approach = StrategicApproach.AGGRESSIVE;
                    } else {
                        approach = StrategicApproach.DEFENSIVE;
                    }
                    break;
                    
                case RANDOM:
                    // If opponent is random, focus on development
                    if (approach == StrategicApproach.BALANCED) {
                        approach = StrategicApproach.DEVELOPMENT;
                    }
                    break;
            }
        }
        
        // Adjust based on spatial analysis if available
        if (spatialResult != null) {
            float openness = spatialResult.openness;
            float complexity = spatialResult.complexity;
            
            // Very open environments favor mobility
            if (openness > 0.8f && approach == StrategicApproach.DEFENSIVE) {
                approach = StrategicApproach.MOBILE_DEFENSE;
            }
            
            // Complex environments favor deceptive approaches
            if (complexity > 0.7f && approach == StrategicApproach.BALANCED) {
                approach = StrategicApproach.DECEPTIVE;
            }
        }
        
        return approach;
    }
    
    /**
     * Generate strategy recommendations based on pattern analysis
     * @param result Pattern analysis result
     * @return Pattern-based strategy
     */
    private String generatePatternStrategy(GamePatternModel.PatternRecognitionResult result) {
        StringBuilder strategy = new StringBuilder();
        
        switch (result.dominantPattern) {
            case REPETITIVE:
                strategy.append("Opponent shows repetitive behavior. Develop specific counters ")
                       .append("for their repeated actions and exploit the predictability.");
                break;
                
            case ALTERNATING:
                strategy.append("Opponent alternates between different actions. Anticipate ")
                       .append("the alternation pattern and prepare responses for both options.");
                break;
                
            case ESCALATING:
                strategy.append("Opponent follows an escalating pattern. Prepare for increasing ")
                       .append("intensity and consider preemptive action before they reach full strength.");
                break;
                
            case CYCLIC:
                strategy.append("Opponent follows a cyclical pattern. Identify the cycle length ")
                       .append("and exploit the predictable transitions between phases.");
                break;
                
            case REACTIVE:
                strategy.append("Opponent is primarily reactive. Take initiative to force them ")
                       .append("into unfavorable responses or use feints to trigger predictable reactions.");
                break;
                
            case STRATEGIC:
                strategy.append("Opponent is following a strategic plan. Disrupt their execution ")
                       .append("by targeting key resources or positions essential to their strategy.");
                break;
                
            case ADAPTIVE:
                strategy.append("Opponent adapts to changing conditions. Avoid establishing ")
                       .append("patterns yourself and make unpredictable changes to your approach.");
                break;
                
            case PREDICTIVE:
                strategy.append("Opponent tries to predict your actions. Use deception and ")
                       .append("misdirection to lead them to incorrect predictions.");
                break;
                
            case RANDOM:
                strategy.append("Opponent appears to act randomly. Focus on your own strategy ")
                       .append("development rather than trying to predict their actions.");
                break;
                
            default:
                strategy.append("No clear pattern detected. Maintain balanced approach while ")
                       .append("gathering more information about opponent tendencies.");
        }
        
        return strategy.toString();
    }
    
    /**
     * Get description for a strategic approach
     * @param approach Strategic approach
     * @return Description of the approach
     */
    private String getApproachDescription(StrategicApproach approach) {
        switch (approach) {
            case AGGRESSIVE:
                return "Take initiative and apply pressure to exploit your advantageous position. " +
                       "Focus on offensive actions while maintaining awareness of counterattacks.";
                
            case DEFENSIVE:
                return "Strengthen your position and focus on countering enemy initiatives. " +
                       "Conserve resources and look for opportunities created by enemy overextension.";
                
            case PROBING:
                return "Test enemy defenses with limited engagements to identify weaknesses. " +
                       "Maintain flexibility to exploit opportunities or withdraw as needed.";
                
            case CAUTIOUS:
                return "Move carefully while gathering more information. " +
                       "Prioritize security and avoid unnecessary risks until situation improves.";
                
            case BALANCED:
                return "Maintain even distribution of resources between offense, defense, and development. " +
                       "Adapt based on emerging opportunities and threats.";
                
            case DECEPTIVE:
                return "Use misdirection to conceal your true intentions. " +
                       "Create diversions to draw attention away from your main effort.";
                
            case EVASIVE:
                return "Avoid direct confrontation and focus on mobility. " +
                       "Use terrain and movement to minimize exposure to enemy strengths.";
                
            case DEVELOPMENT:
                return "Focus on building resources and capabilities for future advantage. " +
                       "Accept short-term tactical disadvantages for long-term strategic gain.";
                
            case MOBILE_DEFENSE:
                return "Defend through movement rather than fixed positions. " +
                       "Trade space for time and counterattack opportunities.";
                
            default:
                return "Adapt to changing conditions while maintaining strategic focus.";
        }
    }
    
    /**
     * Strategic approach types
     */
    public enum StrategicApproach {
        AGGRESSIVE,     // Take initiative, apply pressure
        DEFENSIVE,      // Focus on protection and counter-attacks
        PROBING,        // Test opponent with limited engagements
        CAUTIOUS,       // Move carefully, minimize risks
        BALANCED,       // Even distribution of resources
        DECEPTIVE,      // Use misdirection and unpredictability
        EVASIVE,        // Avoid direct confrontation
        DEVELOPMENT,    // Focus on building future advantage
        MOBILE_DEFENSE  // Defend through movement
    }
}
