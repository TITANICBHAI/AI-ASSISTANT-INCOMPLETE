package com.aiassistant.ui.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for demonstrating the Google Duplex-like calling capabilities
 */
public class DuplexCallDemoActivity extends AppCompatActivity implements TelephonyManager.CallEventListener {
    private static final String TAG = "DuplexCallDemo";
    private static final int REQUEST_CALL_PERMISSION = 100;
    
    // UI elements
    private Spinner scriptTypeSpinner;
    private EditText phoneNumberEditText;
    private EditText businessNameEditText;
    private EditText userNameEditText;
    private EditText serviceDetailsEditText;
    private Button makeCallButton;
    private Button endCallButton;
    private TextView callStatusTextView;
    
    // Components
    private TelephonyManager telephonyManager;
    private String selectedScriptType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplex_call_demo);
        
        // Initialize UI elements
        scriptTypeSpinner = findViewById(R.id.scriptTypeSpinner);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        businessNameEditText = findViewById(R.id.businessNameEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        serviceDetailsEditText = findViewById(R.id.serviceDetailsEditText);
        makeCallButton = findViewById(R.id.makeCallButton);
        endCallButton = findViewById(R.id.endCallButton);
        callStatusTextView = findViewById(R.id.callStatusTextView);
        
        // Get telephony manager from AI state manager
        AIStateManager aiStateManager = AIStateManager.getInstance();
        if (aiStateManager != null && aiStateManager.isInitialized()) {
            telephonyManager = aiStateManager.getTelephonyManager();
        }
        
        if (telephonyManager == null) {
            // Create a local instance if not available from AI state manager
            telephonyManager = new TelephonyManager(this);
        }
        
        // Add call event listener
        telephonyManager.addCallEventListener(this);
        
        // Set up script type spinner
        setupScriptTypeSpinner();
        
        // Set up button click listeners
        makeCallButton.setOnClickListener(v -> makeDuplexCall());
        endCallButton.setOnClickListener(v -> endCall());
        
        // Check call permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
        
        // Update UI state
        updateUIState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Register call event listener
        if (telephonyManager != null) {
            telephonyManager.addCallEventListener(this);
        }
        
        // Update UI state
        updateUIState();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Unregister call event listener
        if (telephonyManager != null) {
            telephonyManager.removeCallEventListener(this);
        }
    }
    
    /**
     * Set up the script type spinner
     */
    private void setupScriptTypeSpinner() {
        List<String> scriptTypes = telephonyManager.getAvailableScriptTypes();
        
        if (scriptTypes.isEmpty()) {
            // Add default script types if none available
            scriptTypes.add("restaurant_reservation");
            scriptTypes.add("salon_appointment");
        }
        
        // Create adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, scriptTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set adapter
        scriptTypeSpinner.setAdapter(adapter);
        
        // Set selection listener
        scriptTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedScriptType = (String) parent.getItemAtPosition(position);
                updateServiceDetailsHint();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedScriptType = null;
            }
        });
        
        // Set initial selection
        if (!scriptTypes.isEmpty()) {
            selectedScriptType = scriptTypes.get(0);
            updateServiceDetailsHint();
        }
    }
    
    /**
     * Update service details hint based on selected script type
     */
    private void updateServiceDetailsHint() {
        if ("restaurant_reservation".equals(selectedScriptType)) {
            serviceDetailsEditText.setHint("Enter details (e.g., '4 people, tonight at 7:30 PM')");
        } else if ("salon_appointment".equals(selectedScriptType)) {
            serviceDetailsEditText.setHint("Enter details (e.g., 'haircut, Friday at 2 PM')");
        } else {
            serviceDetailsEditText.setHint("Enter service details");
        }
    }
    
    /**
     * Make a duplex call
     */
    private void makeDuplexCall() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedScriptType == null) {
            Toast.makeText(this, "Please select a script type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Prepare parameters
        Map<String, String> parameters = new HashMap<>();
        
        String businessName = businessNameEditText.getText().toString().trim();
        if (!businessName.isEmpty()) {
            parameters.put("business_name", businessName);
            parameters.put("restaurant_name", businessName);
            parameters.put("salon_name", businessName);
        }
        
        String userName = userNameEditText.getText().toString().trim();
        if (!userName.isEmpty()) {
            parameters.put("user_name", userName);
        }
        
        String serviceDetails = serviceDetailsEditText.getText().toString().trim();
        if (!serviceDetails.isEmpty()) {
            // Parse service details based on script type
            if ("restaurant_reservation".equals(selectedScriptType)) {
                // Try to parse restaurant booking details
                if (serviceDetails.contains("people") && serviceDetails.contains("at")) {
                    try {
                        String partySizeStr = serviceDetails.split("people")[0].trim();
                        String dateTimeStr = serviceDetails.split("at")[1].trim();
                        
                        parameters.put("party_size", partySizeStr);
                        
                        if (dateTimeStr.contains(" ")) {
                            String dateStr = "today";
                            String timeStr = dateTimeStr;
                            
                            if (dateTimeStr.contains("on")) {
                                dateStr = dateTimeStr.split("on")[1].trim();
                                timeStr = dateTimeStr.split("on")[0].trim();
                            } else if (dateTimeStr.toLowerCase().contains("tonight")) {
                                dateStr = "tonight";
                                timeStr = dateTimeStr.replace("tonight", "").trim();
                            }
                            
                            parameters.put("date", dateStr);
                            parameters.put("time", timeStr);
                        } else {
                            parameters.put("time", dateTimeStr);
                        }
                    } catch (Exception e) {
                        // If parsing fails, just use as-is
                        parameters.put("service_details", serviceDetails);
                    }
                } else {
                    parameters.put("service_details", serviceDetails);
                }
            } else if ("salon_appointment".equals(selectedScriptType)) {
                // Try to parse salon appointment details
                if (serviceDetails.contains(",")) {
                    try {
                        String serviceType = serviceDetails.split(",")[0].trim();
                        String dateTimeStr = serviceDetails.split(",")[1].trim();
                        
                        parameters.put("service_type", serviceType);
                        
                        if (dateTimeStr.contains(" at ")) {
                            String dateStr = dateTimeStr.split(" at ")[0].trim();
                            String timeStr = dateTimeStr.split(" at ")[1].trim();
                            
                            parameters.put("date", dateStr);
                            parameters.put("time", timeStr);
                        } else {
                            parameters.put("date_time", dateTimeStr);
                        }
                    } catch (Exception e) {
                        // If parsing fails, just use as-is
                        parameters.put("service_details", serviceDetails);
                    }
                } else {
                    parameters.put("service_details", serviceDetails);
                }
            } else {
                parameters.put("service_details", serviceDetails);
            }
        }
        
        // Make the duplex call
        boolean success = telephonyManager.makeDuplexBusinessCall(phoneNumber, selectedScriptType, parameters);
        
        if (success) {
            Toast.makeText(this, "Making Duplex call...", Toast.LENGTH_SHORT).show();
            callStatusTextView.setText("Calling " + phoneNumber + "...");
        } else {
            Toast.makeText(this, "Failed to make call", Toast.LENGTH_SHORT).show();
        }
        
        // Update UI state
        updateUIState();
    }
    
    /**
     * End the current call
     */
    private void endCall() {
        if (telephonyManager != null && telephonyManager.isInCall()) {
            boolean success = telephonyManager.endCall();
            
            if (success) {
                Toast.makeText(this, "Ending call", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to end call", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Update UI state based on call status
     */
    private void updateUIState() {
        boolean inCall = telephonyManager != null && telephonyManager.isInCall();
        boolean isDuplexCall = telephonyManager != null && telephonyManager.isDuplexCallActive();
        
        makeCallButton.setEnabled(!inCall);
        endCallButton.setEnabled(inCall);
        
        if (inCall) {
            String phoneNumber = telephonyManager.getCurrentPhoneNumber();
            if (isDuplexCall) {
                callStatusTextView.setText("Duplex call in progress with " + phoneNumber);
            } else {
                callStatusTextView.setText("Call in progress with " + phoneNumber);
            }
        } else {
            callStatusTextView.setText("No call in progress");
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission denied, cannot make calls", Toast.LENGTH_LONG).show();
                makeCallButton.setEnabled(false);
            }
        }
    }
    
    @Override
    public void onIncomingCall(String phoneNumber) {
        runOnUiThread(() -> {
            callStatusTextView.setText("Incoming call from " + phoneNumber);
            updateUIState();
        });
    }
    
    @Override
    public void onCallStarted(String phoneNumber) {
        runOnUiThread(() -> {
            callStatusTextView.setText("Call started with " + phoneNumber);
            updateUIState();
        });
    }
    
    @Override
    public void onCallEnded(String phoneNumber) {
        runOnUiThread(() -> {
            callStatusTextView.setText("Call ended with " + phoneNumber);
            updateUIState();
        });
    }
}
