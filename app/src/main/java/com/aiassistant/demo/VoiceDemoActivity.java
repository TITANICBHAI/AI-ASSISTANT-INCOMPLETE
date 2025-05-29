package com.aiassistant.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.ai.features.behavior.AdaptiveBehaviorDetectionFeature;
import com.aiassistant.ai.features.behavior.BehaviorDetectionManager;
import com.aiassistant.ai.features.voice.VoiceCommandFeature;
import com.aiassistant.ai.features.voice.VoiceCommandManager;
import com.aiassistant.ai.features.voice.VoiceIntegrationManager;
import com.aiassistant.ai.features.voice.VoiceResponseFeature;
import com.aiassistant.ai.features.voice.VoiceResponseManager;
import com.aiassistant.ai.features.voice.advanced.AdvancedVoiceConversation;
import com.aiassistant.core.ai.AIFeatureInitializer;

/**
 * Voice Demo Activity
 * Simple demonstration of the voice features
 */
public class VoiceDemoActivity extends AppCompatActivity implements 
    AdvancedVoiceConversation.AdvancedVoiceListener {
    
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    // UI elements
    private Button startButton;
    private Button stopButton;
    private Button speakButton;
    private ToggleButton personaToggle;
    private TextView statusText;
    private TextView conversationText;
    
    // Features
    private AIFeatureInitializer featureInitializer;
    private VoiceCommandFeature voiceCommandFeature;
    private VoiceResponseFeature voiceResponseFeature;
    private AdaptiveBehaviorDetectionFeature behaviorDetectionFeature;
    
    // Managers
    private VoiceCommandManager commandManager;
    private VoiceResponseManager responseManager;
    private VoiceIntegrationManager integrationManager;
    private BehaviorDetectionManager behaviorManager;
    
    // Advanced conversation
    private AdvancedVoiceConversation advancedConversation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_demo);
        
        // Initialize UI elements
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        speakButton = findViewById(R.id.speak_button);
        personaToggle = findViewById(R.id.persona_toggle);
        statusText = findViewById(R.id.status_text);
        conversationText = findViewById(R.id.conversation_text);
        
        // Check and request permissions
        if (!checkPermissions()) {
            requestPermissions();
        }
        
        // Set up buttons
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceConversation();
            }
        });
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoiceConversation();
            }
        });
        
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakDemoPhrase();
            }
        });
        
        personaToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePersona();
            }
        });
        
        // Initialize features
        initializeFeatures();
    }
    
    @Override
    protected void onDestroy() {
        // Shut down features
        if (advancedConversation != null) {
            advancedConversation.stopConversation();
        }
        
        if (voiceResponseFeature != null) {
            voiceResponseFeature.shutdown();
        }
        
        if (voiceCommandFeature != null) {
            voiceCommandFeature.shutdown();
        }
        
        if (behaviorDetectionFeature != null) {
            behaviorDetectionFeature.shutdown();
        }
        
        super.onDestroy();
    }
    
    /**
     * Initialize AI features
     */
    private void initializeFeatures() {
        statusText.setText("Initializing features...");
        
        // Create feature initializer
        featureInitializer = new AIFeatureInitializer(this);
        
        // Initialize features
        voiceCommandFeature = featureInitializer.initializeVoiceCommandFeature();
        voiceResponseFeature = featureInitializer.initializeVoiceResponseFeature();
        behaviorDetectionFeature = featureInitializer.initializeAdaptiveBehaviorDetectionFeature();
        
        // Create managers
        commandManager = featureInitializer.createVoiceCommandManager(voiceCommandFeature);
        responseManager = featureInitializer.createVoiceResponseManager(voiceResponseFeature);
        integrationManager = featureInitializer.createVoiceIntegrationManager(commandManager, responseManager);
        behaviorManager = featureInitializer.createBehaviorDetectionManager(behaviorDetectionFeature);
        
        // Start behavior tracking
        behaviorManager.startTracking("voice_demo");
        
        // Create advanced conversation
        advancedConversation = new AdvancedVoiceConversation(this, integrationManager, behaviorManager);
        advancedConversation.addListener(this);
        
        // Set voice language
        responseManager.setLanguage("en-US");
        
        // Enable buttons
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        speakButton.setEnabled(true);
        
        statusText.setText("Features initialized. Ready to start conversation.");
    }
    
    /**
     * Start voice conversation
     */
    private void startVoiceConversation() {
        if (advancedConversation != null) {
            advancedConversation.startConversation();
            
            // Update UI
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusText.setText("Conversation active. Speak to the assistant.");
            
            Toast.makeText(this, "Voice conversation started", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Stop voice conversation
     */
    private void stopVoiceConversation() {
        if (advancedConversation != null) {
            advancedConversation.stopConversation();
            
            // Update UI
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusText.setText("Conversation stopped.");
            
            Toast.makeText(this, "Voice conversation stopped", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Speak a demo phrase
     */
    private void speakDemoPhrase() {
        if (responseManager != null) {
            String phrase = "Hello, I am your AI gaming assistant. I can help you with tactical analysis, resource management, and many other features. My voice can adapt to different situations and personalities.";
            responseManager.speak(phrase);
            addToConversation("AI: " + phrase);
            
            // Record in behavior detection
            behaviorManager.recordAction("communication", "greeting", 1.0f);
            
            Toast.makeText(this, "Speaking demo phrase...", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Toggle between different personas
     */
    private void togglePersona() {
        if (advancedConversation != null) {
            AdvancedVoiceConversation.VoicePersona currentPersona = advancedConversation.getCurrentPersona();
            AdvancedVoiceConversation.VoicePersona newPersona;
            
            // Cycle through personas
            switch (currentPersona) {
                case ASSISTANT:
                    newPersona = AdvancedVoiceConversation.VoicePersona.TACTICAL;
                    break;
                case TACTICAL:
                    newPersona = AdvancedVoiceConversation.VoicePersona.COMPANION;
                    break;
                case COMPANION:
                    newPersona = AdvancedVoiceConversation.VoicePersona.INSTRUCTOR;
                    break;
                case INSTRUCTOR:
                    newPersona = AdvancedVoiceConversation.VoicePersona.STEALTH;
                    break;
                default:
                    newPersona = AdvancedVoiceConversation.VoicePersona.ASSISTANT;
                    break;
            }
            
            // Set new persona
            advancedConversation.setPersona(newPersona);
            
            // Demonstrate the new persona
            String demoPhrase = "I've switched to " + newPersona.toString() + " voice persona. This is how I sound now.";
            responseManager.speak(demoPhrase);
            addToConversation("AI: " + demoPhrase);
            
            // Update toggle button text
            personaToggle.setText(newPersona.toString());
            
            Toast.makeText(this, "Switched to " + newPersona.toString() + " persona", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Add text to conversation display
     * @param text Text to add
     */
    private void addToConversation(String text) {
        String current = conversationText.getText().toString();
        conversationText.setText(current + "\n" + text);
    }
    
    /**
     * Check if we have required permissions
     * @return true if permissions granted
     */
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Request required permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.RECORD_AUDIO}, 
            PERMISSION_REQUEST_CODE);
    }
    
    // AdvancedVoiceListener implementation
    
    @Override
    public void onVoiceSystemReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Voice system ready");
            }
        });
    }
    
    @Override
    public void onListeningStateChanged(final boolean isListening) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isListening) {
                    statusText.setText("Listening...");
                } else {
                    statusText.setText("Not listening");
                }
            }
        });
    }
    
    @Override
    public void onUserSpeech(final String utterance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addToConversation("You: " + utterance);
            }
        });
    }
    
    @Override
    public void onAISpeaking(final boolean isSpeaking) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isSpeaking) {
                    statusText.setText("Speaking...");
                } else {
                    statusText.setText("Not speaking");
                }
            }
        });
    }
    
    @Override
    public void onUtteranceAdded(final String text, final boolean isUser) {
        // Already handling in onUserSpeech and directly
    }
    
    @Override
    public void onConversationCleared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationText.setText("");
            }
        });
    }
    
    @Override
    public void onConversationError(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Error: " + errorMessage);
                Toast.makeText(VoiceDemoActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onPersonaChanged(final AdvancedVoiceConversation.VoicePersona persona) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                personaToggle.setText(persona.toString());
            }
        });
    }
    
    @Override
    public void onEmotionalStateChanged(final AdvancedVoiceConversation.EmotionalState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VoiceDemoActivity.this, "Emotional state: " + state.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
