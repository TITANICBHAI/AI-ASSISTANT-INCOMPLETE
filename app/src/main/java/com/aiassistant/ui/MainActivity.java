package com.aiassistant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.services.CallHandlingService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "CallHandlingPrefs";
    
    private AIStateManager aiStateManager;
    private Switch callHandlingSwitch;
    private Spinner userStatusSpinner;
    private Spinner responseModeSpinner;
    private Button testCallButton;
    
    private String[] requiredPermissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get AIStateManager instance
        aiStateManager = AIStateManager.getInstance();
        // Add button to launch call handling activity
        Button callHandlingButton = findViewById(R.id.call_handling_button);
        if (callHandlingButton != null) {
            callHandlingButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CallHandlingActivity.class);
                startActivity(intent);
            });
        }
        
        // Initialize UI components
        callHandlingSwitch = findViewById(R.id.call_handling_switch);
        userStatusSpinner = findViewById(R.id.user_status_spinner);
        responseModeSpinner = findViewById(R.id.response_mode_spinner);
        testCallButton = findViewById(R.id.test_call_button);
        
        // Set up user status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userStatusSpinner.setAdapter(statusAdapter);
        
        // Set up response mode spinner
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.response_mode_options, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        responseModeSpinner.setAdapter(modeAdapter);
        
        // Set up call handling switch listener
        callHandlingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CallHandlingService.setCallHandlingEnabled(this, isChecked);
            updateUIState();
        });
        
        // Set up user status spinner listener
        userStatusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getItemAtPosition(position).toString().toLowerCase();
                CallHandlingService.setUserStatus(MainActivity.this, status);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Set up response mode spinner listener
        responseModeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String mode = parent.getItemAtPosition(position).toString().toLowerCase();
                CallHandlingService.setResponseMode(MainActivity.this, mode);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Set up test call button
        testCallButton.setOnClickListener(v -> {
            testCallHandling();
        });
        
        // Check permissions
        checkPermissions();
        
        // Load saved settings
        loadSettings();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUIState();
    }
    
    /**
     * Check and request required permissions
     */
    private void checkPermissions() {
        boolean allPermissionsGranted = true;
        
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "All permissions are required for call handling", Toast.LENGTH_LONG).show();
            } else {
                // Permissions granted, now check if we need to set this app as the default
                // call screening app
                TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
                if (telecomManager != null && telecomManager.getDefaultDialerPackage() != null &&
                        !telecomManager.getDefaultDialerPackage().equals(getPackageName())) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                    startActivity(intent);
                }
            }
        }
    }
    
    /**
     * Load saved settings
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("call_handling_enabled", false);
        String status = prefs.getString("user_status", "unavailable");
        String mode = prefs.getString("response_mode", "normal");
        
        callHandlingSwitch.setChecked(enabled);
        
        // Set user status spinner position
        ArrayAdapter statusAdapter = (ArrayAdapter) userStatusSpinner.getAdapter();
        int statusPosition = statusAdapter.getPosition(status);
        if (statusPosition >= 0) {
            userStatusSpinner.setSelection(statusPosition);
        }
        
        // Set response mode spinner position
        ArrayAdapter modeAdapter = (ArrayAdapter) responseModeSpinner.getAdapter();
        int modePosition = modeAdapter.getPosition(mode);
        if (modePosition >= 0) {
            responseModeSpinner.setSelection(modePosition);
        }
    }
    
    /**
     * Update UI state based on current settings
     */
    private void updateUIState() {
        boolean enabled = callHandlingSwitch.isChecked();
        
        userStatusSpinner.setEnabled(enabled);
        responseModeSpinner.setEnabled(enabled);
        testCallButton.setEnabled(enabled);
    }
    
    /**
     * Test call handling
     */
    private void testCallHandling() {
        // TODO: Implement test call functionality
        Toast.makeText(this, "Test call feature not yet implemented", Toast.LENGTH_SHORT).show();
    }
}
