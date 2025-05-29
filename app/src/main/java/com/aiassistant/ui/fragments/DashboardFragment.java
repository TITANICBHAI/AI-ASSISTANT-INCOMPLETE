package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;

public class DashboardFragment extends Fragment implements AIStateManager.AIStateListener {

    private static final String TAG = "DashboardFragment";
    
    private TextView statusTextView;
    private Button startButton;
    private Button stopButton;
    private RadioGroup modeRadioGroup;
    private RadioButton autoRadioButton;
    private RadioButton learningRadioButton;
    private RadioButton copilotRadioButton;
    private RadioButton passiveRadioButton;
    private Button applyModeButton;
    private Button aiModesButton;
    private Button gamesButton;
    private Button learningButton;
    private Button settingsButton;
    
    // AI state manager
    private AIStateManager aiStateManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get the AI state manager
        aiStateManager = AIStateManager.getInstance();
        
        // Initialize views
        statusTextView = view.findViewById(R.id.statusTextView);
        startButton = view.findViewById(R.id.startButton);
        stopButton = view.findViewById(R.id.stopButton);
        modeRadioGroup = view.findViewById(R.id.modeRadioGroup);
        autoRadioButton = view.findViewById(R.id.autoRadioButton);
        learningRadioButton = view.findViewById(R.id.learningRadioButton);
        copilotRadioButton = view.findViewById(R.id.copilotRadioButton);
        passiveRadioButton = view.findViewById(R.id.passiveRadioButton);
        applyModeButton = view.findViewById(R.id.applyModeButton);
        aiModesButton = view.findViewById(R.id.aiModesButton);
        gamesButton = view.findViewById(R.id.gamesButton);
        learningButton = view.findViewById(R.id.learningButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        
        // Set up radio buttons based on current mode
        updateModeRadioButtons();
        
        // Set up button click listeners
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAI();
            }
        });
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAI();
            }
        });
        
        applyModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySelectedMode();
            }
        });
        
        // Set up navigation button click listeners
        aiModesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_ai_modes);
            }
        });
        
        gamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_games);
            }
        });
        
        learningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_learning);
            }
        });
        
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_settings);
            }
        });
        
        // Update the UI with current state
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register as AI state listener
        aiStateManager.addStateListener(this);
        // Update UI
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister as AI state listener
        aiStateManager.removeStateListener(this);
    }

    @Override
    public void onAIStateChanged(AIStateManager.AIState oldState, AIStateManager.AIState newState) {
        // Update the UI when AI state changes
        updateUI();
    }
    
    private void updateUI() {
        if (getActivity() == null) {
            return;
        }
        
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update status text
                AIStateManager.AIState currentState = aiStateManager.getCurrentState();
                statusTextView.setText("Status: " + currentState.toString());
                
                // Update button states
                boolean isActive = currentState != AIStateManager.AIState.INACTIVE;
                startButton.setEnabled(!isActive);
                stopButton.setEnabled(isActive);
                
                // Update mode radio buttons
                updateModeRadioButtons();
            }
        });
    }
    
    private void updateModeRadioButtons() {
        int currentMode = aiStateManager.getCurrentMode();
        
        switch (currentMode) {
            case AIStateManager.MODE_AUTO:
                autoRadioButton.setChecked(true);
                break;
            case AIStateManager.MODE_LEARNING:
                learningRadioButton.setChecked(true);
                break;
            case AIStateManager.MODE_COPILOT:
                copilotRadioButton.setChecked(true);
                break;
            case AIStateManager.MODE_PASSIVE:
                passiveRadioButton.setChecked(true);
                break;
        }
    }
    
    private void startAI() {
        // For demo purposes, we'll just start with a "demo" package
        aiStateManager.start("com.example.game");
        Toast.makeText(getContext(), "AI started", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "AI started");
    }
    
    private void stopAI() {
        aiStateManager.stop();
        Toast.makeText(getContext(), "AI stopped", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "AI stopped");
    }
    
    private void applySelectedMode() {
        int selectedMode;
        
        if (autoRadioButton.isChecked()) {
            selectedMode = AIStateManager.MODE_AUTO;
        } else if (learningRadioButton.isChecked()) {
            selectedMode = AIStateManager.MODE_LEARNING;
        } else if (copilotRadioButton.isChecked()) {
            selectedMode = AIStateManager.MODE_COPILOT;
        } else {
            selectedMode = AIStateManager.MODE_PASSIVE;
        }
        
        aiStateManager.setMode(selectedMode);
        Toast.makeText(getContext(), "Mode set to: " + aiStateManager.getModeString(selectedMode), 
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Mode set to: " + aiStateManager.getModeString(selectedMode));
    }
}
