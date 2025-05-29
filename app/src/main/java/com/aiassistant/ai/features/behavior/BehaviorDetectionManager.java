package com.aiassistant.ai.features.behavior;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Behavior Detection Manager
 * Simplified interface for using the adaptive behavior detection feature
 */
public class BehaviorDetectionManager implements 
    AdaptiveBehaviorDetectionFeature.BehaviorDetectionListener {
    
    private static final String TAG = "BehaviorManager";
    
    private final Context context;
    private final AdaptiveBehaviorDetectionFeature behaviorDetectionFeature;
    private final List<BehaviorEventListener> eventListeners;
    private final Map<String, BehaviorRecommendation> recommendations;
    
    // Current game ID
    private String currentGameId;
    
    /**
     * Constructor
     * @param context Application context
     * @param behaviorDetectionFeature Behavior detection feature
     */
    public BehaviorDetectionManager(Context context, 
                                  AdaptiveBehaviorDetectionFeature behaviorDetectionFeature) {
        this.context = context;
        this.behaviorDetectionFeature = behaviorDetectionFeature;
        this.eventListeners = new ArrayList<>();
        this.recommendations = new HashMap<>();
        this.currentGameId = null;
        
        // Register as listener
        behaviorDetectionFeature.addListener(this);
    }
    
    /**
     * Start behavior tracking for a game
     * @param gameId Game identifier
     */
    public void startTracking(String gameId) {
        if (behaviorDetectionFeature.isEnabled() && !behaviorDetectionFeature.isAnalyzing()) {
            this.currentGameId = gameId;
            
            // Create profile ID using game ID
            String profileId = "profile_" + gameId;
            
            // Start analysis
            behaviorDetectionFeature.startAnalysis(profileId);
        }
    }
    
    /**
     * Stop behavior tracking
     */
    public void stopTracking() {
        if (behaviorDetectionFeature.isEnabled() && behaviorDetectionFeature.isAnalyzing()) {
            behaviorDetectionFeature.stopAnalysis();
            currentGameId = null;
        }
    }
    
    /**
     * Check if tracking is active
     * @return true if tracking
     */
    public boolean isTracking() {
        return behaviorDetectionFeature.isEnabled() && 
               behaviorDetectionFeature.isAnalyzing();
    }
    
    /**
     * Record player action
     * @param category Action category (e.g., "combat", "movement")
     * @param action Specific action (e.g., "attack", "explore")
     * @param value Action value or intensity
     * @return true if action was recorded
     */
    public boolean recordAction(String category, String action, float value) {
        if (behaviorDetectionFeature.isEnabled() && behaviorDetectionFeature.isAnalyzing()) {
            return behaviorDetectionFeature.recordObservation(category, action, value);
        }
        return false;
    }
    
    /**
     * Get current behavior profile
     * @return Current profile or null if not tracking
     */
    public AdaptiveBehaviorDetectionFeature.BehaviorProfile getCurrentProfile() {
        if (behaviorDetectionFeature.isEnabled() && behaviorDetectionFeature.isAnalyzing()) {
            return behaviorDetectionFeature.getCurrentProfile();
        }
        return null;
    }
    
    /**
     * Get current dominant behavior type
     * @return Dominant behavior type or null if not tracking
     */
    public AdaptiveBehaviorDetectionFeature.BehaviorType getDominantBehaviorType() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile != null) {
            return profile.getDominantType();
        }
        return null;
    }
    
    /**
     * Get behavior insights
     * @return List of current behavior insights
     */
    public List<AdaptiveBehaviorDetectionFeature.BehaviorInsight> getBehaviorInsights() {
        if (!behaviorDetectionFeature.isEnabled() || 
            !behaviorDetectionFeature.isAnalyzing() ||
            behaviorDetectionFeature.getCurrentProfile() == null) {
            return new ArrayList<>();
        }
        
        return behaviorDetectionFeature.getInsights(
            behaviorDetectionFeature.getCurrentProfile().getId());
    }
    
    /**
     * Get behavior summary
     * @return Text summary of current behavior profile
     */
    public String getBehaviorSummary() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile != null) {
            return profile.getSummary();
        }
        return "No active behavior profile";
    }
    
    /**
     * Get recommendations based on behavior
     * @return List of behavior recommendations
     */
    public List<BehaviorRecommendation> getRecommendations() {
        // Update recommendations
        updateRecommendations();
        
        // Return all recommendations as a list
        return new ArrayList<>(recommendations.values());
    }
    
    /**
     * Get player's style description
     * @return Description of player's style
     */
    public String getPlayerStyleDescription() {
        AdaptiveBehaviorDetectionFeature.BehaviorType dominantType = getDominantBehaviorType();
        if (dominantType == null) {
            return "Unknown play style";
        }
        
        switch (dominantType) {
            case AGGRESSIVE:
                return "Aggressive player who prioritizes offense and direct confrontation";
                
            case DEFENSIVE:
                return "Defensive player who prioritizes protection and cautious play";
                
            case TACTICAL:
                return "Tactical player who focuses on positioning and strategic advantage";
                
            case EXPLORER:
                return "Explorer who enjoys discovering and investigating environments";
                
            case COLLECTOR:
                return "Resource-focused player who prioritizes gathering and optimization";
                
            case SOCIAL:
                return "Social player who prioritizes interaction and cooperation";
                
            case COMPLETIONIST:
                return "Completionist who aims to finish all objectives and collections";
                
            case REACTIVE:
                return "Reactive player who responds to situations as they occur";
                
            case BALANCED:
            default:
                return "Balanced player with a mix of different play styles";
        }
    }
    
    /**
     * Check if player tends to be aggressive
     * @return true if player shows aggressive tendencies
     */
    public boolean isPlayerAggressive() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile == null) {
            return false;
        }
        
        // Check dominant type
        if (profile.getDominantType() == AdaptiveBehaviorDetectionFeature.BehaviorType.AGGRESSIVE) {
            return true;
        }
        
        // Check aggressive pattern
        if (profile.hasDetectedPattern("aggressive") && 
            profile.getPatternConfidence("aggressive") > 0.7f) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if player tends to be defensive
     * @return true if player shows defensive tendencies
     */
    public boolean isPlayerDefensive() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile == null) {
            return false;
        }
        
        // Check dominant type
        if (profile.getDominantType() == AdaptiveBehaviorDetectionFeature.BehaviorType.DEFENSIVE) {
            return true;
        }
        
        // Check defensive pattern
        if (profile.hasDetectedPattern("defensive") && 
            profile.getPatternConfidence("defensive") > 0.7f) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if player tends to be tactical
     * @return true if player shows tactical tendencies
     */
    public boolean isPlayerTactical() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile == null) {
            return false;
        }
        
        // Check dominant type
        if (profile.getDominantType() == AdaptiveBehaviorDetectionFeature.BehaviorType.TACTICAL) {
            return true;
        }
        
        // Check tactical pattern
        if (profile.hasDetectedPattern("tactical") && 
            profile.getPatternConfidence("tactical") > 0.7f) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get strongest behavior trait
     * @return Name of strongest behavior trait
     */
    public String getStrongestBehaviorTrait() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile == null) {
            return "Unknown";
        }
        
        // Get all pattern confidences
        Map<String, Float> patterns = profile.getDetectedPatterns();
        if (patterns.isEmpty()) {
            return "Balanced";
        }
        
        // Find highest confidence pattern
        String highestPattern = null;
        float highestConfidence = 0.0f;
        
        for (Map.Entry<String, Float> entry : patterns.entrySet()) {
            if (entry.getValue() > highestConfidence) {
                highestConfidence = entry.getValue();
                highestPattern = entry.getKey();
            }
        }
        
        return highestPattern != null ? highestPattern : "Balanced";
    }
    
    /**
     * Add a behavior event listener
     * @param listener Listener to add
     */
    public void addEventListener(BehaviorEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Remove a behavior event listener
     * @param listener Listener to remove
     */
    public void removeEventListener(BehaviorEventListener listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * Update recommendations based on current behavior profile
     */
    private void updateRecommendations() {
        AdaptiveBehaviorDetectionFeature.BehaviorProfile profile = getCurrentProfile();
        if (profile == null) {
            return;
        }
        
        // Get dominant behavior type
        AdaptiveBehaviorDetectionFeature.BehaviorType dominantType = profile.getDominantType();
        
        // Create type-based recommendations
        createTypeBasedRecommendations(dominantType);
        
        // Create pattern-based recommendations
        createPatternBasedRecommendations(profile);
        
        // Create insight-based recommendations
        createInsightBasedRecommendations();
    }
    
    /**
     * Create recommendations based on behavior type
     * @param dominantType Dominant behavior type
     */
    private void createTypeBasedRecommendations(AdaptiveBehaviorDetectionFeature.BehaviorType dominantType) {
        switch (dominantType) {
            case AGGRESSIVE:
                recommendations.put("aggressive_combat", new BehaviorRecommendation(
                    "aggressive_combat",
                    "Offensive Combat Options",
                    "Provide more offensive combat options and aggressive tactics for this player's style",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
                
            case DEFENSIVE:
                recommendations.put("defensive_options", new BehaviorRecommendation(
                    "defensive_options",
                    "Defensive Strategy Focus",
                    "Prioritize defensive strategy information and protective options",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
                
            case TACTICAL:
                recommendations.put("tactical_analysis", new BehaviorRecommendation(
                    "tactical_analysis",
                    "Enhanced Tactical Analysis",
                    "Provide detailed tactical analysis and positional advantage information",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
                
            case EXPLORER:
                recommendations.put("exploration_focus", new BehaviorRecommendation(
                    "exploration_focus",
                    "Exploration Enhancement",
                    "Emphasize map exploration features and discovery assistance",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
                
            case COLLECTOR:
                recommendations.put("resource_optimization", new BehaviorRecommendation(
                    "resource_optimization",
                    "Resource Collection Focus",
                    "Prioritize resource tracking and optimization features",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
                
            case COMPLETIONIST:
                recommendations.put("completion_tracking", new BehaviorRecommendation(
                    "completion_tracking",
                    "Completion Tracking",
                    "Provide comprehensive objective tracking and completion statistics",
                    RecommendationType.PLAYER_PREFERENCE
                ));
                break;
        }
    }
    
    /**
     * Create recommendations based on detected patterns
     * @param profile Behavior profile
     */
    private void createPatternBasedRecommendations(AdaptiveBehaviorDetectionFeature.BehaviorProfile profile) {
        // Check for specific pattern combinations
        
        // Aggressive + Tactical pattern
        if (profile.hasDetectedPattern("aggressive") && profile.hasDetectedPattern("tactical")) {
            recommendations.put("strategic_aggression", new BehaviorRecommendation(
                "strategic_aggression",
                "Strategic Aggression Support",
                "Support strategic aggressive play with advanced tactical options",
                RecommendationType.COMBAT_ENHANCEMENT
            ));
        }
        
        // Defensive + Resource focused pattern
        if (profile.hasDetectedPattern("defensive") && profile.hasDetectedPattern("resource_focused")) {
            recommendations.put("fortification", new BehaviorRecommendation(
                "fortification",
                "Fortification Strategy",
                "Provide defensive resource allocation strategies and fortification options",
                RecommendationType.STRATEGIC_ADVICE
            ));
        }
        
        // Exploratory + Completionist pattern
        if (profile.hasDetectedPattern("exploratory") && profile.hasDetectedPattern("completionist")) {
            recommendations.put("systematic_exploration", new BehaviorRecommendation(
                "systematic_exploration",
                "Systematic Exploration Tools",
                "Offer tools for systematic exploration and completion tracking",
                RecommendationType.FEATURE_ADAPTATION
            ));
        }
    }
    
    /**
     * Create recommendations based on behavior insights
     */
    private void createInsightBasedRecommendations() {
        List<AdaptiveBehaviorDetectionFeature.BehaviorInsight> insights = getBehaviorInsights();
        
        for (AdaptiveBehaviorDetectionFeature.BehaviorInsight insight : insights) {
            // High confidence insights only
            if (insight.getConfidence() < 0.75f) {
                continue;
            }
            
            // Create insight-specific recommendations
            switch (insight.getId()) {
                case "combat_aggression_trend":
                    recommendations.put("combat_training", new BehaviorRecommendation(
                        "combat_training",
                        "Advanced Combat Training",
                        "Offer advanced combat techniques suited to aggressive playstyle",
                        RecommendationType.SKILL_DEVELOPMENT
                    ));
                    break;
                    
                case "efficient_resource_trend":
                    recommendations.put("resource_mastery", new BehaviorRecommendation(
                        "resource_mastery",
                        "Resource Mastery Tools",
                        "Provide advanced resource management tools for optimization",
                        RecommendationType.FEATURE_ADAPTATION
                    ));
                    break;
                    
                case "exploratory_movement_trend":
                    recommendations.put("exploration_tools", new BehaviorRecommendation(
                        "exploration_tools",
                        "Exploration Enhancement",
                        "Provide advanced map and navigation tools for explorers",
                        RecommendationType.FEATURE_ADAPTATION
                    ));
                    break;
                    
                case "tactical_movement_trend":
                    recommendations.put("tactical_movement", new BehaviorRecommendation(
                        "tactical_movement",
                        "Tactical Movement Analysis",
                        "Offer advanced positioning and tactical movement analysis",
                        RecommendationType.COMBAT_ENHANCEMENT
                    ));
                    break;
            }
        }
    }
    
    // BehaviorDetectionListener implementation
    
    @Override
    public void onAnalysisStarted(AdaptiveBehaviorDetectionFeature.BehaviorProfile profile) {
        // Notify listeners
        for (BehaviorEventListener listener : eventListeners) {
            listener.onBehaviorTrackingStarted(profile.getId());
        }
    }
    
    @Override
    public void onAnalysisStopped(AdaptiveBehaviorDetectionFeature.BehaviorProfile profile) {
        // Notify listeners
        for (BehaviorEventListener listener : eventListeners) {
            listener.onBehaviorTrackingStopped();
        }
    }
    
    @Override
    public void onBehaviorObserved(
            AdaptiveBehaviorDetectionFeature.BehaviorProfile profile, 
            AdaptiveBehaviorDetectionFeature.BehaviorObservation observation) {
        // Notify listeners
        for (BehaviorEventListener listener : eventListeners) {
            listener.onPlayerActionObserved(
                observation.getCategory(), 
                observation.getAction());
        }
    }
    
    @Override
    public void onPatternDetected(
            AdaptiveBehaviorDetectionFeature.BehaviorProfile profile, 
            AdaptiveBehaviorDetectionFeature.BehaviorPattern pattern, 
            float confidence) {
        // Notify listeners
        for (BehaviorEventListener listener : eventListeners) {
            listener.onBehaviorPatternDetected(
                pattern.getName(), confidence);
        }
    }
    
    @Override
    public void onBehaviorAnalysisUpdated(AdaptiveBehaviorDetectionFeature.BehaviorProfile profile) {
        // Update recommendations
        updateRecommendations();
        
        // Notify listeners
        for (BehaviorEventListener listener : eventListeners) {
            listener.onBehaviorAnalysisUpdated(
                profile.getDominantType().toString());
        }
    }
    
    @Override
    public void onInsightsUpdated(
            AdaptiveBehaviorDetectionFeature.BehaviorProfile profile, 
            List<AdaptiveBehaviorDetectionFeature.BehaviorInsight> insights) {
        // Update recommendations
        updateRecommendations();
        
        // Notify listeners only if there are insights
        if (!insights.isEmpty()) {
            for (BehaviorEventListener listener : eventListeners) {
                listener.onBehaviorInsightsUpdated(insights.size());
            }
        }
    }
    
    /**
     * Recommendation Type enum
     * Categories of behavior recommendations
     */
    public enum RecommendationType {
        PLAYER_PREFERENCE,  // Based on player's general preferences
        COMBAT_ENHANCEMENT, // Related to combat improvements
        STRATEGIC_ADVICE,   // General strategic recommendations
        SKILL_DEVELOPMENT,  // Suggestions for skill improvement
        FEATURE_ADAPTATION  // Adjustments to feature behavior
    }
    
    /**
     * Behavior Recommendation class
     * Represents a recommendation based on behavior analysis
     */
    public static class BehaviorRecommendation {
        private final String id;
        private final String title;
        private final String description;
        private final RecommendationType type;
        
        /**
         * Constructor
         * @param id Recommendation ID
         * @param title Recommendation title
         * @param description Recommendation description
         * @param type Recommendation type
         */
        public BehaviorRecommendation(String id, String title, String description, 
                                     RecommendationType type) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
        }
        
        /**
         * Get recommendation ID
         * @return Recommendation ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get recommendation title
         * @return Recommendation title
         */
        public String getTitle() {
            return title;
        }
        
        /**
         * Get recommendation description
         * @return Recommendation description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Get recommendation type
         * @return Recommendation type
         */
        public RecommendationType getType() {
            return type;
        }
        
        @Override
        public String toString() {
            return title + ": " + description;
        }
    }
    
    /**
     * Behavior Event Listener interface
     * For receiving behavior events
     */
    public interface BehaviorEventListener {
        /**
         * Called when behavior tracking starts
         * @param profileId Profile ID
         */
        void onBehaviorTrackingStarted(String profileId);
        
        /**
         * Called when behavior tracking stops
         */
        void onBehaviorTrackingStopped();
        
        /**
         * Called when player action is observed
         * @param category Action category
         * @param action Action name
         */
        void onPlayerActionObserved(String category, String action);
        
        /**
         * Called when behavior pattern is detected
         * @param patternName Pattern name
         * @param confidence Detection confidence
         */
        void onBehaviorPatternDetected(String patternName, float confidence);
        
        /**
         * Called when behavior analysis is updated
         * @param dominantType Dominant behavior type
         */
        void onBehaviorAnalysisUpdated(String dominantType);
        
        /**
         * Called when behavior insights are updated
         * @param insightCount Number of insights
         */
        void onBehaviorInsightsUpdated(int insightCount);
    }
}
