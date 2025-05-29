package com.aiassistant.core.game.analysis;

import com.aiassistant.core.ai.neural.SpatialReasoningModel;

/**
 * Analyzes spatial relationships to generate insights and recommendations
 */
public class SpatialAnalyzer {
    
    /**
     * Generate insights based on spatial analysis results
     * @param result Spatial analysis result
     * @return Spatial insights text
     */
    public String generateInsights(SpatialReasoningModel.SpatialAnalysisResult result) {
        if (result == null) {
            return "No spatial data available";
        }
        
        StringBuilder insights = new StringBuilder();
        
        // Environmental characteristics
        insights.append("Environment: ")
                .append("Openness ").append(Math.round(result.openness * 100)).append("%, ")
                .append("Complexity ").append(Math.round(result.complexity * 100)).append("%, ")
                .append("Dynamism ").append(Math.round(result.dynamism * 100)).append("%\n");
        
        // Spatial assessment
        if (result.openness > 0.7f) {
            insights.append("Open environment provides good visibility but limited cover. ")
                   .append("Movement is less restricted but exposure is higher.\n");
        } else if (result.openness < 0.3f) {
            insights.append("Enclosed environment limits visibility and movement but provides cover. ")
                   .append("Close-quarters tactics may be more effective.\n");
        }
        
        if (result.complexity > 0.7f) {
            insights.append("Complex spatial arrangement creates strategic depth but may ")
                   .append("complicate navigation and coordination.\n");
        } else if (result.complexity < 0.3f) {
            insights.append("Simple spatial arrangement allows for clearer tactical planning ")
                   .append("but offers fewer strategic options.\n");
        }
        
        // Interaction potential
        insights.append("Interaction Potential: ")
                .append(Math.round(result.potentialInteractions * 100)).append("%\n");
                
        if (result.potentialInteractions > 0.7f) {
            insights.append("High interaction potential indicates numerous opportunities ")
                   .append("for engagement and environmental manipulation.\n");
        } else if (result.potentialInteractions < 0.3f) {
            insights.append("Low interaction potential suggests limited environmental ")
                   .append("options and more direct engagement requirements.\n");
        }
        
        // Key positions
        if (result.keyPoints != null && result.keyPoints.size() > 0) {
            insights.append("\nKey Positions:\n");
            for (int i = 0; i < result.keyPoints.size(); i++) {
                SpatialReasoningModel.KeyPosition position = result.keyPoints.get(i);
                insights.append(i+1).append(". ").append(position.toString()).append("\n");
            }
        }
        
        return insights.toString();
    }
}
