package com.aiassistant.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.aiassistant.AIApplication;
import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.utils.Constants;

/**
 * Home fragment showing AI status and controls
 */
public class HomeFragment extends Fragment {
    private TextView statusText;
    private TextView currentGameText;
    private TextView currentModeText;
    private TextView confidenceText;
    
    private Button btnObservationMode;
    private Button btnLearningMode;
    private Button btnCopilotMode;
    private Button btnAutoMode;
    
    private BroadcastReceiver gameDetectedReceiver;
    private BroadcastReceiver modeChangedReceiver;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize UI
        statusText = view.findViewById(R.id.text_status);
        currentGameText = view.findViewById(R.id.text_current_game);
        currentModeText = view.findViewById(R.id.text_current_mode);
        confidenceText = view.findViewById(R.id.text_confidence);
        
        btnObservationMode = view.findViewById(R.id.btn_observation_mode);
        btnLearningMode = view.findViewById(R.id.btn_learning_mode);
        btnCopilotMode = view.findViewById(R.id.btn_copilot_mode);
        btnAutoMode = view.findViewById(R.id.btn_auto_mode);
        
        // Set up button listeners
        setupButtonListeners();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Register broadcast receivers
        registerReceivers();
        
        // Update UI
        updateUI();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // Unregister broadcast receivers
        unregisterReceivers();
    }
    
    /**
     * Set up button listeners
     */
    private void setupButtonListeners() {
        btnObservationMode.setOnClickListener(v -> setMode(Constants.MODE_OBSERVATION));
        btnLearningMode.setOnClickListener(v -> setMode(Constants.MODE_LEARNING));
        btnCopilotMode.setOnClickListener(v -> setMode(Constants.MODE_COPILOT));
        btnAutoMode.setOnClickListener(v -> setMode(Constants.MODE_AUTO));
    }
    
    /**
     * Set AI mode
     * 
     * @param mode Mode to set
     */
    private void setMode(String mode) {
        AIStateManager aiStateManager = AIApplication.getAIStateManager();
        if (aiStateManager != null) {
            aiStateManager.setMode(mode);
            updateUI();
        }
    }
    
    /**
     * Register broadcast receivers
     */
    private void registerReceivers() {
        // Game detected receiver
        gameDetectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        
        // Mode changed receiver
        modeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        
        // Register receivers
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            gameDetectedReceiver, new IntentFilter(Constants.ACTION_GAME_DETECTED));
        
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            modeChangedReceiver, new IntentFilter(Constants.ACTION_AI_MODE_CHANGED));
    }
    
    /**
     * Unregister broadcast receivers
     */
    private void unregisterReceivers() {
        if (gameDetectedReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(gameDetectedReceiver);
        }
        
        if (modeChangedReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(modeChangedReceiver);
        }
    }
    
    /**
     * Update UI with current state
     */
    private void updateUI() {
        AIStateManager aiStateManager = AIApplication.getAIStateManager();
        if (aiStateManager == null) {
            return;
        }
        
        // Update status
        if (AIApplication.getAccessibilityService() != null) {
            statusText.setText(R.string.status_active);
        } else {
            statusText.setText(R.string.status_idle);
        }
        
        // Update game
        String currentGame = aiStateManager.getCurrentGame();
        if (Constants.GAME_GENERIC.equals(currentGame)) {
            currentGameText.setText(getString(R.string.no_game_detected));
        } else if (Constants.GAME_PUBG.equals(currentGame)) {
            currentGameText.setText("PUBG Mobile");
        } else if (Constants.GAME_FREE_FIRE.equals(currentGame)) {
            currentGameText.setText("Free Fire");
        } else if (Constants.GAME_COD_MOBILE.equals(currentGame)) {
            currentGameText.setText("Call of Duty Mobile");
        } else {
            currentGameText.setText(currentGame);
        }
        
        // Update mode
        String currentMode = aiStateManager.getCurrentMode();
        if (Constants.MODE_OBSERVATION.equals(currentMode)) {
            currentModeText.setText(getString(R.string.observation_mode));
        } else if (Constants.MODE_LEARNING.equals(currentMode)) {
            currentModeText.setText(getString(R.string.learning_mode));
        } else if (Constants.MODE_COPILOT.equals(currentMode)) {
            currentModeText.setText(getString(R.string.copilot_mode));
        } else if (Constants.MODE_AUTO.equals(currentMode)) {
            currentModeText.setText(getString(R.string.auto_mode));
        }
        
        // Update confidence
        confidenceText.setText(aiStateManager.getConfidence() + "%");
        
        // Update button states
        btnObservationMode.setEnabled(!Constants.MODE_OBSERVATION.equals(currentMode));
        btnLearningMode.setEnabled(!Constants.MODE_LEARNING.equals(currentMode));
        btnCopilotMode.setEnabled(!Constants.MODE_COPILOT.equals(currentMode));
        btnAutoMode.setEnabled(!Constants.MODE_AUTO.equals(currentMode));
    }
}
