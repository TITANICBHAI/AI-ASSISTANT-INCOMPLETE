package com.aiassistant;

import android.app.Application;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;

/**
 * Application class
 */
public class AIApplication extends Application {
    
    private static final String TAG = "AIApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AI state manager
        AIStateManager.getInstance(this);
        
        Log.d(TAG, "Application initialized");
    }
}
