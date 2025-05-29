package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;

public class LearningFragment extends Fragment {

    private TextView currentGameValue;
    private TextView currentModeValue;
    private TextView totalActionsValue;
    private TextView totalSessionsValue;
    private Button enableTransferLearningButton;
    private Spinner sourceGameSpinner;
    private Spinner targetGameSpinner;
    private Button applyTransferButton;
    private Button backButton;
    
    private AIStateManager aiStateManager;
    private boolean transferLearningEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get AI state manager
        aiStateManager = AIStateManager.getInstance();
        
        // Initialize views
        currentGameValue = view.findViewById(R.id.currentGameValue);
        currentModeValue = view.findViewById(R.id.currentModeValue);
        totalActionsValue = view.findViewById(R.id.totalActionsValue);
        totalSessionsValue = view.findViewById(R.id.totalSessionsValue);
        enableTransferLearningButton = view.findViewById(R.id.enableTransferLearningButton);
        sourceGameSpinner = view.findViewById(R.id.sourceGameSpinner);
        targetGameSpinner = view.findViewById(R.id.targetGameSpinner);
        applyTransferButton = view.findViewById(R.id.applyTransferButton);
        backButton = view.findViewById(R.id.backButton);
        
        // Set up game spinners
        String[] gameList = {"PUBG Mobile", "Free Fire", "Call of Duty Mobile", "Mobile Legends"};
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, gameList);
        gameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceGameSpinner.setAdapter(gameAdapter);
        targetGameSpinner.setAdapter(gameAdapter);
        
        // Update UI with current values
        updateUI();
        
        // Set up button click listeners
        enableTransferLearningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTransferLearning();
            }
        });
        
        applyTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTransferLearning();
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigateUp();
            }
        });
    }
    
    private void updateUI() {
        // Update current game
        String currentGame = aiStateManager.getCurrentGamePackage();
        currentGameValue.setText(currentGame != null ? currentGame : "None");
        
        // Update current mode
        int currentMode = aiStateManager.getCurrentMode();
        currentModeValue.setText(aiStateManager.getModeString(currentMode));
        
        // Update total actions (placeholder)
        totalActionsValue.setText("243");
        
        // Update total sessions (placeholder)
        totalSessionsValue.setText("12");
        
        // Update transfer learning button text
        updateTransferLearningButtonText();
    }
    
    private void toggleTransferLearning() {
        transferLearningEnabled = !transferLearningEnabled;
        updateTransferLearningButtonText();
        
        // Enable or disable the related controls
        sourceGameSpinner.setEnabled(transferLearningEnabled);
        targetGameSpinner.setEnabled(transferLearningEnabled);
        applyTransferButton.setEnabled(transferLearningEnabled);
        
        Toast.makeText(getContext(), 
                "Transfer learning " + (transferLearningEnabled ? "enabled" : "disabled"), 
                Toast.LENGTH_SHORT).show();
    }
    
    private void updateTransferLearningButtonText() {
        enableTransferLearningButton.setText(transferLearningEnabled ? 
                "Disable Transfer Learning" : "Enable Transfer Learning");
    }
    
    private void applyTransferLearning() {
        String sourceGame = (String) sourceGameSpinner.getSelectedItem();
        String targetGame = (String) targetGameSpinner.getSelectedItem();
        
        if (sourceGame.equals(targetGame)) {
            Toast.makeText(getContext(), 
                    "Source and target games must be different", 
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Apply transfer learning (placeholder)
        Toast.makeText(getContext(), 
                "Applied transfer learning from " + sourceGame + " to " + targetGame, 
                Toast.LENGTH_SHORT).show();
    }
}
