package com.aiassistant.ui.demo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.neural.NeuralNetworkManager;

public class NeuralNetworkDemoActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private NeuralNetworkManager neuralNetworkManager;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_network_demo);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        neuralNetworkManager = aiStateManager.getNeuralNetworkManager();
        
        // Initialize UI
        statusTextView = findViewById(R.id.statusTextView);
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        if (neuralNetworkManager != null && neuralNetworkManager.isInitialized()) {
            statusTextView.setText("Neural network system is operational");
        } else {
            statusTextView.setText("Neural network system is not initialized");
        }
    }
}
