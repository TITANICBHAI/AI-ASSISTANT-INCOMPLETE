package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.voice.query.EnhancedVoiceQueryHandler;
import com.aiassistant.core.ai.AIStateManager;

/**
 * Integrates the comprehensive voice query system with the existing SentientVoiceSystem
 * This class connects all assistant features to the voice interface, enabling full
 * access to all capabilities through voice commands.
 */
public class SentientVoiceIntegration {
    private static final String TAG = "SentientVoiceIntegration";
    
    private Context context;
    private EnhancedVoiceQueryHandler queryHandler;
    private AIStateManager stateManager;
    
    /**
     * Constructor
     * @param context Android context
     */
    public SentientVoiceIntegration(Context context) {
        this.context = context;
        this.queryHandler = new EnhancedVoiceQueryHandler(context);
        
        // Connect to AI state manager for persistent state
        try {
            this.stateManager = AIStateManager.getInstance(context);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AIStateManager", e);
        }
        
        Log.i(TAG, "SentientVoiceIntegration initialized - All features now accessible via voice");
    }
    
    /**
     * Process a voice command from the SentientVoiceSystem
     * @param command User's voice command
     * @return Response to speak back
     */
    public String processVoiceCommand(String command) {
        Log.i(TAG, "Processing voice command: " + command);
        
        // Use the enhanced query handler to process the command
        String response = queryHandler.processQuery(command);
        
        // Store interaction in AI state for learning and persistence
        if (stateManager != null) {
            try {
                stateManager.recordInteraction(command, response);
            } catch (Exception e) {
                Log.e(TAG, "Error recording interaction", e);
            }
        }
        
        return response;
    }
    
    /**
     * Check if a given command would activate a specific feature
     * @param command Voice command
     * @param featureType Type of feature to check
     * @return True if command would activate the feature
     */
    public boolean isFeatureCommand(String command, FeatureType featureType) {
        command = command.toLowerCase();
        
        switch (featureType) {
            case AUTO_AI:
                return command.contains("auto ai") || 
                       command.contains("automate") || 
                       command.contains("automation");
                       
            case COPILOT:
                return command.contains("copilot") || 
                       command.contains("co-pilot") || 
                       command.contains("game assist");
                       
            case TASK_SCHEDULER:
                return command.contains("schedule") || 
                       command.contains("reminder") || 
                       command.contains("task");
                       
            case GAMING:
                return command.contains("play") || 
                       (command.contains("open") && (
                           command.contains("game") || 
                           command.contains("free fire") || 
                           command.contains("pubg") || 
                           command.contains("call of duty")));
                       
            case EDUCATION:
                return command.contains("solve") || 
                       command.contains("jee") || 
                       command.contains("physics") ||
                       command.contains("chemistry") ||
                       command.contains("math");
                       
            case EXTERNAL_APP:
                return command.contains("open ") || 
                       command.contains("send ") || 
                       command.contains("message") || 
                       command.contains("call ");
                       
            case PDF_LEARNING:
                return command.contains("pdf") || 
                       command.contains("learn from") || 
                       command.contains("extract") ||
                       command.contains("read document");
                       
            default:
                return false;
        }
    }
    
    /**
     * Get a list of all features that can be accessed by voice
     * @return Array of accessible features
     */
    public String[] getAccessibleFeatures() {
        return new String[] {
            "Auto AI - Automated actions based on user patterns",
            "Copilot - Real-time gaming assistance",
            "Task Scheduler - Manage study sessions and reminders",
            "Gaming - Game launching and tips",
            "JEE Problem Solver - Solve complex physics, chemistry and math problems",
            "External App Control - Open apps and send messages",
            "PDF Learning - Extract knowledge from study materials",
            "Voice Reflection - Self-aware AI personality with opinions and favorites",
            "Persistent Memory - Remember conversations across sessions"
        };
    }
    
    /**
     * Get example commands for a specific feature
     * @param featureType Feature type
     * @return Array of example commands
     */
    public String[] getExampleCommands(FeatureType featureType) {
        switch (featureType) {
            case AUTO_AI:
                return new String[] {
                    "Turn on Auto AI in passive mode",
                    "Enable full automation",
                    "Disable Auto AI",
                    "What's the status of Auto AI?"
                };
                
            case COPILOT:
                return new String[] {
                    "Enable Copilot for Free Fire",
                    "Give me combat advice",
                    "What's the best strategy for resource management?",
                    "Turn off Copilot"
                };
                
            case TASK_SCHEDULER:
                return new String[] {
                    "Schedule a study session for physics at 4 PM",
                    "Remind me to practice calculus in 2 hours",
                    "What's my schedule for today?",
                    "Set a recurring study reminder for weekdays at 7 PM"
                };
                
            case GAMING:
                return new String[] {
                    "Play Free Fire",
                    "Give me a tip for PUBG Mobile",
                    "Open Call of Duty",
                    "What's your advice for Free Fire combat?"
                };
                
            case EDUCATION:
                return new String[] {
                    "Solve this physics problem about projectile motion",
                    "Explain the concept of chemical equilibrium",
                    "Help me with this integration problem",
                    "What's the formula for calculating acceleration?"
                };
                
            case EXTERNAL_APP:
                return new String[] {
                    "Open WhatsApp",
                    "Send a message to Mom saying I'll be late",
                    "Call Dad",
                    "Open Camera"
                };
                
            case PDF_LEARNING:
                return new String[] {
                    "Learn from this physics PDF",
                    "Extract knowledge from my chemistry notes",
                    "What did you learn from the last document?",
                    "Summarize the key points from the PDF"
                };
                
            default:
                return new String[] {
                    "Tell me about yourself",
                    "What features can you access?",
                    "Do you have any opinions about quantum physics?",
                    "What's your favorite scientific concept?"
                };
        }
    }
    
    /**
     * Types of features accessible through voice
     */
    public enum FeatureType {
        AUTO_AI,
        COPILOT,
        TASK_SCHEDULER,
        GAMING,
        EDUCATION,
        EXTERNAL_APP,
        PDF_LEARNING,
        SELF_REFLECTION
    }
    
    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        if (queryHandler instanceof EnhancedVoiceQueryHandler) {
            ((EnhancedVoiceQueryHandler) queryHandler).shutdown();
        }
        
        Log.i(TAG, "SentientVoiceIntegration shutdown");
    }
}
