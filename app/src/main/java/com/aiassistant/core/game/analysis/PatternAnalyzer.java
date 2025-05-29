package com.aiassistant.core.game.analysis;

import com.aiassistant.core.ai.neural.GamePatternModel;

/**
 * Analyzes game patterns to generate insights and recommendations
 */
public class PatternAnalyzer {
    
    /**
     * Generate insights based on pattern recognition results
     * @param result Pattern recognition result
     * @return Pattern insights text
     */
    public String generateInsights(GamePatternModel.PatternRecognitionResult result) {
        if (result == null) {
            return "No pattern data available";
        }
        
        StringBuilder insights = new StringBuilder();
        
        // Dominant pattern analysis
        insights.append("Dominant Pattern: ").append(result.dominantPattern)
                .append(" (Confidence: ").append(Math.round(result.patternConfidence * 100))
                .append("%)\n");
        
        // Pattern-specific insights
        switch (result.dominantPattern) {
            case REPETITIVE:
                insights.append("The pattern shows consistent repetition of similar actions. ")
                       .append("This indicates a systematic approach that may be predictable. ")
                       .append("Consider introducing variations to reduce predictability.\n");
                break;
                
            case ALTERNATING:
                insights.append("The pattern alternates between different action types. ")
                       .append("This balanced approach provides good coverage but may become ")
                       .append("predictable if the alternation is too regular.\n");
                break;
                
            case ESCALATING:
                insights.append("The pattern shows increasing intensity or scale. ")
                       .append("This approach builds momentum but may overextend resources ")
                       .append("if not managed carefully.\n");
                break;
                
            case CYCLIC:
                insights.append("The pattern follows a repeating cycle of actions. ")
                       .append("While structured, this approach may be anticipated by ")
                       .append("observant opponents who identify the cycle.\n");
                break;
                
            case REACTIVE:
                insights.append("The pattern shows primarily reactive behavior responding to ")
                       .append("external events rather than proactive initiative. ")
                       .append("This may limit strategic advantage.\n");
                break;
                
            case STRATEGIC:
                insights.append("The pattern demonstrates long-term strategic planning. ")
                       .append("Actions appear to be coordinated toward specific objectives ")
                       .append("with good strategic depth.\n");
                break;
                
            case ADAPTIVE:
                insights.append("The pattern shows adaptation to changing conditions. ")
                       .append("This flexible approach responds well to changes but may ")
                       .append("lack consistency in execution.\n");
                break;
                
            case PREDICTIVE:
                insights.append("The pattern shows anticipation of future events. ")
                       .append("Actions appear calculated to preempt or intercept ")
                       .append("expected developments.\n");
                break;
                
            case RANDOM:
                insights.append("The pattern appears deliberately unpredictable with no ")
                       .append("discernible structure. While difficult to anticipate, ")
                       .append("this may also indicate lack of coherent strategy.\n");
                break;
                
            default:
                insights.append("No clear pattern detected in recent actions.\n");
        }
        
        // Add predictability assessment
        insights.append("Predictability: ").append(Math.round(result.predictabilityScore * 100))
                .append("%\n");
                
        if (result.predictabilityScore > 0.7f) {
            insights.append("Highly predictable patterns may be easily anticipated by opponents. ")
                   .append("Consider introducing strategic variations.\n");
        } else if (result.predictabilityScore < 0.3f) {
            insights.append("Low predictability may create uncertainty for opponents ")
                   .append("but may also indicate lack of strategic coherence.\n");
        }
        
        return insights.toString();
    }
}
