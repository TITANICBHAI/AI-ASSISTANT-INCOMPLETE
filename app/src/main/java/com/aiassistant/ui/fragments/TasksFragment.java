package com.aiassistant.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.core.AIAssistantApplication;
import com.aiassistant.core.AdaptiveInteractionController;
import com.aiassistant.core.ErrorResolutionWorkflow;
import com.aiassistant.utils.Constants;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that displays information about tasks and errors.
 * Shows error statistics, recent errors, and allows error resolution.
 */
public class TasksFragment extends Fragment {
    private static final String TAG = "TasksFragment";

    // UI components
    private TextView totalErrorsText;
    private TextView gestureErrorsText;
    private TextView uiDetectionErrorsText;
    private TextView gameStateErrorsText;
    private RecyclerView errorsRecyclerView;
    private Button clearErrorsButton;

    // Error adapter
    private ErrorAdapter errorAdapter;

    // Status receiver
    private BroadcastReceiver errorReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialize UI components
        totalErrorsText = view.findViewById(R.id.total_errors_count);
        gestureErrorsText = view.findViewById(R.id.gesture_errors_count);
        uiDetectionErrorsText = view.findViewById(R.id.ui_detection_errors_count);
        gameStateErrorsText = view.findViewById(R.id.game_state_errors_count);
        errorsRecyclerView = view.findViewById(R.id.errors_recycler_view);
        clearErrorsButton = view.findViewById(R.id.clear_errors_button);

        // Set up recycler view
        errorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        errorAdapter = new ErrorAdapter(new ArrayList<>());
        errorsRecyclerView.setAdapter(errorAdapter);

        // Set up clear button
        clearErrorsButton.setOnClickListener(v -> clearErrors());

        // Set up error receiver
        setupErrorReceiver();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateErrorStats();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                errorReceiver,
                new IntentFilter(Constants.ACTION_ERROR_REPORTED)
        );

        // Update UI with latest errors
        updateErrorStats();
    }

    @Override
    public void onPause() {
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(errorReceiver);

        super.onPause();
    }

    /**
     * Sets up the error update receiver
     */
    private void setupErrorReceiver() {
        errorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateErrorStats();
            }
        };
    }

    /**
     * Updates the error statistics in the UI
     */
    private void updateErrorStats() {
        AIAssistantApplication app = AIAssistantApplication.getInstance();
        ErrorResolutionWorkflow errorWorkflow = app.getErrorResolution();

        // Update error counts
        int totalErrors = errorWorkflow.getTotalErrorCount();
        int gestureErrors = errorWorkflow.getErrorCount(ErrorResolutionWorkflow.ErrorCategory.GESTURE_RECOGNITION_FAILURE);
        int uiErrors = errorWorkflow.getErrorCount(ErrorResolutionWorkflow.ErrorCategory.UI_DETECTION_FAILURE);
        int gameStateErrors = errorWorkflow.getErrorCount(ErrorResolutionWorkflow.ErrorCategory.GAME_STATE_INFERENCE_FAILURE);

        totalErrorsText.setText(String.valueOf(totalErrors));
        gestureErrorsText.setText(String.valueOf(gestureErrors));
        uiDetectionErrorsText.setText(String.valueOf(uiErrors));
        gameStateErrorsText.setText(String.valueOf(gameStateErrors));

        // Update recent errors
        List<String> recentErrors = errorWorkflow.getRecentErrors();
        errorAdapter.updateErrors(recentErrors);
    }

    /**
     * Clears all error statistics
     */
    private void clearErrors() {
        AIAssistantApplication app = AIAssistantApplication.getInstance();
        app.getErrorResolution().clearErrorCounts();
        updateErrorStats();
    }

    /**
     * Adapter for error messages recycler view
     */
    private static class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder> {
        private List<String> errors;

        ErrorAdapter(List<String> errors) {
            this.errors = errors;
        }

        void updateErrors(List<String> newErrors) {
            this.errors = newErrors;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_error, parent, false);
            return new ErrorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ErrorViewHolder holder, int position) {
            holder.bind(errors.get(position));
        }

        @Override
        public int getItemCount() {
            return errors.size();
        }

        static class ErrorViewHolder extends RecyclerView.ViewHolder {
            private TextView errorText;
            private MaterialCardView cardView;

            ErrorViewHolder(@NonNull View itemView) {
                super(itemView);
                errorText = itemView.findViewById(R.id.error_text);
                cardView = itemView.findViewById(R.id.error_card);
            }

            void bind(String error) {
                errorText.setText(error);

                // Highlight different error types with different colors
                int colorResId;
                if (error.contains("GESTURE_RECOGNITION_FAILURE")) {
                    colorResId = R.color.error_gesture;
                } else if (error.contains("UI_DETECTION_FAILURE")) {
                    colorResId = R.color.error_ui;
                } else if (error.contains("GAME_STATE_INFERENCE_FAILURE")) {
                    colorResId = R.color.error_game_state;
                } else if (error.contains("SYSTEM_RESOURCE_LIMITATION")) {
                    colorResId = R.color.error_system;
                } else {
                    colorResId = R.color.error_unknown;
                }

                cardView.setStrokeColor(itemView.getContext().getResources().getColor(colorResId, null));
            }
        }
    }
}
