package com.aiassistant.core.voice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;

import com.aiassistant.core.ai.AIAction;
import com.aiassistant.core.interaction.AdvancedGameInteraction;
import com.aiassistant.services.GameInteractionService;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for integrating voice commands with game actions
 * Maps voice commands to specific game interactions
 */
public class GameVoiceCommands {
    private static final String TAG = "GameVoiceCommands";
    
    // Command categories
    public static final int CATEGORY_MOVEMENT = 1;
    public static final int CATEGORY_COMBAT = 2;
    public static final int CATEGORY_TACTICAL = 3;
    public static final int CATEGORY_SYSTEM = 4;
    
    // Command to action mapping
    private static final Map<String, AIAction> commandActionMap = new HashMap<>();
    
    /**
     * Initialize the command mappings
     */
    public static void initialize() {
        // Movement commands
        commandActionMap.put("move_forward", AIAction.createSwipeAction(200, 650, 200, 450, 300));
        commandActionMap.put("move_backward", AIAction.createSwipeAction(200, 650, 200, 850, 300));
        commandActionMap.put("move_left", AIAction.createSwipeAction(200, 650, 0, 650, 300));
        commandActionMap.put("move_right", AIAction.createSwipeAction(200, 650, 400, 650, 300));
        
        // Action commands
        commandActionMap.put("jump", AIAction.createTapAction(950, 700));
        commandActionMap.put("crouch", AIAction.createTapAction(850, 700));
        commandActionMap.put("prone", AIAction.createTapAction(750, 700));
        
        // Combat commands
        commandActionMap.put("fire", AIAction.createTapAction(1050, 500));
        commandActionMap.put("aim", AIAction.createTapAction(950, 550));
        commandActionMap.put("reload", AIAction.createTapAction(750, 500));
        commandActionMap.put("switch_weapon", AIAction.createTapAction(800, 350));
        commandActionMap.put("throw_grenade", AIAction.createTapAction(650, 500));
        
        // Tactical commands
        commandActionMap.put("heal", AIAction.createTapAction(600, 600));
        commandActionMap.put("use_ability", AIAction.createTapAction(900, 400));
        
        // Complex maneuvers
        AIAction dropShotAction = new AIAction(AIAction.ACTION_COMBO);
        dropShotAction.setComboName("drop_shot");
        commandActionMap.put("drop_shot", dropShotAction);
        
        AIAction jumpShotAction = new AIAction(AIAction.ACTION_COMBO);
        jumpShotAction.setComboName("jump_shot");
        commandActionMap.put("jump_shot", jumpShotAction);
        
        Log.d(TAG, "Command mappings initialized with " + commandActionMap.size() + " commands");
    }
    
    /**
     * Execute a voice command
     */
    public static boolean executeCommand(Context context, String command) {
        if (context == null || command == null) {
            return false;
        }
        
        AIAction action = commandActionMap.get(command);
        if (action != null) {
            // Send action to GameInteractionService
            Intent intent = new Intent(context, GameInteractionService.class);
            intent.setAction("VOICE_COMMAND");
            intent.putExtra("command", command);
            
            context.startService(intent);
            
            Log.d(TAG, "Executed voice command: " + command);
            return true;
        }
        
        Log.w(TAG, "Unknown voice command: " + command);
        return false;
    }
    
    /**
     * Get action for command
     */
    public static AIAction getActionForCommand(String command) {
        return commandActionMap.get(command);
    }
    
    /**
     * Get category for command
     */
    public static int getCategoryForCommand(String command) {
        if (command == null) {
            return 0;
        }
        
        if (command.startsWith("move_") || command.equals("jump") || 
                command.equals("crouch") || command.equals("prone")) {
            return CATEGORY_MOVEMENT;
        } else if (command.startsWith("fire") || command.equals("aim") || 
                command.equals("reload") || command.equals("switch_weapon") || 
                command.equals("throw_grenade")) {
            return CATEGORY_COMBAT;
        } else if (command.equals("heal") || command.equals("use_ability") || 
                command.equals("take_cover") || command.startsWith("ping_")) {
            return CATEGORY_TACTICAL;
        } else if (command.startsWith("drop_shot") || command.equals("jump_shot") || 
                command.equals("rush") || command.equals("flank")) {
            return CATEGORY_COMBAT;
        } else if (command.startsWith("stop_") || command.contains("game") || 
                command.contains("screenshot") || command.contains("record")) {
            return CATEGORY_SYSTEM;
        }
        
        return 0;
    }
}
