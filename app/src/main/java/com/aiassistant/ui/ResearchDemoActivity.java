package com.aiassistant.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIResearchIntegration;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.SentientVoiceSystem;
import com.aiassistant.research.ResearchDemo;

/**
 * Demonstrates the AI's research capabilities by allowing the user to query
 * information that will be researched using online sources when available.
 */
public class ResearchDemoActivity extends Activity {
    private static final String TAG = "ResearchDemoActivity";
    
    // UI components
    private EditText queryInput;
    private Button researchButton;
    private TextView resultText;
    private ScrollView scrollView;
    
    // Core components
    private AIStateManager aiStateManager;
    private AIResearchIntegration researchIntegration;
    private SentientVoiceSystem voiceSystem;
    private ResearchDemo researchDemo;
    
    // Handler for UI updates
    private Handler uiHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research_demo);
        
        // Initialize UI components
        queryInput = findViewById(R.id.query_input);
        researchButton = findViewById(R.id.research_button);
        resultText = findViewById(R.id.result_text);
        scrollView = findViewById(R.id.scroll_view);
        
        // Initialize handler for UI updates
        uiHandler = new Handler(Looper.getMainLooper());
        
        // Initialize core components
        aiStateManager = AIStateManager.getInstance(this);
        researchIntegration = new AIResearchIntegration(this, aiStateManager);
        voiceSystem = SentientVoiceSystem.getInstance(this);
        researchDemo = new ResearchDemo(this);
        
        // Set up research button click handler
        researchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performResearch();
            }
        });
        
        // Display intro text
        resultText.setText("Welcome to the Research Demo.\n\n" +
                "Enter a question or topic to research, and I'll gather information " +
                "from online sources when available.\n\n" +
                "For example, try asking about cricket matches, JEE Advanced physics topics, " +
                "or other educational queries.\n\n" +
                "Note: This feature requires an internet connection to work properly.");
    }
    
    /**
     * Perform research based on user input
     */
    private void performResearch() {
        String query = queryInput.getText().toString().trim();
        
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a research query", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button to prevent multiple requests
        researchButton.setEnabled(false);
        
        // Show processing message
        appendToResults("\n\nResearching: " + query + "\n");
        appendToResults("Please wait while I gather information...\n");
        
        // Use research demo to perform research
        researchDemo.performResearch(query, new ResearchDemo.ResearchResultListener() {
            @Override
            public void onResearchComplete(final String query, final String result) {
                // Update UI on main thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Display result
                        appendToResults("\nRESEARCH RESULTS:\n\n" + result + "\n");
                        
                        // Speak result if voice system is available
                        if (voiceSystem != null) {
                            voiceSystem.speak("Here's what I found about " + query, false);
                        }
                        
                        // Re-enable button
                        researchButton.setEnabled(true);
                    }
                });
            }
            
            @Override
            public void onResearchError(final String query, final String error) {
                // Update UI on main thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Display error
                        appendToResults("\nRESEARCH ERROR: " + error + "\n");
                        appendToResults("\nI'm unable to research this topic right now. " +
                                "Please check your internet connection and try again later.\n");
                        
                        // Speak error if voice system is available
                        if (voiceSystem != null) {
                            voiceSystem.speak("I'm sorry, I couldn't research that topic right now.", false);
                        }
                        
                        // Re-enable button
                        researchButton.setEnabled(true);
                    }
                });
            }
        });
    }
    
    /**
     * Append text to the results and scroll to bottom
     */
    private void appendToResults(String text) {
        resultText.append(text);
        
        // Scroll to bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    
    /**
     * Test cricket match prediction
     */
    private void testCricketPrediction() {
        // Demonstrate sports prediction handling
        String predictionQuery = "Who will win between India and Australia?";
        
        appendToResults("\n\nTesting cricket prediction: " + predictionQuery + "\n");
        appendToResults("Please wait...\n");
        
        researchDemo.testCricketMatchResearch(new ResearchDemo.ResearchResultListener() {
            @Override
            public void onResearchComplete(final String query, final String result) {
                // Update UI on main thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Display result
                        appendToResults("\nCRICKET PREDICTION HANDLING:\n\n" + result + "\n");
                    }
                });
            }
            
            @Override
            public void onResearchError(final String query, final String error) {
                // Update UI on main thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Display error
                        appendToResults("\nERROR: " + error + "\n");
                    }
                });
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Stop any speaking when leaving the activity
        if (voiceSystem != null) {
            voiceSystem.stop();
        }
    }
}
