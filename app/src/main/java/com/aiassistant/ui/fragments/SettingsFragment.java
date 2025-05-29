package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;

public class SettingsFragment extends Fragment {

    private SeekBar learningRateSeekBar;
    private TextView learningRateValue;
    private SeekBar explorationRateSeekBar;
    private TextView explorationRateValue;
    private Spinner frameRateSpinner;
    private CheckBox useTensorFlowLiteCheckBox;
    private CheckBox useNNAPICheckBox;
    private Button resetLearningButton;
    private Button exportDataButton;
    private Button importDataButton;
    private Button saveSettingsButton;
    private Button backButton;
    
    private AIStateManager aiStateManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get AI state manager
        aiStateManager = AIStateManager.getInstance();
        
        // Initialize views
        learningRateSeekBar = view.findViewById(R.id.learningRateSeekBar);
        learningRateValue = view.findViewById(R.id.learningRateValue);
        explorationRateSeekBar = view.findViewById(R.id.explorationRateSeekBar);
        explorationRateValue = view.findViewById(R.id.explorationRateValue);
        frameRateSpinner = view.findViewById(R.id.frameRateSpinner);
        useTensorFlowLiteCheckBox = view.findViewById(R.id.useTensorFlowLiteCheckBox);
        useNNAPICheckBox = view.findViewById(R.id.useNNAPICheckBox);
        resetLearningButton = view.findViewById(R.id.resetLearningButton);
        exportDataButton = view.findViewById(R.id.exportDataButton);
        importDataButton = view.findViewById(R.id.importDataButton);
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton);
        backButton = view.findViewById(R.id.backButton);
        
        // Set up frame rate spinner
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"5 FPS", "10 FPS", "15 FPS", "30 FPS"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frameRateSpinner.setAdapter(adapter);
        frameRateSpinner.setSelection(1); // 10 FPS default
        
        // Set up learning rate seek bar
        learningRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                learningRateValue.setText(String.format("%.2f", value));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Set up exploration rate seek bar
        explorationRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                explorationRateValue.setText(String.format("%.2f", value));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Set up button click listeners
        resetLearningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset learning (placeholder)
                Toast.makeText(getContext(), "Learning data reset", Toast.LENGTH_SHORT).show();
            }
        });
        
        exportDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Export data (placeholder)
                Toast.makeText(getContext(), "Data exported", Toast.LENGTH_SHORT).show();
            }
        });
        
        importDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Import data (placeholder)
                Toast.makeText(getContext(), "Data imported", Toast.LENGTH_SHORT).show();
            }
        });
        
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigateUp();
            }
        });
    }
    
    private void saveSettings() {
        // Get learning rate
        float learningRate = learningRateSeekBar.getProgress() / 100.0f;
        
        // Get exploration rate
        float explorationRate = explorationRateSeekBar.getProgress() / 100.0f;
        
        // Get frame rate
        String frameRateStr = (String) frameRateSpinner.getSelectedItem();
        int frameRate = Integer.parseInt(frameRateStr.split(" ")[0]);
        
        // Get other settings
        boolean useTensorFlowLite = useTensorFlowLiteCheckBox.isChecked();
        boolean useNNAPI = useNNAPICheckBox.isChecked();
        
        // Apply settings (placeholder)
        Toast.makeText(getContext(), "Settings saved", Toast.LENGTH_SHORT).show();
    }
}
