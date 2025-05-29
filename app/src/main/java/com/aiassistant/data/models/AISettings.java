package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.aiassistant.utils.Constants;

/**
 * Entity representing AI assistant settings.
 * This class stores configuration parameters for the AI assistant's behavior,
 * including learning parameters, performance settings, and mode selection.
 */
@Entity(tableName = "ai_settings")
public class AISettings {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // AI mode (disabled, learning, autonomous, copilot)
    private String aiMode;
    
    // Last modified timestamp
    private long lastModified;
    
    // Auto-takeover settings
    private boolean autoTakeoverEnabled;
    private int inactivityThresholdMs;
    
    // Learning parameters
    private String learningAlgorithm;
    private float learningRate;
    private float discountFactor;
    private float explorationRate;
    
    // Permission status
    private boolean accessibilityServiceEnabled;
    private boolean overlayPermissionGranted;
    private boolean usageStatsPermissionGranted;
    
    // Performance settings
    private int maxFps;
    private int processingIntervalMs;
    private int screenRecordingQuality;
    
    // Feature flags
    private boolean videoLearningEnabled;
    private boolean multiTouchEnabled;
    private boolean metaLearningEnabled;
    private boolean naturalLanguageProcessingEnabled;
    private boolean contextAwarenessEnabled;
    private boolean strategicPlanningEnabled;
    private boolean patternRecognitionEnabled;
    private boolean adaptiveLearningEnabled;
    private boolean realTimeDecisionMakingEnabled;
    
    // Game-specific settings
    private boolean gameRuleLearningEnabled;
    private boolean novelStrategyDevelopmentEnabled;
    private boolean gameMechanicsUnderstandingEnabled;
    private boolean customUIElementDetectionEnabled;
    private boolean realTimeStrategyEnabled;
    
    // Advanced capabilities
    private boolean contentUnderstandingEnabled;
    private boolean situationAdaptationEnabled;
    private boolean framePerfectTimingEnabled;
    private boolean combatPatternRecognitionEnabled;
    
    // Notification settings
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    
    // UI settings
    private String theme;
    private boolean showControlPanel;
    private boolean showDebugInfo;
    
    // Custom user preferences
    private String customPreferences;
    
    /**
     * Default constructor with default settings
     */
    public AISettings() {
        this.aiMode = Constants.AI_MODE_DISABLED;
        this.lastModified = System.currentTimeMillis();
        this.autoTakeoverEnabled = false;
        this.inactivityThresholdMs = Constants.DEFAULT_INACTIVITY_THRESHOLD_MS;
        this.learningAlgorithm = Constants.ALGORITHM_DQN;
        this.learningRate = Constants.DEFAULT_LEARNING_RATE;
        this.discountFactor = Constants.DEFAULT_DISCOUNT_FACTOR;
        this.explorationRate = Constants.DEFAULT_EXPLORATION_RATE;
        this.accessibilityServiceEnabled = false;
        this.overlayPermissionGranted = false;
        this.usageStatsPermissionGranted = false;
        this.maxFps = Constants.DEFAULT_MAX_FPS;
        this.processingIntervalMs = Constants.DEFAULT_PROCESSING_INTERVAL_MS;
        this.screenRecordingQuality = Constants.DEFAULT_RECORDING_QUALITY;
        this.videoLearningEnabled = Constants.ENABLE_VIDEO_LEARNING;
        this.multiTouchEnabled = true;
        this.metaLearningEnabled = Constants.ENABLE_META_LEARNING;
        this.naturalLanguageProcessingEnabled = Constants.ENABLE_NATURAL_LANGUAGE_PROCESSING;
        this.contextAwarenessEnabled = Constants.ENABLE_CONTEXT_AWARENESS;
        this.strategicPlanningEnabled = Constants.ENABLE_STRATEGIC_PLANNING;
        this.patternRecognitionEnabled = Constants.ENABLE_PATTERN_RECOGNITION;
        this.adaptiveLearningEnabled = Constants.ENABLE_ADAPTIVE_LEARNING;
        this.realTimeDecisionMakingEnabled = Constants.ENABLE_REAL_TIME_DECISION_MAKING;
        this.gameRuleLearningEnabled = Constants.ENABLE_GAME_RULE_LEARNING;
        this.novelStrategyDevelopmentEnabled = Constants.ENABLE_NOVEL_STRATEGY_DEVELOPMENT;
        this.gameMechanicsUnderstandingEnabled = Constants.ENABLE_GAME_MECHANICS_UNDERSTANDING;
        this.customUIElementDetectionEnabled = Constants.ENABLE_CUSTOM_UI_ELEMENT_DETECTION;
        this.realTimeStrategyEnabled = Constants.ENABLE_REAL_TIME_STRATEGY;
        this.contentUnderstandingEnabled = Constants.ENABLE_CONTENT_UNDERSTANDING;
        this.situationAdaptationEnabled = Constants.ENABLE_SITUATION_ADAPTATION;
        this.framePerfectTimingEnabled = Constants.ENABLE_FRAME_PERFECT_TIMING;
        this.combatPatternRecognitionEnabled = Constants.ENABLE_COMBAT_PATTERN_RECOGNITION;
        this.notificationsEnabled = true;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        this.theme = "system";
        this.showControlPanel = true;
        this.showDebugInfo = false;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getAiMode() {
        return aiMode;
    }
    
    public void setAiMode(String aiMode) {
        this.aiMode = aiMode;
        this.lastModified = System.currentTimeMillis();
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean isAutoTakeoverEnabled() {
        return autoTakeoverEnabled;
    }
    
    public void setAutoTakeoverEnabled(boolean autoTakeoverEnabled) {
        this.autoTakeoverEnabled = autoTakeoverEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getInactivityThresholdMs() {
        return inactivityThresholdMs;
    }
    
    public void setInactivityThresholdMs(int inactivityThresholdMs) {
        this.inactivityThresholdMs = inactivityThresholdMs;
        this.lastModified = System.currentTimeMillis();
    }
    
    public String getLearningAlgorithm() {
        return learningAlgorithm;
    }
    
    public void setLearningAlgorithm(String learningAlgorithm) {
        this.learningAlgorithm = learningAlgorithm;
        this.lastModified = System.currentTimeMillis();
    }
    
    public float getLearningRate() {
        return learningRate;
    }
    
    public void setLearningRate(float learningRate) {
        this.learningRate = learningRate;
        this.lastModified = System.currentTimeMillis();
    }
    
    public float getDiscountFactor() {
        return discountFactor;
    }
    
    public void setDiscountFactor(float discountFactor) {
        this.discountFactor = discountFactor;
        this.lastModified = System.currentTimeMillis();
    }
    
    public float getExplorationRate() {
        return explorationRate;
    }
    
    public void setExplorationRate(float explorationRate) {
        this.explorationRate = explorationRate;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isAccessibilityServiceEnabled() {
        return accessibilityServiceEnabled;
    }
    
    public void setAccessibilityServiceEnabled(boolean accessibilityServiceEnabled) {
        this.accessibilityServiceEnabled = accessibilityServiceEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isOverlayPermissionGranted() {
        return overlayPermissionGranted;
    }
    
    public void setOverlayPermissionGranted(boolean overlayPermissionGranted) {
        this.overlayPermissionGranted = overlayPermissionGranted;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isUsageStatsPermissionGranted() {
        return usageStatsPermissionGranted;
    }
    
    public void setUsageStatsPermissionGranted(boolean usageStatsPermissionGranted) {
        this.usageStatsPermissionGranted = usageStatsPermissionGranted;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getMaxFps() {
        return maxFps;
    }
    
    public void setMaxFps(int maxFps) {
        this.maxFps = maxFps;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getProcessingIntervalMs() {
        return processingIntervalMs;
    }
    
    public void setProcessingIntervalMs(int processingIntervalMs) {
        this.processingIntervalMs = processingIntervalMs;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getScreenRecordingQuality() {
        return screenRecordingQuality;
    }
    
    public void setScreenRecordingQuality(int screenRecordingQuality) {
        this.screenRecordingQuality = screenRecordingQuality;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isVideoLearningEnabled() {
        return videoLearningEnabled;
    }
    
    public void setVideoLearningEnabled(boolean videoLearningEnabled) {
        this.videoLearningEnabled = videoLearningEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isMultiTouchEnabled() {
        return multiTouchEnabled;
    }
    
    public void setMultiTouchEnabled(boolean multiTouchEnabled) {
        this.multiTouchEnabled = multiTouchEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isMetaLearningEnabled() {
        return metaLearningEnabled;
    }
    
    public void setMetaLearningEnabled(boolean metaLearningEnabled) {
        this.metaLearningEnabled = metaLearningEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isNaturalLanguageProcessingEnabled() {
        return naturalLanguageProcessingEnabled;
    }
    
    public void setNaturalLanguageProcessingEnabled(boolean naturalLanguageProcessingEnabled) {
        this.naturalLanguageProcessingEnabled = naturalLanguageProcessingEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isContextAwarenessEnabled() {
        return contextAwarenessEnabled;
    }
    
    public void setContextAwarenessEnabled(boolean contextAwarenessEnabled) {
        this.contextAwarenessEnabled = contextAwarenessEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isStrategicPlanningEnabled() {
        return strategicPlanningEnabled;
    }
    
    public void setStrategicPlanningEnabled(boolean strategicPlanningEnabled) {
        this.strategicPlanningEnabled = strategicPlanningEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isPatternRecognitionEnabled() {
        return patternRecognitionEnabled;
    }
    
    public void setPatternRecognitionEnabled(boolean patternRecognitionEnabled) {
        this.patternRecognitionEnabled = patternRecognitionEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isAdaptiveLearningEnabled() {
        return adaptiveLearningEnabled;
    }
    
    public void setAdaptiveLearningEnabled(boolean adaptiveLearningEnabled) {
        this.adaptiveLearningEnabled = adaptiveLearningEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isRealTimeDecisionMakingEnabled() {
        return realTimeDecisionMakingEnabled;
    }
    
    public void setRealTimeDecisionMakingEnabled(boolean realTimeDecisionMakingEnabled) {
        this.realTimeDecisionMakingEnabled = realTimeDecisionMakingEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isGameRuleLearningEnabled() {
        return gameRuleLearningEnabled;
    }
    
    public void setGameRuleLearningEnabled(boolean gameRuleLearningEnabled) {
        this.gameRuleLearningEnabled = gameRuleLearningEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isNovelStrategyDevelopmentEnabled() {
        return novelStrategyDevelopmentEnabled;
    }
    
    public void setNovelStrategyDevelopmentEnabled(boolean novelStrategyDevelopmentEnabled) {
        this.novelStrategyDevelopmentEnabled = novelStrategyDevelopmentEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isGameMechanicsUnderstandingEnabled() {
        return gameMechanicsUnderstandingEnabled;
    }
    
    public void setGameMechanicsUnderstandingEnabled(boolean gameMechanicsUnderstandingEnabled) {
        this.gameMechanicsUnderstandingEnabled = gameMechanicsUnderstandingEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isCustomUIElementDetectionEnabled() {
        return customUIElementDetectionEnabled;
    }
    
    public void setCustomUIElementDetectionEnabled(boolean customUIElementDetectionEnabled) {
        this.customUIElementDetectionEnabled = customUIElementDetectionEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isRealTimeStrategyEnabled() {
        return realTimeStrategyEnabled;
    }
    
    public void setRealTimeStrategyEnabled(boolean realTimeStrategyEnabled) {
        this.realTimeStrategyEnabled = realTimeStrategyEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isContentUnderstandingEnabled() {
        return contentUnderstandingEnabled;
    }
    
    public void setContentUnderstandingEnabled(boolean contentUnderstandingEnabled) {
        this.contentUnderstandingEnabled = contentUnderstandingEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isSituationAdaptationEnabled() {
        return situationAdaptationEnabled;
    }
    
    public void setSituationAdaptationEnabled(boolean situationAdaptationEnabled) {
        this.situationAdaptationEnabled = situationAdaptationEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isFramePerfectTimingEnabled() {
        return framePerfectTimingEnabled;
    }
    
    public void setFramePerfectTimingEnabled(boolean framePerfectTimingEnabled) {
        this.framePerfectTimingEnabled = framePerfectTimingEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isCombatPatternRecognitionEnabled() {
        return combatPatternRecognitionEnabled;
    }
    
    public void setCombatPatternRecognitionEnabled(boolean combatPatternRecognitionEnabled) {
        this.combatPatternRecognitionEnabled = combatPatternRecognitionEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }
    
    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
        this.lastModified = System.currentTimeMillis();
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isShowControlPanel() {
        return showControlPanel;
    }
    
    public void setShowControlPanel(boolean showControlPanel) {
        this.showControlPanel = showControlPanel;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isShowDebugInfo() {
        return showDebugInfo;
    }
    
    public void setShowDebugInfo(boolean showDebugInfo) {
        this.showDebugInfo = showDebugInfo;
        this.lastModified = System.currentTimeMillis();
    }
    
    public String getCustomPreferences() {
        return customPreferences;
    }
    
    public void setCustomPreferences(String customPreferences) {
        this.customPreferences = customPreferences;
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Check if all required permissions are granted
     * @return True if all permissions are granted
     */
    public boolean areAllPermissionsGranted() {
        return accessibilityServiceEnabled && overlayPermissionGranted && usageStatsPermissionGranted;
    }
    
    /**
     * Check if the AI is activated
     * @return True if AI is in autonomous or copilot mode
     */
    public boolean isAIActive() {
        return aiMode.equals(Constants.AI_MODE_AUTONOMOUS) || aiMode.equals(Constants.AI_MODE_COPILOT);
    }
    
    /**
     * Check if the AI is in learning mode
     * @return True if AI is in learning mode
     */
    public boolean isInLearningMode() {
        return aiMode.equals(Constants.AI_MODE_LEARNING);
    }
    
    /**
     * Check if the AI is in autonomous mode
     * @return True if AI is in autonomous mode
     */
    public boolean isInAutonomousMode() {
        return aiMode.equals(Constants.AI_MODE_AUTONOMOUS);
    }
    
    /**
     * Check if the AI is in copilot mode
     * @return True if AI is in copilot mode
     */
    public boolean isInCopilotMode() {
        return aiMode.equals(Constants.AI_MODE_COPILOT);
    }
    
    /**
     * Reset settings to their default values
     */
    public void resetToDefaults() {
        this.aiMode = Constants.AI_MODE_DISABLED;
        this.lastModified = System.currentTimeMillis();
        this.autoTakeoverEnabled = false;
        this.inactivityThresholdMs = Constants.DEFAULT_INACTIVITY_THRESHOLD_MS;
        this.learningAlgorithm = Constants.ALGORITHM_DQN;
        this.learningRate = Constants.DEFAULT_LEARNING_RATE;
        this.discountFactor = Constants.DEFAULT_DISCOUNT_FACTOR;
        this.explorationRate = Constants.DEFAULT_EXPLORATION_RATE;
        this.maxFps = Constants.DEFAULT_MAX_FPS;
        this.processingIntervalMs = Constants.DEFAULT_PROCESSING_INTERVAL_MS;
        this.screenRecordingQuality = Constants.DEFAULT_RECORDING_QUALITY;
        this.videoLearningEnabled = Constants.ENABLE_VIDEO_LEARNING;
        this.multiTouchEnabled = true;
        this.metaLearningEnabled = Constants.ENABLE_META_LEARNING;
        this.naturalLanguageProcessingEnabled = Constants.ENABLE_NATURAL_LANGUAGE_PROCESSING;
        this.contextAwarenessEnabled = Constants.ENABLE_CONTEXT_AWARENESS;
        this.strategicPlanningEnabled = Constants.ENABLE_STRATEGIC_PLANNING;
        this.patternRecognitionEnabled = Constants.ENABLE_PATTERN_RECOGNITION;
        this.adaptiveLearningEnabled = Constants.ENABLE_ADAPTIVE_LEARNING;
        this.realTimeDecisionMakingEnabled = Constants.ENABLE_REAL_TIME_DECISION_MAKING;
        this.gameRuleLearningEnabled = Constants.ENABLE_GAME_RULE_LEARNING;
        this.novelStrategyDevelopmentEnabled = Constants.ENABLE_NOVEL_STRATEGY_DEVELOPMENT;
        this.gameMechanicsUnderstandingEnabled = Constants.ENABLE_GAME_MECHANICS_UNDERSTANDING;
        this.customUIElementDetectionEnabled = Constants.ENABLE_CUSTOM_UI_ELEMENT_DETECTION;
        this.realTimeStrategyEnabled = Constants.ENABLE_REAL_TIME_STRATEGY;
        this.contentUnderstandingEnabled = Constants.ENABLE_CONTENT_UNDERSTANDING;
        this.situationAdaptationEnabled = Constants.ENABLE_SITUATION_ADAPTATION;
        this.framePerfectTimingEnabled = Constants.ENABLE_FRAME_PERFECT_TIMING;
        this.combatPatternRecognitionEnabled = Constants.ENABLE_COMBAT_PATTERN_RECOGNITION;
        this.notificationsEnabled = true;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        this.theme = "system";
        this.showControlPanel = true;
        this.showDebugInfo = false;
        this.customPreferences = null;
    }
}