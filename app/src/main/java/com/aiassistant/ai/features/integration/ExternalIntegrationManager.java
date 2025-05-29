package com.aiassistant.ai.features.integration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

/**
 * Manages integration with external applications and services
 */
public class ExternalIntegrationManager {
    private static final String TAG = "ExternalIntegration";
    
    private Context context;
    
    /**
     * Constructor
     * @param context Android context
     */
    public ExternalIntegrationManager(Context context) {
        this.context = context;
        Log.i(TAG, "ExternalIntegrationManager initialized");
    }
    
    /**
     * Process an external integration command
     * @param command Command to process
     * @return Response from the command processing
     */
    public IntegrationResponse processCommand(String command) {
        Log.i(TAG, "Processing command: " + command);
        
        command = command.toLowerCase();
        
        // Check for app opening command
        if (command.startsWith("open ")) {
            return openApplication(command.substring(5).trim());
        }
        
        // Check for messaging command
        if (command.contains("send message") || command.contains("send a message")) {
            return sendMessage(command);
        }
        
        // Check for calling command
        if (command.contains("call ")) {
            return makeCall(command);
        }
        
        // Unknown command
        return new IntegrationResponse(false, "I don't understand that external command. I can open apps, send messages, or make calls.");
    }
    
    /**
     * Open an application by name or package
     * @param appName Application name or package
     * @return Response indicating success or failure
     */
    private IntegrationResponse openApplication(String appName) {
        try {
            // Handle special cases
            if (appName.equals("free fire") || appName.equals("ff")) {
                appName = "com.dts.freefireth";
            } else if (appName.equals("pubg") || appName.equals("pubg mobile")) {
                appName = "com.tencent.ig";
            } else if (appName.equals("call of duty") || appName.equals("cod") || 
                       appName.equals("cod mobile")) {
                appName = "com.activision.callofduty.shooter";
            }
            
            // Check if this looks like a package name
            boolean isPackageName = appName.contains(".");
            
            if (isPackageName) {
                // Try to launch by package name
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appName);
                if (launchIntent != null) {
                    context.startActivity(launchIntent);
                    return new IntegrationResponse(true, "Opening " + appName);
                } else {
                    // Check if the package exists but has no launch intent
                    try {
                        context.getPackageManager().getPackageInfo(appName, 0);
                        return new IntegrationResponse(false, "This app exists but cannot be launched directly.");
                    } catch (PackageManager.NameNotFoundException e) {
                        return new IntegrationResponse(false, "App not found. Would you like to search for it in the Play Store?");
                    }
                }
            } else {
                // Try to find by name in app drawer
                // This is simplified - a real implementation would be more sophisticated
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                
                // For demonstration, we'll just launch the Play Store search
                Intent searchIntent = new Intent(Intent.ACTION_VIEW);
                searchIntent.setData(Uri.parse("market://search?q=" + appName));
                context.startActivity(searchIntent);
                
                return new IntegrationResponse(true, "Searching for " + appName + " in the Play Store.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening application", e);
            return new IntegrationResponse(false, "Error opening the application: " + e.getMessage());
        }
    }
    
    /**
     * Send a message
     * @param command Message command
     * @return Response indicating success or failure
     */
    private IntegrationResponse sendMessage(String command) {
        try {
            // Extract recipient and message (simplified)
            String recipient = "recipient";
            String message = "Hello from AI Assistant";
            
            // Extract "to recipient" if present
            if (command.contains(" to ")) {
                String[] parts = command.split(" to ", 2);
                if (parts.length > 1) {
                    recipient = parts[1].trim();
                    
                    // Extract message content if present
                    if (recipient.contains(" saying ")) {
                        String[] recipientParts = recipient.split(" saying ", 2);
                        recipient = recipientParts[0].trim();
                        message = recipientParts[1].trim();
                    }
                }
            }
            
            // Launch messaging app with info
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + Uri.encode(recipient)));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
            
            return new IntegrationResponse(true, "Sending message to " + recipient);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            return new IntegrationResponse(false, "Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Make a phone call
     * @param command Call command
     * @return Response indicating success or failure
     */
    private IntegrationResponse makeCall(String command) {
        try {
            // Extract number (simplified)
            String number = "";
            
            if (command.startsWith("call ")) {
                number = command.substring(5).trim();
            }
            
            // Launch phone app
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
            
            return new IntegrationResponse(true, "Calling " + number);
        } catch (Exception e) {
            Log.e(TAG, "Error making call", e);
            return new IntegrationResponse(false, "Error making call: " + e.getMessage());
        }
    }
    
    /**
     * Check if an app is installed by package name
     * @param packageName Package name to check
     * @return True if installed
     */
    public boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Launch an app by package name
     * @param packageName Package name to launch
     * @return True if successfully launched
     */
    public boolean launchApp(String packageName) {
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error launching app", e);
            return false;
        }
    }
    
    /**
     * Response class for integration commands
     */
    public static class IntegrationResponse {
        private boolean success;
        private String message;
        
        public IntegrationResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
