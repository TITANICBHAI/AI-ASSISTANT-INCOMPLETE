package com.aiassistant.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.detection.EnemyDetectionSystem;
import com.aiassistant.core.ai.models.GameState;

import java.util.List;

/**
 * Fragment for controlling the AI assistant's operation.
 * Provides UI for switching between modes, viewing state, and controlling learning.
 */
public class AIControlFragment extends Fragment implements AIStateManager.AICallback {
    
    private static final String TAG = "AIControlFragment";
    
    // UI elements
    private ToggleButton toggleActivate;
    private Button btnModeCopilot;
    private Button btnModeAutonomous;
    private Button btnModeLearning;
    private TextView txtStatus;
    private TextView txtDetections;
    private View actionIndicator;
    
    // Live data for updates
    private MutableLiveData<Integer> currentMode = new MutableLiveData<>(AIStateManager.MODE_INACTIVE);
    private MutableLiveData<String> statusText = new MutableLiveData<>("AI Inactive");
    private MutableLiveData<String> detectionsText = new MutableLiveData<>("");
    private MutableLiveData<Boolean> actionPerformed = new MutableLiveData<>(false);
    
    // Callback interface
    private AIControlListener listener;
    
    /**
     * Create new instance
     * @return Fragment instance
     */
    public static AIControlFragment newInstance() {
        return new AIControlFragment();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AIControlListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AIControlListener");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_control, container, false);
        
        // Initialize UI elements
        toggleActivate = view.findViewById(R.id.toggle_activate);
        btnModeCopilot = view.findViewById(R.id.btn_mode_copilot);
        btnModeAutonomous = view.findViewById(R.id.btn_mode_autonomous);
        btnModeLearning = view.findViewById(R.id.btn_mode_learning);
        txtStatus = view.findViewById(R.id.txt_status);
        txtDetections = view.findViewById(R.id.txt_detections);
        actionIndicator = view.findViewById(R.id.action_indicator);
        
        setupClickListeners();
        setupObservers();
        
        // Register with AIStateManager
        AIStateManager.getInstance().initialize(requireContext(), this);
        
        return view;
    }
    
    /**
     * Set up button click listeners
     */
    private void setupClickListeners() {
        toggleActivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AIStateManager.getInstance().switchMode(AIStateManager.MODE_COPILOT);
            } else {
                AIStateManager.getInstance().switchMode(AIStateManager.MODE_INACTIVE);
            }
        });
        
        btnModeCopilot.setOnClickListener(v -> {
            AIStateManager.getInstance().switchMode(AIStateManager.MODE_COPILOT);
        });
        
        btnModeAutonomous.setOnClickListener(v -> {
            AIStateManager.getInstance().switchMode(AIStateManager.MODE_AUTONOMOUS);
        });
        
        btnModeLearning.setOnClickListener(v -> {
            AIStateManager.getInstance().switchMode(AIStateManager.MODE_LEARNING);
        });
    }
    
    /**
     * Set up LiveData observers
     */
    private void setupObservers() {
        currentMode.observe(getViewLifecycleOwner(), mode -> {
            // Update UI based on mode
            toggleActivate.setChecked(mode != AIStateManager.MODE_INACTIVE);
            
            btnModeCopilot.setEnabled(mode != AIStateManager.MODE_INACTIVE);
            btnModeAutonomous.setEnabled(mode != AIStateManager.MODE_INACTIVE);
            btnModeLearning.setEnabled(mode != AIStateManager.MODE_INACTIVE);
            
            // Highlight active mode button
            btnModeCopilot.setSelected(mode == AIStateManager.MODE_COPILOT);
            btnModeAutonomous.setSelected(mode == AIStateManager.MODE_AUTONOMOUS);
            btnModeLearning.setSelected(mode == AIStateManager.MODE_LEARNING);
        });
        
        statusText.observe(getViewLifecycleOwner(), status -> {
            txtStatus.setText(status);
        });
        
        detectionsText.observe(getViewLifecycleOwner(), detections -> {
            txtDetections.setText(detections);
        });
        
        actionPerformed.observe(getViewLifecycleOwner(), performed -> {
            if (performed) {
                // Flash action indicator
                actionIndicator.setVisibility(View.VISIBLE);
                actionIndicator.postDelayed(() -> {
                    actionIndicator.setVisibility(View.INVISIBLE);
                    actionPerformed.setValue(false);
                }, 300);
            } else {
                actionIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }
    
    @Override
    public void onModeChanged(int oldMode, int newMode) {
        currentMode.postValue(newMode);
        
        switch (newMode) {
            case AIStateManager.MODE_INACTIVE:
                statusText.postValue("AI Inactive");
                break;
                
            case AIStateManager.MODE_LEARNING:
                statusText.postValue("Learning Mode");
                break;
                
            case AIStateManager.MODE_COPILOT:
                statusText.postValue("Copilot Mode");
                break;
                
            case AIStateManager.MODE_AUTONOMOUS:
                statusText.postValue("Autonomous Mode");
                break;
        }
        
        if (listener != null) {
            listener.onAIModeChanged(newMode);
        }
    }
    
    @Override
    public void onStateUpdated(GameState gameState) {
        // Could update more detailed status here
    }
    
    @Override
    public void onEnemiesDetected(List<EnemyDetectionSystem.DetectedEnemy> enemies) {
        if (enemies.isEmpty()) {
            detectionsText.postValue("No enemies detected");
        } else {
            detectionsText.postValue("Detected " + enemies.size() + " enemies");
        }
    }
    
    @Override
    public void onPatternsRecognized(List<com.aiassistant.core.ai.combat.CombatPatternRecognition.PatternMatch> patterns) {
        // Could show recognized patterns in UI
    }
    
    @Override
    public void onActionExecuted(com.aiassistant.core.ai.models.GameAction action) {
        actionPerformed.postValue(true);
        statusText.postValue("Action: " + action.getDescription());
    }
    
    @Override
    public void onAutoTakeover() {
        // Notify that AI has taken control due to inactivity
        if (getContext() != null) {
            // Could show a toast or notification here
        }
    }
    
    /**
     * Interface for AI control events
     */
    public interface AIControlListener {
        /**
         * Called when AI mode changes
         * @param newMode New AI mode
         */
        void onAIModeChanged(int newMode);
    }
}