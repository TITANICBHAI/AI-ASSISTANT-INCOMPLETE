package com.aiassistant.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aiassistant.R;
import com.aiassistant.services.CallHandlingService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CallHandlingActivity extends AppCompatActivity {

    private Switch callHandlingSwitch;
    private RadioGroup statusRadioGroup;
    private RadioGroup responseStyleRadioGroup;
    private Button testCallButton;
    private TextView testCallResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_handling);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize UI components
        callHandlingSwitch = findViewById(R.id.call_handling_switch);
        statusRadioGroup = findViewById(R.id.status_radio_group);
        responseStyleRadioGroup = findViewById(R.id.response_style_radio_group);
        testCallButton = findViewById(R.id.test_call_button);
        testCallResult = findViewById(R.id.test_call_result);
        
        // Set up floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Toast.makeText(this, "Call handling settings saved", Toast.LENGTH_SHORT).show();
            saveSettings();
        });
        
        // Set up call handling switch
        callHandlingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CallHandlingService.setCallHandlingEnabled(this, isChecked);
            updateUIState();
        });
        
        // Set up status radio group
        statusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String status = getStatusFromCheckedId(checkedId);
            CallHandlingService.setUserStatus(this, status);
        });
        
        // Set up response style radio group
        responseStyleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String style = getStyleFromCheckedId(checkedId);
            CallHandlingService.setResponseMode(this, style);
        });
        
        // Set up test call button
        testCallButton.setOnClickListener(v -> {
            simulateIncomingCall();
        });
        
        // Load saved settings
        loadSettings();
    }
    
    private void updateUIState() {
        boolean enabled = callHandlingSwitch.isChecked();
        
        // Enable/disable UI components based on switch state
        for (int i = 0; i < statusRadioGroup.getChildCount(); i++) {
            statusRadioGroup.getChildAt(i).setEnabled(enabled);
        }
        
        for (int i = 0; i < responseStyleRadioGroup.getChildCount(); i++) {
            responseStyleRadioGroup.getChildAt(i).setEnabled(enabled);
        }
        
        testCallButton.setEnabled(enabled);
    }
    
    private String getStatusFromCheckedId(int checkedId) {
        if (checkedId == R.id.status_busy) {
            return "busy";
        } else if (checkedId == R.id.status_driving) {
            return "driving";
        } else if (checkedId == R.id.status_sleeping) {
            return "sleeping";
        } else {
            return "unavailable";
        }
    }
    
    private String getStyleFromCheckedId(int checkedId) {
        if (checkedId == R.id.style_formal) {
            return "formal";
        } else if (checkedId == R.id.style_friendly) {
            return "friendly";
        } else {
            return "normal";
        }
    }
    
    private void loadSettings() {
        // Load call handling enabled state
        boolean enabled = CallHandlingService.isCallHandlingEnabled(this);
        callHandlingSwitch.setChecked(enabled);
        
        // Load user status
        String status = CallHandlingService.getUserStatus(this);
        setStatusRadioButton(status);
        
        // Load response style
        String style = CallHandlingService.getResponseMode(this);
        setStyleRadioButton(style);
        
        // Update UI state
        updateUIState();
    }
    
    private void setStatusRadioButton(String status) {
        if (status.equals("busy")) {
            statusRadioGroup.check(R.id.status_busy);
        } else if (status.equals("driving")) {
            statusRadioGroup.check(R.id.status_driving);
        } else if (status.equals("sleeping")) {
            statusRadioGroup.check(R.id.status_sleeping);
        } else {
            statusRadioGroup.check(R.id.status_unavailable);
        }
    }
    
    private void setStyleRadioButton(String style) {
        if (style.equals("formal")) {
            responseStyleRadioGroup.check(R.id.style_formal);
        } else if (style.equals("friendly")) {
            responseStyleRadioGroup.check(R.id.style_friendly);
        } else {
            responseStyleRadioGroup.check(R.id.style_normal);
        }
    }
    
    private void saveSettings() {
        // Save call handling enabled state
        CallHandlingService.setCallHandlingEnabled(this, callHandlingSwitch.isChecked());
        
        // Save user status
        int statusId = statusRadioGroup.getCheckedRadioButtonId();
        String status = getStatusFromCheckedId(statusId);
        CallHandlingService.setUserStatus(this, status);
        
        // Save response style
        int styleId = responseStyleRadioGroup.getCheckedRadioButtonId();
        String style = getStyleFromCheckedId(styleId);
        CallHandlingService.setResponseMode(this, style);
    }
    
    private void simulateIncomingCall() {
        // Show a loading indicator
        testCallButton.setEnabled(false);
        testCallResult.setText("Simulating incoming call...");
        testCallResult.setVisibility(View.VISIBLE);
        
        // In a real implementation, we would use TelecomManager to create a test call
        // For now, just simulate with a delay
        testCallResult.postDelayed(() -> {
            testCallResult.setText("Test call completed. The AI successfully answered the call and responded based on your settings.");
            testCallButton.setEnabled(true);
        }, 3000);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
