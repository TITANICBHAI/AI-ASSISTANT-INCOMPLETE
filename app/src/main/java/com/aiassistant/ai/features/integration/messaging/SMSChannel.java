package com.aiassistant.ai.features.integration.messaging;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * SMS messaging channel implementation
 */
public class SMSChannel implements MessageChannel {
    private static final String TAG = "SMSChannel";
    
    private Context context;
    
    /**
     * Constructor
     * @param context Android context
     */
    public SMSChannel(Context context) {
        this.context = context;
    }
    
    @Override
    public boolean sendMessage(String recipient, String message) {
        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            
            if (message.length() > 160) {
                // Split long message into parts
                for (String part : smsManager.divideMessage(message)) {
                    smsManager.sendTextMessage(recipient, null, part, null, null);
                }
            } else {
                smsManager.sendTextMessage(recipient, null, message, null, null);
            }
            
            Log.i(TAG, "SMS sent to " + recipient);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS", e);
            return false;
        }
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
}
