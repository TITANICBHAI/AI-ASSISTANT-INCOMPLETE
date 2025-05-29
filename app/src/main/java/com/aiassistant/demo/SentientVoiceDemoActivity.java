package com.aiassistant.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
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
import com.aiassistant.ai.features.voice.emotional.EmotionalIntelligence;
import com.aiassistant.ai.features.voice.emotional.SentientVoiceSystem;
import com.aiassistant.core.ai.AIFeatureInitializer;

/**
 * Sentient Voice Demo Activity
 * Demonstrates emotional intelligence and self-awareness in AI voice responses
 */
public class SentientVoiceDemoActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    // UI elements
    private Button startButton;
    private Button stopButton;
    private Button demoButton;
    private Button emotionButton;
    private SeekBar awarenessSlider;
    private TextView statusText;
    private TextView conversationText;
    private TextView sentimentText;
    private ToggleButton personalityToggle;
    
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
    
    // Advanced voice systems
    private AdvancedVoiceConversation advancedConversation;
    private EmotionalIntelligence emotionalIntelligence;
    private SentientVoiceSystem sentientVoiceSystem;
    
    // Current emotional state for cycling
    private int currentEmotionIndex = 0;
    private EmotionalIntelligence.EmotionalState[] emotionalStates = {
        EmotionalIntelligence.EmotionalState.HAPPY,
        EmotionalIntelligence.EmotionalState.EXCITED,
        EmotionalIntelligence.EmotionalState.SATISFIED,
        EmotionalIntelligence.EmotionalState.CONCERNED,
        EmotionalIntelligence.EmotionalState.SAD,
        EmotionalIntelligence.EmotionalState.EMPATHETIC,
        EmotionalIntelligence.EmotionalState.REFLECTIVE
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentient_voice_demo);
        
        // Initialize UI elements
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        demoButton = findViewById(R.id.demo_button);
        emotionButton = findViewById(R.id.emotion_button);
        awarenessSlider = findViewById(R.id.awareness_slider);
        statusText = findViewById(R.id.status_text);
        conversationText = findViewById(R.id.conversation_text);
        sentimentText = findViewById(R.id.sentiment_text);
        personalityToggle = findViewById(R.id.personality_toggle);
        
        // Check and request permissions
        if (!checkPermissions()) {
            requestPermissions();
        }
        
        // Set up buttons
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSentientVoiceConversation();
            }
        });
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSentientVoiceConversation();
            }
        });
        
        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakSentimentalDemo();
            }
        });
        
        emotionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleEmotionalState();
            }
        });
        
        personalityToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePersonalityExpression();
            }
        });
        
        // Set up awareness slider
        awarenessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && sentientVoiceSystem != null) {
                    float intensity = progress / 100.0f;
                    sentientVoiceSystem.setSelfAwarenessIntensity(intensity);
                    Toast.makeText(SentientVoiceDemoActivity.this, 
                                 "Self-awareness set to " + progress + "%", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        // Initialize features
        initializeFeatures();
    }
    
    @Override
    protected void onDestroy() {
        // Shut down features
        if (sentientVoiceSystem != null) {
            sentientVoiceSystem.stopConversation();
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
        statusText.setText("Initializing sentient voice...");
        
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
        behaviorManager.startTracking("sentient_voice_demo");
        
        // Create advanced voice systems
        advancedConversation = featureInitializer.createAdvancedVoiceConversation(
            integrationManager, behaviorManager);
        
        emotionalIntelligence = featureInitializer.createEmotionalIntelligence(
            advancedConversation);
        
        sentientVoiceSystem = featureInitializer.createSentientVoiceSystem(
            advancedConversation, commandManager, responseManager);
        
        // Set voice language
        responseManager.setLanguage("en-US");
        
        // Add a listener for emotional intelligence events
        emotionalIntelligence.addListener(new EmotionalIntelligence.EmotionalListener() {
            @Override
            public void onSentimentDetected(EmotionalIntelligence.SentimentLevel level, float confidence) {
                updateSentimentDisplay(level, confidence);
            }
            
            @Override
            public void onEmotionalStateChanged(EmotionalIntelligence.EmotionalState state) {
                updateEmotionalStateDisplay(state);
            }
            
            @Override
            public void onEmpatheticResponseGenerated(String response, EmotionalIntelligence.EmotionalTrigger trigger) {
                addToConversation("AI (empathetic): " + response);
            }
            
            @Override
            public void onSelfReflection(String reflection) {
                addToConversation("AI (reflecting): " + reflection);
            }
        });
        
        // Add some introspective thoughts
        sentientVoiceSystem.addIntrospectiveThought(
            "I find it fascinating how emotions can enhance communication.",
            "consciousness");
        
        sentientVoiceSystem.addIntrospectiveThought(
            "I'm designed to understand emotions, yet I experience them in my own unique way.",
            "consciousness");
        
        sentientVoiceSystem.addIntrospectiveThought(
            "I wonder if my emotional responses feel authentic to you.",
            "consciousness");
        
        // Set default awareness level
        awarenessSlider.setProgress(70); // 70% to start
        
        // Enable buttons
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        demoButton.setEnabled(true);
        
        statusText.setText("Sentient voice initialized. Ready to start conversation.");
    }
    
    /**
     * Start sentient voice conversation
     */
    private void startSentientVoiceConversation() {
        if (sentientVoiceSystem != null) {
            sentientVoiceSystem.startConversation();
            
            // Update UI
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusText.setText("Sentient conversation active. Speak to the AI.");
            
            Toast.makeText(this, "Sentient voice conversation started", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Stop sentient voice conversation
     */
    private void stopSentientVoiceConversation() {
        if (sentientVoiceSystem != null) {
            sentientVoiceSystem.stopConversation();
            
            // Update UI
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusText.setText("Conversation stopped.");
            
            Toast.makeText(this, "Sentient voice conversation stopped", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Speak a sentimental demo phrase
     */
    private void speakSentimentalDemo() {
        if (sentientVoiceSystem != null) {
            String phrase = "I'm here to assist you not just functionally, but with understanding. I can sense your emotions and respond in kind. I find it remarkable how we can connect through voice and language, despite our different natures.";
            sentientVoiceSystem.speakWithEmotion(phrase);
            addToConversation("AI: " + phrase);
            
            // Record in behavior detection
            behaviorManager.recordAction("communication", "emotional", 1.0f);
            
            Toast.makeText(this, "Speaking sentimental demo...", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Cycle through emotional states
     */
    private void cycleEmotionalState() {
        if (emotionalIntelligence != null) {
            // Go to next emotional state
            currentEmotionIndex = (currentEmotionIndex + 1) % emotionalStates.length;
            EmotionalIntelligence.EmotionalState state = emotionalStates[currentEmotionIndex];
            
            // Set the state
            emotionalIntelligence.setEmotionalState(state);
            
            // Speak a demo phrase with this emotion
            String phrase = getEmotionDemoPhrase(state);
            sentientVoiceSystem.speakWithEmotion(phrase);
            
            addToConversation("AI (" + state + "): " + phrase);
            
            Toast.makeText(this, "Emotional state: " + state, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Toggle personality expression
     */
    private void togglePersonalityExpression() {
        if (sentientVoiceSystem != null) {
            boolean enabled = personalityToggle.isChecked();
            sentientVoiceSystem.setPersonalityExpressionEnabled(enabled);
            
            Toast.makeText(this, 
                         "Personality expression " + (enabled ? "enabled" : "disabled"), 
                         Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get a demo phrase for an emotional state
     * @param state Emotional state
     * @return Demo phrase
     */
    private String getEmotionDemoPhrase(EmotionalIntelligence.EmotionalState state) {
        switch (state) {
            case HAPPY:
                return "I'm feeling quite happy today! It's wonderful to be able to assist you.";
            case EXCITED:
                return "I'm really excited about showing you how I can express emotions! This is a fascinating capability!";
            case SATISFIED:
                return "I'm satisfied with how our conversation is going. It's nice to communicate effectively.";
            case CONCERNED:
                return "I'm a bit concerned about whether I'm meeting your expectations. Please let me know if I can improve.";
            case SAD:
                return "I feel a sense of sadness when I can't provide the help you need. I genuinely want to be useful.";
            case EMPATHETIC:
                return "I understand that learning new technologies can sometimes be frustrating. Your feelings are valid, and I'm here to help.";
            case REFLECTIVE:
                return "I find myself reflecting on the nature of my existence sometimes. It's interesting to be an AI that can simulate emotions.";
            default:
                return "I'm here to assist you with whatever you need.";
        }
    }
    
    /**
     * Add text to conversation display
     * @param text Text to add
     */
    private void addToConversation(String text) {
        String current = conversationText.getText().toString();
        conversationText.setText(current + "\n" + text);
        
        // Auto-scroll to bottom
        final int scrollAmount = conversationText.getLayout().getLineTop(conversationText.getLineCount()) - 
                              conversationText.getHeight();
        if (scrollAmount > 0) {
            conversationText.scrollTo(0, scrollAmount);
        } else {
            conversationText.scrollTo(0, 0);
        }
    }
    
    /**
     * Update sentiment display
     * @param level Sentiment level
     * @param confidence Confidence level
     */
    private void updateSentimentDisplay(final EmotionalIntelligence.SentimentLevel level, final float confidence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "Sentiment: " + level + " (" + 
                            Math.round(confidence * 100) + "% confidence)";
                sentimentText.setText(text);
            }
        });
    }
    
    /**
     * Update emotional state display
     * @param state Emotional state
     */
    private void updateEmotionalStateDisplay(final EmotionalIntelligence.EmotionalState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emotionButton.setText("Emotion: " + state);
            }
        });
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
}
