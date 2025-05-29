package com.aiassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.aiassistant.utils.Constants;
import com.aiassistant.utils.TaskUtils;

/**
 * Receiver for boot completed event
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed received");
            
            // Schedule pending tasks
            TaskUtils.scheduleAllPendingTasks(context);
            
            // Check if AI should be started
            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            boolean aiEnabled = prefs.getBoolean(Constants.PREF_AI_ENABLED, false);
            
            if (aiEnabled) {
                // Get current game
                String currentGame = prefs.getString(Constants.PREF_CURRENT_GAME, null);
                
                if (currentGame != null) {
                    // Start AI service
                    Intent serviceIntent = new Intent(context, com.aiassistant.services.AIService.class);
                    serviceIntent.setAction(Constants.ACTION_START_AI);
                    serviceIntent.putExtra(Constants.EXTRA_GAME_ID, currentGame);
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
                    
                    Log.d(TAG, "Started AI service for game: " + currentGame);
                }
            }
        }
    }
}
