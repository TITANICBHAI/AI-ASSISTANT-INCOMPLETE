package com.aiassistant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.ai.integration.AIAssistantCore;
import com.aiassistant.debug.DebugLogger;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Initialize AI core in background
        new Thread(() -> {
            try {
                // Get AI Assistant Core instance
                AIAssistantCore aiAssistantCore = AIAssistantCore.getInstance(this);
                
                // Initialize the assistant
                boolean initSuccess = aiAssistantCore.initialize();
                DebugLogger.i(TAG, "AI Core initialization " + (initSuccess ? "successful" : "failed"));
                
            } catch (Exception e) {
                DebugLogger.e(TAG, "Error initializing AI Core", e);
            }

            // Launch MainActivity after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, SPLASH_DELAY);
        }).start();
    }
}
