package com.aiassistant.ui.voice;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ml.models.VoiceBiometricModel;

public class VoiceSecurityDemoActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private VoiceBiometricModel voiceBiometricModel;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_security_demo);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        voiceBiometricModel = aiStateManager.getVoiceBiometricModel();
        
        // Initialize UI
        statusTextView = findViewById(R.id.statusTextView);
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        if (voiceBiometricModel != null) {
            statusTextView.setText("Voice biometric system is operational");
        } else {
            statusTextView.setText("Voice biometric system is not initialized");
        }
    }
}
