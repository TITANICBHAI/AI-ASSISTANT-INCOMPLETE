package com.aiassistant.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIAgentMode;
import com.aiassistant.ui.viewmodels.AISettingsViewModel;

/**
 * Dialog for configuring advanced AI settings including learning rate,
 * reinforcement learning algorithm selection, and autonomous behavior settings.
 */
public class AIAdvancedSettingsDialog extends DialogFragment {

    private AISettingsViewModel viewModel;
    
    private SeekBar learningRateSeekBar;
    private TextView learningRateValueText;
    private RadioGroup algorithmGroup;
    private CheckBox useMetaLearningCheck;
    private CheckBox inactivityDetectionCheck;
    private Spinner inactivityTimeoutSpinner;
    private CheckBox prioritizeGameActionsCheck;
    private SeekBar explorationRateSeekBar;
    private TextView explorationRateValueText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AISettingsViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ai_advanced_settings, null);
        
        initViews(view);
        loadCurrentSettings();
        setupListeners();
        
        builder.setView(view)
                .setTitle(R.string.advanced_ai_settings)
                .setPositiveButton(R.string.save, (dialog, id) -> saveSettings())
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // User cancelled the dialog
                    dialog.dismiss();
                });
        
        return builder.create();
    }
    
    private void initViews(View view) {
        learningRateSeekBar = view.findViewById(R.id.seek_learning_rate);
        learningRateValueText = view.findViewById(R.id.text_learning_rate_value);
        algorithmGroup = view.findViewById(R.id.radio_group_algorithms);
        useMetaLearningCheck = view.findViewById(R.id.check_use_meta_learning);
        inactivityDetectionCheck = view.findViewById(R.id.check_inactivity_detection);
        inactivityTimeoutSpinner = view.findViewById(R.id.spinner_inactivity_timeout);
        prioritizeGameActionsCheck = view.findViewById(R.id.check_prioritize_game_actions);
        explorationRateSeekBar = view.findViewById(R.id.seek_exploration_rate);
        explorationRateValueText = view.findViewById(R.id.text_exploration_rate_value);
    }
    
    private void loadCurrentSettings() {
        // Load current settings from the view model
        viewModel.getSettings().observe(this, settings -> {
            // Set learning rate (0.0 - 1.0 mapped to 0-100)
            int learningRate = (int)(settings.getLearningRate() * 100);
            learningRateSeekBar.setProgress(learningRate);
            updateLearningRateText(learningRate);
            
            // Set reinforcement learning algorithm
            switch (settings.getReinforcementAlgorithm()) {
                case Q_LEARNING:
                    ((RadioButton) algorithmGroup.findViewById(R.id.radio_q_learning)).setChecked(true);
                    break;
                case SARSA:
                    ((RadioButton) algorithmGroup.findViewById(R.id.radio_sarsa)).setChecked(true);
                    break;
                case DQN:
                    ((RadioButton) algorithmGroup.findViewById(R.id.radio_dqn)).setChecked(true);
                    break;
                case PPO:
                    ((RadioButton) algorithmGroup.findViewById(R.id.radio_ppo)).setChecked(true);
                    break;
            }
            
            // Set other options
            useMetaLearningCheck.setChecked(settings.isUseMetaLearning());
            inactivityDetectionCheck.setChecked(settings.isInactivityDetectionEnabled());
            prioritizeGameActionsCheck.setChecked(settings.isPrioritizeGameActions());
            
            // Set inactivity timeout selection (index in spinner)
            inactivityTimeoutSpinner.setSelection(getTimeoutSelectionIndex(settings.getInactivityTimeoutMinutes()));
            
            // Set exploration rate
            int explorationRate = (int)(settings.getExplorationRate() * 100);
            explorationRateSeekBar.setProgress(explorationRate);
            updateExplorationRateText(explorationRate);
        });
    }
    
    private void setupListeners() {
        learningRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateLearningRateText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        explorationRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateExplorationRateText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        inactivityDetectionCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            inactivityTimeoutSpinner.setEnabled(isChecked);
        });
    }
    
    private void saveSettings() {
        // Convert progress to a float between 0.0 and 1.0
        float learningRate = learningRateSeekBar.getProgress() / 100.0f;
        float explorationRate = explorationRateSeekBar.getProgress() / 100.0f;
        
        // Get selected algorithm
        AIAgentMode algorithm;
        int selectedId = algorithmGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_sarsa) {
            algorithm = AIAgentMode.SARSA;
        } else if (selectedId == R.id.radio_dqn) {
            algorithm = AIAgentMode.DQN;
        } else if (selectedId == R.id.radio_ppo) {
            algorithm = AIAgentMode.PPO;
        } else {
            algorithm = AIAgentMode.Q_LEARNING; // Default
        }
        
        // Get inactivity timeout
        int inactivityTimeout = getTimeoutValueFromSpinner(inactivityTimeoutSpinner.getSelectedItemPosition());
        
        // Save all settings
        viewModel.updateSettings(
                learningRate,
                explorationRate,
                algorithm,
                useMetaLearningCheck.isChecked(),
                inactivityDetectionCheck.isChecked(),
                inactivityTimeout,
                prioritizeGameActionsCheck.isChecked()
        );
    }
    
    private void updateLearningRateText(int progress) {
        float value = progress / 100.0f;
        learningRateValueText.setText(String.format("%.2f", value));
    }
    
    private void updateExplorationRateText(int progress) {
        float value = progress / 100.0f;
        explorationRateValueText.setText(String.format("%.2f", value));
    }
    
    private int getTimeoutSelectionIndex(int minutes) {
        // Convert minutes to spinner position
        switch (minutes) {
            case 1: return 0;
            case 2: return 1;
            case 5: return 2;
            case 10: return 3;
            case 15: return 4;
            case 30: return 5;
            default: return 1; // Default to 2 minutes
        }
    }
    
    private int getTimeoutValueFromSpinner(int position) {
        // Convert spinner position to minutes
        switch (position) {
            case 0: return 1;
            case 1: return 2;
            case 2: return 5;
            case 3: return 10;
            case 4: return 15;
            case 5: return 30;
            default: return 2; // Default to 2 minutes
        }
    }
}