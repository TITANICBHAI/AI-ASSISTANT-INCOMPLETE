package com.aiassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.adapters.TaskAdapter;
import com.aiassistant.data.models.Task;
import com.aiassistant.ui.viewmodels.AppManagementViewModel;

import java.util.ArrayList;

/**
 * Main dashboard fragment displaying AI status and recent tasks/actions
 */
public class DashboardFragment extends Fragment {
    
    private AppManagementViewModel viewModel;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView aiStatusText;
    private TextView detectionStatusText;
    private Button startStopButton;
    
    public DashboardFragment() {
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
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        aiStatusText = view.findViewById(R.id.aiStatusText);
        detectionStatusText = view.findViewById(R.id.detectionStatusText);
        startStopButton = view.findViewById(R.id.startStopButton);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        
        // Set up RecyclerView
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        tasksRecyclerView.setAdapter(taskAdapter);
        
        // Set up button listeners
        setupButtonListeners();
        
        // Observe ViewModel data
        observeViewModelData();
    }
    
    private void setupButtonListeners() {
        startStopButton.setOnClickListener(v -> {
            if (viewModel.isAIServiceRunning()) {
                viewModel.stopAIService();
                startStopButton.setText(R.string.start_ai_service);
            } else {
                viewModel.startAIService();
                startStopButton.setText(R.string.stop_ai_service);
            }
        });
        
        // Update button text based on current status
        if (viewModel.isAIServiceRunning()) {
            startStopButton.setText(R.string.stop_ai_service);
        } else {
            startStopButton.setText(R.string.start_ai_service);
        }
    }
    
    private void observeViewModelData() {
        // Observe AI status changes
        viewModel.getAIStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            aiStatusText.setText(status);
        });
        
        // Observe detection status changes
        viewModel.getDetectionStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            detectionStatusText.setText(status);
        });
        
        // Observe tasks list
        viewModel.getRecentTasksLiveData().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.updateTasks(tasks);
        });
    }
    
    /**
     * Add a new task to the list for testing
     * 
     * @param task The task to add
     */
    public void addTask(Task task) {
        viewModel.addTask(task);
    }
}
