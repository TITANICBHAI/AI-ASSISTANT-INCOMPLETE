package com.aiassistant.ui.voice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.core.voice.VoiceCommandManager;
import com.aiassistant.services.GameInteractionService;

/**
 * Activity for demonstrating voice control of games
 */
public class VoiceGameControlActivity extends AppCompatActivity implements VoiceCommandManager.VoiceCommandListener {
    private static final String TAG = "VoiceGameControl";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    
    // UI elements
    private TextView statusTextView;
    private TextView commandTextView;
    private TextView lastCommandTextView;
    private Button startListeningButton;
    private Button stopListeningButton;
    private ImageButton continuousListeningButton;
    private ProgressBar listeningProgressBar;
    
    // Components
    private VoiceCommandManager voiceCommandManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;
    
    // State tracking
    private boolean isContinuousListening = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_game_control);
        
        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView);
        commandTextView = findViewById(R.id.commandTextView);
        lastCommandTextView = findViewById(R.id.lastCommandTextView);
        startListeningButton = findViewById(R.id.startListeningButton);
        stopListeningButton = findViewById(R.id.stopListeningButton);
        continuousListeningButton = findViewById(R.id.continuousListeningButton);
        listeningProgressBar = findViewById(R.id.listeningProgressBar);
        
        // Set up button click listeners
        startListeningButton.setOnClickListener(v -> startVoiceListening());
        stopListeningButton.setOnClickListener(v -> stopVoiceListening());
        continuousListeningButton.setOnClickListener(v -> toggleContinuousListening());
        
        // Initialize components
        voiceCommandManager = VoiceCommandManager.getInstance(this);
        voiceCommandManager.addCommandListener(this);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Start GameInteractionService to handle game interactions
        startGameInteractionService();
        
        // Check for audio recording permission
        checkPermissions();
        
        // Set initial UI state
        updateUIState(false);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        voiceCommandManager.addCommandListener(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        voiceCommandManager.removeCommandListener(this);
        
        // Stop listening when paused
        if (isContinuousListening) {
            voiceCommandManager.stopListening();
        }
    }
    
    /**
     * Start the game interaction service
     */
    private void startGameInteractionService() {
        Intent intent = new Intent(this, GameInteractionService.class);
        intent.setAction("START");
        startService(intent);
    }
    
    /**
     * Check and request audio recording permission
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }
    
    /**
     * Start voice command listening
     */
    private void startVoiceListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        voiceCommandManager.startListening();
    }
    
    /**
     * Stop voice command listening
     */
    private void stopVoiceListening() {
        voiceCommandManager.stopListening();
        isContinuousListening = false;
        updateContinuousButtonState();
    }
    
    /**
     * Toggle continuous listening mode
     */
    private void toggleContinuousListening() {
        isContinuousListening = !isContinuousListening;
        
        if (isContinuousListening) {
            voiceCommandManager.startContinuousListening();
            Toast.makeText(this, "Continuous listening mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            voiceCommandManager.stopListening();
            Toast.makeText(this, "Continuous listening mode disabled", Toast.LENGTH_SHORT).show();
        }
        
        updateContinuousButtonState();
    }
    
    /**
     * Update the UI state based on listening status
     */
    private void updateUIState(boolean isListening) {
        startListeningButton.setEnabled(!isListening);
        stopListeningButton.setEnabled(isListening);
        listeningProgressBar.setVisibility(isListening ? View.VISIBLE : View.INVISIBLE);
        
        statusTextView.setText(isListening ? getString(R.string.voice_command_listening) : getString(R.string.voice_command_ready));
        
        updateContinuousButtonState();
    }
    
    /**
     * Update the continuous listening button state
     */
    private void updateContinuousButtonState() {
        continuousListeningButton.setImageResource(isContinuousListening ? 
                R.drawable.ic_mic_continuous_on : R.drawable.ic_mic_continuous_off);
    }
    
    /**
     * Provide feedback to the user when a command is recognized
     */
    private void provideFeedback() {
        // Vibrate to indicate command recognition
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied, voice commands unavailable", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // VoiceCommandListener implementation
    
    @Override
    public void onListeningStarted() {
        handler.post(() -> {
            updateUIState(true);
            commandTextView.setText("");
        });
    }
    
    @Override
    public void onListeningStopped() {
        handler.post(() -> updateUIState(false));
    }
    
    @Override
    public void onPartialCommandRecognized(String partialCommand) {
        handler.post(() -> commandTextView.setText(partialCommand));
    }
    
    @Override
    public void onCommandRecognized(String command, int commandType) {
        handler.post(() -> {
            provideFeedback();
            
            String typeStr;
            switch (commandType) {
                case VoiceCommandManager.COMMAND_TYPE_GAME:
                    typeStr = "Game";
                    break;
                case VoiceCommandManager.COMMAND_TYPE_SYSTEM:
                    typeStr = "System";
                    break;
                case VoiceCommandManager.COMMAND_TYPE_CONVERSATION:
                    typeStr = "Conversation";
                    break;
                default:
                    typeStr = "Unknown";
                    break;
            }
            
            String displayText = String.format("Recognized: %s (%s)", command, typeStr);
            lastCommandTextView.setText(displayText);
        });
    }
    
    @Override
    public void onCommandProcessing(String rawCommand) {
        handler.post(() -> {
            statusTextView.setText(getString(R.string.voice_command_processing));
            commandTextView.setText(rawCommand);
        });
    }
    
    @Override
    public void onCommandExecuted(String command, int commandType) {
        handler.post(() -> {
            String actionText = String.format(getString(R.string.voice_command_action), command);
            statusTextView.setText(actionText);
        });
    }
    
    @Override
    public void onCommandUnknown(String command) {
        handler.post(() -> {
            statusTextView.setText(getString(R.string.voice_command_error));
            Toast.makeText(VoiceGameControlActivity.this, "Unknown command: " + command, Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onCommandError(String error) {
        handler.post(() -> {
            statusTextView.setText(getString(R.string.voice_command_error));
            Toast.makeText(VoiceGameControlActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
        });
    }
}
