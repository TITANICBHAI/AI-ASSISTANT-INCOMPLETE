package com.aiassistant.ai.features.combat;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.HybridAILearningSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat Analysis Manager
 * Simplified interface for using the combat analysis feature
 */
public class CombatAnalysisManager implements CombatAnalysisFeature.CombatAnalysisListener {
    private static final String TAG = "CombatAnalysisManager";
    
    private final Context context;
    private final CombatAnalysisFeature combatAnalysisFeature;
    private final List<CombatRecommendationListener> recommendationListeners;
    private final List<CombatStatisticsListener> statisticsListeners;
    private HybridAILearningSystem hybridAI;
    
    /**
     * Constructor
     * @param context Application context
     * @param combatAnalysisFeature Combat analysis feature
     */
    public CombatAnalysisManager(Context context, CombatAnalysisFeature combatAnalysisFeature) {
        this.context = context;
        this.combatAnalysisFeature = combatAnalysisFeature;
        this.recommendationListeners = new ArrayList<>();
        this.statisticsListeners = new ArrayList<>();
        this.hybridAI = HybridAILearningSystem.getInstance(context);
        
        // Register as a listener
        combatAnalysisFeature.addListener(this);
    }
    
    /**
     * Start combat analysis
     * @param combatType Type of combat
     */
    public void startAnalysis(String combatType) {
        if (combatAnalysisFeature.isEnabled() && !combatAnalysisFeature.isInCombat()) {
            combatAnalysisFeature.startCombat(combatType);
            Log.d(TAG, "Started combat analysis for " + combatType);
        }
    }
    
    /**
     * End combat analysis
     */
    public void endAnalysis() {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            CombatAnalysisFeature.CombatSession session = combatAnalysisFeature.endCombat();
            Log.d(TAG, "Ended combat analysis, duration: " + 
                  (session != null ? session.getDuration() / 1000.0f : 0) + "s");
        }
    }
    
    /**
     * Record player attack
     * @param target Target name
     * @param damage Damage dealt
     */
    public void recordPlayerAttack(String target, float damage) {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            combatAnalysisFeature.recordPlayerAction(
                "attack", target, damage, System.currentTimeMillis());
        }
    }
    
    /**
     * Record player skill use
     * @param skillName Skill name
     * @param target Target name
     * @param damage Damage dealt
     */
    public void recordPlayerSkill(String skillName, String target, float damage) {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            combatAnalysisFeature.recordPlayerAction(
                "skill:" + skillName, target, damage, System.currentTimeMillis());
        }
    }
    
    /**
     * Record enemy attack
     * @param enemy Enemy name
     * @param damage Damage dealt
     */
    public void recordEnemyAttack(String enemy, float damage) {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            combatAnalysisFeature.recordEnemyAction(
                "attack", enemy, damage, System.currentTimeMillis());
        }
    }
    
    /**
     * Record enemy skill use
     * @param enemy Enemy name
     * @param skillName Skill name
     * @param damage Damage dealt
     */
    public void recordEnemySkill(String enemy, String skillName, float damage) {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            combatAnalysisFeature.recordEnemyAction(
                "skill:" + skillName, enemy, damage, System.currentTimeMillis());
        }
    }
    
    /**
     * Get current DPS
     * @return Current DPS over last 5 seconds
     */
    public float getCurrentDPS() {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            return combatAnalysisFeature.getCurrentDPS(5000); // 5 seconds window
        }
        return 0.0f;
    }
    
    /**
     * Get combat efficiency
     * @return Efficiency rating (0-1)
     */
    public float getCombatEfficiency() {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            CombatAnalysisFeature.CombatSession session = 
                combatAnalysisFeature.getCurrentSession();
            if (session != null) {
                return session.getEfficiencyRating();
            }
        }
        return 0.0f;
    }
    
    /**
     * Get combat duration
     * @return Duration in seconds
     */
    public float getCombatDuration() {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            CombatAnalysisFeature.CombatSession session = 
                combatAnalysisFeature.getCurrentSession();
            if (session != null) {
                return session.getDuration() / 1000.0f;
            }
        }
        return 0.0f;
    }
    
    /**
     * Get top recommended tactic
     * @return Top tactic or null if none available
     */
    public String getTopRecommendation() {
        if (combatAnalysisFeature.isEnabled() && combatAnalysisFeature.isInCombat()) {
            List<CombatAnalysisFeature.CombatTactic> tactics = 
                combatAnalysisFeature.getRecommendedTactics();
            
            if (!tactics.isEmpty()) {
                return tactics.get(0).getDescription();
            }
        }
        return null;
    }
    
    /**
     * Get AI-powered tactical recommendation for current combat situation
     * @param combatContext Description of the combat situation
     * @return Tactical recommendation as String
     */
    public String getTacticalRecommendation(String combatContext) {
        if (!combatAnalysisFeature.isEnabled() || !combatAnalysisFeature.isInCombat()) {
            return "Not currently in combat.";
        }
        
        // Build comprehensive context
        float currentDPS = getCurrentDPS();
        float efficiency = getCombatEfficiency();
        float duration = getCombatDuration();
        
        String context = "Combat situation: " + combatContext + 
                        ". Current DPS: " + currentDPS + 
                        ". Efficiency: " + (efficiency * 100) + "%" +
                        ". Combat duration: " + duration + " seconds" +
                        ". Provide one tactical recommendation to improve performance.";
        
        final String[] recommendation = new String[1];
        final Object lock = new Object();
        
        // Use HybridAI to generate tactical recommendation
        hybridAI.processQuery(context, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                synchronized (lock) {
                    recommendation[0] = response;
                    Log.d(TAG, "Tactical recommendation from " + source + ": " + response);
                    lock.notify();
                }
            }
            
            @Override
            public void onError(String error) {
                synchronized (lock) {
                    recommendation[0] = "Focus on improving efficiency and DPS. Current performance: " + 
                                       (efficiency * 100) + "%";
                    Log.e(TAG, "Error getting tactical recommendation: " + error);
                    lock.notify();
                }
            }
        });
        
        // Wait for response with timeout
        synchronized (lock) {
            try {
                lock.wait(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for tactical recommendation", e);
                recommendation[0] = "Maintain focus and continue current strategy.";
            }
        }
        
        return recommendation[0] != null ? recommendation[0] : "Continue current tactics.";
    }
    
    /**
     * Add a recommendation listener
     * @param listener Listener to add
     */
    public void addRecommendationListener(CombatRecommendationListener listener) {
        if (listener != null && !recommendationListeners.contains(listener)) {
            recommendationListeners.add(listener);
        }
    }
    
    /**
     * Remove a recommendation listener
     * @param listener Listener to remove
     */
    public void removeRecommendationListener(CombatRecommendationListener listener) {
        recommendationListeners.remove(listener);
    }
    
    /**
     * Add a statistics listener
     * @param listener Listener to add
     */
    public void addStatisticsListener(CombatStatisticsListener listener) {
        if (listener != null && !statisticsListeners.contains(listener)) {
            statisticsListeners.add(listener);
        }
    }
    
    /**
     * Remove a statistics listener
     * @param listener Listener to remove
     */
    public void removeStatisticsListener(CombatStatisticsListener listener) {
        statisticsListeners.remove(listener);
    }
    
    @Override
    public void onCombatStarted(CombatAnalysisFeature.CombatSession session) {
        // Notify statistics listeners
        for (CombatStatisticsListener listener : statisticsListeners) {
            listener.onCombatStarted();
        }
    }
    
    @Override
    public void onCombatEnded(CombatAnalysisFeature.CombatSession session) {
        // Notify statistics listeners
        CombatStatistics stats = new CombatStatistics(
            session.getEfficiencyRating(),
            session.getAverageDPS(),
            session.getDuration() / 1000.0f,
            session.getMetric("total_player_damage"),
            session.getMetric("total_enemy_damage")
        );
        
        for (CombatStatisticsListener listener : statisticsListeners) {
            listener.onCombatEnded(stats);
        }
    }
    
    @Override
    public void onPlayerAction(CombatAnalysisFeature.CombatAction action) {
        // Update statistics listeners
        float currentDps = getCurrentDPS();
        float efficiency = getCombatEfficiency();
        
        for (CombatStatisticsListener listener : statisticsListeners) {
            listener.onStatsUpdated(currentDps, efficiency);
        }
    }
    
    @Override
    public void onEnemyAction(CombatAnalysisFeature.CombatAction action) {
        // Update statistics listeners
        float currentDps = getCurrentDPS();
        float efficiency = getCombatEfficiency();
        
        for (CombatStatisticsListener listener : statisticsListeners) {
            listener.onStatsUpdated(currentDps, efficiency);
        }
    }
    
    @Override
    public void onTacticalRecommendations(List<CombatAnalysisFeature.CombatTactic> tactics) {
        // Create recommendation objects
        List<CombatRecommendation> recommendations = new ArrayList<>();
        
        for (CombatAnalysisFeature.CombatTactic tactic : tactics) {
            recommendations.add(new CombatRecommendation(
                tactic.getId(),
                tactic.getDescription(),
                tactic.getPriority()
            ));
        }
        
        // Notify recommendation listeners
        for (CombatRecommendationListener listener : recommendationListeners) {
            listener.onRecommendationsUpdated(recommendations);
        }
    }
    
    /**
     * Combat Recommendation class
     * Simplified version of CombatTactic for UI display
     */
    public static class CombatRecommendation {
        private final String id;
        private final String description;
        private final int priority;
        
        /**
         * Constructor
         * @param id Recommendation ID
         * @param description Description text
         * @param priority Priority (lower = higher priority)
         */
        public CombatRecommendation(String id, String description, int priority) {
            this.id = id;
            this.description = description;
            this.priority = priority;
        }
        
        public String getId() {
            return id;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    /**
     * Combat Statistics class
     * Holds combat performance statistics
     */
    public static class CombatStatistics {
        private final float efficiency;
        private final float dps;
        private final float duration;
        private final float damageDealt;
        private final float damageTaken;
        
        /**
         * Constructor
         * @param efficiency Efficiency rating (0-1)
         * @param dps Average DPS
         * @param duration Duration in seconds
         * @param damageDealt Total damage dealt
         * @param damageTaken Total damage taken
         */
        public CombatStatistics(float efficiency, float dps, float duration,
                               float damageDealt, float damageTaken) {
            this.efficiency = efficiency;
            this.dps = dps;
            this.duration = duration;
            this.damageDealt = damageDealt;
            this.damageTaken = damageTaken;
        }
        
        public float getEfficiency() {
            return efficiency;
        }
        
        public float getDps() {
            return dps;
        }
        
        public float getDuration() {
            return duration;
        }
        
        public float getDamageDealt() {
            return damageDealt;
        }
        
        public float getDamageTaken() {
            return damageTaken;
        }
        
        /**
         * Get efficiency grade (A, B, C, D, F)
         * @return Letter grade
         */
        public String getEfficiencyGrade() {
            if (efficiency >= 0.9f) return "A";
            if (efficiency >= 0.7f) return "B";
            if (efficiency >= 0.5f) return "C";
            if (efficiency >= 0.3f) return "D";
            return "F";
        }
    }
    
    /**
     * Combat Recommendation Listener interface
     * For receiving tactical recommendations
     */
    public interface CombatRecommendationListener {
        /**
         * Called when recommendations are updated
         * @param recommendations List of recommendations
         */
        void onRecommendationsUpdated(List<CombatRecommendation> recommendations);
    }
    
    /**
     * Combat Statistics Listener interface
     * For receiving combat statistics updates
     */
    public interface CombatStatisticsListener {
        /**
         * Called when combat starts
         */
        void onCombatStarted();
        
        /**
         * Called when combat ends
         * @param statistics Final combat statistics
         */
        void onCombatEnded(CombatStatistics statistics);
        
        /**
         * Called when combat stats are updated
         * @param currentDps Current DPS
         * @param efficiency Current efficiency
         */
        void onStatsUpdated(float currentDps, float efficiency);
    }
}
