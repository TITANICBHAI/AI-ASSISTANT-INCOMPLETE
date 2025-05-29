package com.aiassistant.ai.features.integration.messaging;

/**
 * Interface for various messaging channels
 */
public interface MessageChannel {
    /**
     * Send a message through this channel
     * @param recipient Recipient identifier (phone number, username, etc.)
     * @param message Message content
     * @return Success status
     */
    boolean sendMessage(String recipient, String message);
    
    /**
     * Get the channel name
     * @return Channel name
     */
    String getChannelName();
}
