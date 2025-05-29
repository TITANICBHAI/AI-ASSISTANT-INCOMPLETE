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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.ui.adapters.ScheduledTasksAdapter;
import com.aiassistant.ui.dialogs.CreateTaskDialog;
import com.aiassistant.ui.viewmodels.TaskSchedulerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Fragment for creating and managing scheduled tasks that the AI will execute
 * autonomously, even when the user is absent.
 */
public class TaskSchedulerFragment extends Fragment implements CreateTaskDialog.TaskCreationListener {

    private TaskSchedulerViewModel viewModel;
    private RecyclerView tasksRecyclerView;
    private ScheduledTasksAdapter tasksAdapter;
    private FloatingActionButton addTaskButton;
    private View emptyStateView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TaskSchedulerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_scheduler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void initViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.recycler_scheduled_tasks);
        addTaskButton = view.findViewById(R.id.fab_add_task);
        emptyStateView = view.findViewById(R.id.layout_empty_state);
        
        // Add additional buttons for quick task creation
        view.findViewById(R.id.button_create_game_task).setOnClickListener(v -> 
                showCreateTaskDialog("Game Automation"));
        
        view.findViewById(R.id.button_create_message_task).setOnClickListener(v -> 
                showCreateTaskDialog("Send Message"));
        
        view.findViewById(R.id.button_create_app_task).setOnClickListener(v -> 
                showCreateTaskDialog("App Interaction"));
    }

    private void setupRecyclerView() {
        tasksAdapter = new ScheduledTasksAdapter(new ArrayList<>(), 
                taskId -> viewModel.deleteTask(taskId),
                taskId -> viewModel.toggleTaskEnabled(taskId));
                
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksRecyclerView.setAdapter(tasksAdapter);
    }

    private void setupObservers() {
        viewModel.getScheduledTasks().observe(getViewLifecycleOwner(), tasks -> {
            tasksAdapter.updateData(tasks);
            updateEmptyState(tasks.isEmpty());
        });
    }

    private void setupListeners() {
        addTaskButton.setOnClickListener(v -> showCreateTaskDialog(null));
    }

    private void showCreateTaskDialog(String taskType) {
        CreateTaskDialog dialog = new CreateTaskDialog();
        
        if (taskType != null) {
            Bundle args = new Bundle();
            args.putString("task_type", taskType);
            dialog.setArguments(args);
        }
        
        dialog.setTaskCreationListener(this);
        dialog.show(getChildFragmentManager(), "create_task");
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateView.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskCreated(ScheduledTask task) {
        viewModel.saveTask(task);
        Toast.makeText(requireContext(), 
                R.string.task_created_successfully, Toast.LENGTH_SHORT).show();
    }
}