package com.aiassistant.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.emotional.EmotionalIntelligenceManager;
import com.aiassistant.services.CallHandlingService;

/**
 * Fragment for managing emotional intelligence settings
 */
public class EmotionalIntelligenceSettingsFragment extends Fragment {

    private EmotionalIntelligenceManager emotionalManager;
    private Switch switchEmotionalIntelligence;
    private CardView cardEmotionalSettings;
    private SeekBar seekBarAdaptationRate;
    private SeekBar seekBarMimicryRate;
    private SeekBar seekBarBaselineRate;
    private Button buttonResetSettings;

    public EmotionalIntelligenceSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the EmotionalIntelligenceManager instance
        AIStateManager aiStateManager = AIStateManager.getInstance();
        emotionalManager = EmotionalIntelligenceManager.getInstance(
                requireContext(), aiStateManager, aiStateManager.getMemoryStorage());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emotional_intelligence_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get references to UI elements
        switchEmotionalIntelligence = view.findViewById(R.id.switch_emotional_intelligence);
        cardEmotionalSettings = view.findViewById(R.id.card_emotional_settings);
        seekBarAdaptationRate = view.findViewById(R.id.seekbar_adaptation_rate);
        seekBarMimicryRate = view.findViewById(R.id.seekbar_mimicry_rate);
        seekBarBaselineRate = view.findViewById(R.id.seekbar_baseline_rate);
        buttonResetSettings = view.findViewById(R.id.button_reset_settings);
        
        // Set initial states
        boolean emotionalEnabled = CallHandlingService.isEmotionalIntelligenceEnabled(requireContext());
        switchEmotionalIntelligence.setChecked(emotionalEnabled);
        cardEmotionalSettings.setEnabled(emotionalEnabled);
        
        // Set initial progress values
        seekBarAdaptationRate.setProgress((int)(emotionalManager.getAdaptationRate() * 100));
        seekBarMimicryRate.setProgress((int)(emotionalManager.getMimicryRate() * 100));
        seekBarBaselineRate.setProgress((int)(emotionalManager.getBaselineRate() * 100));
        
        // Set up listeners
        switchEmotionalIntelligence.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CallHandlingService.setEmotionalIntelligenceEnabled(requireContext(), isChecked);
            emotionalManager.setEnabled(isChecked);
            cardEmotionalSettings.setEnabled(isChecked);
        });
        
        seekBarAdaptationRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float rate = progress / 100.0f;
                    emotionalManager.setAdaptationRate(rate);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        seekBarMimicryRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float rate = progress / 100.0f;
                    emotionalManager.setMimicryRate(rate);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        seekBarBaselineRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float rate = progress / 100.0f;
                    emotionalManager.setBaselineRate(rate);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        buttonResetSettings.setOnClickListener(v -> {
            emotionalManager.resetSettings();
            seekBarAdaptationRate.setProgress((int)(emotionalManager.getAdaptationRate() * 100));
            seekBarMimicryRate.setProgress((int)(emotionalManager.getMimicryRate() * 100));
            seekBarBaselineRate.setProgress((int)(emotionalManager.getBaselineRate() * 100));
        });
    }
}
