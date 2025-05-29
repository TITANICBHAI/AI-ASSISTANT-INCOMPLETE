package com.aiassistant.ui.voice;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.voice.VoiceCommandManager;

public class VoiceIntegrationDemoActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private VoiceCommandManager voiceCommandManager;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_integration_demo);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        voiceCommandManager = aiStateManager.getVoiceCommandManager();
        
        // Initialize UI
        statusTextView = findViewById(R.id.statusTextView);
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        if (voiceCommandManager != null && voiceCommandManager.isInitialized()) {
            statusTextView.setText("Voice command system is operational");
        } else {
            statusTextView.setText("Voice command system is not initialized");
        }
    }
}
