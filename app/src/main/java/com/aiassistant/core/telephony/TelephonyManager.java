package com.aiassistant.core.telephony;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.nlp.ConversationManager;
import com.aiassistant.data.models.CallerInfo;
import com.aiassistant.learning.memory.MemoryStorage;
import com.aiassistant.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Manager for handling telephone interactions
 */
public class TelephonyManager {
    private static final String TAG = "TelephonyManager";
    
    // Call notification channel
    private static final String CALL_NOTIFICATION_CHANNEL = "call_notifications";
    
    private Context context;
    private MemoryStorage memoryStorage;
    private ConversationManager conversationManager;
    
    // Track call history for emergency pattern detection
    private Map<String, List<Long>> recentCalls = new HashMap<>();
    
    // Track active conversations
    private Map<String, String> activeConversations = new HashMap<>();
    
    // Priority callers
    private List<String> priorityCallers = new ArrayList<>();
    
    // Response templates for different user statuses
    private Map<String, List<String>> responseTemplates = new HashMap<>();
    
    /**
     * Create a new TelephonyManager
     */
    public TelephonyManager(Context context) {
        this.context = context;
        initializeResponseTemplates();
    }
    
    /**
     * Initialize the telephony manager
     */
    public boolean initialize() {
        try {
            Log.d(TAG, "Initializing TelephonyManager");
            
            // Get memory storage
            AIStateManager aiStateManager = AIStateManager.getInstance();
            if (aiStateManager != null) {
                memoryStorage = aiStateManager.getMemoryStorage();
            }
            
            // Initialize conversation manager
            conversationManager = new ConversationManager(context);
            
            // Initialize from persistent storage
            loadPriorityCallers();
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize TelephonyManager", e);
            return false;
        }
    }
    
    /**
     * Initialize response templates
     */
    private void initializeResponseTemplates() {
        // Busy templates
        List<String> busyTemplates = new ArrayList<>();
        busyTemplates.add("Hello, this is the AI assistant. %s is in a meeting right now and can't come to the phone. How can I help you?");
        busyTemplates.add("Hi there, I'm the AI assistant. %s is currently busy in a meeting. Is there something I can help you with?");
        busyTemplates.add("Good %s, this is %s's AI assistant speaking. They're in a meeting at the moment. What can I assist you with today?");
        responseTemplates.put("busy", busyTemplates);
        
        // Driving templates
        List<String> drivingTemplates = new ArrayList<>();
        drivingTemplates.add("Hello, this is the AI assistant. %s is driving right now and can't answer the phone safely. How can I help you?");
        drivingTemplates.add("Hi there, I'm the AI assistant. %s is currently driving and can't take your call. Is there something I can assist with?");
        drivingTemplates.add("Good %s, this is %s's AI assistant. They're driving at the moment and asked me to answer calls. What can I do for you?");
        responseTemplates.put("driving", drivingTemplates);
        
        // Sleeping templates
        List<String> sleepingTemplates = new ArrayList<>();
        sleepingTemplates.add("Hello, this is the AI assistant. %s is sleeping right now. Can I take a message or help you with something?");
        sleepingTemplates.add("Hi there, I'm the AI assistant. %s is currently sleeping. Is there something urgent I can assist with?");
        sleepingTemplates.add("Good %s, this is %s's AI assistant. They're sleeping at the moment. How may I help you today?");
        responseTemplates.put("sleeping", sleepingTemplates);
        
        // Unavailable templates
        List<String> unavailableTemplates = new ArrayList<>();
        unavailableTemplates.add("Hello, this is the AI assistant. %s is unavailable right now. Is there something I can help you with?");
        unavailableTemplates.add("Hi there, I'm the AI assistant. %s can't come to the phone right now. How can I assist you?");
        unavailableTemplates.add("Good %s, this is %s's AI assistant. They're not available at the moment. What can I do for you today?");
        responseTemplates.put("unavailable", unavailableTemplates);
    }
    
    /**
     * Load priority callers from storage
     */
    private void loadPriorityCallers() {
        // In a real app, load this from persistent storage
        // For now, we'll just use a placeholder
        priorityCallers = new ArrayList<>();
    }
    
    /**
     * Add a priority caller
     */
    public void addPriorityCaller(String phoneNumber) {
        if (!priorityCallers.contains(phoneNumber)) {
            priorityCallers.add(phoneNumber);
            Log.d(TAG, "Added priority caller: " + phoneNumber);
        }
    }
    
    /**
     * Remove a priority caller
     */
    public void removePriorityCaller(String phoneNumber) {
        priorityCallers.remove(phoneNumber);
        Log.d(TAG, "Removed priority caller: " + phoneNumber);
    }
    
    /**
     * Check if a caller is a priority caller
     */
    public boolean isPriorityCaller(String phoneNumber) {
        return priorityCallers.contains(phoneNumber);
    }
    
    /**
     * Begin tracking a call
     */
    public void beginCallTracking(String callId, CallerInfo callerInfo, String userStatus) {
        // Start a new conversation
        String conversationId = conversationManager.startConversation(callerInfo);
        activeConversations.put(callId, conversationId);
        
        // Track recent call (for emergency pattern detection)
        String phoneNumber = callerInfo.getPhoneNumber();
        List<Long> callTimes = recentCalls.getOrDefault(phoneNumber, new ArrayList<>());
        callTimes.add(System.currentTimeMillis());
        
        // Only keep recent calls (within last hour)
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        callTimes.removeIf(time -> time < oneHourAgo);
        
        recentCalls.put(phoneNumber, callTimes);
    }
    
    /**
     * Get a response template for the user's status
     */
    public String getResponseTemplate(String userStatus, CallerInfo callerInfo) {
        return getResponseTemplate(userStatus, callerInfo, "normal");
    }
    
    /**
     * Get a response template for the user's status with specified response mode
     */
    public String getResponseTemplate(String userStatus, CallerInfo callerInfo, String responseMode) {
        List<String> templates = responseTemplates.get(userStatus);
        if (templates == null || templates.isEmpty()) {
            // Fallback to unavailable templates
            templates = responseTemplates.get("unavailable");
        }
        
        // Select a template randomly
        Random random = new Random();
        String template = templates.get(random.nextInt(templates.size()));
        
        // Insert time of day
        int hour = java.time.LocalTime.now().getHour();
        String timeOfDay = (hour < 12) ? "morning" : (hour < 18) ? "afternoon" : "evening";
        
        // Format template with caller info
        String displayName = callerInfo.getDisplayName();
        
        // Add conversational fillers based on response mode
        String formattedTemplate = template.replace("%s", displayName);
        formattedTemplate = formattedTemplate.replace("Good %s", "Good " + timeOfDay);
        
        if (responseMode.equals("friendly")) {
            // Add more conversational fillers for friendly mode
            formattedTemplate = addConversationalFillers(formattedTemplate);
        }
        
        return formattedTemplate;
    }
    
    /**
     * Add conversational fillers to make speech more natural
     */
    private String addConversationalFillers(String text) {
        Random random = new Random();
        
        // Fillers to potentially add
        String[] fillers = {
            "um, ", "uh, ", "hmm, ", "let's see, ", "well, "
        };
        
        // Add a filler at the beginning (30% chance)
        if (random.nextInt(10) < 3) {
            String filler = fillers[random.nextInt(fillers.length)];
            text = filler + text.substring(0, 1).toLowerCase() + text.substring(1);
        }
        
        return text;
    }
    
    /**
     * Process call conversation
     */
    public String processCallConversation(String speech, String userStatus, CallerInfo callerInfo) {
        // Extract intent from speech
        String intent = conversationManager.extractIntent(speech);
        
        // Generate appropriate response
        String response = conversationManager.generateResponse(intent, speech, userStatus);
        
        // Log conversation for learning
        if (memoryStorage != null) {
            memoryStorage.storeInteraction("CALL_CONVERSATION", callerInfo.getPhoneNumber(), 
                    speech, response, intent);
        }
        
        return response;
    }
    
    /**
     * Check for emergency call patterns
     */
    public boolean isEmergencyCallPattern(String phoneNumber) {
        List<Long> callTimes = recentCalls.get(phoneNumber);
        if (callTimes == null) {
            return false;
        }
        
        // Check if there are multiple calls within a short time period
        if (callTimes.size() >= 3) {
            long now = System.currentTimeMillis();
            long tenMinutesAgo = now - (10 * 60 * 1000);
            
            int recentCallCount = 0;
            for (Long time : callTimes) {
                if (time >= tenMinutesAgo) {
                    recentCallCount++;
                }
            }
            
            return recentCallCount >= 3;
        }
        
        return false;
    }
    
    /**
     * Notify user of priority call
     */
    public void notifyUserOfPriorityCall(CallerInfo callerInfo) {
        // Create a notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create intent for notification
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL)
                .setContentTitle("Priority Call")
                .setContentText("Priority call from " + callerInfo.getDisplayName())
                .setSmallIcon(R.drawable.call_handling_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Show notification
        notificationManager.notify(callerInfo.getPhoneNumber().hashCode(), builder.build());
    }
    
    /**
     * Generate call summary
     */
    public String generateCallSummary(CallerInfo callerInfo) {
        // For demo purposes, return a simple summary
        return "Caller wanted to leave a message. The AI assistant handled the call appropriately.";
    }
    
    /**
     * Check if user should be notified about this call
     */
    public boolean shouldNotifyUser(CallerInfo callerInfo, String callSummary) {
        // For demo purposes, notify for all calls
        return true;
    }
    
    /**
     * Send user notification
     */
    public void sendUserNotification(String message, CallerInfo callerInfo) {
        // Create a notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create intent for notification
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL)
                .setContentTitle("Call Summary")
                .setContentText(message)
                .setSmallIcon(R.drawable.call_handling_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Show notification
        notificationManager.notify(callerInfo.getPhoneNumber().hashCode() + 1000, builder.build());
    }
    
    /**
     * Store call data for future learning
     */
    public void storeCallData(CallerInfo callerInfo, String callSummary, String userStatus) {
        if (memoryStorage != null) {
            Map<String, Object> callData = new HashMap<>();
            callData.put("callerNumber", callerInfo.getPhoneNumber());
            callData.put("callerName", callerInfo.getDisplayName());
            callData.put("timestamp", System.currentTimeMillis());
            callData.put("userStatus", userStatus);
            callData.put("summary", callSummary);
            
            memoryStorage.storeData("CALL_HISTORY", callerInfo.getPhoneNumber(), callData);
        }
    }
    
    /**
     * Look up caller information
     */
    public CallerInfo lookupCallerInfo(String phoneNumber) {
        // In a real app, look up contacts provider
        return new CallerInfo(phoneNumber);
    }
    
    /**
     * Log call to memory
     */
    public void logCallToMemory(CallerInfo callerInfo, String userStatus, String callSummary) {
        if (memoryStorage != null) {
            Map<String, Object> callData = new HashMap<>();
            callData.put("callerNumber", callerInfo.getPhoneNumber());
            callData.put("callerName", callerInfo.getDisplayName());
            callData.put("timestamp", System.currentTimeMillis());
            callData.put("userStatus", userStatus);
            callData.put("summary", callSummary);
            
            memoryStorage.storeData("CALL_LOG", callerInfo.getPhoneNumber(), callData);
        }
    }
    
    /**
     * Shutdown telephony manager
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down TelephonyManager");
    }
}
