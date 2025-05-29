package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;

public class AIModesFragment extends Fragment {

    private Button selectAutoButton;
    private Button selectLearningButton;
    private Button selectCopilotButton;
    private Button selectPassiveButton;
    private Button backButton;
    
    private AIStateManager aiStateManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_modes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get AI state manager
        aiStateManager = AIStateManager.getInstance();
        
        // Initialize views
        selectAutoButton = view.findViewById(R.id.selectAutoButton);
        selectLearningButton = view.findViewById(R.id.selectLearningButton);
        selectCopilotButton = view.findViewById(R.id.selectCopilotButton);
        selectPassiveButton = view.findViewById(R.id.selectPassiveButton);
        backButton = view.findViewById(R.id.backButton);
        
        // Set up button click listeners
        selectAutoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(AIStateManager.MODE_AUTO);
            }
        });
        
        selectLearningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(AIStateManager.MODE_LEARNING);
            }
        });
        
        selectCopilotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(AIStateManager.MODE_COPILOT);
            }
        });
        
        selectPassiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(AIStateManager.MODE_PASSIVE);
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigateUp();
            }
        });
    }
    
    private void setMode(int mode) {
        aiStateManager.setMode(mode);
        Toast.makeText(getContext(), 
                "Mode set to: " + aiStateManager.getModeString(mode), 
                Toast.LENGTH_SHORT).show();
    }
}
