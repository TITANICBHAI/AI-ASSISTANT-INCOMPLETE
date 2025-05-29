package com.aiassistant.core.ai.neural;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Neural network model for strategic decision making.
 * This model evaluates options and recommends optimal decisions.
 */
public class DecisionMakingModel extends BaseTFLiteModel {
    private static final String TAG = "DecisionMakingModel";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "Model for strategic decision making and action selection";
    
    // Model configuration
    private static final int MAX_OPTIONS = 8;       // Maximum number of options to evaluate
    private static final int OPTION_FEATURES = 16;  // Features per option
    private static final int CONTEXT_FEATURES = 32; // Context features
    
    /**
     * Constructor
     * @param modelName Model name
     */
    public DecisionMakingModel(String modelName) {
        super(modelName);
        this.modelPath = "models/decision_making.tflite";
    }
    
    @Override
    public String getModelVersion() {
        return VERSION;
    }
    
    @Override
    public String getModelDescription() {
        return DESCRIPTION;
    }
    
    /**
     * Evaluate multiple options and recommend the best decision
     * @param options Array of decision options
     * @param context Current context data
     * @return Decision evaluation result
     */
    public DecisionEvaluationResult evaluateOptions(DecisionOption[] options, DecisionContext context) {
        if (!isReady()) {
            Log.e(TAG, "Model not initialized");
            return new DecisionEvaluationResult();
        }
        
        if (options == null || options.length == 0) {
            Log.e(TAG, "No options provided for evaluation");
            return new DecisionEvaluationResult();
        }
        
        try {
            // Extract features from options and context
            float[] features = extractDecisionFeatures(options, context);
            
            // Load data into input buffer
            inputBuffer.rewind();
            for (float value : features) {
                inputBuffer.putFloat(value);
            }
            
            // Prepare output buffer for scores and explanations
            // Each option gets a score and confidence value
            int numOptions = Math.min(options.length, MAX_OPTIONS);
            float[][] output = new float[1][numOptions * 2];
            
            // Run inference
            interpreter.run(inputBuffer, output);
            
            // Process scores and rank options
            return processDecisionOutput(output[0], options);
            
        } catch (Exception e) {
            Log.e(TAG, "Error during decision evaluation: " + e.getMessage());
            return new DecisionEvaluationResult();
        }
    }
    
    /**
     * Extract features from decision options and context
     * @param options Array of decision options
     * @param context Current context data
     * @return Feature array for model input
     */
    private float[] extractDecisionFeatures(DecisionOption[] options, DecisionContext context) {
        // Calculate total feature size
        int numOptions = Math.min(options.length, MAX_OPTIONS);
        int totalFeatureSize = (numOptions * OPTION_FEATURES) + CONTEXT_FEATURES;
        float[] features = new float[totalFeatureSize];
        
        // Default to zeros
        Arrays.fill(features, 0.0f);
        
        // Extract context features first
        if (context != null) {
            // Extract features representing the current game state and context
            int offset = 0;
            
            // Player state (8 features)
            features[offset++] = normalizeValue(context.playerHealth, 0, 100);
            features[offset++] = normalizeValue(context.playerEnergy, 0, 100);
            features[offset++] = normalizeValue(context.playerExperience, 0, 1000);
            features[offset++] = normalizeValue(context.playerLevel, 1, 50);
            features[offset++] = context.playerPosition.x / 1000.0f; // Normalized position
            features[offset++] = context.playerPosition.y / 1000.0f;
            features[offset++] = context.playerPosition.z / 1000.0f;
            features[offset++] = normalizeValue(context.playerVulnerability, 0, 100);
            
            // Environment state (8 features)
            features[offset++] = normalizeValue(context.environmentDanger, 0, 100);
            features[offset++] = normalizeValue(context.environmentVisibility, 0, 100);
            features[offset++] = normalizeValue(context.environmentComplexity, 0, 100);
            features[offset++] = normalizeValue(context.environmentRestriction, 0, 100);
            features[offset++] = context.timeOfDay / 24.0f;
            features[offset++] = context.weatherCondition / (float)(WeatherCondition.values().length - 1);
            features[offset++] = context.terrainType / (float)(TerrainType.values().length - 1);
            features[offset++] = normalizeValue(context.areaFamiliarity, 0, 100);
            
            // Resource state (8 features)
            features[offset++] = normalizeValue(context.resourceAmmo, 0, 1000);
            features[offset++] = normalizeValue(context.resourceHealth, 0, 100);
            features[offset++] = normalizeValue(context.resourceEnergy, 0, 100);
            features[offset++] = normalizeValue(context.resourceMoney, 0, 10000);
            features[offset++] = normalizeValue(context.resourceInventorySpace, 0, 100);
            features[offset++] = normalizeValue(context.resourceRarity, 0, 100);
            features[offset++] = normalizeValue(context.resourceDistance, 0, 1000);
            features[offset++] = normalizeValue(context.resourceQuantity, 0, 100);
            
            // Tactical state (8 features)
            features[offset++] = normalizeValue(context.tacticalAdvantage, -100, 100);
            features[offset++] = normalizeValue(context.tacticalCoverQuality, 0, 100);
            features[offset++] = normalizeValue(context.tacticalEnemyCount, 0, 20);
            features[offset++] = normalizeValue(context.tacticalAllyCount, 0, 10);
            features[offset++] = normalizeValue(context.tacticalEnemyStrength, 0, 100);
            features[offset++] = normalizeValue(context.tacticalPositionStrength, 0, 100);
            features[offset++] = normalizeValue(context.tacticalRouteOptions, 1, 10);
            features[offset++] = normalizeValue(context.tacticalDetectionRisk, 0, 100);
        }
        
        // Extract features for each option
        for (int i = 0; i < numOptions; i++) {
            DecisionOption option = options[i];
            int baseOffset = CONTEXT_FEATURES + (i * OPTION_FEATURES);
            
            // Option type (one-hot encoding across 8 features)
            int typeOrdinal = option.type.ordinal();
            for (int j = 0; j < 8; j++) {
                features[baseOffset + j] = (j == typeOrdinal) ? 1.0f : 0.0f;
            }
            
            // Option characteristics (8 features)
            features[baseOffset + 8] = normalizeValue(option.risk, 0, 100);
            features[baseOffset + 9] = normalizeValue(option.reward, 0, 100);
            features[baseOffset + 10] = normalizeValue(option.timeRequired, 0, 300);
            features[baseOffset + 11] = normalizeValue(option.resourceCost, 0, 100);
            features[baseOffset + 12] = normalizeValue(option.complexity, 0, 100);
            features[baseOffset + 13] = normalizeValue(option.alignmentWithObjective, 0, 100);
            features[baseOffset + 14] = normalizeValue(option.chancesOfSuccess, 0, 100);
            features[baseOffset + 15] = normalizeValue(option.strategicValue, 0, 100);
        }
        
        return features;
    }
    
    /**
     * Normalize a value to 0-1 range
     */
    private float normalizeValue(float value, float min, float max) {
        if (max == min) return 0.5f;
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
    
    /**
     * Process model output into decision evaluation result
     * @param output Raw model output
     * @param options Original decision options
     * @return Structured decision evaluation result
     */
    private DecisionEvaluationResult processDecisionOutput(float[] output, DecisionOption[] options) {
        DecisionEvaluationResult result = new DecisionEvaluationResult();
        
        int numOptions = Math.min(options.length, MAX_OPTIONS);
        
        // Create scored options list
        List<ScoredOption> scoredOptions = new ArrayList<>(numOptions);
        
        for (int i = 0; i < numOptions; i++) {
            float score = output[i * 2];         // Score
            float confidence = output[i * 2 + 1]; // Confidence
            
            scoredOptions.add(new ScoredOption(options[i], score, confidence));
        }
        
        // Sort by score (descending)
        scoredOptions.sort(Comparator.comparing(ScoredOption::getScore).reversed());
        
        // Set result values
        result.rankedOptions = new DecisionOption[numOptions];
        result.scores = new float[numOptions];
        result.confidences = new float[numOptions];
        
        for (int i = 0; i < numOptions; i++) {
            ScoredOption scoredOption = scoredOptions.get(i);
            result.rankedOptions[i] = scoredOption.option;
            result.scores[i] = scoredOption.score;
            result.confidences[i] = scoredOption.confidence;
        }
        
        // Calculate diversity of top recommendations
        if (numOptions >= 2) {
            float diversitySum = 0;
            int pairCount = 0;
            
            for (int i = 0; i < Math.min(3, numOptions); i++) {
                for (int j = i + 1; j < Math.min(3, numOptions); j++) {
                    diversitySum += calculateOptionDiversity(
                            result.rankedOptions[i], 
                            result.rankedOptions[j]);
                    pairCount++;
                }
            }
            
            result.recommendationDiversity = (pairCount > 0) ? diversitySum / pairCount : 0.0f;
        }
        
        return result;
    }
    
    /**
     * Calculate diversity (difference) between two options
     * Higher value means more diverse options
     */
    private float calculateOptionDiversity(DecisionOption option1, DecisionOption option2) {
        float typeDiversity = (option1.type == option2.type) ? 0.0f : 1.0f;
        
        float riskDiversity = Math.abs(option1.risk - option2.risk) / 100.0f;
        float rewardDiversity = Math.abs(option1.reward - option2.reward) / 100.0f;
        float timeDiversity = Math.abs(option1.timeRequired - option2.timeRequired) / 300.0f;
        float resourceDiversity = Math.abs(option1.resourceCost - option2.resourceCost) / 100.0f;
        
        // Weighted diversity calculation
        return (typeDiversity * 0.4f) + 
               (riskDiversity * 0.15f) + 
               (rewardDiversity * 0.15f) + 
               (timeDiversity * 0.15f) + 
               (resourceDiversity * 0.15f);
    }
    
    /**
     * Helper class for scoring options
     */
    private static class ScoredOption {
        DecisionOption option;
        float score;
        float confidence;
        
        ScoredOption(DecisionOption option, float score, float confidence) {
            this.option = option;
            this.score = score;
            this.confidence = confidence;
        }
        
        float getScore() {
            return score;
        }
    }
    
    /**
     * Types of decisions/actions
     */
    public enum DecisionType {
        ATTACK,
        DEFEND,
        MOVE,
        COLLECT,
        BUILD,
        UPGRADE,
        COMMUNICATE,
        OTHER
    }
    
    /**
     * Weather conditions for context
     */
    public enum WeatherCondition {
        CLEAR,
        RAIN,
        SNOW,
        FOG,
        STORM
    }
    
    /**
     * Terrain types for context
     */
    public enum TerrainType {
        URBAN,
        FOREST,
        MOUNTAIN,
        DESERT,
        WATER,
        UNDERGROUND
    }
    
    /**
     * 3D position
     */
    public static class Position3D {
        public float x, y, z;
        
        public Position3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    /**
     * Decision option to evaluate
     */
    public static class DecisionOption {
        public DecisionType type;
        public String description;
        public float risk;              // 0-100 scale
        public float reward;            // 0-100 scale
        public float timeRequired;      // in seconds
        public float resourceCost;      // 0-100 scale
        public float complexity;        // 0-100 scale
        public float alignmentWithObjective; // 0-100 scale
        public float chancesOfSuccess;  // 0-100 scale
        public float strategicValue;    // 0-100 scale
        
        public DecisionOption(DecisionType type, String description) {
            this.type = type;
            this.description = description;
            this.risk = 50;
            this.reward = 50;
            this.timeRequired = 60;
            this.resourceCost = 50;
            this.complexity = 50;
            this.alignmentWithObjective = 50;
            this.chancesOfSuccess = 50;
            this.strategicValue = 50;
        }
        
        @Override
        public String toString() {
            return description + " [" + type + "]";
        }
    }
    
    /**
     * Context data for decision making
     */
    public static class DecisionContext {
        // Player state
        public float playerHealth;
        public float playerEnergy;
        public float playerExperience;
        public float playerLevel;
        public Position3D playerPosition;
        public float playerVulnerability;
        
        // Environment state
        public float environmentDanger;
        public float environmentVisibility;
        public float environmentComplexity;
        public float environmentRestriction;
        public float timeOfDay;           // 0-24 scale
        public int weatherCondition;      // Index of WeatherCondition
        public int terrainType;           // Index of TerrainType
        public float areaFamiliarity;     // 0-100 scale
        
        // Resource state
        public float resourceAmmo;
        public float resourceHealth;
        public float resourceEnergy;
        public float resourceMoney;
        public float resourceInventorySpace;
        public float resourceRarity;
        public float resourceDistance;
        public float resourceQuantity;
        
        // Tactical state
        public float tacticalAdvantage;      // -100 to 100 scale
        public float tacticalCoverQuality;
        public float tacticalEnemyCount;
        public float tacticalAllyCount;
        public float tacticalEnemyStrength;
        public float tacticalPositionStrength;
        public float tacticalRouteOptions;
        public float tacticalDetectionRisk;
        
        public DecisionContext() {
            // Initialize with default values
            playerHealth = 100;
            playerEnergy = 100;
            playerExperience = 0;
            playerLevel = 1;
            playerPosition = new Position3D(0, 0, 0);
            playerVulnerability = 50;
            
            environmentDanger = 50;
            environmentVisibility = 80;
            environmentComplexity = 50;
            environmentRestriction = 20;
            timeOfDay = 12;
            weatherCondition = WeatherCondition.CLEAR.ordinal();
            terrainType = TerrainType.URBAN.ordinal();
            areaFamiliarity = 50;
            
            resourceAmmo = 100;
            resourceHealth = 100;
            resourceEnergy = 100;
            resourceMoney = 1000;
            resourceInventorySpace = 80;
            resourceRarity = 50;
            resourceDistance = 100;
            resourceQuantity = 50;
            
            tacticalAdvantage = 0;
            tacticalCoverQuality = 50;
            tacticalEnemyCount = 0;
            tacticalAllyCount = 0;
            tacticalEnemyStrength = 50;
            tacticalPositionStrength = 50;
            tacticalRouteOptions = 3;
            tacticalDetectionRisk = 50;
        }
    }
    
    /**
     * Result of decision evaluation
     */
    public static class DecisionEvaluationResult {
        public DecisionOption[] rankedOptions;     // Options sorted by score
        public float[] scores;                     // Scores for each option (0-1)
        public float[] confidences;                // Confidence in scores (0-1)
        public float recommendationDiversity;      // Diversity of top recommendations (0-1)
        
        public DecisionEvaluationResult() {
            rankedOptions = new DecisionOption[0];
            scores = new float[0];
            confidences = new float[0];
            recommendationDiversity = 0;
        }
        
        /**
         * Get the top recommendation
         * @return The highest-ranked option or null if none available
         */
        public DecisionOption getTopRecommendation() {
            if (rankedOptions != null && rankedOptions.length > 0) {
                return rankedOptions[0];
            }
            return null;
        }
        
        /**
         * Get score for the top recommendation
         * @return Score or 0 if none available
         */
        public float getTopScore() {
            if (scores != null && scores.length > 0) {
                return scores[0];
            }
            return 0;
        }
        
        /**
         * Get confidence for the top recommendation
         * @return Confidence or 0 if none available
         */
        public float getTopConfidence() {
            if (confidences != null && confidences.length > 0) {
                return confidences[0];
            }
            return 0;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Decision Evaluation Results:\n");
            
            if (rankedOptions != null && rankedOptions.length > 0) {
                sb.append("Top recommendation: ").append(rankedOptions[0])
                  .append(" (score: ").append(String.format("%.2f", scores[0]))
                  .append(", confidence: ").append(String.format("%.2f", confidences[0]))
                  .append(")\n");
                
                if (rankedOptions.length > 1) {
                    sb.append("Alternative options:\n");
                    for (int i = 1; i < Math.min(3, rankedOptions.length); i++) {
                        sb.append("  ").append(i + 1).append(". ").append(rankedOptions[i])
                          .append(" (score: ").append(String.format("%.2f", scores[i]))
                          .append(", confidence: ").append(String.format("%.2f", confidences[i]))
                          .append(")\n");
                    }
                }
                
                sb.append("Recommendation diversity: ")
                  .append(String.format("%.2f", recommendationDiversity));
            } else {
                sb.append("No recommendations available");
            }
            
            return sb.toString();
        }
    }
}
