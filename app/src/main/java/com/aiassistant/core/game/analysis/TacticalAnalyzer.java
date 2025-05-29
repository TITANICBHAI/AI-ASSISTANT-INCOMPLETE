package com.aiassistant.core.game.analysis;

import com.aiassistant.core.ai.neural.TacticalAnalysisModel;

/**
 * Analyzes tactical situations to generate insights and recommendations
 */
public class TacticalAnalyzer {
    
    /**
     * Generate insights based on tactical analysis results
     * @param result Tactical analysis result
     * @return Tactical insights text
     */
    public String generateInsights(TacticalAnalysisModel.TacticalAnalysisResult result) {
        if (result == null) {
            return "No tactical data available";
        }
        
        StringBuilder insights = new StringBuilder();
        
        // Advantage assessment
        insights.append("Tactical Position:\n");
        insights.append("Overall Advantage: ").append(formatAdvantage(result.overallAdvantage)).append("\n");
        insights.append("Positional Advantage: ").append(formatAdvantage(result.positionalAdvantage)).append("\n");
        insights.append("Resource Advantage: ").append(formatAdvantage(result.resourceAdvantage)).append("\n");
        insights.append("Time Advantage: ").append(formatAdvantage(result.timeAdvantage)).append("\n");
        
        // Tactical metrics
        insights.append("\nTactical Metrics:\n");
        insights.append("Combat Readiness: ").append(Math.round(result.combatReadiness * 100)).append("%\n");
        insights.append("Detection Risk: ").append(Math.round(result.detectionRisk * 100)).append("%\n");
        insights.append("Environmental Threat: ").append(Math.round(result.environmentalThreat * 100)).append("%\n");
        insights.append("Surprise Potential: ").append(Math.round(result.surprisePotential * 100)).append("%\n");
        
        // Engagement assessment
        insights.append("\nEngagement Assessment:\n");
        insights.append("Optimal Engagement Range: ").append(Math.round(result.optimalRangeEngagement)).append(" units\n");
        insights.append("Escape Viability: ").append(Math.round(result.escapeViability * 100)).append("%\n");
        insights.append("Counter-Attack Potential: ").append(Math.round(result.counterAttackPotential * 100)).append("%\n");
        insights.append("Defensive Position Quality: ").append(Math.round(result.defensivePositionQuality * 100)).append("%\n");
        insights.append("Offensive Opportunity: ").append(Math.round(result.offensiveOpportunity * 100)).append("%\n");
        
        // Threats
        if (result.identifiedThreats != null && !result.identifiedThreats.isEmpty()) {
            insights.append("\nIdentified Threats:\n");
            for (int i = 0; i < result.identifiedThreats.size(); i++) {
                insights.append(i+1).append(". ")
                       .append(result.identifiedThreats.get(i).toString()).append("\n");
            }
        }
        
        // Opportunities
        if (result.identifiedOpportunities != null && !result.identifiedOpportunities.isEmpty()) {
            insights.append("\nIdentified Opportunities:\n");
            for (int i = 0; i < result.identifiedOpportunities.size(); i++) {
                insights.append(i+1).append(". ")
                       .append(result.identifiedOpportunities.get(i).toString()).append("\n");
            }
        }
        
        // Recommendations
        if (result.tacticalRecommendations != null && !result.tacticalRecommendations.isEmpty()) {
            insights.append("\nTactical Recommendations:\n");
            for (int i = 0; i < result.tacticalRecommendations.size(); i++) {
                insights.append(i+1).append(". ")
                       .append(result.tacticalRecommendations.get(i).toString()).append("\n");
            }
        }
        
        return insights.toString();
    }
    
    /**
     * Format advantage value for display
     * @param advantage Advantage value (-1 to 1)
     * @return Formatted advantage string
     */
    private String formatAdvantage(float advantage) {
        String percentage = Math.round(Math.abs(advantage) * 100) + "%";
        if (advantage > 0.05f) {
            return "+" + percentage + " (Advantage)";
        } else if (advantage < -0.05f) {
            return "-" + percentage + " (Disadvantage)";
        } else {
            return "Neutral";
        }
    }
}
