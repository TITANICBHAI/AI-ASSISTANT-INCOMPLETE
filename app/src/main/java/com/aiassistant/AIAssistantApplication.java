package com.aiassistant;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.memory.MemoryManager;
import com.aiassistant.services.CallHandlingService;
import com.aiassistant.services.MemoryService;

/**
 * Application class for initializing global components
 */
public class AIAssistantApplication extends Application {
    private static final String TAG = "AIAssistantApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AI state manager
        AIStateManager.getInstance(this);
        
        // Initialize memory manager
        MemoryManager.getInstance(this);
        
        // Start memory service
        startService(new Intent(this, MemoryService.class));
        
        // Start call handling service if permissions are granted
        // (This would require proper permission checking in a real app)
        startService(new Intent(this, CallHandlingService.class));
        
        Log.d(TAG, "AI Assistant application initialized");
    }
    
    @Override
    public void onTerminate() {
        // Clean up resources
        AIStateManager.getInstance(this).close();
        
        // Stop services
        stopService(new Intent(this, MemoryService.class));
        stopService(new Intent(this, CallHandlingService.class));
        
        Log.d(TAG, "AI Assistant application terminated");
        super.onTerminate();
    }
}
