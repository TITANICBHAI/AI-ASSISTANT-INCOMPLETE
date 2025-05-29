package com.aiassistant.ui.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.speech.SpeechSynthesisManager;
import com.aiassistant.core.telephony.TelephonyManager;
import com.aiassistant.data.models.CallerInfo;
import com.aiassistant.services.CallHandlingService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Demo activity for call handling features
 */
public class CallHandlingDemoActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private Switch switchCallHandling;
    private RadioGroup radioGroupStatus;
    private RadioGroup radioGroupMode;
    private EditText editPhoneNumber;
    private Button buttonTestResponse;
    private TextView textRecentCalls;
    private Button buttonClearHistory;
    
    private TelephonyManager telephonyManager;
    private SpeechSynthesisManager speechSynthesisManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_handling_demo);
        
        // Initialize UI elements
        initializeUI();
        
        // Get managers from AIStateManager
        AIStateManager aiStateManager = AIStateManager.getInstance();
        telephonyManager = aiStateManager.getTelephonyManager();
        speechSynthesisManager = aiStateManager.getSpeechSynthesisManager();
        
        // Check permissions
        checkAndRequestPermissions();
        
        // Setup event handlers
        setupEventHandlers();
        
        // Update UI with current state
        updateUIWithCurrentState();
    }
    
    /**
     * Initialize UI elements
     */
    private void initializeUI() {
        switchCallHandling = findViewById(R.id.switch_call_handling);
        radioGroupStatus = findViewById(R.id.radio_group_status);
        radioGroupMode = findViewById(R.id.radio_group_mode);
        editPhoneNumber = findViewById(R.id.edit_phone_number);
        buttonTestResponse = findViewById(R.id.button_test_response);
        textRecentCalls = findViewById(R.id.text_recent_calls);
        buttonClearHistory = findViewById(R.id.button_clear_history);
    }
    
    /**
     * Check and request required permissions
     */
    private void checkAndRequestPermissions() {
        String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO
        };
        
        boolean needsPermissions = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needsPermissions = true;
                break;
            }
        }
        
        if (needsPermissions) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Enable/disable call handling
        switchCallHandling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (telephonyManager != null) {
                if (isChecked) {
                    enableCallHandling();
                } else {
                    telephonyManager.disableCallHandling();
                }
            }
        });
        
        // Handle status changes
        radioGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_unavailable) {
                setUserStatus("unavailable");
            } else if (checkedId == R.id.radio_busy) {
                setUserStatus("busy");
            } else if (checkedId == R.id.radio_driving) {
                setUserStatus("driving");
            } else if (checkedId == R.id.radio_sleeping) {
                setUserStatus("sleeping");
            }
        });
        
        // Handle mode changes
        radioGroupMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (telephonyManager != null) {
                if (checkedId == R.id.radio_mode_basic) {
                    telephonyManager.setCallResponseMode(TelephonyManager.AutoCallResponseMode.BASIC);
                } else if (checkedId == R.id.radio_mode_smart) {
                    telephonyManager.setCallResponseMode(TelephonyManager.AutoCallResponseMode.SMART);
                } else if (checkedId == R.id.radio_mode_custom) {
                    telephonyManager.setCallResponseMode(TelephonyManager.AutoCallResponseMode.CUSTOM);
                }
            }
        });
        
        // Test response button
        buttonTestResponse.setOnClickListener(v -> {
            String phoneNumber = editPhoneNumber.getText().toString();
            if (!TextUtils.isEmpty(phoneNumber)) {
                testCallResponse(phoneNumber);
            } else {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Clear history button
        buttonClearHistory.setOnClickListener(v -> {
            textRecentCalls.setText("No recent calls");
        });
    }
    
    /**
     * Enable call handling with the selected mode
     */
    private void enableCallHandling() {
        if (telephonyManager == null) {
            return;
        }
        
        // Get the selected mode
        TelephonyManager.AutoCallResponseMode mode = TelephonyManager.AutoCallResponseMode.SMART;
        
        int checkedModeId = radioGroupMode.getCheckedRadioButtonId();
        if (checkedModeId == R.id.radio_mode_basic) {
            mode = TelephonyManager.AutoCallResponseMode.BASIC;
        } else if (checkedModeId == R.id.radio_mode_smart) {
            mode = TelephonyManager.AutoCallResponseMode.SMART;
        } else if (checkedModeId == R.id.radio_mode_custom) {
            mode = TelephonyManager.AutoCallResponseMode.CUSTOM;
        }
        
        // Enable call handling
        telephonyManager.enableCallHandling(mode);
        
        // Set initial status
        int checkedStatusId = radioGroupStatus.getCheckedRadioButtonId();
        if (checkedStatusId == R.id.radio_unavailable) {
            setUserStatus("unavailable");
        } else if (checkedStatusId == R.id.radio_busy) {
            setUserStatus("busy");
        } else if (checkedStatusId == R.id.radio_driving) {
            setUserStatus("driving");
        } else if (checkedStatusId == R.id.radio_sleeping) {
            setUserStatus("sleeping");
        }
    }
    
    /**
     * Set user status
     */
    private void setUserStatus(String status) {
        if (telephonyManager != null) {
            telephonyManager.setUserStatus(status);
        }
    }
    
    /**
     * Test call response
     */
    private void testCallResponse(String phoneNumber) {
        if (telephonyManager == null || speechSynthesisManager == null) {
            Toast.makeText(this, "Telephony manager not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get the current user status
        String userStatus = "unavailable";
        int checkedStatusId = radioGroupStatus.getCheckedRadioButtonId();
        if (checkedStatusId == R.id.radio_unavailable) {
            userStatus = "unavailable";
        } else if (checkedStatusId == R.id.radio_busy) {
            userStatus = "busy";
        } else if (checkedStatusId == R.id.radio_driving) {
            userStatus = "driving";
        } else if (checkedStatusId == R.id.radio_sleeping) {
            userStatus = "sleeping";
        }
        
        // Create a caller info
        CallerInfo callerInfo = new CallerInfo(phoneNumber, "Test Caller");
        
        // Get the response template
        String responseTemplate = telephonyManager.getResponseTemplate(userStatus, callerInfo);
        
        // Format the response
        String userName = "User"; // Replace with actual user name
        String response = String.format(responseTemplate, userName, userName);
        
        // Speak the response
        speechSynthesisManager.speak(response, false, false);
        
        // Add to test call log
        addTestCallToLog(phoneNumber, userStatus);
    }
    
    /**
     * Add test call to log
     */
    private void addTestCallToLog(String phoneNumber, String userStatus) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date());
        
        String existingLog = textRecentCalls.getText().toString();
        
        if (existingLog.equals("No recent calls")) {
            existingLog = "";
        }
        
        String newEntry = formattedTime + " - " + phoneNumber + " (Test) - Status: " + userStatus;
        
        if (existingLog.isEmpty()) {
            textRecentCalls.setText(newEntry);
        } else {
            textRecentCalls.setText(newEntry + "\n\n" + existingLog);
        }
    }
    
    /**
     * Update UI with current state
     */
    private void updateUIWithCurrentState() {
        if (telephonyManager != null) {
            // Update call handling switch
            switchCallHandling.setChecked(telephonyManager.isCallHandlingEnabled());
            
            // Update call response mode
            TelephonyManager.AutoCallResponseMode mode = telephonyManager.getCallResponseMode();
            switch (mode) {
                case BASIC:
                    radioGroupMode.check(R.id.radio_mode_basic);
                    break;
                case SMART:
                    radioGroupMode.check(R.id.radio_mode_smart);
                    break;
                case CUSTOM:
                    radioGroupMode.check(R.id.radio_mode_custom);
                    break;
            }
            
            // Update recent calls
            updateRecentCallsList();
        }
    }
    
    /**
     * Update recent calls list
     */
    private void updateRecentCallsList() {
        if (telephonyManager == null) {
            return;
        }
        
        List<TelephonyManager.RecentCall> recentCalls = telephonyManager.getRecentCalls();
        
        if (recentCalls.isEmpty()) {
            textRecentCalls.setText("No recent calls");
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        
        for (TelephonyManager.RecentCall call : recentCalls) {
            String formattedTime = sdf.format(new Date(call.getTimestamp()));
            
            sb.append(formattedTime)
              .append(" - ")
              .append(call.getCallerName())
              .append(" (")
              .append(call.getPhoneNumber())
              .append(") - Status: ")
              .append(call.getUserStatus())
              .append("\n\n");
        }
        
        textRecentCalls.setText(sb.toString().trim());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithCurrentState();
    }
}
