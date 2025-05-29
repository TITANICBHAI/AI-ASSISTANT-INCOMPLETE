package com.aiassistant.ui.external;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.external.PDFLearningManager;
import com.aiassistant.core.external.business.BusinessNegotiationEngine;

public class AdvancedFeaturesActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private PDFLearningManager pdfLearningManager;
    private BusinessNegotiationEngine businessNegotiationEngine;
    private TextView pdfStatusTextView;
    private TextView businessStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_features);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        pdfLearningManager = aiStateManager.getPDFLearningManager();
        businessNegotiationEngine = aiStateManager.getBusinessNegotiationEngine();
        
        // Initialize UI
        pdfStatusTextView = findViewById(R.id.pdfStatusTextView);
        businessStatusTextView = findViewById(R.id.businessStatusTextView);
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        // PDF Learning status
        if (pdfLearningManager != null && pdfLearningManager.isInitialized()) {
            pdfStatusTextView.setText("PDF Learning system is operational");
        } else {
            pdfStatusTextView.setText("PDF Learning system is not initialized");
        }
        
        // Business Negotiation status
        if (businessNegotiationEngine != null && businessNegotiationEngine.isInitialized()) {
            businessStatusTextView.setText("Business Negotiation engine is operational");
        } else {
            businessStatusTextView.setText("Business Negotiation engine is not initialized");
        }
    }
}
