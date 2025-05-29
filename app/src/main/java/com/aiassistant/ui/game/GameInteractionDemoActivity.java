package com.aiassistant.ui.game;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.core.interaction.AdvancedGameInteraction;
import com.aiassistant.services.AIAccessibilityService;
import com.aiassistant.services.GameInteractionService;
import com.aiassistant.utils.AccessibilityUtils;

/**
 * Demo activity for game interaction features
 * Shows how to use the AdvancedGameInteraction system to interact with games
 */
public class GameInteractionDemoActivity extends AppCompatActivity {
    private static final String TAG = "GameInteractionDemo";
    private static final int REQUEST_MEDIA_PROJECTION = 1001;
    private static final int REQUEST_PERMISSIONS = 1002;
    
    // UI elements
    private TextView statusTextView;
    private Button startServiceButton;
    private Button stopServiceButton;
    private Switch autoPlaySwitch;
    private Button tapButton;
    private Button swipeButton;
    private Button comboButton;
    
    // Components
    private AdvancedGameInteraction gameInteraction;
    private MediaProjectionManager mediaProjectionManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_interaction_demo);
        
        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView);
        startServiceButton = findViewById(R.id.startServiceButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);
        autoPlaySwitch = findViewById(R.id.autoPlaySwitch);
        tapButton = findViewById(R.id.tapButton);
        swipeButton = findViewById(R.id.swipeButton);
        comboButton = findViewById(R.id.comboButton);
        
        // Initialize components
        gameInteraction = AdvancedGameInteraction.getInstance(this);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        
        // Set up button click listeners
        startServiceButton.setOnClickListener(v -> startGameInteractionService());
        stopServiceButton.setOnClickListener(v -> stopGameInteractionService());
        
        autoPlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAutoPlayMode(isChecked);
        });
        
        tapButton.setOnClickListener(v -> performTapDemo());
        swipeButton.setOnClickListener(v -> performSwipeDemo());
        comboButton.setOnClickListener(v -> performComboDemo());
        
        // Check for necessary permissions
        checkPermissions();
        
        // Update initial status
        updateStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    
    /**
     * Check and request necessary permissions
     */
    private void checkPermissions() {
        // Check accessibility service
        if (!AccessibilityUtils.isAccessibilityServiceEnabled(this)) {
            promptEnableAccessibilityService();
        }
        
        // Check screen capture permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }
    }
    
    /**
     * Prompt user to enable accessibility service
     */
    private void promptEnableAccessibilityService() {
        Toast.makeText(this, "Please enable the AI Assistant accessibility service", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
    
    /**
     * Update the UI status
     */
    private void updateStatus() {
        boolean accessibilityEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(this);
        boolean serviceRunning = AIAccessibilityService.isRunning();
        
        StringBuilder status = new StringBuilder();
        status.append("Accessibility Service: ").append(accessibilityEnabled ? "Enabled" : "Disabled").append("\n");
        status.append("Service Running: ").append(serviceRunning ? "Yes" : "No").append("\n");
        
        if (serviceRunning) {
            AIAccessibilityService service = AIAccessibilityService.getInstance();
            String currentPackage = service != null ? service.getCurrentPackage() : "Unknown";
            status.append("Current App: ").append(currentPackage).append("\n");
        }
        
        statusTextView.setText(status.toString());
        
        // Update button states
        startServiceButton.setEnabled(accessibilityEnabled && !serviceRunning);
        stopServiceButton.setEnabled(serviceRunning);
        autoPlaySwitch.setEnabled(serviceRunning);
        tapButton.setEnabled(serviceRunning);
        swipeButton.setEnabled(serviceRunning);
        comboButton.setEnabled(serviceRunning);
    }
    
    /**
     * Start the media projection for screenshots
     */
    private void startMediaProjection() {
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }
    
    /**
     * Start the game interaction service
     */
    private void startGameInteractionService() {
        // First ensure we have media projection permission for screenshots
        startMediaProjection();
    }
    
    /**
     * Actually start the service (after permissions)
     */
    private void startServiceAfterPermissions() {
        Intent intent = new Intent(this, GameInteractionService.class);
        intent.setAction("START");
        startService(intent);
        
        updateStatus();
        Toast.makeText(this, "Game Interaction Service started", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stop the game interaction service
     */
    private void stopGameInteractionService() {
        Intent intent = new Intent(this, GameInteractionService.class);
        intent.setAction("STOP");
        startService(intent);
        
        updateStatus();
        Toast.makeText(this, "Game Interaction Service stopped", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Set auto-play mode
     */
    private void setAutoPlayMode(boolean enabled) {
        Intent intent = new Intent(this, GameInteractionService.class);
        intent.setAction("AUTO_PLAY");
        intent.putExtra("auto_play", enabled);
        startService(intent);
        
        Toast.makeText(this, "Auto-play mode: " + (enabled ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Perform a simple tap demo (tap center of screen)
     */
    private void performTapDemo() {
        int centerX = getResources().getDisplayMetrics().widthPixels / 2;
        int centerY = getResources().getDisplayMetrics().heightPixels / 2;
        
        boolean success = gameInteraction.tap(centerX, centerY);
        Toast.makeText(this, "Tap result: " + success, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Perform a simple swipe demo (swipe up from center)
     */
    private void performSwipeDemo() {
        int centerX = getResources().getDisplayMetrics().widthPixels / 2;
        int centerY = getResources().getDisplayMetrics().heightPixels / 2;
        
        boolean success = gameInteraction.swipe(centerX, centerY, centerX, centerY - 300);
        Toast.makeText(this, "Swipe result: " + success, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Perform a combo demo (if in a supported game)
     */
    private void performComboDemo() {
        AIAccessibilityService service = AIAccessibilityService.getInstance();
        if (service == null) {
            Toast.makeText(this, "Service not running", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String currentPackage = service.getCurrentPackage();
        gameInteraction.updateGamePackage(currentPackage);
        
        if (currentPackage != null && currentPackage.contains("freefire")) {
            boolean success = gameInteraction.performCombo("jump_crouch");
            Toast.makeText(this, "Jump-crouch combo result: " + success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No combos available for current app", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Set media projection in accessibility service
                MediaProjection projection = mediaProjectionManager.getMediaProjection(resultCode, data);
                AIAccessibilityService service = AIAccessibilityService.getInstance();
                if (service != null) {
                    service.setMediaProjection(projection);
                    startServiceAfterPermissions();
                } else {
                    Toast.makeText(this, "Accessibility service not running", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateStatus();
            } else {
                Toast.makeText(this, "Storage permissions required for screenshots", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
