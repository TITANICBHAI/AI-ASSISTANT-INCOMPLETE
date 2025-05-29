package com.aiassistant.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.aiassistant.R;

/**
 * Generic confirmation dialog that shows a message and provides confirm/cancel options.
 * Used for dangerous operations requiring user confirmation.
 */
public class ConfirmationDialog extends DialogFragment {

    private final String message;
    private final Runnable onConfirmAction;
    private final Runnable onCancelAction;

    /**
     * Creates a confirmation dialog with the given message and confirm action.
     * 
     * @param message The warning or confirmation message to display
     * @param onConfirmAction Action to execute when user confirms
     */
    public ConfirmationDialog(String message, Runnable onConfirmAction) {
        this(message, onConfirmAction, null);
    }

    /**
     * Creates a confirmation dialog with the given message, confirm and cancel actions.
     * 
     * @param message The warning or confirmation message to display
     * @param onConfirmAction Action to execute when user confirms
     * @param onCancelAction Action to execute when user cancels
     */
    public ConfirmationDialog(String message, Runnable onConfirmAction, Runnable onCancelAction) {
        this.message = message;
        this.onConfirmAction = onConfirmAction;
        this.onCancelAction = onCancelAction;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_confirmation, null);
        
        TextView messageText = view.findViewById(R.id.text_dialog_message);
        Button confirmButton = view.findViewById(R.id.button_confirm);
        Button cancelButton = view.findViewById(R.id.button_cancel);
        
        messageText.setText(message);
        
        builder.setView(view);
        
        AlertDialog dialog = builder.create();
        
        confirmButton.setOnClickListener(v -> {
            if (onConfirmAction != null) {
                onConfirmAction.run();
            }
            dialog.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> {
            if (onCancelAction != null) {
                onCancelAction.run();
            }
            dialog.dismiss();
        });
        
        return dialog;
    }
}