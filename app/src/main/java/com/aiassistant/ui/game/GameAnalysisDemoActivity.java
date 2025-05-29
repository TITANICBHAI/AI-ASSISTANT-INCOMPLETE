package com.aiassistant.ui.game;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.game.GameAnalysisManager;

public class GameAnalysisDemoActivity extends AppCompatActivity {

    private AIStateManager aiStateManager;
    private GameAnalysisManager gameAnalysisManager;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_analysis_demo);
        
        // Get manager instances
        aiStateManager = AIStateManager.getInstance();
        gameAnalysisManager = aiStateManager.getGameAnalysisManager();
        
        // Initialize UI
        statusTextView = findViewById(R.id.statusTextView);
        
        // Show status
        updateStatus();
    }
    
    private void updateStatus() {
        if (gameAnalysisManager != null) {
            statusTextView.setText("Game analysis system is operational");
        } else {
            statusTextView.setText("Game analysis system is not initialized");
        }
    }
}
