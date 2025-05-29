package com.aiassistant.research;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.call.ResearchEnabledCallHandler;
import com.aiassistant.ai.features.call.ResearchEnabledCallHandler.ResearchResponseCallback;

/**
 * ResearchDemo provides a simple interface to demonstrate and test
 * the AI's research capabilities.
 */
public class ResearchDemo {
    private static final String TAG = "ResearchDemo";
    
    // Core components
    private Context context;
    private ResearchManager researchManager;
    private InformationVerifier verifier;
    private WebContentProcessor webProcessor;
    private ResearchEnabledCallHandler callHandler;
    
    public ResearchDemo(Context context) {
        this.context = context;
        this.researchManager = ResearchManager.getInstance(context);
        this.verifier = new InformationVerifier(context);
        this.webProcessor = new WebContentProcessor();
        this.callHandler = ResearchEnabledCallHandler.getInstance(context);
    }
    
    /**
     * Interface for receiving research results
     */
    public interface ResearchResultListener {
        void onResearchComplete(String query, String result);
        void onResearchError(String query, String error);
    }
    
    /**
     * Perform a simple research query and return results
     */
    public void performResearch(String query, ResearchResultListener listener) {
        if (researchManager == null) {
            if (listener != null) {
                listener.onResearchError(query, "Research manager not initialized");
            }
            return;
        }
        
        // Check internet connectivity
        if (!researchManager.isInternetAvailable()) {
            if (listener != null) {
                listener.onResearchError(query, "No internet connection available for research");
            }
            return;
        }
        
        Log.d(TAG, "Performing research for: " + query);
        
        // Use the research manager to generate a response
        researchManager.generateResearchedResponse(query, new ResearchManager.ResponseCallback() {
            @Override
            public void onResponseGenerated(String researchQuery, String response) {
                if (listener != null) {
                    listener.onResearchComplete(researchQuery, response);
                }
            }
        });
    }
    
    /**
     * Simulate a call where research is needed
     */
    public void simulateResearchCall(String callerName, String question, ResearchResultListener listener) {
        Log.d(TAG, "Simulating research call from " + callerName + ": " + question);
        
        callHandler.handleCallMessageWithResearch(callerName, question, new ResearchResponseCallback() {
            @Override
            public void onResponseGenerated(String query, String response) {
                if (listener != null) {
                    listener.onResearchComplete(query, response);
                }
            }
        });
    }
    
    /**
     * Test the sports prediction handling
     */
    public void testSportsPrediction(String question, ResearchResultListener listener) {
        Log.d(TAG, "Testing sports prediction handling: " + question);
        
        // Use a typical caller name for the test
        simulateResearchCall("Friend", question, listener);
    }
    
    /**
     * Test research with cricket match question
     */
    public void testCricketMatchResearch(ResearchResultListener listener) {
        String question = "Who will win between India and Australia?";
        Log.d(TAG, "Testing cricket match research: " + question);
        
        testSportsPrediction(question, listener);
    }
    
    /**
     * Process a URL and extract content for testing
     */
    public void processUrl(String url, WebContentListener listener) {
        try {
            WebContentProcessor.WebPageContent content = webProcessor.fetchAndProcessUrl(url);
            
            if (listener != null) {
                listener.onContentExtracted(url, content);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing URL: " + e.getMessage());
            
            if (listener != null) {
                listener.onExtractionError(url, e.getMessage());
            }
        }
    }
    
    /**
     * Interface for web content extraction results
     */
    public interface WebContentListener {
        void onContentExtracted(String url, WebContentProcessor.WebPageContent content);
        void onExtractionError(String url, String error);
    }
}
