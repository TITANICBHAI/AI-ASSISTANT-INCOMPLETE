package com.aiassistant.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aiassistant.R;
import com.aiassistant.core.AIAssistantApplication;
import com.aiassistant.learning.ReinforcementLearner;
import com.aiassistant.data.models.ActionParameters;
import com.aiassistant.services.AccessibilityDetectionService;
import com.aiassistant.services.BackgroundMonitoringService;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment for controlling AI modes (Auto AI and Copilot)
 * and their specific settings.
 */
public class AIModeFragment extends Fragment {
    
    private RadioGroup modeRadioGroup;
    private RadioButton autoModeRadio;
    private RadioButton copilotModeRadio;
    
    private ViewGroup autoModeSettings;
    private ViewGroup copilotModeSettings;
    
    private SwitchMaterial inactivitySwitch;
    private Slider inactivityTimeSlider;
    private TextView inactivityTimeLabel;
    
    private Button startMonitoringButton;
    private Button stopMonitoringButton;
    private Button accessibilitySettingsButton;
    
    private RadioGroup algorithmRadioGroup;
    private ToggleButton continuousLearningToggle;
    
    private int inactivityTimeMinutes = 2; // Default inactivity timeout
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_mode, container, false);
        
        // Find views
        modeRadioGroup = view.findViewById(R.id.radio_group_mode);
        autoModeRadio = view.findViewById(R.id.radio_auto_mode);
        copilotModeRadio = view.findViewById(R.id.radio_copilot_mode);
        
        autoModeSettings = view.findViewById(R.id.layout_auto_mode_settings);
        copilotModeSettings = view.findViewById(R.id.layout_copilot_mode_settings);
        
        inactivitySwitch = view.findViewById(R.id.switch_inactivity);
        inactivityTimeSlider = view.findViewById(R.id.slider_inactivity_time);
        inactivityTimeLabel = view.findViewById(R.id.text_inactivity_time);
        
        startMonitoringButton = view.findViewById(R.id.button_start_monitoring);
        stopMonitoringButton = view.findViewById(R.id.button_stop_monitoring);
        accessibilitySettingsButton = view.findViewById(R.id.button_accessibility_settings);
        
        algorithmRadioGroup = view.findViewById(R.id.radio_group_algorithm);
        continuousLearningToggle = view.findViewById(R.id.toggle_continuous_learning);
        
        // Set up listeners
        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateModeSelection(checkedId);
        });
        
        inactivitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            inactivityTimeSlider.setEnabled(isChecked);
            inactivityTimeLabel.setEnabled(isChecked);
        });
        
        inactivityTimeSlider.addOnChangeListener((slider, value, fromUser) -> {
            inactivityTimeMinutes = (int) value;
            updateInactivityTimeLabel();
        });
        
        startMonitoringButton.setOnClickListener(v -> startMonitoring());
        stopMonitoringButton.setOnClickListener(v -> stopMonitoring());
        accessibilitySettingsButton.setOnClickListener(v -> openAccessibilitySettings());
        
        algorithmRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateAlgorithmSelection(checkedId);
        });
        
        continuousLearningToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setContinuousLearning(isChecked);
        });
        
        // Initialize UI state
        updateModeSelection(modeRadioGroup.getCheckedRadioButtonId());
        updateInactivityTimeLabel();
        updateUIState();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUIState();
    }
    
    /**
     * Updates the UI based on the selected mode
     * @param checkedId The ID of the checked radio button
     */
    private void updateModeSelection(int checkedId) {
        if (checkedId == R.id.radio_auto_mode) {
            // Show auto mode settings, hide copilot settings
            autoModeSettings.setVisibility(View.VISIBLE);
            copilotModeSettings.setVisibility(View.GONE);
            
            // Update AI mode
            setAIMode(true); // Auto mode = true
            
        } else if (checkedId == R.id.radio_copilot_mode) {
            // Show copilot settings, hide auto mode settings
            autoModeSettings.setVisibility(View.GONE);
            copilotModeSettings.setVisibility(View.VISIBLE);
            
            // Update AI mode
            setAIMode(false); // Copilot mode = false
        }
    }
    
    /**
     * Updates the inactivity time label
     */
    private void updateInactivityTimeLabel() {
        inactivityTimeLabel.setText(getString(R.string.inactivity_time_format, inactivityTimeMinutes));
    }
    
    /**
     * Updates the UI state based on current service status
     */
    private void updateUIState() {
        boolean accessibilityRunning = AccessibilityDetectionService.isRunning();
        boolean monitoringRunning = BackgroundMonitoringService.isRunning();
        
        // Update buttons
        startMonitoringButton.setEnabled(accessibilityRunning && !monitoringRunning);
        stopMonitoringButton.setEnabled(monitoringRunning);
        
        // Show appropriate message if accessibility service is not running
        if (!accessibilityRunning) {
            Toast.makeText(getContext(), R.string.accessibility_service_required, Toast.LENGTH_SHORT).show();
        }
        
        // Set current algorithm
        AIAssistantApplication app = (AIAssistantApplication) requireActivity().getApplication();
        ReinforcementLearner learner = app.getReinforcementLearner();
        
        if (learner != null) {
            ReinforcementLearner.Algorithm currentAlgo = learner.getAlgorithm();
            
            switch (currentAlgo) {
                case Q_LEARNING:
                    algorithmRadioGroup.check(R.id.radio_q_learning);
                    break;
                case SARSA:
                    algorithmRadioGroup.check(R.id.radio_sarsa);
                    break;
                case DQN:
                    algorithmRadioGroup.check(R.id.radio_dqn);
                    break;
                case PPO:
                    algorithmRadioGroup.check(R.id.radio_ppo);
                    break;
            }
            
            // Check current mode
            boolean autoMode = learner.isAutonomousModeEnabled();
            if (autoMode) {
                autoModeRadio.setChecked(true);
            } else {
                copilotModeRadio.setChecked(true);
            }
        }
    }
    
    /**
     * Sets the AI mode (Auto or Copilot)
     * @param autoMode true for Auto mode, false for Copilot mode
     */
    private void setAIMode(boolean autoMode) {
        AIAssistantApplication app = (AIAssistantApplication) requireActivity().getApplication();
        ReinforcementLearner learner = app.getReinforcementLearner();
        
        if (learner != null) {
            learner.setAutonomousModeEnabled(autoMode);
            
            // Configure inactivity settings if enabled
            if (autoMode && inactivitySwitch.isChecked()) {
                BackgroundMonitoringService.setInactivityTimeout(inactivityTimeMinutes * 60 * 1000);
                BackgroundMonitoringService.setInactivityDetectionEnabled(true);
            } else {
                BackgroundMonitoringService.setInactivityDetectionEnabled(false);
            }
            
            // Show toast message
            String modeStr = autoMode ? getString(R.string.auto_mode) : getString(R.string.copilot_mode);
            Toast.makeText(getContext(), getString(R.string.mode_changed_format, modeStr), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Updates the selected reinforcement learning algorithm
     * @param checkedId The ID of the checked radio button
     */
    private void updateAlgorithmSelection(int checkedId) {
        AIAssistantApplication app = (AIAssistantApplication) requireActivity().getApplication();
        ReinforcementLearner learner = app.getReinforcementLearner();
        
        if (learner != null) {
            ReinforcementLearner.Algorithm algorithm;
            
            if (checkedId == R.id.radio_q_learning) {
                algorithm = ReinforcementLearner.Algorithm.Q_LEARNING;
            } else if (checkedId == R.id.radio_sarsa) {
                algorithm = ReinforcementLearner.Algorithm.SARSA;
            } else if (checkedId == R.id.radio_dqn) {
                algorithm = ReinforcementLearner.Algorithm.DQN;
            } else if (checkedId == R.id.radio_ppo) {
                algorithm = ReinforcementLearner.Algorithm.PPO;
            } else {
                algorithm = ReinforcementLearner.Algorithm.Q_LEARNING; // Default
            }
            
            learner.setAlgorithm(algorithm);
            Toast.makeText(getContext(), getString(R.string.algorithm_changed_format, algorithm.name()), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Enables or disables continuous learning
     * @param enabled Whether continuous learning should be enabled
     */
    private void setContinuousLearning(boolean enabled) {
        AIAssistantApplication app = (AIAssistantApplication) requireActivity().getApplication();
        app.setContinuousLearningEnabled(enabled);
        
        Toast.makeText(getContext(), 
                enabled ? R.string.continuous_learning_enabled : R.string.continuous_learning_disabled, 
                Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Starts the background monitoring service
     */
    private void startMonitoring() {
        if (!AccessibilityDetectionService.isRunning()) {
            Toast.makeText(getContext(), R.string.accessibility_service_required, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Create action parameters
        ActionParameters params = new ActionParameters();
        params.setBoolean("inactivityDetection", inactivitySwitch.isChecked());
        params.setInteger("inactivityTimeoutMs", inactivityTimeMinutes * 60 * 1000);
        
        // Start the service
        Intent intent = new Intent(getContext(), BackgroundMonitoringService.class);
        intent.putExtra("parameters", params);
        requireActivity().startService(intent);
        
        updateUIState();
        Toast.makeText(getContext(), R.string.monitoring_started, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stops the background monitoring service
     */
    private void stopMonitoring() {
        Intent intent = new Intent(getContext(), BackgroundMonitoringService.class);
        requireActivity().stopService(intent);
        
        updateUIState();
        Toast.makeText(getContext(), R.string.monitoring_stopped, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Opens the accessibility settings
     */
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
}