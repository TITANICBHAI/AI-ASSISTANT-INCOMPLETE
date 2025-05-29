package com.aiassistant.ai.features.integration.messaging;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Message channel for various messaging apps
 */
public class AppMessageChannel implements MessageChannel {
    private static final String TAG = "AppMessageChannel";
    
    private Context context;
    private String packageName;
    
    /**
     * Constructor
     * @param context Android context
     * @param packageName App package name
     */
    public AppMessageChannel(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
    }
    
    @Override
    public boolean sendMessage(String recipient, String message) {
        try {
            Intent intent = null;
            
            // Different handling based on app
            if (packageName.equals("com.whatsapp")) {
                // WhatsApp
                intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?phone=" + recipient + "&text=" + Uri.encode(message);
                intent.setData(Uri.parse(url));
            } else if (packageName.equals("org.telegram.messenger")) {
                // Telegram
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://msg?to=" + recipient + "&text=" + Uri.encode(message)));
            } else if (packageName.equals("com.facebook.orca")) {
                // Messenger
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/" + recipient));
            } else {
                // Generic approach for other apps - may not work for all
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                intent.setPackage(packageName);
            }
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i(TAG, "Message intent sent to " + packageName);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error sending message via app: " + packageName, e);
            return false;
        }
    }
    
    @Override
    public String getChannelName() {
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}
