package com.aiassistant.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.aiassistant.R;
import com.aiassistant.data.models.ActionSequence;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.data.models.TaskType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for creating new scheduled tasks that can execute autonomously.
 * Supports various task types including game automation, messaging, and app interactions.
 */
public class CreateTaskDialog extends DialogFragment {

    public interface TaskCreationListener {
        void onTaskCreated(ScheduledTask task);
    }

    private TaskCreationListener listener;
    private EditText taskNameInput;
    private Spinner taskTypeSpinner;
    private Spinner repeatSpinner;
    private TextView scheduledTimeText;
    private Button selectTimeButton;
    private EditText descriptionInput;
    private View targetAppLayout;
    private Spinner targetAppSpinner;
    private View actionSequenceLayout;
    private Button recordActionButton;
    private TextView recordedActionsText;

    private Calendar scheduledTime;
    private List<String> installedAppPackages;
    private ActionSequence recordedActions;
    
    public void setTaskCreationListener(TaskCreationListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_task, null);
        
        initViews(view);
        loadInstalledApps();
        setupInitialState();
        setupListeners();
        
        builder.setView(view)
                .setTitle(R.string.create_new_task)
                .setPositiveButton(R.string.create, null) // Set in onStart to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss());
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateInputs()) {
                    createTask();
                    dismiss();
                }
            });
        }
    }
    
    private void initViews(View view) {
        taskNameInput = view.findViewById(R.id.input_task_name);
        taskTypeSpinner = view.findViewById(R.id.spinner_task_type);
        repeatSpinner = view.findViewById(R.id.spinner_repeat);
        scheduledTimeText = view.findViewById(R.id.text_scheduled_time);
        selectTimeButton = view.findViewById(R.id.button_select_time);
        descriptionInput = view.findViewById(R.id.input_description);
        targetAppLayout = view.findViewById(R.id.layout_target_app);
        targetAppSpinner = view.findViewById(R.id.spinner_target_app);
        actionSequenceLayout = view.findViewById(R.id.layout_action_sequence);
        recordActionButton = view.findViewById(R.id.button_record_actions);
        recordedActionsText = view.findViewById(R.id.text_recorded_actions);
        
        // Initialize scheduledTime to current time + 1 hour
        scheduledTime = Calendar.getInstance();
        scheduledTime.add(Calendar.HOUR_OF_DAY, 1);
        updateScheduledTimeText();
        
        // Setup task type spinner
        ArrayAdapter<CharSequence> taskTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.task_types, android.R.layout.simple_spinner_item);
        taskTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTypeSpinner.setAdapter(taskTypeAdapter);
        
        // Setup repeat spinner
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.repeat_options, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);
    }
    
    private void setupInitialState() {
        // Check if we have a pre-selected task type from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("task_type")) {
            String taskType = args.getString("task_type");
            int position = getTaskTypePosition(taskType);
            if (position >= 0) {
                taskTypeSpinner.setSelection(position);
            }
        }
    }
    
    private void setupListeners() {
        selectTimeButton.setOnClickListener(v -> showDateTimePicker());
        
        taskTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLayoutVisibility(getTaskTypeFromPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        recordActionButton.setOnClickListener(v -> {
            showActionRecordingDialog();
        });
    }
    
    private void loadInstalledApps() {
        installedAppPackages = new ArrayList<>();
        List<String> appNames = new ArrayList<>();
        
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo app : apps) {
            // Only include non-system apps
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                installedAppPackages.add(app.packageName);
                String appName = pm.getApplicationLabel(app).toString();
                appNames.add(appName);
            }
        }
        
        ArrayAdapter<String> appAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, appNames);
        appAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetAppSpinner.setAdapter(appAdapter);
    }
    
    private void showDateTimePicker() {
        // Get current values
        final Calendar calendar = Calendar.getInstance();
        
        // Show date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // After date is set, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                
                                // Update the scheduled time
                                scheduledTime = calendar;
                                updateScheduledTimeText();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
                
        datePickerDialog.show();
    }
    
    private void updateScheduledTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault());
        scheduledTimeText.setText(sdf.format(scheduledTime.getTime()));
    }
    
    private void updateLayoutVisibility(TaskType taskType) {
        // Show/hide layouts based on task type
        switch (taskType) {
            case GAME_AUTOMATION:
            case APP_INTERACTION:
                targetAppLayout.setVisibility(View.VISIBLE);
                actionSequenceLayout.setVisibility(View.VISIBLE);
                break;
            case SEND_MESSAGE:
                targetAppLayout.setVisibility(View.VISIBLE);
                actionSequenceLayout.setVisibility(View.VISIBLE);
                break;
            case SYSTEM_ACTION:
                targetAppLayout.setVisibility(View.GONE);
                actionSequenceLayout.setVisibility(View.VISIBLE);
                break;
            default:
                targetAppLayout.setVisibility(View.GONE);
                actionSequenceLayout.setVisibility(View.GONE);
                break;
        }
    }
    
    private void showActionRecordingDialog() {
        // In a real implementation, this would start an activity for recording actions
        // For now, simulate recorded actions
        Toast.makeText(requireContext(), 
                R.string.action_recording_not_available, Toast.LENGTH_SHORT).show();
        
        // Simulate some recorded actions
        recordedActions = new ActionSequence();
        recordedActions.addAction("tap", "x=500,y=300");
        recordedActions.addAction("swipe", "start=300,400;end=300,100");
        recordedActions.addAction("wait", "2000");
        recordedActions.addAction("tap", "x=200,y=500");
        
        updateRecordedActionsText();
    }
    
    private void updateRecordedActionsText() {
        if (recordedActions != null && !recordedActions.getActions().isEmpty()) {
            recordedActionsText.setText(recordedActions.toString());
            recordedActionsText.setVisibility(View.VISIBLE);
        } else {
            recordedActionsText.setVisibility(View.GONE);
        }
    }
    
    private boolean validateInputs() {
        if (taskNameInput.getText().toString().trim().isEmpty()) {
            taskNameInput.setError(getString(R.string.error_task_name_required));
            return false;
        }
        
        TaskType taskType = getTaskTypeFromPosition(taskTypeSpinner.getSelectedItemPosition());
        if ((taskType == TaskType.GAME_AUTOMATION || 
             taskType == TaskType.APP_INTERACTION || 
             taskType == TaskType.SEND_MESSAGE) && 
            targetAppSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), 
                    R.string.error_select_target_app, Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (recordedActions == null || recordedActions.getActions().isEmpty()) {
            if (taskType != TaskType.CUSTOM) {
                Toast.makeText(requireContext(), 
                        R.string.error_record_actions, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        return true;
    }
    
    private void createTask() {
        String name = taskNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        TaskType taskType = getTaskTypeFromPosition(taskTypeSpinner.getSelectedItemPosition());
        String repeatPattern = repeatSpinner.getSelectedItem().toString();
        
        String targetApp = null;
        if (targetAppSpinner.getSelectedItemPosition() >= 0 && 
            targetAppSpinner.getSelectedItemPosition() < installedAppPackages.size()) {
            targetApp = installedAppPackages.get(targetAppSpinner.getSelectedItemPosition());
        }
        
        ScheduledTask task = new ScheduledTask(
                System.currentTimeMillis(), // ID
                name,
                description,
                taskType,
                scheduledTime.getTimeInMillis(),
                repeatPattern,
                targetApp,
                recordedActions,
                true // enabled by default
        );
        
        if (listener != null) {
            listener.onTaskCreated(task);
        }
    }
    
    private int getTaskTypePosition(String taskTypeName) {
        TaskType type = null;
        
        if ("Game Automation".equals(taskTypeName)) {
            type = TaskType.GAME_AUTOMATION;
        } else if ("Send Message".equals(taskTypeName)) {
            type = TaskType.SEND_MESSAGE;
        } else if ("App Interaction".equals(taskTypeName)) {
            type = TaskType.APP_INTERACTION;
        } else if ("System Action".equals(taskTypeName)) {
            type = TaskType.SYSTEM_ACTION;
        } else if ("Custom".equals(taskTypeName)) {
            type = TaskType.CUSTOM;
        }
        
        if (type != null) {
            return type.ordinal();
        }
        
        return -1;
    }
    
    private TaskType getTaskTypeFromPosition(int position) {
        TaskType[] types = TaskType.values();
        if (position >= 0 && position < types.length) {
            return types[position];
        }
        return TaskType.CUSTOM;
    }
}