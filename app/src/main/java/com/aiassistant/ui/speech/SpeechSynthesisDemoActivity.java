package com.aiassistant.ui.speech;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.speech.SpeechSynthesisManager;

public class SpeechSynthesisDemoActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private SpeechSynthesisManager speechSynthesisManager;
    private TextView statusTextView;
    private EditText textToSpeakEditText;
    private Button speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_synthesis_demo);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        speechSynthesisManager = aiStateManager.getSpeechSynthesisManager();
        
        // Initialize UI
        statusTextView = findViewById(R.id.statusTextView);
        textToSpeakEditText = findViewById(R.id.textToSpeakEditText);
        speakButton = findViewById(R.id.speakButton);
        
        // Set up speak button
        speakButton.setOnClickListener(v -> {
            String text = textToSpeakEditText.getText().toString();
            if (!text.isEmpty() && speechSynthesisManager != null && speechSynthesisManager.isInitialized()) {
                speechSynthesisManager.speak(text, true);
            }
        });
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        if (speechSynthesisManager != null && speechSynthesisManager.isInitialized()) {
            statusTextView.setText("Speech synthesis system is operational");
            speakButton.setEnabled(true);
        } else {
            statusTextView.setText("Speech synthesis system is not initialized");
            speakButton.setEnabled(false);
        }
    }
}
