package com.aiassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.memory.MemoryManager;
import com.aiassistant.data.repository.CallerProfileRepository;
import com.aiassistant.services.CallHandlingService;
import com.aiassistant.core.voice.VoiceManager;

/**
 * Main activity for the AI Assistant application
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // UI elements
    private TextView statusTextView;
    private Button startServicesButton;
    private Button stopServicesButton;
    private Button testVoiceButton;
    
    // Components
    private AIStateManager aiStateManager;
    private MemoryManager memoryManager;
    private CallerProfileRepository callerProfileRepository;
    private VoiceManager voiceManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView);
        startServicesButton = findViewById(R.id.startServicesButton);
        stopServicesButton = findViewById(R.id.stopServicesButton);
        testVoiceButton = findViewById(R.id.testVoiceButton);
        
        // Initialize components
        initializeComponents();
        
        // Set up button listeners
        setupButtonListeners();
        
        // Start services
        startServices();
        
        Log.d(TAG, "MainActivity created");
    }
    
    /**
     * Initialize AI components
     */
    private void initializeComponents() {
        aiStateManager = AIStateManager.getInstance(this);
        memoryManager = MemoryManager.getInstance(this);
        callerProfileRepository = new CallerProfileRepository(this);
        voiceManager = new VoiceManager(this);
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Set up button click listeners
     */
    private void setupButtonListeners() {
        startServicesButton.setOnClickListener(v -> startServices());
        stopServicesButton.setOnClickListener(v -> stopServices());
        testVoiceButton.setOnClickListener(v -> testVoice());
    }
    
    /**
     * Start AI assistant services
     */
    private void startServices() {
        // Start call handling service
        Intent callServiceIntent = new Intent(this, CallHandlingService.class);
        startService(callServiceIntent);
        
        // Update status
        statusTextView.setText("Services running");
        
        Log.d(TAG, "Services started");
    }
    
    /**
     * Stop AI assistant services
     */
    private void stopServices() {
        // Stop call handling service
        Intent callServiceIntent = new Intent(this, CallHandlingService.class);
        stopService(callServiceIntent);
        
        // Update status
        statusTextView.setText("Services stopped");
        
        Log.d(TAG, "Services stopped");
    }
    
    /**
     * Test voice capabilities
     */
    private void testVoice() {
        voiceManager.speak("Hello, I am your AI assistant. Voice synthesis is working correctly.");
        
        Log.d(TAG, "Voice test executed");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Release voice manager
        if (voiceManager != null) {
            voiceManager.release();
        }
        
        Log.d(TAG, "MainActivity destroyed");
    }
}
