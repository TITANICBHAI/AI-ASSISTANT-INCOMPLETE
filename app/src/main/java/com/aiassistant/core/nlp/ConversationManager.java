package com.aiassistant.core.nlp;

import android.content.Context;
import android.util.Log;

import com.aiassistant.data.models.CallerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages conversations with advanced natural language understanding
 */
public class ConversationManager {
    private static final String TAG = "ConversationManager";
    
    private Context context;
    
    // Map of conversation IDs to conversation histories
    private Map<String, List<ConversationTurn>> conversations = new HashMap<>();
    
    /**
     * Create a new ConversationManager
     */
    public ConversationManager(Context context) {
        this.context = context;
    }
    
    /**
     * Start a new conversation
     */
    public String startConversation(CallerInfo callerInfo) {
        String conversationId = generateConversationId(callerInfo);
        conversations.put(conversationId, new ArrayList<>());
        Log.d(TAG, "Started new conversation with ID: " + conversationId);
        return conversationId;
    }
    
    /**
     * Add user message to conversation
     */
    public void addUserMessage(String conversationId, String message) {
        List<ConversationTurn> history = getConversationHistory(conversationId);
        if (history != null) {
            history.add(new ConversationTurn(message, ConversationTurn.ROLE_USER));
            Log.d(TAG, "Added user message to conversation " + conversationId + ": " + message);
        }
    }
    
    /**
     * Add AI message to conversation
     */
    public void addAIMessage(String conversationId, String message) {
        List<ConversationTurn> history = getConversationHistory(conversationId);
        if (history != null) {
            history.add(new ConversationTurn(message, ConversationTurn.ROLE_AI));
            Log.d(TAG, "Added AI message to conversation " + conversationId + ": " + message);
        }
    }
    
    /**
     * Get conversation history
     */
    public List<ConversationTurn> getConversationHistory(String conversationId) {
        return conversations.get(conversationId);
    }
    
    /**
     * Clear conversation history
     */
    public void clearConversation(String conversationId) {
        conversations.remove(conversationId);
        Log.d(TAG, "Cleared conversation with ID: " + conversationId);
    }
    
    /**
     * Generate conversation ID
     */
    private String generateConversationId(CallerInfo callerInfo) {
        return "conv-" + System.currentTimeMillis() + "-" + callerInfo.getPhoneNumber();
    }
    
    /**
     * Get intent from user message
     */
    public String extractIntent(String message) {
        // Simplified intent extraction - in a real app, use ML-based intent recognition
        message = message.toLowerCase();
        
        if (message.contains("call back") || message.contains("return call")) {
            return "request_callback";
        } else if (message.contains("urgent") || message.contains("emergency")) {
            return "emergency";
        } else if (message.contains("appointment") || message.contains("schedule") || 
                   message.contains("meeting")) {
            return "schedule_appointment";
        } else if (message.contains("message") || message.contains("tell")) {
            return "leave_message";
        } else if (message.contains("later") || message.contains("another time")) {
            return "call_later";
        } else if (message.contains("thank") || message.contains("bye") || 
                   message.contains("goodbye")) {
            return "end_conversation";
        }
        
        return "general_inquiry";
    }
    
    /**
     * Generate appropriate response based on intent and context
     */
    public String generateResponse(String intent, String userMessage, String userStatus) {
        // Simplified response generation - in a real app, use more sophisticated NLG
        switch (intent) {
            case "request_callback":
                return "I'll let them know you'd like a callback as soon as they're available. " +
                       "Is there a specific time that would work best for you?";
                
            case "emergency":
                return "I understand this is urgent. Let me see if I can reach them immediately. " +
                       "If not, what's the best way for them to contact you?";
                
            case "schedule_appointment":
                return "I'd be happy to help schedule an appointment. They currently have " +
                       "availability next Tuesday afternoon or Wednesday morning. Would either " +
                       "of those work for you?";
                
            case "leave_message":
                return "I'd be happy to pass along a message. What would you like me to tell them?";
                
            case "call_later":
                return "Absolutely, I'll let them know you called. What would be a good time " +
                       "for them to reach you?";
                
            case "end_conversation":
                return "Thank you for calling. I'll make sure they get your message. Have a great day!";
                
            case "general_inquiry":
            default:
                return "I'll let them know you called. Is there anything specific you'd like " +
                       "me to tell them?";
        }
    }
    
    /**
     * Generate conversation summary
     */
    public String summarizeConversation(String conversationId) {
        List<ConversationTurn> history = getConversationHistory(conversationId);
        if (history == null || history.isEmpty()) {
            return "No conversation to summarize";
        }
        
        // Simplified summary generation - in a real app, use NLP summarization
        StringBuilder summary = new StringBuilder();
        
        // Find main intent
        String mainIntent = "unknown";
        for (ConversationTurn turn : history) {
            if (turn.getRole().equals(ConversationTurn.ROLE_USER)) {
                String intent = extractIntent(turn.getMessage());
                if (!intent.equals("general_inquiry")) {
                    mainIntent = intent;
                    break;
                }
            }
        }
        
        // Generate summary based on intent
        switch (mainIntent) {
            case "request_callback":
                summary.append("Caller requested a callback");
                break;
                
            case "emergency":
                summary.append("URGENT: Caller had an emergency");
                break;
                
            case "schedule_appointment":
                summary.append("Caller wanted to schedule an appointment");
                break;
                
            case "leave_message":
                // Find the message content
                for (ConversationTurn turn : history) {
                    if (turn.getRole().equals(ConversationTurn.ROLE_USER)) {
                        if (turn.getMessage().length() > 20) {
                            summary.append("Message: ").append(turn.getMessage());
                            break;
                        }
                    }
                }
                break;
                
            case "call_later":
                summary.append("Caller will call back later");
                break;
                
            default:
                summary.append("General call, no specific action required");
                break;
        }
        
        return summary.toString();
    }
    
    /**
     * Conversation turn class
     */
    public static class ConversationTurn {
        public static final String ROLE_USER = "user";
        public static final String ROLE_AI = "ai";
        
        private String message;
        private String role;
        private long timestamp;
        
        public ConversationTurn(String message, String role) {
            this.message = message;
            this.role = role;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getRole() {
            return role;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
