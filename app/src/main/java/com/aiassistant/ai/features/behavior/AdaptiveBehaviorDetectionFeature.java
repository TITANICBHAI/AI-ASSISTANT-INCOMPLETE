package com.aiassistant.ai.features.behavior;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptive Behavior Detection Feature
 * - Identifies and analyzes player behavior patterns
 * - Adapts to changing player tactics and strategies
 * - Provides personalized insights based on behavior
 * - Tracks behavioral metrics and trends over time
 */
public class AdaptiveBehaviorDetectionFeature extends BaseFeature {
    private static final String TAG = "BehaviorDetection";
    private static final String FEATURE_NAME = "adaptive_behavior_detection";
    
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 1000;
    
    // Last update timestamp
    private long lastUpdateTime;
    
    // Player behavior profiles
    private final Map<String, BehaviorProfile> behaviorProfiles;
    
    // Behavior patterns registry
    private final Map<String, BehaviorPattern> behaviorPatterns;
    
    // Current active profile
    private String currentProfileId;
    
    // Behavior observation history
    private final Map<String, List<BehaviorObservation>> observationHistory;
    
    // Behavior insight registry
    private final Map<String, BehaviorInsight> insightRegistry;
    
    // Listeners for behavior events
    private final List<BehaviorDetectionListener> listeners;
    
    // Analysis state
    private boolean isAnalyzing;
    
    // Learning rate (0.0-1.0)
    private float learningRate;
    
    // Confidence threshold (0.0-1.0)
    private float confidenceThreshold;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AdaptiveBehaviorDetectionFeature(Context context) {
        super(context, FEATURE_NAME);
        this.lastUpdateTime = 0;
        this.behaviorProfiles = new ConcurrentHashMap<>();
        this.behaviorPatterns = new ConcurrentHashMap<>();
        this.currentProfileId = null;
        this.observationHistory = new ConcurrentHashMap<>();
        this.insightRegistry = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.isAnalyzing = false;
        this.learningRate = 0.2f;
        this.confidenceThreshold = 0.65f;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Register default behavior patterns
                registerDefaultPatterns();
                
                Log.d(TAG, "Behavior detection system initialized with " +
                      behaviorPatterns.size() + " patterns");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize behavior detection", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled() || !isAnalyzing) return;
        
        // Check if update is needed
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update behavior analysis
            updateBehaviorAnalysis();
            
            // Generate insights
            generateBehaviorInsights();
            
            // Update timestamp
            lastUpdateTime = currentTime;
        } catch (Exception e) {
            Log.e(TAG, "Error updating behavior detection", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Stop analysis
        stopAnalysis();
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start behavior analysis
     * @param profileId Profile ID
     * @return Created or existing profile
     */
    public BehaviorProfile startAnalysis(String profileId) {
        if (!isEnabled()) return null;
        
        // Check if profile exists
        BehaviorProfile profile = behaviorProfiles.get(profileId);
        
        // Create new profile if not found
        if (profile == null) {
            profile = new BehaviorProfile(profileId);
            behaviorProfiles.put(profileId, profile);
            
            // Initialize observation history
            observationHistory.put(profileId, new ArrayList<>());
        }
        
        // Set as current profile
        currentProfileId = profileId;
        
        // Set analyzing state
        isAnalyzing = true;
        
        Log.d(TAG, "Started behavior analysis for profile: " + profileId);
        
        // Notify listeners
        for (BehaviorDetectionListener listener : listeners) {
            listener.onAnalysisStarted(profile);
        }
        
        return profile;
    }
    
    /**
     * Stop behavior analysis
     */
    public void stopAnalysis() {
        if (!isAnalyzing) return;
        
        isAnalyzing = false;
        
        Log.d(TAG, "Stopped behavior analysis");
        
        // Notify listeners
        if (currentProfileId != null) {
            BehaviorProfile profile = behaviorProfiles.get(currentProfileId);
            if (profile != null) {
                for (BehaviorDetectionListener listener : listeners) {
                    listener.onAnalysisStopped(profile);
                }
            }
        }
    }
    
    /**
     * Check if analysis is running
     * @return true if analyzing
     */
    public boolean isAnalyzing() {
        return isAnalyzing;
    }
    
    /**
     * Get current profile
     * @return Current profile or null if none
     */
    public BehaviorProfile getCurrentProfile() {
        if (currentProfileId == null) return null;
        return behaviorProfiles.get(currentProfileId);
    }
    
    /**
     * Get profile by ID
     * @param profileId Profile ID
     * @return Behavior profile or null if not found
     */
    public BehaviorProfile getProfile(String profileId) {
        return behaviorProfiles.get(profileId);
    }
    
    /**
     * Get all behavior profiles
     * @return Map of all profiles
     */
    public Map<String, BehaviorProfile> getAllProfiles() {
        return new HashMap<>(behaviorProfiles);
    }
    
    /**
     * Register a behavior pattern
     * @param pattern Behavior pattern
     */
    public void registerPattern(BehaviorPattern pattern) {
        if (pattern != null) {
            behaviorPatterns.put(pattern.getId(), pattern);
            Log.d(TAG, "Registered behavior pattern: " + pattern.getName());
        }
    }
    
    /**
     * Get a behavior pattern
     * @param patternId Pattern ID
     * @return Behavior pattern or null if not found
     */
    public BehaviorPattern getPattern(String patternId) {
        return behaviorPatterns.get(patternId);
    }
    
    /**
     * Get all behavior patterns
     * @return Map of all patterns
     */
    public Map<String, BehaviorPattern> getAllPatterns() {
        return new HashMap<>(behaviorPatterns);
    }
    
    /**
     * Record a behavior observation
     * @param category Behavior category
     * @param action Action performed
     * @param value Value associated with action (optional)
     * @return true if observation was recorded
     */
    public boolean recordObservation(String category, String action, float value) {
        if (!isEnabled() || !isAnalyzing || currentProfileId == null) {
            return false;
        }
        
        // Get current profile
        BehaviorProfile profile = behaviorProfiles.get(currentProfileId);
        if (profile == null) {
            return false;
        }
        
        // Create observation
        BehaviorObservation observation = new BehaviorObservation(
            category, action, value, System.currentTimeMillis());
        
        // Add to history
        List<BehaviorObservation> history = observationHistory.get(currentProfileId);
        if (history != null) {
            history.add(observation);
            
            // Limit history size
            while (history.size() > 1000) {
                history.remove(0);
            }
        }
        
        // Update metrics in profile
        updateProfileMetrics(profile, observation);
        
        // Detect patterns
        detectPatterns(profile, observation);
        
        Log.d(TAG, "Recorded behavior observation: " + category + " - " + action);
        
        // Notify listeners
        for (BehaviorDetectionListener listener : listeners) {
            listener.onBehaviorObserved(profile, observation);
        }
        
        return true;
    }
    
    /**
     * Get behavior observations for a profile
     * @param profileId Profile ID
     * @return List of observations or empty list if not found
     */
    public List<BehaviorObservation> getObservations(String profileId) {
        List<BehaviorObservation> history = observationHistory.get(profileId);
        if (history != null) {
            return new ArrayList<>(history);
        }
        return new ArrayList<>();
    }
    
    /**
     * Get behavior insights for a profile
     * @param profileId Profile ID
     * @return List of insights
     */
    public List<BehaviorInsight> getInsights(String profileId) {
        BehaviorProfile profile = behaviorProfiles.get(profileId);
        if (profile == null) {
            return new ArrayList<>();
        }
        
        List<BehaviorInsight> insights = new ArrayList<>();
        
        // Get active insights for this profile
        for (BehaviorInsight insight : insightRegistry.values()) {
            if (insight.isRelevantFor(profileId) && insight.getConfidence() >= confidenceThreshold) {
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    /**
     * Get dominant behavior type for a profile
     * @param profileId Profile ID
     * @return Dominant behavior type or null if not enough data
     */
    public BehaviorType getDominantBehaviorType(String profileId) {
        BehaviorProfile profile = behaviorProfiles.get(profileId);
        if (profile == null) {
            return null;
        }
        
        return profile.getDominantType();
    }
    
    /**
     * Set learning rate
     * @param rate Learning rate (0.0-1.0)
     */
    public void setLearningRate(float rate) {
        this.learningRate = Math.max(0.0f, Math.min(1.0f, rate));
        Log.d(TAG, "Learning rate set to " + learningRate);
    }
    
    /**
     * Get learning rate
     * @return Learning rate
     */
    public float getLearningRate() {
        return learningRate;
    }
    
    /**
     * Set confidence threshold
     * @param threshold Confidence threshold (0.0-1.0)
     */
    public void setConfidenceThreshold(float threshold) {
        this.confidenceThreshold = Math.max(0.0f, Math.min(1.0f, threshold));
        Log.d(TAG, "Confidence threshold set to " + confidenceThreshold);
    }
    
    /**
     * Get confidence threshold
     * @return Confidence threshold
     */
    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }
    
    /**
     * Add a behavior detection listener
     * @param listener Listener to add
     */
    public void addListener(BehaviorDetectionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a behavior detection listener
     * @param listener Listener to remove
     */
    public void removeListener(BehaviorDetectionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Register default behavior patterns
     */
    private void registerDefaultPatterns() {
        // Create default patterns
        
        // Aggressive Pattern
        BehaviorPattern aggressive = new BehaviorPattern("aggressive", "Aggressive");
        aggressive.addAction("combat", "attack");
        aggressive.addAction("movement", "rush");
        aggressive.setDescription("Player favors aggressive actions and direct confrontation");
        aggressive.setAssociatedType(BehaviorType.AGGRESSIVE);
        behaviorPatterns.put(aggressive.getId(), aggressive);
        
        // Defensive Pattern
        BehaviorPattern defensive = new BehaviorPattern("defensive", "Defensive");
        defensive.addAction("combat", "block");
        defensive.addAction("movement", "retreat");
        defensive.setDescription("Player prioritizes defensive positioning and cautious actions");
        defensive.setAssociatedType(BehaviorType.DEFENSIVE);
        behaviorPatterns.put(defensive.getId(), defensive);
        
        // Tactical Pattern
        BehaviorPattern tactical = new BehaviorPattern("tactical", "Tactical");
        tactical.addAction("combat", "flank");
        tactical.addAction("movement", "position");
        tactical.setDescription("Player demonstrates tactical awareness and positioning");
        tactical.setAssociatedType(BehaviorType.TACTICAL);
        behaviorPatterns.put(tactical.getId(), tactical);
        
        // Exploratory Pattern
        BehaviorPattern exploratory = new BehaviorPattern("exploratory", "Exploratory");
        exploratory.addAction("movement", "explore");
        exploratory.addAction("interaction", "examine");
        exploratory.setDescription("Player tends to explore and examine the environment thoroughly");
        exploratory.setAssociatedType(BehaviorType.EXPLORER);
        behaviorPatterns.put(exploratory.getId(), exploratory);
        
        // Resource Focused Pattern
        BehaviorPattern resourceFocused = new BehaviorPattern("resource_focused", "Resource Focused");
        resourceFocused.addAction("resource", "collect");
        resourceFocused.addAction("resource", "optimize");
        resourceFocused.setDescription("Player prioritizes resource collection and management");
        resourceFocused.setAssociatedType(BehaviorType.COLLECTOR);
        behaviorPatterns.put(resourceFocused.getId(), resourceFocused);
        
        // Social Pattern
        BehaviorPattern social = new BehaviorPattern("social", "Social");
        social.addAction("social", "communicate");
        social.addAction("social", "cooperate");
        social.setDescription("Player engages in social interactions and team coordination");
        social.setAssociatedType(BehaviorType.SOCIAL);
        behaviorPatterns.put(social.getId(), social);
        
        // Completionist Pattern
        BehaviorPattern completionist = new BehaviorPattern("completionist", "Completionist");
        completionist.addAction("objective", "complete");
        completionist.addAction("collection", "collect");
        completionist.setDescription("Player seeks to complete all objectives and collections");
        completionist.setAssociatedType(BehaviorType.COMPLETIONIST);
        behaviorPatterns.put(completionist.getId(), completionist);
        
        // Reactive Pattern
        BehaviorPattern reactive = new BehaviorPattern("reactive", "Reactive");
        reactive.addAction("response", "react");
        reactive.addAction("timing", "delay");
        reactive.setDescription("Player tends to react to situations rather than initiating");
        reactive.setAssociatedType(BehaviorType.REACTIVE);
        behaviorPatterns.put(reactive.getId(), reactive);
    }
    
    /**
     * Update profile metrics based on observation
     * @param profile Profile to update
     * @param observation New observation
     */
    private void updateProfileMetrics(BehaviorProfile profile, BehaviorObservation observation) {
        // Update action counter
        String actionKey = observation.getCategory() + ":" + observation.getAction();
        int count = profile.getActionCount(actionKey);
        profile.setActionCount(actionKey, count + 1);
        
        // Update category metrics
        float categoryValue = profile.getCategoryMetric(observation.getCategory());
        float newValue = (categoryValue * (1.0f - learningRate)) + (observation.getValue() * learningRate);
        profile.setCategoryMetric(observation.getCategory(), newValue);
        
        // Update behavior type weights
        updateBehaviorTypeWeights(profile, observation);
    }
    
    /**
     * Update behavior type weights based on observation
     * @param profile Profile to update
     * @param observation New observation
     */
    private void updateBehaviorTypeWeights(BehaviorProfile profile, BehaviorObservation observation) {
        // Check each pattern to see if this observation matches
        for (BehaviorPattern pattern : behaviorPatterns.values()) {
            if (pattern.matchesObservation(observation)) {
                // Get associated behavior type
                BehaviorType type = pattern.getAssociatedType();
                if (type != null) {
                    // Update weight for this type
                    float weight = profile.getTypeWeight(type);
                    float newWeight = (weight * (1.0f - learningRate)) + (1.0f * learningRate);
                    profile.setTypeWeight(type, newWeight);
                    
                    Log.d(TAG, "Updated " + type + " weight to " + newWeight + 
                          " for profile " + profile.getId());
                }
            }
        }
        
        // Normalize weights
        profile.normalizeTypeWeights();
    }
    
    /**
     * Detect patterns based on observation
     * @param profile Profile to update
     * @param observation New observation
     */
    private void detectPatterns(BehaviorProfile profile, BehaviorObservation observation) {
        // For each pattern, check if there's enough evidence to detect it
        for (BehaviorPattern pattern : behaviorPatterns.values()) {
            float patternConfidence = calculatePatternConfidence(profile, pattern);
            
            // If confidence exceeds threshold and pattern is not already detected
            if (patternConfidence >= confidenceThreshold && 
                !profile.hasDetectedPattern(pattern.getId())) {
                
                // Record detection
                profile.addDetectedPattern(pattern.getId(), patternConfidence);
                
                Log.d(TAG, "Detected pattern " + pattern.getName() + 
                      " with confidence " + patternConfidence);
                
                // Notify listeners
                for (BehaviorDetectionListener listener : listeners) {
                    listener.onPatternDetected(profile, pattern, patternConfidence);
                }
            }
            // Update confidence for already detected patterns
            else if (profile.hasDetectedPattern(pattern.getId())) {
                profile.updatePatternConfidence(pattern.getId(), patternConfidence);
            }
        }
    }
    
    /**
     * Calculate confidence for a pattern
     * @param profile Behavior profile
     * @param pattern Pattern to evaluate
     * @return Confidence value (0.0-1.0)
     */
    private float calculatePatternConfidence(BehaviorProfile profile, BehaviorPattern pattern) {
        float confidence = 0.0f;
        int patternActions = 0;
        int matchedActions = 0;
        
        // Check each action in the pattern
        for (String category : pattern.getCategories()) {
            for (String action : pattern.getActionsForCategory(category)) {
                patternActions++;
                
                // Check if this action has been observed
                String actionKey = category + ":" + action;
                int count = profile.getActionCount(actionKey);
                
                if (count > 0) {
                    matchedActions++;
                }
            }
        }
        
        // Calculate basic confidence based on matched actions
        if (patternActions > 0) {
            confidence = (float) matchedActions / patternActions;
        }
        
        // Adjust confidence based on behavior type match
        BehaviorType patternType = pattern.getAssociatedType();
        if (patternType != null) {
            float typeWeight = profile.getTypeWeight(patternType);
            confidence = (confidence + typeWeight) / 2.0f;
        }
        
        return confidence;
    }
    
    /**
     * Update behavior analysis
     */
    private void updateBehaviorAnalysis() {
        if (currentProfileId == null) {
            return;
        }
        
        BehaviorProfile profile = behaviorProfiles.get(currentProfileId);
        if (profile == null) {
            return;
        }
        
        // Update dominant behavior type
        profile.updateDominantType();
        
        // Notify listeners of update
        for (BehaviorDetectionListener listener : listeners) {
            listener.onBehaviorAnalysisUpdated(profile);
        }
    }
    
    /**
     * Generate behavior insights
     */
    private void generateBehaviorInsights() {
        if (currentProfileId == null) {
            return;
        }
        
        BehaviorProfile profile = behaviorProfiles.get(currentProfileId);
        if (profile == null) {
            return;
        }
        
        // Check for pattern combinations and generate insights
        generatePatternBasedInsights(profile);
        
        // Check for trend-based insights
        generateTrendBasedInsights(profile);
        
        // Get active insights
        List<BehaviorInsight> activeInsights = getInsights(currentProfileId);
        
        // Notify listeners
        if (!activeInsights.isEmpty()) {
            for (BehaviorDetectionListener listener : listeners) {
                listener.onInsightsUpdated(profile, activeInsights);
            }
        }
    }
    
    /**
     * Generate insights based on pattern combinations
     * @param profile Behavior profile
     */
    private void generatePatternBasedInsights(BehaviorProfile profile) {
        // Check if aggressive and tactical patterns are both detected
        if (profile.hasDetectedPattern("aggressive") && profile.hasDetectedPattern("tactical")) {
            float aggressiveConfidence = profile.getPatternConfidence("aggressive");
            float tacticalConfidence = profile.getPatternConfidence("tactical");
            float combinedConfidence = (aggressiveConfidence + tacticalConfidence) / 2.0f;
            
            // Create strategic aggressor insight
            BehaviorInsight insight = new BehaviorInsight(
                "strategic_aggressor",
                "Strategic Aggressor",
                "Player combines aggressive actions with tactical awareness",
                combinedConfidence
            );
            insight.addRelevantProfile(profile.getId());
            insightRegistry.put(insight.getId(), insight);
        }
        
        // Check if defensive and resource_focused patterns are both detected
        if (profile.hasDetectedPattern("defensive") && profile.hasDetectedPattern("resource_focused")) {
            float defensiveConfidence = profile.getPatternConfidence("defensive");
            float resourceConfidence = profile.getPatternConfidence("resource_focused");
            float combinedConfidence = (defensiveConfidence + resourceConfidence) / 2.0f;
            
            // Create defensive builder insight
            BehaviorInsight insight = new BehaviorInsight(
                "defensive_builder",
                "Defensive Builder",
                "Player focuses on building resources while maintaining strong defenses",
                combinedConfidence
            );
            insight.addRelevantProfile(profile.getId());
            insightRegistry.put(insight.getId(), insight);
        }
        
        // Check if exploratory and completionist patterns are both detected
        if (profile.hasDetectedPattern("exploratory") && profile.hasDetectedPattern("completionist")) {
            float exploratoryConfidence = profile.getPatternConfidence("exploratory");
            float completionistConfidence = profile.getPatternConfidence("completionist");
            float combinedConfidence = (exploratoryConfidence + completionistConfidence) / 2.0f;
            
            // Create thorough explorer insight
            BehaviorInsight insight = new BehaviorInsight(
                "thorough_explorer",
                "Thorough Explorer",
                "Player systematically explores and completes all available content",
                combinedConfidence
            );
            insight.addRelevantProfile(profile.getId());
            insightRegistry.put(insight.getId(), insight);
        }
    }
    
    /**
     * Generate insights based on behavior trends
     * @param profile Behavior profile
     */
    private void generateTrendBasedInsights(BehaviorProfile profile) {
        // Get observation history
        List<BehaviorObservation> history = observationHistory.get(profile.getId());
        if (history == null || history.size() < 10) {
            return; // Not enough data
        }
        
        // Analyze recent combat trends
        analyzeCombatTrends(profile, history);
        
        // Analyze resource management trends
        analyzeResourceTrends(profile, history);
        
        // Analyze movement patterns
        analyzeMovementTrends(profile, history);
    }
    
    /**
     * Analyze combat trends
     * @param profile Behavior profile
     * @param history Observation history
     */
    private void analyzeCombatTrends(BehaviorProfile profile, List<BehaviorObservation> history) {
        // Count recent combat actions
        int combatActions = 0;
        int aggressiveActions = 0;
        
        // Look at most recent 50 observations
        int startIndex = Math.max(0, history.size() - 50);
        for (int i = startIndex; i < history.size(); i++) {
            BehaviorObservation obs = history.get(i);
            if (obs.getCategory().equals("combat")) {
                combatActions++;
                
                // Check for aggressive actions
                if (obs.getAction().equals("attack") || 
                    obs.getAction().equals("rush") ||
                    obs.getAction().equals("flank")) {
                    aggressiveActions++;
                }
            }
        }
        
        // If enough combat actions and mostly aggressive
        if (combatActions >= 10 && aggressiveActions > (combatActions * 0.7f)) {
            float confidence = (float) aggressiveActions / combatActions;
            
            // Create combat aggression insight
            BehaviorInsight insight = new BehaviorInsight(
                "combat_aggression_trend",
                "Increasing Combat Aggression",
                "Player is showing an increasing preference for aggressive combat tactics",
                confidence
            );
            insight.addRelevantProfile(profile.getId());
            insightRegistry.put(insight.getId(), insight);
        }
    }
    
    /**
     * Analyze resource trends
     * @param profile Behavior profile
     * @param history Observation history
     */
    private void analyzeResourceTrends(BehaviorProfile profile, List<BehaviorObservation> history) {
        // Count resource actions
        int resourceActions = 0;
        float totalEfficiency = 0.0f;
        
        // Look at most recent 50 observations
        int startIndex = Math.max(0, history.size() - 50);
        for (int i = startIndex; i < history.size(); i++) {
            BehaviorObservation obs = history.get(i);
            if (obs.getCategory().equals("resource")) {
                resourceActions++;
                totalEfficiency += obs.getValue();
            }
        }
        
        // If enough resource actions and high efficiency
        if (resourceActions >= 10) {
            float avgEfficiency = totalEfficiency / resourceActions;
            
            if (avgEfficiency > 0.7f) {
                // Create efficient resource management insight
                BehaviorInsight insight = new BehaviorInsight(
                    "efficient_resource_trend",
                    "Efficient Resource Management",
                    "Player demonstrates high efficiency in resource collection and allocation",
                    avgEfficiency
                );
                insight.addRelevantProfile(profile.getId());
                insightRegistry.put(insight.getId(), insight);
            }
        }
    }
    
    /**
     * Analyze movement trends
     * @param profile Behavior profile
     * @param history Observation history
     */
    private void analyzeMovementTrends(BehaviorProfile profile, List<BehaviorObservation> history) {
        // Count movement action types
        int moveActions = 0;
        int exploratoryMoves = 0;
        int tacticalMoves = 0;
        
        // Look at most recent 50 observations
        int startIndex = Math.max(0, history.size() - 50);
        for (int i = startIndex; i < history.size(); i++) {
            BehaviorObservation obs = history.get(i);
            if (obs.getCategory().equals("movement")) {
                moveActions++;
                
                // Check movement type
                String action = obs.getAction();
                if (action.equals("explore") || action.equals("examine")) {
                    exploratoryMoves++;
                } else if (action.equals("position") || action.equals("flank")) {
                    tacticalMoves++;
                }
            }
        }
        
        // If enough movement actions
        if (moveActions >= 15) {
            // Check if mostly exploratory
            if (exploratoryMoves > (moveActions * 0.6f)) {
                float confidence = (float) exploratoryMoves / moveActions;
                
                // Create exploratory movement insight
                BehaviorInsight insight = new BehaviorInsight(
                    "exploratory_movement_trend",
                    "Exploratory Movement Pattern",
                    "Player tends to explore the environment thoroughly before proceeding",
                    confidence
                );
                insight.addRelevantProfile(profile.getId());
                insightRegistry.put(insight.getId(), insight);
            }
            // Check if mostly tactical
            else if (tacticalMoves > (moveActions * 0.6f)) {
                float confidence = (float) tacticalMoves / moveActions;
                
                // Create tactical movement insight
                BehaviorInsight insight = new BehaviorInsight(
                    "tactical_movement_trend",
                    "Tactical Movement Pattern",
                    "Player prioritizes tactical positioning and movement",
                    confidence
                );
                insight.addRelevantProfile(profile.getId());
                insightRegistry.put(insight.getId(), insight);
            }
        }
    }
    
    /**
     * Behavior Type enum
     * Represents fundamental player behavior types
     */
    public enum BehaviorType {
        AGGRESSIVE,     // Prioritizes offense and direct confrontation
        DEFENSIVE,      // Prioritizes protection and cautious play
        TACTICAL,       // Focuses on positioning and strategic advantage
        EXPLORER,       // Enjoys discovering and exploring environments
        COLLECTOR,      // Focuses on gathering resources and items
        SOCIAL,         // Prioritizes interaction and cooperation
        COMPLETIONIST,  // Aims to complete all objectives and collections
        REACTIVE,       // Reacts to situations rather than initiating
        BALANCED        // Shows a mix of different behavior types
    }
    
    /**
     * Behavior Profile class
     * Represents a player's behavior profile
     */
    public static class BehaviorProfile {
        private final String id;
        private String name;
        private final Map<String, Integer> actionCounts;
        private final Map<String, Float> categoryMetrics;
        private final Map<BehaviorType, Float> typeWeights;
        private final Map<String, Float> detectedPatterns;
        private BehaviorType dominantType;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Profile ID
         */
        public BehaviorProfile(String id) {
            this.id = id;
            this.name = id;
            this.actionCounts = new HashMap<>();
            this.categoryMetrics = new HashMap<>();
            this.typeWeights = new HashMap<>();
            this.detectedPatterns = new HashMap<>();
            this.dominantType = BehaviorType.BALANCED;
            this.properties = new HashMap<>();
            
            // Initialize type weights
            for (BehaviorType type : BehaviorType.values()) {
                typeWeights.put(type, 0.1f); // Initial even distribution
            }
            typeWeights.put(BehaviorType.BALANCED, 0.2f); // Slight bias toward balanced
        }
        
        /**
         * Get profile ID
         * @return Profile ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get profile name
         * @return Profile name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set profile name
         * @param name Profile name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get action count
         * @param actionKey Action key (category:action)
         * @return Count or 0 if not found
         */
        public int getActionCount(String actionKey) {
            return actionCounts.getOrDefault(actionKey, 0);
        }
        
        /**
         * Set action count
         * @param actionKey Action key (category:action)
         * @param count New count
         */
        public void setActionCount(String actionKey, int count) {
            actionCounts.put(actionKey, count);
        }
        
        /**
         * Get category metric
         * @param category Category name
         * @return Metric value or 0.0 if not found
         */
        public float getCategoryMetric(String category) {
            return categoryMetrics.getOrDefault(category, 0.0f);
        }
        
        /**
         * Set category metric
         * @param category Category name
         * @param value Metric value
         */
        public void setCategoryMetric(String category, float value) {
            categoryMetrics.put(category, value);
        }
        
        /**
         * Get behavior type weight
         * @param type Behavior type
         * @return Weight or 0.0 if not found
         */
        public float getTypeWeight(BehaviorType type) {
            return typeWeights.getOrDefault(type, 0.0f);
        }
        
        /**
         * Set behavior type weight
         * @param type Behavior type
         * @param weight New weight
         */
        public void setTypeWeight(BehaviorType type, float weight) {
            typeWeights.put(type, weight);
        }
        
        /**
         * Normalize type weights
         */
        public void normalizeTypeWeights() {
            float total = 0.0f;
            
            // Calculate total
            for (float weight : typeWeights.values()) {
                total += weight;
            }
            
            // Normalize
            if (total > 0.0f) {
                for (BehaviorType type : typeWeights.keySet()) {
                    float normalizedWeight = typeWeights.get(type) / total;
                    typeWeights.put(type, normalizedWeight);
                }
            }
        }
        
        /**
         * Get dominant behavior type
         * @return Dominant type
         */
        public BehaviorType getDominantType() {
            return dominantType;
        }
        
        /**
         * Update dominant type based on current weights
         */
        public void updateDominantType() {
            BehaviorType highestType = BehaviorType.BALANCED;
            float highestWeight = 0.0f;
            
            // Find type with highest weight
            for (Map.Entry<BehaviorType, Float> entry : typeWeights.entrySet()) {
                if (entry.getValue() > highestWeight) {
                    highestWeight = entry.getValue();
                    highestType = entry.getKey();
                }
            }
            
            // Check if weight is significant enough
            if (highestWeight > 0.3f) {
                dominantType = highestType;
            } else {
                dominantType = BehaviorType.BALANCED;
            }
        }
        
        /**
         * Check if a pattern has been detected
         * @param patternId Pattern ID
         * @return true if pattern detected
         */
        public boolean hasDetectedPattern(String patternId) {
            return detectedPatterns.containsKey(patternId);
        }
        
        /**
         * Add a detected pattern
         * @param patternId Pattern ID
         * @param confidence Detection confidence
         */
        public void addDetectedPattern(String patternId, float confidence) {
            detectedPatterns.put(patternId, confidence);
        }
        
        /**
         * Get pattern confidence
         * @param patternId Pattern ID
         * @return Confidence or 0.0 if not detected
         */
        public float getPatternConfidence(String patternId) {
            return detectedPatterns.getOrDefault(patternId, 0.0f);
        }
        
        /**
         * Update pattern confidence
         * @param patternId Pattern ID
         * @param confidence New confidence
         */
        public void updatePatternConfidence(String patternId, float confidence) {
            if (detectedPatterns.containsKey(patternId)) {
                detectedPatterns.put(patternId, confidence);
            }
        }
        
        /**
         * Get all detected patterns
         * @return Map of pattern IDs to confidence values
         */
        public Map<String, Float> getDetectedPatterns() {
            return new HashMap<>(detectedPatterns);
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
        
        /**
         * Get all action counts
         * @return Map of action keys to counts
         */
        public Map<String, Integer> getAllActionCounts() {
            return new HashMap<>(actionCounts);
        }
        
        /**
         * Get all category metrics
         * @return Map of categories to metric values
         */
        public Map<String, Float> getAllCategoryMetrics() {
            return new HashMap<>(categoryMetrics);
        }
        
        /**
         * Get all type weights
         * @return Map of behavior types to weights
         */
        public Map<BehaviorType, Float> getAllTypeWeights() {
            return new HashMap<>(typeWeights);
        }
        
        /**
         * Get behavior summary
         * @return Text summary of behavior profile
         */
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            
            summary.append("Profile: ").append(name).append("\n");
            summary.append("Dominant Type: ").append(dominantType).append("\n");
            
            // Add top three behavior types
            summary.append("Top Behaviors:\n");
            List<Map.Entry<BehaviorType, Float>> sortedTypes = new ArrayList<>(typeWeights.entrySet());
            sortedTypes.sort((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()));
            
            for (int i = 0; i < Math.min(3, sortedTypes.size()); i++) {
                Map.Entry<BehaviorType, Float> entry = sortedTypes.get(i);
                summary.append("- ").append(entry.getKey())
                       .append(": ").append(String.format("%.2f", entry.getValue() * 100))
                       .append("%\n");
            }
            
            // Add detected patterns
            if (!detectedPatterns.isEmpty()) {
                summary.append("Detected Patterns:\n");
                
                for (Map.Entry<String, Float> entry : detectedPatterns.entrySet()) {
                    summary.append("- ").append(entry.getKey())
                           .append(": ").append(String.format("%.2f", entry.getValue() * 100))
                           .append("%\n");
                }
            }
            
            return summary.toString();
        }
    }
    
    /**
     * Behavior Pattern class
     * Represents a specific behavior pattern
     */
    public static class BehaviorPattern {
        private final String id;
        private String name;
        private String description;
        private final Map<String, List<String>> categoryActions;
        private BehaviorType associatedType;
        
        /**
         * Constructor
         * @param id Pattern ID
         * @param name Pattern name
         */
        public BehaviorPattern(String id, String name) {
            this.id = id;
            this.name = name;
            this.description = "";
            this.categoryActions = new HashMap<>();
            this.associatedType = null;
        }
        
        /**
         * Get pattern ID
         * @return Pattern ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get pattern name
         * @return Pattern name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set pattern name
         * @param name Pattern name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Set description
         * @param description Description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Add an action to the pattern
         * @param category Action category
         * @param action Action name
         */
        public void addAction(String category, String action) {
            List<String> actions = categoryActions.computeIfAbsent(category, k -> new ArrayList<>());
            if (!actions.contains(action)) {
                actions.add(action);
            }
        }
        
        /**
         * Remove an action from the pattern
         * @param category Action category
         * @param action Action name
         * @return true if action was removed
         */
        public boolean removeAction(String category, String action) {
            List<String> actions = categoryActions.get(category);
            return actions != null && actions.remove(action);
        }
        
        /**
         * Get all categories
         * @return Set of categories
         */
        public Set<String> getCategories() {
            return categoryActions.keySet();
        }
        
        /**
         * Get actions for a category
         * @param category Category name
         * @return List of actions or empty list if category not found
         */
        public List<String> getActionsForCategory(String category) {
            List<String> actions = categoryActions.get(category);
            return actions != null ? new ArrayList<>(actions) : new ArrayList<>();
        }
        
        /**
         * Check if pattern contains an action
         * @param category Action category
         * @param action Action name
         * @return true if pattern contains the action
         */
        public boolean containsAction(String category, String action) {
            List<String> actions = categoryActions.get(category);
            return actions != null && actions.contains(action);
        }
        
        /**
         * Check if observation matches this pattern
         * @param observation Behavior observation
         * @return true if observation matches
         */
        public boolean matchesObservation(BehaviorObservation observation) {
            return containsAction(observation.getCategory(), observation.getAction());
        }
        
        /**
         * Get associated behavior type
         * @return Associated type or null if none
         */
        public BehaviorType getAssociatedType() {
            return associatedType;
        }
        
        /**
         * Set associated behavior type
         * @param type Associated type
         */
        public void setAssociatedType(BehaviorType type) {
            this.associatedType = type;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Behavior Observation class
     * Represents a single observed behavior
     */
    public static class BehaviorObservation {
        private final String category;
        private final String action;
        private final float value;
        private final long timestamp;
        
        /**
         * Constructor
         * @param category Behavior category
         * @param action Action performed
         * @param value Value associated with action
         * @param timestamp Observation timestamp
         */
        public BehaviorObservation(String category, String action, 
                                   float value, long timestamp) {
            this.category = category;
            this.action = action;
            this.value = value;
            this.timestamp = timestamp;
        }
        
        /**
         * Get behavior category
         * @return Category
         */
        public String getCategory() {
            return category;
        }
        
        /**
         * Get action
         * @return Action
         */
        public String getAction() {
            return action;
        }
        
        /**
         * Get value
         * @return Value
         */
        public float getValue() {
            return value;
        }
        
        /**
         * Get timestamp
         * @return Timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return category + ":" + action + " (" + value + ")";
        }
    }
    
    /**
     * Behavior Insight class
     * Represents an insight derived from behavior analysis
     */
    public static class BehaviorInsight {
        private final String id;
        private final String name;
        private final String description;
        private float confidence;
        private final List<String> relevantProfiles;
        
        /**
         * Constructor
         * @param id Insight ID
         * @param name Insight name
         * @param description Insight description
         * @param confidence Confidence level (0.0-1.0)
         */
        public BehaviorInsight(String id, String name, String description, float confidence) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
            this.relevantProfiles = new ArrayList<>();
        }
        
        /**
         * Get insight ID
         * @return Insight ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get insight name
         * @return Insight name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Get confidence
         * @return Confidence level
         */
        public float getConfidence() {
            return confidence;
        }
        
        /**
         * Set confidence
         * @param confidence New confidence level
         */
        public void setConfidence(float confidence) {
            this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        }
        
        /**
         * Add a relevant profile
         * @param profileId Profile ID
         */
        public void addRelevantProfile(String profileId) {
            if (!relevantProfiles.contains(profileId)) {
                relevantProfiles.add(profileId);
            }
        }
        
        /**
         * Remove a relevant profile
         * @param profileId Profile ID
         */
        public void removeRelevantProfile(String profileId) {
            relevantProfiles.remove(profileId);
        }
        
        /**
         * Check if insight is relevant for a profile
         * @param profileId Profile ID
         * @return true if relevant
         */
        public boolean isRelevantFor(String profileId) {
            return relevantProfiles.contains(profileId);
        }
        
        /**
         * Get all relevant profiles
         * @return List of profile IDs
         */
        public List<String> getRelevantProfiles() {
            return new ArrayList<>(relevantProfiles);
        }
        
        @Override
        public String toString() {
            return name + " (" + String.format("%.0f", confidence * 100) + "% confidence)";
        }
    }
    
    /**
     * Behavior Detection Listener interface
     * For receiving behavior detection events
     */
    public interface BehaviorDetectionListener {
        /**
         * Called when analysis starts
         * @param profile Behavior profile
         */
        void onAnalysisStarted(BehaviorProfile profile);
        
        /**
         * Called when analysis stops
         * @param profile Behavior profile
         */
        void onAnalysisStopped(BehaviorProfile profile);
        
        /**
         * Called when behavior is observed
         * @param profile Behavior profile
         * @param observation Behavior observation
         */
        void onBehaviorObserved(BehaviorProfile profile, BehaviorObservation observation);
        
        /**
         * Called when a pattern is detected
         * @param profile Behavior profile
         * @param pattern Detected pattern
         * @param confidence Detection confidence
         */
        void onPatternDetected(BehaviorProfile profile, BehaviorPattern pattern, float confidence);
        
        /**
         * Called when behavior analysis is updated
         * @param profile Updated profile
         */
        void onBehaviorAnalysisUpdated(BehaviorProfile profile);
        
        /**
         * Called when insights are updated
         * @param profile Behavior profile
         * @param insights Updated insights
         */
        void onInsightsUpdated(BehaviorProfile profile, List<BehaviorInsight> insights);
    }
}
