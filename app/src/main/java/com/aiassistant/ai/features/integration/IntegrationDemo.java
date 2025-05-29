package com.aiassistant.ai.features.integration;

import android.content.Context;
import android.util.Log;

/**
 * Demo class to illustrate the capabilities of the external integration features
 */
public class IntegrationDemo {
    private static final String TAG = "IntegrationDemo";
    
    private ExternalIntegrationManager integrationManager;
    
    /**
     * Constructor
     * @param context Android context
     */
    public IntegrationDemo(Context context) {
        this.integrationManager = new ExternalIntegrationManager(context);
    }
    
    /**
     * Run a demonstration of messaging capabilities
     * @return Result message
     */
    public String demonstrateMessaging() {
        StringBuilder result = new StringBuilder();
        result.append("Messaging System Capabilities:\n\n");
        
        // List available messaging channels
        result.append("Available messaging channels: ");
        result.append(String.join(", ", integrationManager.getMessagingSystem().getAvailableChannels()));
        result.append("\n\n");
        
        // Demonstrate command processing
        result.append("Example commands the AI Assistant can process:\n");
        result.append("- \"Send a message to John saying I'll be late\"\n");
        result.append("- \"Send a WhatsApp message to Mom saying I love you\"\n");
        result.append("- \"Text Sarah that says let's meet at 7\"\n");
        result.append("- \"Open WhatsApp\"\n\n");
        
        // Show sample response
        result.append("Sample response to 'Send a message to John saying hello':\n");
        ExternalIntegrationManager.IntegrationResponse response = 
            integrationManager.processCommand("Send a message to John saying hello");
        result.append(response.getMessage());
        
        return result.toString();
    }
    
    /**
     * Run a demonstration of app control capabilities
     * @return Result message
     */
    public String demonstrateAppControl() {
        StringBuilder result = new StringBuilder();
        result.append("App Control System Capabilities:\n\n");
        
        // Show sample app control commands
        result.append("Example app control commands:\n");
        result.append("- \"Open Facebook\"\n");
        result.append("- \"Launch Camera\"\n");
        result.append("- \"Go to google.com\"\n");
        result.append("- \"Call Mom\"\n");
        result.append("- \"Open WiFi settings\"\n");
        result.append("- \"Share Hello world\"\n\n");
        
        // Show a sample of installed apps
        result.append("Some installed apps on this device: ");
        int count = 0;
        for (String app : integrationManager.getAppControlSystem().getInstalledAppNames()) {
            result.append(app).append(", ");
            if (++count > 5) break;
        }
        if (count > 0) {
            result.delete(result.length() - 2, result.length());
        }
        result.append("...\n\n");
        
        // Show sample response
        result.append("Sample response to 'Open Camera':\n");
        ExternalIntegrationManager.IntegrationResponse response = 
            integrationManager.processCommand("Open Camera");
        result.append(response.getMessage());
        
        return result.toString();
    }
    
    /**
     * Process a custom integration command
     * @param command Command text
     * @return Result message
     */
    public String processCustomCommand(String command) {
        Log.i(TAG, "Processing custom command: " + command);
        
        ExternalIntegrationManager.IntegrationResponse response = 
            integrationManager.processCommand(command);
            
        return "Command: " + command + "\nResponse: " + response.getMessage() + 
               "\nSuccess: " + response.isSuccess();
    }
}
