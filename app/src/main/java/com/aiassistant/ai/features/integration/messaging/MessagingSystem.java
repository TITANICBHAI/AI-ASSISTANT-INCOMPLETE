package com.aiassistant.ai.features.integration.messaging;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Messaging system for interacting with SMS and messaging apps
 * This system allows the AI Assistant to send messages through various channels
 * and interact with messaging applications.
 */
public class MessagingSystem {
    private static final String TAG = "MessagingSystem";
    
    // Store popular messaging package names
    private static final Map<String, String> MESSAGING_APPS = new HashMap<>();
    static {
        MESSAGING_APPS.put("whatsapp", "com.whatsapp");
        MESSAGING_APPS.put("telegram", "org.telegram.messenger");
        MESSAGING_APPS.put("signal", "org.thoughtcrime.securesms");
        MESSAGING_APPS.put("messenger", "com.facebook.orca");
        MESSAGING_APPS.put("skype", "com.skype.raider");
        MESSAGING_APPS.put("slack", "com.Slack");
        MESSAGING_APPS.put("discord", "com.discord");
        MESSAGING_APPS.put("viber", "com.viber.voip");
        MESSAGING_APPS.put("line", "jp.naver.line.android");
        MESSAGING_APPS.put("wechat", "com.tencent.mm");
    }
    
    private Context context;
    private Map<String, MessageChannel> messageChannels;
    
    /**
     * Constructor
     * @param context Android context
     */
    public MessagingSystem(Context context) {
        this.context = context;
        this.messageChannels = new HashMap<>();
        
        // Initialize default channels
        initializeChannels();
        
        Log.i(TAG, "MessagingSystem initialized");
    }
    
    /**
     * Initialize messaging channels
     */
    private void initializeChannels() {
        // Add SMS channel
        messageChannels.put("sms", new SMSChannel(context));
        
        // Add app-specific channels
        for (Map.Entry<String, String> entry : MESSAGING_APPS.entrySet()) {
            if (isAppInstalled(entry.getValue())) {
                messageChannels.put(entry.getKey(), new AppMessageChannel(context, entry.getValue()));
            }
        }
    }
    
    /**
     * Send a message through the default SMS channel
     * @param phoneNumber Recipient phone number
     * @param message The message content
     * @return Success status
     */
    public boolean sendSMS(String phoneNumber, String message) {
        MessageChannel smsChannel = messageChannels.get("sms");
        if (smsChannel != null) {
            return smsChannel.sendMessage(phoneNumber, message);
        }
        return false;
    }
    
    /**
     * Send a message through a specific app channel
     * @param app App name (e.g., "whatsapp", "telegram")
     * @param recipient Recipient identifier (phone number, username, etc.)
     * @param message The message content
     * @return Success status
     */
    public boolean sendAppMessage(String app, String recipient, String message) {
        app = app.toLowerCase();
        MessageChannel channel = messageChannels.get(app);
        
        if (channel != null) {
            return channel.sendMessage(recipient, message);
        } else {
            Log.w(TAG, "App channel " + app + " not available");
            return false;
        }
    }
    
    /**
     * Open a messaging app
     * @param app App name (e.g., "whatsapp", "telegram")
     * @return Success status
     */
    public boolean openMessagingApp(String app) {
        app = app.toLowerCase();
        String packageName = MESSAGING_APPS.get(app);
        
        if (packageName != null && isAppInstalled(packageName)) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening app: " + app, e);
            }
        }
        
        return false;
    }
    
    /**
     * Get a list of available messaging channels
     * @return List of channel names
     */
    public List<String> getAvailableChannels() {
        return new ArrayList<>(messageChannels.keySet());
    }
    
    /**
     * Check if a messaging app is installed
     * @param packageName App package name
     * @return True if installed
     */
    private boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
