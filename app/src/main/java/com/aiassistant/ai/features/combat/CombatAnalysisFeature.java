package com.aiassistant.ai.features.combat;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Advanced Combat Analysis Feature
 * - Real-time combat strategy optimization
 * - Damage calculation and efficiency metrics
 * - Enemy pattern recognition
 * - Tactical recommendations
 */
public class CombatAnalysisFeature extends BaseFeature {
    private static final String TAG = "CombatAnalysis";
    private static final String FEATURE_NAME = "advanced_combat_analysis";
    
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 500;
    
    // Last update timestamp
    private long lastUpdateTime;
    
    // Combat mode
    private boolean inCombatMode;
    
    // Current combat session
    private CombatSession currentSession;
    
    // Historical combat data
    private final List<CombatSession> combatHistory;
    
    // Combat scenario recognition
    private final Map<String, CombatScenario> knownScenarios;
    
    // Tactics library
    private final Map<String, CombatTactic> tacticsLibrary;
    
    // Listeners for combat events
    private final List<CombatAnalysisListener> listeners;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CombatAnalysisFeature(Context context) {
        super(context, FEATURE_NAME);
        this.lastUpdateTime = 0;
        this.inCombatMode = false;
        this.currentSession = null;
        this.combatHistory = new ArrayList<>();
        this.knownScenarios = new HashMap<>();
        this.tacticsLibrary = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Load predefined combat scenarios
                loadCombatScenarios();
                
                // Load tactics library
                loadTacticsLibrary();
                
                Log.d(TAG, "Combat analysis system initialized with " +
                      knownScenarios.size() + " scenarios and " +
                      tacticsLibrary.size() + " tactics");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize combat analysis", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled()) return;
        
        // Check if update is needed
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update combat state
            updateCombatState();
            
            // If in combat, analyze current situation
            if (inCombatMode && currentSession != null) {
                analyzeCurrentCombat();
            }
            
            // Update timestamp
            lastUpdateTime = currentTime;
        } catch (Exception e) {
            Log.e(TAG, "Error updating combat analysis", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // End current combat session if one is active
        if (inCombatMode && currentSession != null) {
            endCombat();
        }
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start a new combat session
     * @param combatType Type of combat (e.g., "melee", "ranged", "boss")
     * @return The new combat session
     */
    public CombatSession startCombat(String combatType) {
        // End current session if one exists
        if (inCombatMode && currentSession != null) {
            endCombat();
        }
        
        // Create new session
        currentSession = new CombatSession(combatType);
        inCombatMode = true;
        
        Log.d(TAG, "Started new " + combatType + " combat session");
        
        // Notify listeners
        for (CombatAnalysisListener listener : listeners) {
            listener.onCombatStarted(currentSession);
        }
        
        return currentSession;
    }
    
    /**
     * End current combat session
     * @return The completed combat session
     */
    public CombatSession endCombat() {
        if (!inCombatMode || currentSession == null) {
            Log.w(TAG, "No active combat session to end");
            return null;
        }
        
        // Finalize session
        currentSession.end();
        
        // Add to history
        combatHistory.add(currentSession);
        
        // Analyze completed session
        analyzeCombatSession(currentSession);
        
        Log.d(TAG, "Ended combat session, duration: " + 
              currentSession.getDuration() + "ms, efficiency: " + 
              currentSession.getEfficiencyRating());
        
        // Notify listeners
        for (CombatAnalysisListener listener : listeners) {
            listener.onCombatEnded(currentSession);
        }
        
        // Reset state
        CombatSession completedSession = currentSession;
        currentSession = null;
        inCombatMode = false;
        
        return completedSession;
    }
    
    /**
     * Record a player action in combat
     * @param actionType Type of action (attack, defend, use item, etc.)
     * @param target Target of the action
     * @param damage Damage dealt
     * @param timestamp Time of action
     * @return Created combat action
     */
    public CombatAction recordPlayerAction(String actionType, String target, 
                                          float damage, long timestamp) {
        if (!inCombatMode || currentSession == null) {
            Log.w(TAG, "No active combat session for recording player action");
            return null;
        }
        
        // Create and add action
        CombatAction action = new CombatAction(
            actionType, target, damage, true, timestamp);
        
        currentSession.addAction(action);
        
        // Notify listeners
        for (CombatAnalysisListener listener : listeners) {
            listener.onPlayerAction(action);
        }
        
        return action;
    }
    
    /**
     * Record an enemy action in combat
     * @param actionType Type of action
     * @param source Source of the action
     * @param damage Damage dealt
     * @param timestamp Time of action
     * @return Created combat action
     */
    public CombatAction recordEnemyAction(String actionType, String source, 
                                         float damage, long timestamp) {
        if (!inCombatMode || currentSession == null) {
            Log.w(TAG, "No active combat session for recording enemy action");
            return null;
        }
        
        // Create and add action
        CombatAction action = new CombatAction(
            actionType, source, damage, false, timestamp);
        
        currentSession.addAction(action);
        
        // Notify listeners
        for (CombatAnalysisListener listener : listeners) {
            listener.onEnemyAction(action);
        }
        
        return action;
    }
    
    /**
     * Get current DPS (Damage Per Second)
     * @param windowMs Time window in milliseconds
     * @return Current DPS value
     */
    public float getCurrentDPS(long windowMs) {
        if (!inCombatMode || currentSession == null) {
            return 0.0f;
        }
        
        return currentSession.calculateDPS(windowMs);
    }
    
    /**
     * Get recommended tactics for current combat
     * @return List of recommended tactics
     */
    public List<CombatTactic> getRecommendedTactics() {
        if (!inCombatMode || currentSession == null) {
            return new ArrayList<>();
        }
        
        // Identify current scenario
        CombatScenario scenario = identifyCurrentScenario();
        if (scenario == null) {
            return new ArrayList<>();
        }
        
        // Get tactics for this scenario
        return getTacticsForScenario(scenario);
    }
    
    /**
     * Add a listener for combat events
     * @param listener Listener to add
     */
    public void addListener(CombatAnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     * @param listener Listener to remove
     */
    public void removeListener(CombatAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Check if currently in combat mode
     * @return true if in combat
     */
    public boolean isInCombat() {
        return inCombatMode && currentSession != null;
    }
    
    /**
     * Get combat history
     * @return List of past combat sessions
     */
    public List<CombatSession> getCombatHistory() {
        return new ArrayList<>(combatHistory);
    }
    
    /**
     * Get current combat session
     * @return Current session or null if not in combat
     */
    public CombatSession getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Load predefined combat scenarios
     */
    private void loadCombatScenarios() {
        // This would load from storage or predefined data
        // For now, add some sample scenarios
        
        // Boss combat scenario
        CombatScenario bossScenario = new CombatScenario("boss_combat");
        bossScenario.addCharacteristic("single_target", 1.0f);
        bossScenario.addCharacteristic("high_damage", 0.8f);
        bossScenario.addCharacteristic("predictable_pattern", 0.6f);
        knownScenarios.put(bossScenario.getId(), bossScenario);
        
        // Multiple enemies scenario
        CombatScenario multipleEnemiesScenario = new CombatScenario("multiple_enemies");
        multipleEnemiesScenario.addCharacteristic("multiple_targets", 1.0f);
        multipleEnemiesScenario.addCharacteristic("low_individual_damage", 0.7f);
        multipleEnemiesScenario.addCharacteristic("swarming_behavior", 0.9f);
        knownScenarios.put(multipleEnemiesScenario.getId(), multipleEnemiesScenario);
        
        // Ranged combat scenario
        CombatScenario rangedScenario = new CombatScenario("ranged_combat");
        rangedScenario.addCharacteristic("distance_attacks", 1.0f);
        rangedScenario.addCharacteristic("cover_usage", 0.8f);
        rangedScenario.addCharacteristic("line_of_sight_important", 0.9f);
        knownScenarios.put(rangedScenario.getId(), rangedScenario);
    }
    
    /**
     * Load tactics library
     */
    private void loadTacticsLibrary() {
        // This would load from storage or predefined data
        // For now, add some sample tactics
        
        // Boss tactics
        CombatTactic bossTactic1 = new CombatTactic("boss_kiting");
        bossTactic1.setDescription("Keep distance and attack between boss abilities");
        bossTactic1.setPriority(1);
        bossTactic1.addScenarioId("boss_combat");
        tacticsLibrary.put(bossTactic1.getId(), bossTactic1);
        
        CombatTactic bossTactic2 = new CombatTactic("boss_burst_damage");
        bossTactic2.setDescription("Save cooldowns for vulnerability phases");
        bossTactic2.setPriority(2);
        bossTactic2.addScenarioId("boss_combat");
        tacticsLibrary.put(bossTactic2.getId(), bossTactic2);
        
        // Multiple enemies tactics
        CombatTactic mobTactic1 = new CombatTactic("aoe_focus");
        mobTactic1.setDescription("Use area attacks to hit multiple targets");
        mobTactic1.setPriority(1);
        mobTactic1.addScenarioId("multiple_enemies");
        tacticsLibrary.put(mobTactic1.getId(), mobTactic1);
        
        CombatTactic mobTactic2 = new CombatTactic("crowd_control");
        mobTactic2.setDescription("Use stuns and slows to manage multiple enemies");
        mobTactic2.setPriority(2);
        mobTactic2.addScenarioId("multiple_enemies");
        tacticsLibrary.put(mobTactic2.getId(), mobTactic2);
        
        // Ranged combat tactics
        CombatTactic rangedTactic1 = new CombatTactic("use_cover");
        rangedTactic1.setDescription("Utilize cover between attacks");
        rangedTactic1.setPriority(1);
        rangedTactic1.addScenarioId("ranged_combat");
        tacticsLibrary.put(rangedTactic1.getId(), rangedTactic1);
        
        CombatTactic rangedTactic2 = new CombatTactic("headshot_focus");
        rangedTactic2.setDescription("Aim for headshots to maximize damage");
        rangedTactic2.setPriority(2);
        rangedTactic2.addScenarioId("ranged_combat");
        tacticsLibrary.put(rangedTactic2.getId(), rangedTactic2);
    }
    
    /**
     * Update the current combat state
     */
    private void updateCombatState() {
        // This would use game state data to determine if combat has started or ended
        // For now, it's just a stub
        
        // If we detect combat but no session is active, start one
        // This is a placeholder for actual combat detection logic
        
        // If we detect combat has ended, end current session
        // This is a placeholder for actual combat end detection logic
    }
    
    /**
     * Analyze current combat in progress
     */
    private void analyzeCurrentCombat() {
        if (currentSession == null) {
            return;
        }
        
        // Update metrics
        currentSession.updateMetrics();
        
        // Check if we need to update tactical recommendations
        CombatScenario scenario = identifyCurrentScenario();
        if (scenario != null) {
            List<CombatTactic> tactics = getTacticsForScenario(scenario);
            
            // Notify listeners of new recommendations
            for (CombatAnalysisListener listener : listeners) {
                listener.onTacticalRecommendations(tactics);
            }
        }
    }
    
    /**
     * Analyze a completed combat session
     * @param session The combat session to analyze
     */
    private void analyzeCombatSession(CombatSession session) {
        if (session == null) {
            return;
        }
        
        // Calculate final metrics
        session.calculateFinalMetrics();
        
        // Look for patterns that could improve future combats
        // This would analyze the session for patterns to store for future reference
        
        Log.d(TAG, "Combat session analysis complete. Efficiency: " + 
              session.getEfficiencyRating() + ", DPS: " + 
              session.getAverageDPS());
    }
    
    /**
     * Identify the current combat scenario
     * @return Matched scenario or null if no match
     */
    private CombatScenario identifyCurrentScenario() {
        if (currentSession == null) {
            return null;
        }
        
        // Extract characteristics from current session
        Map<String, Float> currentCharacteristics = extractSessionCharacteristics();
        
        // Find best matching scenario
        CombatScenario bestMatch = null;
        float bestScore = 0.5f; // Minimum threshold for matching
        
        for (CombatScenario scenario : knownScenarios.values()) {
            float matchScore = scenario.calculateMatchScore(currentCharacteristics);
            if (matchScore > bestScore) {
                bestScore = matchScore;
                bestMatch = scenario;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Extract characteristics from current session
     * @return Map of characteristics and their values
     */
    private Map<String, Float> extractSessionCharacteristics() {
        Map<String, Float> characteristics = new HashMap<>();
        
        if (currentSession == null) {
            return characteristics;
        }
        
        // Count targets
        Set<String> targets = new HashSet<>();
        for (CombatAction action : currentSession.getActions()) {
            if (!action.isPlayerAction()) {
                targets.add(action.getSource());
            }
        }
        
        // Single vs multiple targets
        if (targets.size() == 1) {
            characteristics.put("single_target", 1.0f);
            characteristics.put("multiple_targets", 0.0f);
        } else if (targets.size() > 1) {
            characteristics.put("single_target", 0.0f);
            characteristics.put("multiple_targets", Math.min(1.0f, targets.size() / 5.0f));
        }
        
        // Damage characteristics
        float maxDamage = 0;
        float totalDamage = 0;
        int enemyActionCount = 0;
        
        for (CombatAction action : currentSession.getActions()) {
            if (!action.isPlayerAction()) {
                maxDamage = Math.max(maxDamage, action.getDamage());
                totalDamage += action.getDamage();
                enemyActionCount++;
            }
        }
        
        if (enemyActionCount > 0) {
            float avgDamage = totalDamage / enemyActionCount;
            
            // High damage characteristic
            if (maxDamage > 50) { // Arbitrary threshold
                characteristics.put("high_damage", 1.0f);
            } else {
                characteristics.put("high_damage", maxDamage / 50.0f);
            }
            
            // Low individual damage characteristic
            if (avgDamage < 10) { // Arbitrary threshold
                characteristics.put("low_individual_damage", 1.0f);
            } else {
                characteristics.put("low_individual_damage", 10.0f / avgDamage);
            }
        }
        
        // Analyze attack patterns for predictability
        // This would require more complex analysis in a real implementation
        
        return characteristics;
    }
    
    /**
     * Get tactics for a specific scenario
     * @param scenario The combat scenario
     * @return List of applicable tactics
     */
    private List<CombatTactic> getTacticsForScenario(CombatScenario scenario) {
        List<CombatTactic> applicableTactics = new ArrayList<>();
        
        for (CombatTactic tactic : tacticsLibrary.values()) {
            if (tactic.isApplicableToScenario(scenario.getId())) {
                applicableTactics.add(tactic);
            }
        }
        
        // Sort by priority
        Collections.sort(applicableTactics, (t1, t2) -> 
            Integer.compare(t1.getPriority(), t2.getPriority()));
        
        return applicableTactics;
    }
    
    /**
     * Combat Action class
     * Represents a single action in combat
     */
    public static class CombatAction {
        private final String type;
        private final String targetOrSource;
        private final float damage;
        private final boolean playerAction;
        private final long timestamp;
        
        /**
         * Constructor
         * @param type Action type
         * @param targetOrSource Target (for player) or source (for enemy)
         * @param damage Damage dealt
         * @param playerAction true if player action, false if enemy
         * @param timestamp Time of action
         */
        public CombatAction(String type, String targetOrSource, float damage,
                           boolean playerAction, long timestamp) {
            this.type = type;
            this.targetOrSource = targetOrSource;
            this.damage = damage;
            this.playerAction = playerAction;
            this.timestamp = timestamp;
        }
        
        public String getType() {
            return type;
        }
        
        public String getTarget() {
            return playerAction ? targetOrSource : null;
        }
        
        public String getSource() {
            return playerAction ? null : targetOrSource;
        }
        
        public float getDamage() {
            return damage;
        }
        
        public boolean isPlayerAction() {
            return playerAction;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Combat Session class
     * Represents a single combat encounter
     */
    public static class CombatSession {
        private final String type;
        private final long startTime;
        private long endTime;
        private final List<CombatAction> actions;
        private float efficiencyRating;
        private float averageDPS;
        private Map<String, Float> metrics;
        
        /**
         * Constructor
         * @param type Type of combat
         */
        public CombatSession(String type) {
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.endTime = 0;
            this.actions = new ArrayList<>();
            this.efficiencyRating = 0.0f;
            this.averageDPS = 0.0f;
            this.metrics = new HashMap<>();
        }
        
        /**
         * End the session
         */
        public void end() {
            if (endTime == 0) {
                endTime = System.currentTimeMillis();
            }
        }
        
        /**
         * Add an action to the session
         * @param action Combat action
         */
        public void addAction(CombatAction action) {
            actions.add(action);
        }
        
        /**
         * Get all actions in the session
         * @return List of combat actions
         */
        public List<CombatAction> getActions() {
            return new ArrayList<>(actions);
        }
        
        /**
         * Calculate current DPS over a time window
         * @param windowMs Time window in milliseconds
         * @return Current DPS
         */
        public float calculateDPS(long windowMs) {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - windowMs;
            
            float totalDamage = 0.0f;
            
            for (CombatAction action : actions) {
                if (action.isPlayerAction() && action.getTimestamp() >= windowStart) {
                    totalDamage += action.getDamage();
                }
            }
            
            return totalDamage / (windowMs / 1000.0f);
        }
        
        /**
         * Update metrics during combat
         */
        public void updateMetrics() {
            // Calculate current metrics
            calculateCurrentMetrics();
        }
        
        /**
         * Calculate final metrics after combat
         */
        public void calculateFinalMetrics() {
            if (endTime == 0) {
                end();
            }
            
            // Calculate total player and enemy damage
            float totalPlayerDamage = 0.0f;
            float totalEnemyDamage = 0.0f;
            
            for (CombatAction action : actions) {
                if (action.isPlayerAction()) {
                    totalPlayerDamage += action.getDamage();
                } else {
                    totalEnemyDamage += action.getDamage();
                }
            }
            
            // Calculate duration in seconds
            float durationSeconds = getDuration() / 1000.0f;
            
            // Calculate average DPS
            averageDPS = durationSeconds > 0 ? totalPlayerDamage / durationSeconds : 0;
            
            // Calculate efficiency (higher is better)
            // Ratio of damage dealt to damage taken, normalized to 0-1 range
            if (totalEnemyDamage > 0) {
                float ratio = totalPlayerDamage / totalEnemyDamage;
                efficiencyRating = Math.min(1.0f, ratio / 3.0f); // Normalize, 3.0 ratio = perfect
            } else {
                efficiencyRating = 1.0f; // No damage taken = perfect
            }
            
            // Store in metrics
            metrics.put("total_player_damage", totalPlayerDamage);
            metrics.put("total_enemy_damage", totalEnemyDamage);
            metrics.put("average_dps", averageDPS);
            metrics.put("efficiency_rating", efficiencyRating);
            metrics.put("duration_seconds", durationSeconds);
        }
        
        /**
         * Calculate current metrics
         */
        private void calculateCurrentMetrics() {
            // Similar to calculateFinalMetrics, but for in-progress combat
            // Calculate metrics up to current time
            long currentTime = System.currentTimeMillis();
            
            float totalPlayerDamage = 0.0f;
            float totalEnemyDamage = 0.0f;
            
            for (CombatAction action : actions) {
                if (action.isPlayerAction()) {
                    totalPlayerDamage += action.getDamage();
                } else {
                    totalEnemyDamage += action.getDamage();
                }
            }
            
            // Calculate current duration in seconds
            float currentDurationSeconds = (currentTime - startTime) / 1000.0f;
            
            // Calculate current DPS
            float currentDPS = currentDurationSeconds > 0 ? 
                totalPlayerDamage / currentDurationSeconds : 0;
            
            // Store in metrics
            metrics.put("current_player_damage", totalPlayerDamage);
            metrics.put("current_enemy_damage", totalEnemyDamage);
            metrics.put("current_dps", currentDPS);
            metrics.put("current_duration_seconds", currentDurationSeconds);
        }
        
        /**
         * Get session duration
         * @return Duration in milliseconds
         */
        public long getDuration() {
            if (endTime > 0) {
                return endTime - startTime;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        }
        
        /**
         * Get combat type
         * @return Combat type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get efficiency rating
         * @return Efficiency rating (0-1)
         */
        public float getEfficiencyRating() {
            return efficiencyRating;
        }
        
        /**
         * Get average DPS
         * @return Average DPS
         */
        public float getAverageDPS() {
            return averageDPS;
        }
        
        /**
         * Get start time
         * @return Start time in milliseconds
         */
        public long getStartTime() {
            return startTime;
        }
        
        /**
         * Get end time
         * @return End time in milliseconds, or 0 if not ended
         */
        public long getEndTime() {
            return endTime;
        }
        
        /**
         * Get all metrics
         * @return Map of metrics
         */
        public Map<String, Float> getMetrics() {
            return new HashMap<>(metrics);
        }
        
        /**
         * Get a specific metric
         * @param name Metric name
         * @return Metric value or 0 if not found
         */
        public float getMetric(String name) {
            return metrics.getOrDefault(name, 0.0f);
        }
    }
    
    /**
     * Combat Scenario class
     * Represents a type of combat situation
     */
    public static class CombatScenario {
        private final String id;
        private final Map<String, Float> characteristics;
        
        /**
         * Constructor
         * @param id Scenario ID
         */
        public CombatScenario(String id) {
            this.id = id;
            this.characteristics = new HashMap<>();
        }
        
        /**
         * Add a characteristic to this scenario
         * @param name Characteristic name
         * @param value Characteristic value (0-1)
         */
        public void addCharacteristic(String name, float value) {
            characteristics.put(name, value);
        }
        
        /**
         * Get scenario ID
         * @return Scenario ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get all characteristics
         * @return Map of characteristics
         */
        public Map<String, Float> getCharacteristics() {
            return new HashMap<>(characteristics);
        }
        
        /**
         * Calculate match score against a set of characteristics
         * @param otherCharacteristics Characteristics to match against
         * @return Match score (0-1)
         */
        public float calculateMatchScore(Map<String, Float> otherCharacteristics) {
            if (otherCharacteristics == null || otherCharacteristics.isEmpty()) {
                return 0.0f;
            }
            
            float totalScore = 0.0f;
            int matchCount = 0;
            
            // Calculate score for each characteristic this scenario has
            for (Map.Entry<String, Float> entry : characteristics.entrySet()) {
                String name = entry.getKey();
                float thisValue = entry.getValue();
                
                if (otherCharacteristics.containsKey(name)) {
                    float otherValue = otherCharacteristics.get(name);
                    
                    // Higher score for closer matches
                    float similarity = 1.0f - Math.abs(thisValue - otherValue);
                    totalScore += similarity;
                    matchCount++;
                }
            }
            
            // Return average match score
            return matchCount > 0 ? totalScore / matchCount : 0.0f;
        }
    }
    
    /**
     * Combat Tactic class
     * Represents a tactical recommendation
     */
    public static class CombatTactic {
        private final String id;
        private String description;
        private int priority;
        private final List<String> applicableScenarios;
        
        /**
         * Constructor
         * @param id Tactic ID
         */
        public CombatTactic(String id) {
            this.id = id;
            this.description = "";
            this.priority = 1;
            this.applicableScenarios = new ArrayList<>();
        }
        
        /**
         * Set tactic description
         * @param description Description text
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Set priority (lower number = higher priority)
         * @param priority Priority value
         */
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        /**
         * Add a scenario this tactic applies to
         * @param scenarioId Scenario ID
         */
        public void addScenarioId(String scenarioId) {
            if (!applicableScenarios.contains(scenarioId)) {
                applicableScenarios.add(scenarioId);
            }
        }
        
        /**
         * Check if tactic applies to a scenario
         * @param scenarioId Scenario ID
         * @return true if applicable
         */
        public boolean isApplicableToScenario(String scenarioId) {
            return applicableScenarios.contains(scenarioId);
        }
        
        /**
         * Get tactic ID
         * @return Tactic ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get description
         * @return Description text
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Get priority
         * @return Priority value
         */
        public int getPriority() {
            return priority;
        }
        
        /**
         * Get all applicable scenarios
         * @return List of scenario IDs
         */
        public List<String> getApplicableScenarios() {
            return new ArrayList<>(applicableScenarios);
        }
    }
    
    /**
     * Combat Analysis Listener interface
     * For receiving combat events
     */
    public interface CombatAnalysisListener {
        /**
         * Called when combat starts
         * @param session New combat session
         */
        void onCombatStarted(CombatSession session);
        
        /**
         * Called when combat ends
         * @param session Completed combat session
         */
        void onCombatEnded(CombatSession session);
        
        /**
         * Called when player performs an action
         * @param action Player action
         */
        void onPlayerAction(CombatAction action);
        
        /**
         * Called when enemy performs an action
         * @param action Enemy action
         */
        void onEnemyAction(CombatAction action);
        
        /**
         * Called when tactical recommendations are updated
         * @param tactics List of recommended tactics
         */
        void onTacticalRecommendations(List<CombatTactic> tactics);
    }
}
