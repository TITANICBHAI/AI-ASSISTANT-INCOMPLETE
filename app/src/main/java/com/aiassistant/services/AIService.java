package com.aiassistant.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aiassistant.core.ai.AIStateManager;

/**
 * Service for running AI in the background
 */
public class AIService extends Service {

    private static final String TAG = "AIService";
    
    private AIStateManager aiStateManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        aiStateManager = AIStateManager.getInstance(this);
        Log.d(TAG, "AIService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START":
                        String gamePackage = intent.getStringExtra("PACKAGE");
                        if (gamePackage != null) {
                            aiStateManager.start(gamePackage);
                        }
                        break;
                    case "STOP":
                        aiStateManager.stop();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        aiStateManager.stop();
        super.onDestroy();
        Log.d(TAG, "AIService destroyed");
    }
}
