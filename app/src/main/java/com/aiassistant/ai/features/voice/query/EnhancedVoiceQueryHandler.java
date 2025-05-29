package com.aiassistant.ai.features.voice.query;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.automation.AutoAISystem;
import com.aiassistant.ai.features.copilot.CopilotSystem;
import com.aiassistant.ai.features.gaming.GameAnalyzer;
import com.aiassistant.ai.features.scheduler.TaskSchedulerSystem;
import com.aiassistant.ai.features.scheduler.ScheduledTask;
import com.aiassistant.ai.features.integration.ExternalIntegrationManager;

import java.util.Calendar;
import java.util.List;

/**
 * Enhanced voice query handler that can access ALL features across the app
 * This serves as the central hub for voice commands to interact with any
 * aspect of the AI Assistant.
 */
public class EnhancedVoiceQueryHandler extends VoiceQueryHandler {
    private static final String TAG = "EnhancedVoiceQuery";
    
    // Additional feature systems
    private AutoAISystem autoAISystem;
    private CopilotSystem copilotSystem;
    private TaskSchedulerSystem schedulerSystem;
    private GameAnalyzer gameAnalyzer;
    
    /**
     * Constructor
     * @param context Android context
     */
    public EnhancedVoiceQueryHandler(Context context) {
        super(context);
        
        // Initialize additional systems
        this.autoAISystem = new AutoAISystem(context);
        this.copilotSystem = new CopilotSystem(context);
        this.schedulerSystem = new TaskSchedulerSystem(context);
        this.gameAnalyzer = new GameAnalyzer(context);
        
        // Start persistent systems
        schedulerSystem.start();
        
        Log.i(TAG, "EnhancedVoiceQueryHandler initialized with ALL features accessible");
    }
    
    /**
     * Process a voice query with access to all features
     * @param query User query
     * @return AI response
     */
    @Override
    public String processQuery(String query) {
        Log.i(TAG, "Processing enhanced query: " + query);
        
        // First, check if this is a command for one of the specialized systems
        String response = null;
        
        // Check for Auto AI commands
        if (isAutoAICommand(query)) {
            response = handleAutoAICommand(query);
        }
        
        // Check for Copilot commands
        else if (isCopilotCommand(query)) {
            response = handleCopilotCommand(query);
        }
        
        // Check for Scheduler commands
        else if (isSchedulerCommand(query)) {
            response = handleSchedulerCommand(query);
        }
        
        // Check for Gaming commands
        else if (isGamingCommand(query)) {
            response = handleGamingCommand(query);
        }
        
        // If not handled by specialized systems, use the base handler
        if (response == null) {
            response = super.processQuery(query);
        }
        
        return response;
    }
    
    /**
     * Check if query is an Auto AI command
     */
    private boolean isAutoAICommand(String query) {
        query = query.toLowerCase();
        return query.contains("auto ai") || 
               query.contains("automate") || 
               query.contains("automation") ||
               (query.contains("auto") && query.contains("mode"));
    }
    
    /**
     * Handle Auto AI commands
     */
    private String handleAutoAICommand(String query) {
        query = query.toLowerCase();
        
        // Check for mode changes
        if (query.contains("turn on") || query.contains("enable") || 
            query.contains("activate") || query.contains("start")) {
            
            // Determine mode
            AutoAISystem.AutoMode mode = AutoAISystem.AutoMode.PASSIVE;
            
            if (query.contains("full") || query.contains("maximum") || 
                query.contains("complete")) {
                mode = AutoAISystem.AutoMode.FULL;
            } else if (query.contains("active")) {
                mode = AutoAISystem.AutoMode.ACTIVE;
            }
            
            // Start the system
            autoAISystem.start(mode);
            
            return "Auto AI system activated in " + mode + " mode. I'll " +
                  (mode == AutoAISystem.AutoMode.PASSIVE ? "suggest actions based on your patterns." :
                   mode == AutoAISystem.AutoMode.ACTIVE ? "proactively assist you with some automated actions." :
                   "fully automate assistance based on your patterns and preferences.");
        }
        
        // Check for turning off
        if (query.contains("turn off") || query.contains("disable") || 
            query.contains("deactivate") || query.contains("stop")) {
            
            autoAISystem.stop();
            return "Auto AI system has been deactivated. I'll no longer perform automated actions.";
        }
        
        // Check for status query
        if (query.contains("status") || query.contains("running") || 
            query.contains("enabled") || query.contains("active")) {
            
            boolean running = autoAISystem.isRunning();
            AutoAISystem.AutoMode mode = autoAISystem.getMode();
            
            if (running) {
                return "The Auto AI system is currently active in " + mode + " mode.";
            } else {
                return "The Auto AI system is currently inactive.";
            }
        }
        
        // Default response
        return "The Auto AI system can automate tasks based on your patterns. You can " +
               "enable it in passive, active, or full mode, or check its status.";
    }
    
    /**
     * Check if query is a Copilot command
     */
    private boolean isCopilotCommand(String query) {
        query = query.toLowerCase();
        return query.contains("copilot") || 
               query.contains("co-pilot") || 
               query.contains("game assist") ||
               query.contains("gaming help") ||
               query.contains("game advice");
    }
    
    /**
     * Handle Copilot commands
     */
    private String handleCopilotCommand(String query) {
        query = query.toLowerCase();
        
        // Check for copilot activation
        if (query.contains("turn on") || query.contains("enable") || 
            query.contains("activate") || query.contains("start")) {
            
            // Extract game name
            String game = "Unknown";
            
            if (query.contains("for ")) {
                String[] parts = query.split("for ", 2);
                if (parts.length > 1) {
                    game = parts[1].trim();
                }
            }
            
            // Determine mode
            CopilotSystem.CopilotMode mode = CopilotSystem.CopilotMode.PASSIVE;
            
            if (query.contains("predictive")) {
                mode = CopilotSystem.CopilotMode.PREDICTIVE;
            } else if (query.contains("active")) {
                mode = CopilotSystem.CopilotMode.ACTIVE;
            }
            
            // Start the system
            copilotSystem.start(mode, game);
            
            return "Copilot system activated in " + mode + " mode for " + game + ". " +
                   "I'll provide real-time assistance while you play.";
        }
        
        // Check for turning off
        if (query.contains("turn off") || query.contains("disable") || 
            query.contains("deactivate") || query.contains("stop")) {
            
            copilotSystem.stop();
            return "Copilot system has been deactivated. I'll no longer provide gaming assistance.";
        }
        
        // Check for advice request
        if (query.contains("advice") || query.contains("tip") || 
            query.contains("help") || query.contains("strategy")) {
            
            // Extract the situation
            String situation = "general";
            
            if (query.contains("combat") || query.contains("fight")) {
                situation = "combat";
            } else if (query.contains("resource") || query.contains("item")) {
                situation = "resource";
            } else if (query.contains("move") || query.contains("position")) {
                situation = "movement";
            }
            
            String advice = copilotSystem.getAdviceForSituation(situation);
            return advice;
        }
        
        // Default response
        return "The Copilot system can provide real-time gaming assistance. You can " +
               "enable it for a specific game, ask for advice, or turn it off when not needed.";
    }
    
    /**
     * Check if query is a Scheduler command
     */
    private boolean isSchedulerCommand(String query) {
        query = query.toLowerCase();
        return query.contains("schedule") || 
               query.contains("reminder") || 
               query.contains("task") ||
               query.contains("plan") ||
               (query.contains("set") && (query.contains("study") || query.contains("reminder")));
    }
    
    /**
     * Handle Scheduler commands
     */
    private String handleSchedulerCommand(String query) {
        query = query.toLowerCase();
        
        // Check for scheduling a study session
        if ((query.contains("schedule") || query.contains("set")) && 
            query.contains("study")) {
            
            // Extract time - this is simplified, would need NLP in real app
            Calendar time = Calendar.getInstance();
            time.add(Calendar.HOUR_OF_DAY, 1); // Default to 1 hour from now
            
            // Extract topic
            String topic = "JEE preparation";
            
            if (query.contains("for ")) {
                String[] parts = query.split("for ", 2);
                if (parts.length > 1) {
                    topic = parts[1].trim();
                    if (topic.endsWith(".")) {
                        topic = topic.substring(0, topic.length() - 1);
                    }
                }
            }
            
            // Schedule the session
            String taskId = schedulerSystem.scheduleStudySession(
                "Study: " + topic,
                "Time for your scheduled study session on " + topic,
                time
            );
            
            if (taskId != null) {
                return "I've scheduled a study session for " + topic + " at " + 
                       time.get(Calendar.HOUR_OF_DAY) + ":" + 
                       String.format("%02d", time.get(Calendar.MINUTE)) + ".";
            } else {
                return "I couldn't schedule the study session. Please try again.";
            }
        }
        
        // Check for setting a reminder
        if ((query.contains("reminder") || query.contains("remind")) && 
            query.contains("to ")) {
            
            // Extract message
            String message = "";
            String[] parts = query.split("to ", 2);
            if (parts.length > 1) {
                message = parts[1].trim();
                if (message.endsWith(".")) {
                    message = message.substring(0, message.length() - 1);
                }
            }
            
            // Extract time - this is simplified, would need NLP in real app
            Calendar time = Calendar.getInstance();
            time.add(Calendar.HOUR_OF_DAY, 1); // Default to 1 hour from now
            
            // Schedule the reminder
            String taskId = schedulerSystem.scheduleVoiceReminder(message, time);
            
            if (taskId != null) {
                return "I've set a reminder to " + message + " at " + 
                       time.get(Calendar.HOUR_OF_DAY) + ":" + 
                       String.format("%02d", time.get(Calendar.MINUTE)) + ".";
            } else {
                return "I couldn't set the reminder. Please try again.";
            }
        }
        
        // Check for today's schedule
        if (query.contains("today") && 
            (query.contains("schedule") || query.contains("tasks") || 
             query.contains("reminders") || query.contains("plan"))) {
            
            List<ScheduledTask> todayTasks = schedulerSystem.getTodayTasks();
            
            if (todayTasks.isEmpty()) {
                return "You don't have any scheduled tasks or reminders for today.";
            }
            
            StringBuilder response = new StringBuilder("Here's your schedule for today:\n\n");
            
            for (ScheduledTask task : todayTasks) {
                Calendar time = task.getScheduledTime();
                response.append("- ")
                        .append(time.get(Calendar.HOUR_OF_DAY))
                        .append(":")
                        .append(String.format("%02d", time.get(Calendar.MINUTE)))
                        .append(": ")
                        .append(task.getTitle());
                
                if (task.isExecuted()) {
                    response.append(" (completed)");
                }
                
                response.append("\n");
            }
            
            return response.toString();
        }
        
        // Default response
        return "I can help you schedule study sessions, set reminders, or view your daily tasks. " +
               "What would you like to schedule?";
    }
    
    /**
     * Check if query is a Gaming command
     */
    private boolean isGamingCommand(String query) {
        query = query.toLowerCase();
        return query.contains("play") || 
               (query.contains("open") && (
                   query.contains("game") || 
                   query.contains("free fire") || 
                   query.contains("pubg") || 
                   query.contains("call of duty"))) ||
               query.contains("gaming tip") ||
               query.contains("game tip");
    }
    
    /**
     * Handle Gaming commands
     */
    private String handleGamingCommand(String query) {
        query = query.toLowerCase();
        
        // Handle play/open game command
        if (query.contains("play") || (query.contains("open") && query.contains("game"))) {
            // Extract game name
            String gameName = null;
            
            if (query.contains("play ")) {
                String[] parts = query.split("play ", 2);
                if (parts.length > 1) {
                    gameName = parts[1].trim();
                    if (gameName.endsWith(".")) {
                        gameName = gameName.substring(0, gameName.length() - 1);
                    }
                }
            } else if (query.contains("open ")) {
                String[] parts = query.split("open ", 2);
                if (parts.length > 1) {
                    gameName = parts[1].trim();
                    if (gameName.endsWith(".")) {
                        gameName = gameName.substring(0, gameName.length() - 1);
                    }
                }
            }
            
            if (gameName == null) {
                return "Which game would you like me to open?";
            }
            
            // Check if the game is installed
            boolean installed = gameAnalyzer.isGameInstalled(gameName);
            
            if (installed) {
                // Get package name
                String packageName = gameAnalyzer.getGamePackageName(gameName);
                
                // Set as current game
                gameAnalyzer.setCurrentGame(gameName);
                
                // Try to launch the game using integration manager
                ExternalIntegrationManager.IntegrationResponse response = 
                    new ExternalIntegrationManager(super.getContext()).processCommand("open " + packageName);
                
                return "Opening " + gameAnalyzer.getGameProfile(gameName).getName() + 
                       ". Would you like to enable Copilot mode for gaming assistance?";
            } else {
                // Game not installed
                return "I couldn't find " + gameName + " installed on this device. " +
                       "Would you like me to help you find it in the Play Store?";
            }
        }
        
        // Handle game tip request
        if (query.contains("tip") || query.contains("advice") || query.contains("help")) {
            // Extract game name
            String gameName = null;
            
            if (query.contains("for ")) {
                String[] parts = query.split("for ", 2);
                if (parts.length > 1) {
                    gameName = parts[1].trim();
                    if (gameName.endsWith(".")) {
                        gameName = gameName.substring(0, gameName.length() - 1);
                    }
                }
            }
            
            // If no specific game is mentioned, use current game or default to general
            if (gameName == null) {
                if (gameAnalyzer.getCurrentGameProfile() != null) {
                    String tip = gameAnalyzer.getRandomGameTip();
                    return "Tip for " + gameAnalyzer.getCurrentGameProfile().getName() + ": " + tip;
                } else {
                    return "What game would you like a tip for? I have tips for Free Fire, PUBG Mobile, and Call of Duty Mobile.";
                }
            } else {
                String tip = gameAnalyzer.getRandomGameTip(gameName);
                
                if (tip != null) {
                    return "Tip for " + gameAnalyzer.getGameProfile(gameName).getName() + ": " + tip;
                } else {
                    return "I don't have specific tips for that game yet. I currently have tips for Free Fire, PUBG Mobile, and Call of Duty Mobile.";
                }
            }
        }
        
        // Default response
        return "I can help you open games or provide gaming tips. What game would you like assistance with?";
    }
    
    /**
     * Shutdown and cleanup all systems
     */
    public void shutdown() {
        autoAISystem.shutdown();
        copilotSystem.shutdown();
        schedulerSystem.shutdown();
        
        Log.i(TAG, "EnhancedVoiceQueryHandler systems shutdown");
    }
}
