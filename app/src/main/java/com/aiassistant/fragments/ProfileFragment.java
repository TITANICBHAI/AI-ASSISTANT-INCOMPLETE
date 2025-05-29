package com.aiassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aiassistant.R;
import com.aiassistant.data.models.UserProfile;
import com.aiassistant.ui.viewmodels.AppManagementViewModel;

/**
 * Fragment for user profile management and statistics
 */
public class ProfileFragment extends Fragment {
    
    private AppManagementViewModel viewModel;
    private EditText usernameEditText;
    private TextView experienceValueText;
    private TextView sessionsValueText;
    private TextView accuracyValueText;
    private ProgressBar experienceProgressBar;
    private Button saveProfileButton;
    private Button resetStatsButton;
    
    public ProfileFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AppManagementViewModel.class);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        experienceValueText = view.findViewById(R.id.experienceValueText);
        sessionsValueText = view.findViewById(R.id.sessionsValueText);
        accuracyValueText = view.findViewById(R.id.accuracyValueText);
        experienceProgressBar = view.findViewById(R.id.experienceProgressBar);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        resetStatsButton = view.findViewById(R.id.resetStatsButton);
        
        // Set up button listeners
        setupButtonListeners();
        
        // Observe ViewModel data
        observeViewModelData();
    }
    
    private void setupButtonListeners() {
        saveProfileButton.setOnClickListener(v -> {
            UserProfile currentProfile = viewModel.getUserProfileLiveData().getValue();
            if (currentProfile != null) {
                currentProfile.setUsername(usernameEditText.getText().toString());
                viewModel.updateUserProfile(currentProfile);
            }
        });
        
        resetStatsButton.setOnClickListener(v -> {
            viewModel.resetUserStats();
        });
    }
    
    private void observeViewModelData() {
        // Observe user profile data
        viewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                usernameEditText.setText(profile.getUsername());
                experienceValueText.setText(String.valueOf(profile.getExperiencePoints()));
                sessionsValueText.setText(String.valueOf(profile.getSessionCount()));
                accuracyValueText.setText(String.format("%.1f%%", profile.getAverageAccuracy()));
                
                // Calculate level progress
                int currentLevel = profile.getCurrentLevel();
                int expToNextLevel = profile.getExperienceToNextLevel();
                int levelProgress = profile.getLevelProgress();
                
                experienceProgressBar.setMax(expToNextLevel);
                experienceProgressBar.setProgress(levelProgress);
            }
        });
    }
}
