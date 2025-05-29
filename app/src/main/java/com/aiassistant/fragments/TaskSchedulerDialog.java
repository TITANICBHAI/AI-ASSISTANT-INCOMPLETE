package com.aiassistant.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aiassistant.R;
import com.aiassistant.models.ScheduledTask;
import com.aiassistant.scheduler.AdvancedTaskScheduler;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for scheduling automated tasks
 */
public class TaskSchedulerDialog extends DialogFragment {

    private static final String TAG = "TaskSchedulerDialog";

    // UI Components
    private TextInputEditText etTaskName;
    private TextInputEditText etTaskDescription;
    private RadioGroup rgTaskType;
    private RadioButton rbAppAutomation;
    private RadioButton rbMessageSend;
    private RadioButton rbGamePlay;
    private RadioButton rbCustomAction;
    private LinearLayout llAppSelection;
    private Spinner spinnerAppSelection;
    private Button btnDate;
    private Button btnTime;
    private TextView tvSelectedDateTime;
    private CheckBox cbRepeat;
    private LinearLayout llRepeatOptions;
    private RadioGroup rgRepeatType;
    private RadioButton rbDaily;
    private RadioButton rbWeekly;
    private RadioButton rbCustom;
    private LinearLayout llCustomRepeat;
    private EditText etRepeatInterval;
    private Spinner spinnerIntervalUnit;
    private Button btnCancel;
    private Button btnSave;

    // Data
    private Calendar selectedDateTime;
    private List<ApplicationInfo> installedApps;
    private AdvancedTaskScheduler taskScheduler;
    private TaskSchedulerListener listener;

    // Interface for callback
    public interface TaskSchedulerListener {
        void onTaskScheduled(ScheduledTask task);
    }

    public static TaskSchedulerDialog newInstance() {
        return new TaskSchedulerDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TaskSchedulerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TaskSchedulerListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.task_scheduler_title);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_task_scheduler, container, false);
        taskScheduler = new AdvancedTaskScheduler(requireContext());
        selectedDateTime = Calendar.getInstance();
        
        // Add 1 hour to the current time for default selection
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
        
        initViews(view);
        setupListeners();
        loadInstalledApps();
        updateDateTimeDisplay();
        
        return view;
    }

    private void initViews(View view) {
        // Initialize all UI components
        etTaskName = view.findViewById(R.id.et_task_name);
        etTaskDescription = view.findViewById(R.id.et_task_description);
        rgTaskType = view.findViewById(R.id.rg_task_type);
        rbAppAutomation = view.findViewById(R.id.rb_app_automation);
        rbMessageSend = view.findViewById(R.id.rb_message_send);
        rbGamePlay = view.findViewById(R.id.rb_game_play);
        rbCustomAction = view.findViewById(R.id.rb_custom_action);
        llAppSelection = view.findViewById(R.id.ll_app_selection);
        spinnerAppSelection = view.findViewById(R.id.spinner_app_selection);
        btnDate = view.findViewById(R.id.btn_date);
        btnTime = view.findViewById(R.id.btn_time);
        tvSelectedDateTime = view.findViewById(R.id.tv_selected_datetime);
        cbRepeat = view.findViewById(R.id.cb_repeat);
        llRepeatOptions = view.findViewById(R.id.ll_repeat_options);
        rgRepeatType = view.findViewById(R.id.rg_repeat_type);
        rbDaily = view.findViewById(R.id.rb_daily);
        rbWeekly = view.findViewById(R.id.rb_weekly);
        rbCustom = view.findViewById(R.id.rb_custom);
        llCustomRepeat = view.findViewById(R.id.ll_custom_repeat);
        etRepeatInterval = view.findViewById(R.id.et_repeat_interval);
        spinnerIntervalUnit = view.findViewById(R.id.spinner_interval_unit);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);

        // Set up interval units spinner
        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.interval_units,
                android.R.layout.simple_spinner_item);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntervalUnit.setAdapter(intervalAdapter);
    }

    private void setupListeners() {
        // Task type selection
        rgTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_app_automation || checkedId == R.id.rb_game_play) {
                llAppSelection.setVisibility(View.VISIBLE);
            } else {
                llAppSelection.setVisibility(View.GONE);
            }
        });

        // Date and time selection
        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        // Repeat options
        cbRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            llRepeatOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            llCustomRepeat.setVisibility(checkedId == R.id.rb_custom ? View.VISIBLE : View.GONE);
        });

        // Buttons
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        boolean is24HourFormat = DateFormat.is24HourFormat(requireContext());
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                is24HourFormat
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDateTime.getTime());
        tvSelectedDateTime.setText(formattedDate);
    }

    private void loadInstalledApps() {
        // Get list of installed apps
        PackageManager packageManager = requireContext().getPackageManager();
        installedApps = new ArrayList<>();
        
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : apps) {
            // Only include apps that are launchable
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                installedApps.add(appInfo);
            }
        }
        
        // Sort apps by name
        Collections.sort(installedApps, (app1, app2) -> {
            String label1 = packageManager.getApplicationLabel(app1).toString();
            String label2 = packageManager.getApplicationLabel(app2).toString();
            return label1.compareToIgnoreCase(label2);
        });
        
        // Create adapter for app selection spinner
        List<String> appNames = new ArrayList<>();
        for (ApplicationInfo app : installedApps) {
            appNames.add(packageManager.getApplicationLabel(app).toString());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                appNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAppSelection.setAdapter(adapter);
    }

    private void saveTask() {
        String taskName = etTaskName.getText().toString().trim();
        String taskDescription = etTaskDescription.getText().toString().trim();
        
        // Validation
        if (taskName.isEmpty()) {
            etTaskName.setError(getString(R.string.error_task_name_required));
            return;
        }
        
        // Get task type
        int taskType;
        if (rbAppAutomation.isChecked()) {
            taskType = ScheduledTask.TYPE_APP_AUTOMATION;
        } else if (rbMessageSend.isChecked()) {
            taskType = ScheduledTask.TYPE_MESSAGE_SEND;
        } else if (rbGamePlay.isChecked()) {
            taskType = ScheduledTask.TYPE_GAME_PLAY;
        } else {
            taskType = ScheduledTask.TYPE_CUSTOM_ACTION;
        }
        
        // Get target app if applicable
        String targetPackage = null;
        if (taskType == ScheduledTask.TYPE_APP_AUTOMATION || 
            taskType == ScheduledTask.TYPE_GAME_PLAY) {
            int selectedAppPosition = spinnerAppSelection.getSelectedItemPosition();
            if (selectedAppPosition >= 0 && selectedAppPosition < installedApps.size()) {
                targetPackage = installedApps.get(selectedAppPosition).packageName;
            } else {
                Toast.makeText(requireContext(), R.string.error_select_app, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Ensure selected time is in the future
        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(), R.string.error_future_time, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create the scheduled task
        ScheduledTask task = new ScheduledTask();
        task.setName(taskName);
        task.setDescription(taskDescription);
        task.setTaskType(taskType);
        task.setTargetPackage(targetPackage);
        task.setScheduledTime(selectedDateTime.getTimeInMillis());
        
        // Handle repeat options
        if (cbRepeat.isChecked()) {
            task.setRepeating(true);
            
            if (rbDaily.isChecked()) {
                task.setRepeatInterval(24 * 60 * 60 * 1000); // 24 hours in milliseconds
            } else if (rbWeekly.isChecked()) {
                task.setRepeatInterval(7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds
            } else if (rbCustom.isChecked()) {
                String intervalStr = etRepeatInterval.getText().toString().trim();
                if (intervalStr.isEmpty()) {
                    etRepeatInterval.setError(getString(R.string.error_interval_required));
                    return;
                }
                
                int interval = Integer.parseInt(intervalStr);
                int unitPosition = spinnerIntervalUnit.getSelectedItemPosition();
                long multiplier;
                
                switch (unitPosition) {
                    case 0: // Minutes
                        multiplier = 60 * 1000;
                        break;
                    case 1: // Hours
                        multiplier = 60 * 60 * 1000;
                        break;
                    case 2: // Days
                        multiplier = 24 * 60 * 60 * 1000;
                        break;
                    case 3: // Weeks
                        multiplier = 7 * 24 * 60 * 60 * 1000;
                        break;
                    default:
                        multiplier = 60 * 60 * 1000; // Default to hours
                }
                
                task.setRepeatInterval(interval * multiplier);
            }
        } else {
            task.setRepeating(false);
        }
        
        // Schedule the task
        long taskId = taskScheduler.scheduleTask(task);
        task.setId(taskId);
        
        // Notify listener and dismiss dialog
        listener.onTaskScheduled(task);
        dismiss();
        
        // Show confirmation toast
        Toast.makeText(requireContext(), R.string.task_scheduled, Toast.LENGTH_SHORT).show();
    }
}