package com.aiassistant.ui.learning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.services.GroqApiService;
import com.aiassistant.core.ai.learning.adaptive.AdaptiveLearningSystem;
import com.aiassistant.core.ai.memory.MemoryManager;
import com.aiassistant.data.repositories.LearningRepository;
import com.aiassistant.data.models.VoiceSampleEntity;
import com.aiassistant.data.models.GestureSampleEntity;
import com.aiassistant.data.models.LabelDefinitionEntity;
import com.aiassistant.utils.StorageManager;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Voice Teaching Activity - Allows users to teach AI using voice commands and tap gestures
 * Features:
 * - Voice recognition for teaching commands
 * - Tap/gesture recording and pattern recognition
 * - Integration with Groq API for natural language understanding
 * - Independent learning modules for each teaching session
 */
public class VoiceTeachingActivity extends AppCompatActivity {
    private static final String TAG = "VoiceTeachingActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // UI Components
    private TextView statusTextView;
    private TextView instructionTextView;
    private TextView tapCountTextView;
    private Button startVoiceButton;
    private Button stopVoiceButton;
    private Button saveSessionButton;
    private Button clearButton;
    private LinearLayout teachingCanvas;
    private RecyclerView teachingHistoryRecycler;
    
    // Voice Recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    
    // Groq API
    private GroqApiService groqService;
    
    // Learning System
    private AdaptiveLearningSystem learningSystem;
    private MemoryManager memoryManager;
    private LearningRepository learningRepository;
    private StorageManager storageManager;
    
    // Teaching Session Data
    private List<TapGesture> currentSessionGestures;
    private List<String> currentSessionVoiceCommands;
    private Map<String, LearningModule> independentModules;
    private String currentTeachingContext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_teaching);
        
        initializeViews();
        initializeServices();
        setupListeners();
        checkPermissions();
        
        Log.d(TAG, "Voice Teaching Activity initialized");
    }
    
    private void initializeViews() {
        statusTextView = findViewById(R.id.statusTextView);
        instructionTextView = findViewById(R.id.instructionTextView);
        tapCountTextView = findViewById(R.id.tapCountTextView);
        startVoiceButton = findViewById(R.id.startVoiceButton);
        stopVoiceButton = findViewById(R.id.stopVoiceButton);
        saveSessionButton = findViewById(R.id.saveSessionButton);
        clearButton = findViewById(R.id.clearButton);
        teachingCanvas = findViewById(R.id.teachingCanvas);
        teachingHistoryRecycler = findViewById(R.id.teachingHistoryRecycler);
        
        teachingHistoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        updateStatus("Ready to learn from you!");
    }
    
    private void initializeServices() {
        groqService = GroqApiService.getInstance(this);
        memoryManager = MemoryManager.getInstance(this);
        learningSystem = new AdaptiveLearningSystem(this);
        learningRepository = new LearningRepository(this);
        storageManager = new StorageManager(this);
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new VoiceRecognitionListener());
        
        currentSessionGestures = new ArrayList<>();
        currentSessionVoiceCommands = new ArrayList<>();
        independentModules = new HashMap<>();
    }
    
    private void setupListeners() {
        // Voice control buttons
        startVoiceButton.setOnClickListener(v -> startVoiceTeaching());
        stopVoiceButton.setOnClickListener(v -> stopVoiceTeaching());
        
        // Session management
        saveSessionButton.setOnClickListener(v -> saveTeachingSession());
        clearButton.setOnClickListener(v -> clearSession());
        
        // Canvas touch listener for gesture recording
        teachingCanvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleCanvasTouch(event);
            }
        });
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }
    
    private void startVoiceTeaching() {
        if (!isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            
            speechRecognizer.startListening(intent);
            isListening = true;
            
            updateStatus("Listening... Speak your teaching command");
            startVoiceButton.setEnabled(false);
            stopVoiceButton.setEnabled(true);
            
            Log.d(TAG, "Started voice teaching");
        }
    }
    
    private void stopVoiceTeaching() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            
            updateStatus("Voice teaching stopped");
            startVoiceButton.setEnabled(true);
            stopVoiceButton.setEnabled(false);
            
            Log.d(TAG, "Stopped voice teaching");
        }
    }
    
    private boolean handleCanvasTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long timestamp = event.getEventTime();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recordTapGesture(x, y, timestamp, "TAP_DOWN");
                Log.d(TAG, "Tap recorded: (" + x + ", " + y + ")");
                break;
                
            case MotionEvent.ACTION_MOVE:
                recordTapGesture(x, y, timestamp, "DRAG");
                break;
                
            case MotionEvent.ACTION_UP:
                recordTapGesture(x, y, timestamp, "TAP_UP");
                processGestureSequence();
                break;
        }
        
        updateTapCount();
        return true;
    }
    
    private void recordTapGesture(float x, float y, long timestamp, String type) {
        TapGesture gesture = new TapGesture(x, y, timestamp, type);
        currentSessionGestures.add(gesture);
        
        // Visual feedback
        addGestureMarker(x, y);
    }
    
    private void addGestureMarker(float x, float y) {
        // Add visual marker to show where user tapped
        View marker = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
        params.leftMargin = (int) x - 10;
        params.topMargin = (int) y - 10;
        marker.setLayoutParams(params);
        marker.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        teachingCanvas.addView(marker);
    }
    
    private void processGestureSequence() {
        if (currentSessionGestures.isEmpty()) {
            return;
        }
        
        // Analyze gesture pattern
        String pattern = analyzeGesturePattern(currentSessionGestures);
        Log.d(TAG, "Gesture pattern detected: " + pattern);
        
        // Send to Groq for contextual understanding
        processWithGroq(pattern, "gesture");
    }
    
    private String analyzeGesturePattern(List<TapGesture> gestures) {
        if (gestures.size() == 1) {
            return "SINGLE_TAP";
        } else if (gestures.size() == 2) {
            return "DOUBLE_TAP";
        } else {
            // Analyze for swipe, circle, etc.
            float totalDistance = calculateTotalDistance(gestures);
            if (totalDistance > 200) {
                return "SWIPE";
            } else {
                return "MULTI_TAP";
            }
        }
    }
    
    private float calculateTotalDistance(List<TapGesture> gestures) {
        float distance = 0;
        for (int i = 1; i < gestures.size(); i++) {
            TapGesture prev = gestures.get(i - 1);
            TapGesture curr = gestures.get(i);
            distance += Math.sqrt(Math.pow(curr.x - prev.x, 2) + Math.pow(curr.y - prev.y, 2));
        }
        return distance;
    }
    
    private void processWithGroq(String input, String type) {
        String prompt = buildGroqPrompt(input, type);
        
        updateStatus("Processing with AI...");
        
        groqService.chatCompletion(prompt, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> handleGroqResponse(response, input, type));
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    updateStatus("AI processing error: " + error);
                    Log.e(TAG, "Groq API error: " + error);
                });
            }
        });
    }
    
    private String buildGroqPrompt(String input, String type) {
        StringBuilder prompt = new StringBuilder();
        
        if (type.equals("voice")) {
            prompt.append("Analyze this voice teaching command: '").append(input).append("'\n\n");
            prompt.append("Extract:\n");
            prompt.append("1. The action or concept being taught\n");
            prompt.append("2. The category (e.g., navigation, interaction, custom action)\n");
            prompt.append("3. Suggested implementation or response\n");
            prompt.append("4. Related gestures or taps that might accompany this command\n\n");
        } else {
            prompt.append("Analyze this gesture pattern: '").append(input).append("'\n\n");
            prompt.append("Recent voice commands: ").append(getRecentVoiceCommands()).append("\n\n");
            prompt.append("Determine:\n");
            prompt.append("1. What action this gesture might represent\n");
            prompt.append("2. How it relates to recent voice commands\n");
            prompt.append("3. Suggested mapping for this gesture\n");
        }
        
        prompt.append("\nProvide response in JSON format with fields: action, category, implementation, confidence");
        
        return prompt.toString();
    }
    
    private String getRecentVoiceCommands() {
        if (currentSessionVoiceCommands.isEmpty()) {
            return "None";
        }
        return String.join(", ", currentSessionVoiceCommands.subList(
                Math.max(0, currentSessionVoiceCommands.size() - 3),
                currentSessionVoiceCommands.size()));
    }
    
    private void handleGroqResponse(String response, String input, String type) {
        try {
            // Parse JSON response from Groq
            JSONObject jsonResponse = new JSONObject(response);
            String action = jsonResponse.optString("action", "Unknown");
            String category = jsonResponse.optString("category", "General");
            String implementation = jsonResponse.optString("implementation", "");
            double confidence = jsonResponse.optDouble("confidence", 0.5);
            
            // Create or update learning module
            createOrUpdateLearningModule(action, category, input, type, confidence);
            
            updateStatus("Learned: " + action + " (" + category + ") - Confidence: " 
                    + String.format("%.0f%%", confidence * 100));
            
            // Store in memory
            memoryManager.storeKnowledge(category, action, implementation);
            
            Log.d(TAG, "Successfully processed: " + action);
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse Groq response", e);
            // Fallback to simple learning
            String learningKey = type + "_" + input;
            createOrUpdateLearningModule(input, type, input, type, 0.7);
            updateStatus("Learned: " + input);
        }
    }
    
    private void createOrUpdateLearningModule(String action, String category, 
            String input, String type, double confidence) {
        String moduleKey = category + "_" + action;
        
        LearningModule module = independentModules.get(moduleKey);
        if (module == null) {
            module = new LearningModule(action, category);
            independentModules.put(moduleKey, module);
            
            learningRepository.getLabelByName(action, new LearningRepository.OnLabelCallback() {
                @Override
                public void onLabelRetrieved(LabelDefinitionEntity label) {
                    if (label == null) {
                        LabelDefinitionEntity newLabel = new LabelDefinitionEntity();
                        newLabel.setName(action);
                        newLabel.setPurpose("Voice/Gesture teaching: " + type);
                        newLabel.setCategory(category);
                        newLabel.setCreatedAt(new Date());
                        
                        learningRepository.insertLabel(newLabel, new LearningRepository.OnInsertCallback() {
                            @Override
                            public void onSuccess(long id) {
                                Log.d(TAG, "Created label definition for: " + action);
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to create label definition", e);
                            }
                        });
                    } else {
                        learningRepository.incrementLabelUsage(label.getLabelId(), new LearningRepository.OnUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Incremented usage for label: " + action);
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to increment label usage", e);
                            }
                        });
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to check label existence", e);
                }
            });
        }
        
        module.addExample(input, type, confidence);
        module.incrementUsageCount();
        
        // Train adaptive learning system
        learningSystem.learn(category, action, (float) confidence);
        
        Log.d(TAG, "Learning module updated: " + moduleKey + " (examples: " 
                + module.getExampleCount() + ")");
    }
    
    private void saveTeachingSession() {
        if (currentSessionGestures.isEmpty() && currentSessionVoiceCommands.isEmpty()) {
            Toast.makeText(this, "No teaching data to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateStatus("Saving teaching session to database...");
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger totalOperations = new AtomicInteger(
                currentSessionVoiceCommands.size() + currentSessionGestures.size());
        
        for (String voiceCommand : currentSessionVoiceCommands) {
            VoiceSampleEntity voiceSample = new VoiceSampleEntity();
            voiceSample.setTranscript(voiceCommand);
            voiceSample.setLabel(currentTeachingContext != null ? currentTeachingContext : "voice_teaching");
            voiceSample.setTimestamp(new Date());
            voiceSample.setConfidence(0.8f);
            
            learningRepository.insertVoiceSample(voiceSample, new LearningRepository.OnInsertCallback() {
                @Override
                public void onSuccess(long id) {
                    runOnUiThread(() -> {
                        int completed = successCount.incrementAndGet();
                        checkSaveCompletion(completed, totalOperations.get());
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Failed to save voice sample", e);
                        Toast.makeText(VoiceTeachingActivity.this, 
                                "Error saving voice sample: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        
        for (TapGesture gesture : currentSessionGestures) {
            try {
                JSONArray coordsArray = new JSONArray();
                JSONObject coord = new JSONObject();
                coord.put("x", gesture.x);
                coord.put("y", gesture.y);
                coord.put("timestamp", gesture.timestamp);
                coordsArray.put(coord);
                
                GestureSampleEntity gestureSample = new GestureSampleEntity();
                gestureSample.setGestureType(gesture.type);
                gestureSample.setCoordinatesJson(coordsArray.toString());
                gestureSample.setLabel(currentTeachingContext != null ? currentTeachingContext : "gesture_teaching");
                gestureSample.setTimestamp(new Date(gesture.timestamp));
                gestureSample.setConfidence(0.7f);
                
                learningRepository.insertGestureSample(gestureSample, new LearningRepository.OnInsertCallback() {
                    @Override
                    public void onSuccess(long id) {
                        runOnUiThread(() -> {
                            int completed = successCount.incrementAndGet();
                            checkSaveCompletion(completed, totalOperations.get());
                        });
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Failed to save gesture sample", e);
                            Toast.makeText(VoiceTeachingActivity.this, 
                                    "Error saving gesture sample: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create gesture JSON", e);
            }
        }
        
        saveSessionToMemory("Teaching session saved to database");
    }
    
    private void checkSaveCompletion(int completed, int total) {
        if (completed == total) {
            Toast.makeText(this, 
                    "Successfully saved " + completed + " teaching samples to database!", 
                    Toast.LENGTH_LONG).show();
            updateStatus("Session saved - " + independentModules.size() + " modules learned");
            Log.d(TAG, "All teaching data saved successfully");
        }
    }
    
    private String generateSessionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Voice Commands (").append(currentSessionVoiceCommands.size()).append("): ");
        summary.append(String.join(", ", currentSessionVoiceCommands));
        summary.append("\nGestures (").append(currentSessionGestures.size()).append(")\n");
        summary.append("Learning Modules Created: ").append(independentModules.size());
        return summary.toString();
    }
    
    private void saveSessionToMemory(String aiSummary) {
        String sessionData = generateSessionSummary() + "\n\nAI Analysis:\n" + aiSummary;
        memoryManager.storeKnowledge("TeachingSession", 
                "Session_" + System.currentTimeMillis(), sessionData);
        
        Toast.makeText(this, "Teaching session saved successfully!", Toast.LENGTH_LONG).show();
        updateStatus("Session saved - " + independentModules.size() + " modules learned");
        
        Log.d(TAG, "Teaching session saved");
    }
    
    private void clearSession() {
        currentSessionGestures.clear();
        currentSessionVoiceCommands.clear();
        teachingCanvas.removeAllViews();
        updateTapCount();
        updateStatus("Session cleared - Ready for new teaching");
        
        Log.d(TAG, "Session cleared");
    }
    
    private void updateStatus(String message) {
        statusTextView.setText(message);
    }
    
    private void updateTapCount() {
        tapCountTextView.setText("Gestures: " + currentSessionGestures.size() + 
                " | Voice Commands: " + currentSessionVoiceCommands.size());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    
    // Voice Recognition Listener
    private class VoiceRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            updateStatus("Listening... Speak now");
        }
        
        @Override
        public void onBeginningOfSpeech() {
            updateStatus("Processing speech...");
        }
        
        @Override
        public void onRmsChanged(float rmsdB) {
            // Update visual feedback for voice level
        }
        
        @Override
        public void onBufferReceived(byte[] buffer) {
        }
        
        @Override
        public void onEndOfSpeech() {
            updateStatus("Processing command...");
        }
        
        @Override
        public void onError(int error) {
            String errorMessage = getErrorText(error);
            updateStatus("Voice error: " + errorMessage);
            isListening = false;
            startVoiceButton.setEnabled(true);
            stopVoiceButton.setEnabled(false);
            
            Log.e(TAG, "Speech recognition error: " + errorMessage);
        }
        
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String command = matches.get(0);
                currentSessionVoiceCommands.add(command);
                currentTeachingContext = command;
                
                updateStatus("You said: " + command);
                updateTapCount();
                
                // Process with Groq
                processWithGroq(command, "voice");
                
                Log.d(TAG, "Voice command recognized: " + command);
            }
            
            isListening = false;
            startVoiceButton.setEnabled(true);
            stopVoiceButton.setEnabled(false);
        }
        
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                updateStatus("Hearing: " + matches.get(0) + "...");
            }
        }
        
        @Override
        public void onEvent(int eventType, Bundle params) {
        }
        
        private String getErrorText(int errorCode) {
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    return "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    return "Client side error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    return "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    return "Network error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    return "Network timeout";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    return "No speech match";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    return "Recognition service busy";
                case SpeechRecognizer.ERROR_SERVER:
                    return "Server error";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    return "No speech input";
                default:
                    return "Unknown error";
            }
        }
    }
    
    // Data Classes
    private static class TapGesture {
        float x, y;
        long timestamp;
        String type;
        
        TapGesture(float x, float y, long timestamp, String type) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
            this.type = type;
        }
    }
    
    private static class LearningModule {
        String action;
        String category;
        List<Example> examples;
        int usageCount;
        long createdAt;
        
        LearningModule(String action, String category) {
            this.action = action;
            this.category = category;
            this.examples = new ArrayList<>();
            this.usageCount = 0;
            this.createdAt = System.currentTimeMillis();
        }
        
        void addExample(String input, String type, double confidence) {
            examples.add(new Example(input, type, confidence));
        }
        
        void incrementUsageCount() {
            usageCount++;
        }
        
        int getExampleCount() {
            return examples.size();
        }
    }
    
    private static class Example {
        String input;
        String type;
        double confidence;
        long timestamp;
        
        Example(String input, String type, double confidence) {
            this.input = input;
            this.type = type;
            this.confidence = confidence;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
