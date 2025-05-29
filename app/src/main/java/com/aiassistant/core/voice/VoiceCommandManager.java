package com.aiassistant.core.voice;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.learning.memory.MemoryStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager for voice command recognition and processing
 */
public class VoiceCommandManager {
    private static final String TAG = "VoiceCommandManager";
    
    private final Context context;
    private final MemoryStorage memoryStorage;
    private SpeechRecognizer speechRecognizer;
    private AudioManager audioManager;
    private boolean isListening = false;
    private ExecutorService executorService;
    
    // Command handlers
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    
    /**
     * Constructor
     */
    public VoiceCommandManager(Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.executorService = Executors.newCachedThreadPool();
        
        // Get memory storage from AI state manager
        AIStateManager aiStateManager = AIStateManager.getInstance();
        this.memoryStorage = aiStateManager.getMemoryStorage();
        
        // Initialize speech recognizer
        initializeSpeechRecognizer();
        
        // Register default command handlers
        registerDefaultCommandHandlers();
    }
    
    /**
     * Initialize speech recognizer
     */
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
            Log.d(TAG, "Speech recognizer initialized");
        } else {
            Log.e(TAG, "Speech recognition not available on this device");
        }
    }
    
    /**
     * Register default command handlers
     */
    private void registerDefaultCommandHandlers() {
        // Call handling commands
        registerCommandHandler("answer", (command, params) -> {
            Log.d(TAG, "Executing 'answer' command");
            // TODO: Implement call answer logic
            return true;
        });
        
        registerCommandHandler("decline", (command, params) -> {
            Log.d(TAG, "Executing 'decline' command");
            // TODO: Implement call decline logic
            return true;
        });
        
        registerCommandHandler("end call", (command, params) -> {
            Log.d(TAG, "Executing 'end call' command");
            // TODO: Implement end call logic
            return true;
        });
        
        registerCommandHandler("speaker", (command, params) -> {
            Log.d(TAG, "Executing 'speaker' command");
            audioManager.setSpeakerphoneOn(true);
            return true;
        });
        
        registerCommandHandler("headset", (command, params) -> {
            Log.d(TAG, "Executing 'headset' command");
            audioManager.setSpeakerphoneOn(false);
            return true;
        });
        
        registerCommandHandler("mute", (command, params) -> {
            Log.d(TAG, "Executing 'mute' command");
            audioManager.setMicrophoneMute(true);
            return true;
        });
        
        registerCommandHandler("unmute", (command, params) -> {
            Log.d(TAG, "Executing 'unmute' command");
            audioManager.setMicrophoneMute(false);
            return true;
        });
    }
    
    /**
     * Register a command handler
     */
    public void registerCommandHandler(String command, CommandHandler handler) {
        commandHandlers.put(command.toLowerCase(), handler);
        Log.d(TAG, "Registered command handler for: " + command);
    }
    
    /**
     * Start listening for voice commands
     */
    public void startListening() {
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized");
            return;
        }
        
        if (isListening) {
            stopListening();
        }
        
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            speechRecognizer.startListening(intent);
            isListening = true;
            
            Log.d(TAG, "Started listening for voice commands");
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
        }
    }
    
    /**
     * Stop listening for voice commands
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.d(TAG, "Stopped listening for voice commands");
        }
    }
    
    /**
     * Process recognized speech
     */
    private void processRecognizedSpeech(String speech) {
        if (speech == null || speech.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "Processing speech: " + speech);
        
        // Convert to lowercase for case-insensitive matching
        String lowerSpeech = speech.toLowerCase();
        
        // Store this speech in memory
        if (memoryStorage != null) {
            memoryStorage.storeInteraction("VOICE", "Voice command", speech);
        }
        
        // Look for command matches
        boolean commandExecuted = false;
        
        for (Map.Entry<String, CommandHandler> entry : commandHandlers.entrySet()) {
            String command = entry.getKey();
            CommandHandler handler = entry.getValue();
            
            if (lowerSpeech.contains(command)) {
                // Extract parameters (anything after the command)
                String params = lowerSpeech.substring(lowerSpeech.indexOf(command) + command.length()).trim();
                
                // Execute command in background
                executorService.execute(() -> {
                    boolean success = handler.executeCommand(command, params);
                    Log.d(TAG, "Command '" + command + "' executed with " + 
                            (success ? "success" : "failure"));
                });
                
                commandExecuted = true;
                break;
            }
        }
        
        if (!commandExecuted) {
            Log.d(TAG, "No matching command found for: " + speech);
        }
    }
    
    /**
     * Speech recognition listener
     */
    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "Ready for speech");
        }
        
        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech");
        }
        
        @Override
        public void onRmsChanged(float rmsdB) {
            // RMS (root mean square) of the audio signal changed
        }
        
        @Override
        public void onBufferReceived(byte[] buffer) {
            // Audio buffer received
        }
        
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "End of speech");
        }
        
        @Override
        public void onError(int error) {
            String errorMessage;
            
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMessage = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMessage = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMessage = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMessage = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMessage = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMessage = "No speech match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMessage = "Recognition service busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMessage = "Server error";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMessage = "No speech input";
                    break;
                default:
                    errorMessage = "Unknown error";
                    break;
            }
            
            Log.e(TAG, "Speech recognition error: " + errorMessage);
            
            // Restart listening after delay for some errors
            if (error == SpeechRecognizer.ERROR_NO_MATCH || 
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                restartListeningAfterDelay();
            } else {
                isListening = false;
            }
        }
        
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String bestMatch = matches.get(0);
                Log.d(TAG, "Speech recognized: " + bestMatch);
                
                // Process the recognized speech
                processRecognizedSpeech(bestMatch);
            }
            
            // Restart listening
            restartListeningAfterDelay();
        }
        
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String bestMatch = matches.get(0);
                Log.d(TAG, "Partial speech recognized: " + bestMatch);
            }
        }
        
        @Override
        public void onEvent(int eventType, Bundle params) {
            // Speech recognition event occurred
        }
        
        /**
         * Restart listening after a brief delay
         */
        private void restartListeningAfterDelay() {
            isListening = false;
            
            executorService.execute(() -> {
                try {
                    Thread.sleep(1000);
                    startListening();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting to restart speech recognition", e);
                }
            });
        }
    }
    
    /**
     * Command handler interface
     */
    public interface CommandHandler {
        /**
         * Execute a command
         *
         * @param command The command string
         * @param params  Additional parameters
         * @return True if command executed successfully
         */
        boolean executeCommand(String command, String params);
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        if (speechRecognizer != null) {
            if (isListening) {
                stopListening();
            }
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        
        executorService.shutdown();
    }
}
    /**
     * Interface for speech recognition callbacks
     */
    public interface OnSpeechRecognizedListener {
        void onSpeechRecognized(String speech);
        void onSpeechError(String errorMessage);
    }
    
    /**
     * Start listening for speech
     */
    public void startListening(OnSpeechRecognizedListener listener) {
        // Simulate speech recognition
        // In a real app, use SpeechRecognizer
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Simulate recognized speech
                listener.onSpeechRecognized("I'd like to leave a message for them");
            }
        }, 2000);
    }
    }
