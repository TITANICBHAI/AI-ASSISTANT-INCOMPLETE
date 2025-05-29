package com.aiassistant.ui.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.data.models.Task;
import com.aiassistant.data.repositories.TaskRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel responsible for task scheduling and management
 */
public class TaskSchedulerViewModel extends AndroidViewModel {
    private static final String TAG = "TaskSchedulerViewModel";

    private MutableLiveData<List<Task>> tasksLiveData;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private TaskRepository taskRepository;

    public TaskSchedulerViewModel(@NonNull Application application) {
        super(application);
        tasksLiveData = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>("");
        taskRepository = new TaskRepository(application);
        
        // Load initial data
        loadTasks();
    }

    /**
     * Get LiveData containing all tasks
     * 
     * @return LiveData of Task List
     */
    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    /**
     * Get loading state
     * 
     * @return LiveData of loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get error messages
     * 
     * @return LiveData of error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Load all tasks from repository
     */
    public void loadTasks() {
        isLoading.setValue(true);
        
        taskRepository.getAllTasks(new TaskRepository.TaskRepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                tasksLiveData.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tasks", e);
                errorMessage.setValue("Error loading tasks: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Create a new task
     * 
     * @param name Task name
     * @param description Task description
     * @param priorityLevel Priority level (1-5)
     * @param scheduledTime Scheduled execution time
     */
    public void createTask(String name, String description, int priorityLevel, Date scheduledTime) {
        if (name == null || name.isEmpty()) {
            errorMessage.setValue("Task name cannot be empty");
            return;
        }
        
        if (priorityLevel < 1 || priorityLevel > 5) {
            errorMessage.setValue("Priority must be between 1-5");
            return;
        }
        
        if (scheduledTime == null) {
            scheduledTime = Calendar.getInstance().getTime(); // Set to current time as default
        }
        
        Task newTask = new Task();
        newTask.setName(name);
        newTask.setDescription(description);
        newTask.setPriorityLevel(priorityLevel);
        newTask.setScheduledTime(scheduledTime);
        newTask.setCreatedTime(Calendar.getInstance().getTime());
        newTask.setStatus("PENDING");
        
        isLoading.setValue(true);
        
        taskRepository.insertTask(newTask, new TaskRepository.TaskRepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long taskId) {
                Log.d(TAG, "Task created with ID: " + taskId);
                loadTasks(); // Reload tasks
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error creating task", e);
                errorMessage.setValue("Error creating task: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Update an existing task
     * 
     * @param task The task to update
     */
    public void updateTask(Task task) {
        if (task == null) {
            errorMessage.setValue("Cannot update a null task");
            return;
        }
        
        isLoading.setValue(true);
        
        taskRepository.updateTask(task, new TaskRepository.TaskRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Task updated successfully");
                loadTasks(); // Reload tasks
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error updating task", e);
                errorMessage.setValue("Error updating task: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Delete a task
     * 
     * @param task The task to delete
     */
    public void deleteTask(Task task) {
        if (task == null) {
            errorMessage.setValue("Cannot delete a null task");
            return;
        }
        
        isLoading.setValue(true);
        
        taskRepository.deleteTask(task, new TaskRepository.TaskRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Task deleted successfully");
                loadTasks(); // Reload tasks
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error deleting task", e);
                errorMessage.setValue("Error deleting task: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Mark a task as complete
     * 
     * @param task The task to mark complete
     */
    public void completeTask(Task task) {
        if (task == null) {
            errorMessage.setValue("Cannot complete a null task");
            return;
        }
        
        task.setStatus("COMPLETED");
        task.setCompletedTime(Calendar.getInstance().getTime());
        updateTask(task);
    }

    /**
     * Get tasks filtered by status
     * 
     * @param status The status to filter by (PENDING, COMPLETED, etc.)
     */
    public void getTasksByStatus(String status) {
        isLoading.setValue(true);
        
        taskRepository.getTasksByStatus(status, new TaskRepository.TaskRepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                tasksLiveData.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tasks by status", e);
                errorMessage.setValue("Error loading tasks: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Get tasks scheduled for execution within the given timeframe
     * 
     * @param startTime Start of timeframe
     * @param endTime End of timeframe
     */
    public void getTasksInTimeRange(Date startTime, Date endTime) {
        isLoading.setValue(true);
        
        taskRepository.getTasksInTimeRange(startTime, endTime, new TaskRepository.TaskRepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                tasksLiveData.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tasks in time range", e);
                errorMessage.setValue("Error loading tasks: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}
