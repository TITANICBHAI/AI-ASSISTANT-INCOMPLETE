package com.aiassistant.core.ai.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aiassistant.data.models.DetectedEnemy;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.UIElement;

import java.util.List;

/**
 * System for detecting combat situations in the game
 */
public class CombatDetectionSystem {
    
    private static final String TAG = "CombatDetectionSystem";
    
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context The context
     */
    public CombatDetectionSystem(Context context) {
        this.context = context;
    }
    
    /**
     * Check if the player is in combat
     * 
     * @param screenshot The screenshot
     * @param gameState The game state
     * @param enemies The detected enemies
     * @param uiElements The detected UI elements
     * @return Whether the player is in combat
     */
    public boolean isInCombat(Bitmap screenshot, GameState gameState, List<DetectedEnemy> enemies, List<UIElement> uiElements) {
        try {
            // Criteria for combat detection:
            // 1. Enemies are present
            boolean hasEnemies = !enemies.isEmpty();
            
            // 2. Combat UI elements are present (health bars, ammo counters, etc.)
            boolean hasCombatUI = hasCombatUIElements(uiElements);
            
            // 3. Player health is less than max
            boolean playerDamaged = gameState.getPlayerHealth() < 1.0f;
            
            // Different games may have different indicators
            String gamePackage = gameState.getGamePackage();
            
            // Apply game-specific logic
            if (gamePackage != null) {
                if (gamePackage.contains("pubg")) {
                    // For PUBG, just need enemies or being damaged
                    return hasEnemies || playerDamaged;
                } else if (gamePackage.contains("cod")) {
                    // For COD, need combat UI and enemies
                    return hasCombatUI && hasEnemies;
                } else if (gamePackage.contains("freefire")) {
                    // For Free Fire, just need enemies
                    return hasEnemies;
                }
            }
            
            // Default logic: if we have enemies and either combat UI or damaged player
            return hasEnemies && (hasCombatUI || playerDamaged);
            
        } catch (Exception e) {
            Log.e(TAG, "Error detecting combat: " + e.getMessage(), e);
            // Default to false
            return false;
        }
    }
    
    /**
     * Check if combat UI elements are present
     * 
     * @param uiElements The UI elements
     * @return Whether combat UI elements are present
     */
    private boolean hasCombatUIElements(List<UIElement> uiElements) {
        if (uiElements == null || uiElements.isEmpty()) {
            return false;
        }
        
        // Look for combat-related UI elements
        for (UIElement element : uiElements) {
            String type = element.getType();
            if (type != null) {
                if (type.equals("health_bar") || 
                    type.equals("ammo_counter") || 
                    type.equals("damage_indicator") ||
                    type.equals("crosshair") ||
                    type.equals("combat_button")) {
                    return true;
                }
            }
            
            // Also check text
            String text = element.getText();
            if (text != null) {
                if (text.contains("FIRE") || 
                    text.contains("RELOAD") || 
                    text.contains("AIM") ||
                    text.contains("SHOOT")) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
