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
    private Button buttonVoiceTeaching;
    private Button buttonImageLabeling;
    private Button buttonOrchestrationDemo;
    private Button buttonPipelineManager;
    
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
        buttonVoiceTeaching = findViewById(R.id.buttonVoiceTeaching);
        buttonImageLabeling = findViewById(R.id.buttonImageLabeling);
        buttonOrchestrationDemo = findViewById(R.id.buttonOrchestrationDemo);
        buttonPipelineManager = findViewById(R.id.buttonPipelineManager);
        
        // Initialize components (but don't fail if VoiceManager has issues)
        try {
            initializeComponents();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components: " + e.getMessage());
        }
        
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
        
        // VoiceManager has private constructor, will be initialized by AIAssistantApplication
        voiceManager = null;
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Set up button click listeners
     */
    private void setupButtonListeners() {
        startServicesButton.setOnClickListener(v -> startServices());
        stopServicesButton.setOnClickListener(v -> stopServices());
        testVoiceButton.setOnClickListener(v -> testVoice());
        buttonVoiceTeaching.setOnClickListener(v -> openVoiceTeaching());
        buttonImageLabeling.setOnClickListener(v -> openImageLabeling());
        buttonOrchestrationDemo.setOnClickListener(v -> openOrchestrationDemo());
        buttonPipelineManager.setOnClickListener(v -> openPipelineManager());
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
        if (voiceManager != null) {
            voiceManager.speak("Hello, I am your AI assistant. Voice synthesis is working correctly.");
        }
        Log.d(TAG, "Voice test executed");
    }
    
    /**
     * Open Voice Teaching Activity
     */
    private void openVoiceTeaching() {
        Intent intent = new Intent(this, com.aiassistant.ui.learning.VoiceTeachingActivity.class);
        startActivity(intent);
        Log.d(TAG, "Opened Voice Teaching Activity");
    }
    
    /**
     * Open Image Labeling Activity
     */
    private void openImageLabeling() {
        Intent intent = new Intent(this, com.aiassistant.ui.learning.ImageLabelingActivity.class);
        startActivity(intent);
        Log.d(TAG, "Opened Image Labeling Activity");
    }
    
    /**
     * Open Orchestration Demo Activity - Monitor the coordinated AI loop system
     */
    private void openOrchestrationDemo() {
        Intent intent = new Intent(this, com.aiassistant.ui.OrchestrationDemoActivity.class);
        startActivity(intent);
        Log.d(TAG, "Opened Orchestration Demo Activity");
    }
    
    /**
     * Open Pipeline Manager - Configure AI component sequences
     */
    private void openPipelineManager() {
        Intent intent = new Intent(this, com.aiassistant.ui.PipelineManagerActivity.class);
        startActivity(intent);
        Log.d(TAG, "Opened Pipeline Manager Activity");
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
