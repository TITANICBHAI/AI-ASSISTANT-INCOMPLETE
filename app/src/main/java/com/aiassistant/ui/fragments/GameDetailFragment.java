package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for detailed game profile view
 */
public class GameDetailFragment extends Fragment {
    
    private TextView tvGameName;
    private TextView tvGamePackage;
    private TextView tvAiModeStatus;
    private RecyclerView rvGameStatistics;
    private Button btnEnableAI;
    private Button btnConfigureAI;
    private Button btnViewLearningData;
    
    private String gameName = "Unknown Game";
    private String gamePackage = "com.example.game";
    private boolean aiEnabled = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupStatistics();
        setupButtons();
        loadGameData();
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeViews(View view) {
        tvGameName = view.findViewById(R.id.tvGameName);
        tvGamePackage = view.findViewById(R.id.tvGamePackage);
        tvAiModeStatus = view.findViewById(R.id.tvAiModeStatus);
        rvGameStatistics = view.findViewById(R.id.rvGameStatistics);
        btnEnableAI = view.findViewById(R.id.btnEnableAI);
        btnConfigureAI = view.findViewById(R.id.btnConfigureAI);
        btnViewLearningData = view.findViewById(R.id.btnViewLearningData);
        
        rvGameStatistics.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    /**
     * Setup the statistics RecyclerView
     */
    private void setupStatistics() {
        List<GameStatistic> statistics = new ArrayList<>();
        statistics.add(new GameStatistic("Total Play Sessions", "0"));
        statistics.add(new GameStatistic("AI Assists", "0"));
        statistics.add(new GameStatistic("Win Rate", "0%"));
        statistics.add(new GameStatistic("Average Session Time", "0 min"));
        statistics.add(new GameStatistic("AI Learning Progress", "0%"));
        
        GameStatisticsAdapter adapter = new GameStatisticsAdapter(statistics);
        rvGameStatistics.setAdapter(adapter);
    }
    
    /**
     * Setup button click listeners
     */
    private void setupButtons() {
        btnEnableAI.setOnClickListener(v -> {
            aiEnabled = !aiEnabled;
            updateAIStatus();
            Toast.makeText(getContext(), 
                aiEnabled ? "AI Assistant Enabled" : "AI Assistant Disabled", 
                Toast.LENGTH_SHORT).show();
        });
        
        btnConfigureAI.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configure AI Settings", Toast.LENGTH_SHORT).show();
        });
        
        btnViewLearningData.setOnClickListener(v -> {
            Toast.makeText(getContext(), "View Learning Data", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Load game data from arguments or default values
     */
    private void loadGameData() {
        Bundle args = getArguments();
        if (args != null) {
            gameName = args.getString("game_name", "Unknown Game");
            gamePackage = args.getString("game_package", "com.example.game");
            aiEnabled = args.getBoolean("ai_enabled", false);
        }
        
        updateUI();
    }
    
    /**
     * Update UI with current game data
     */
    private void updateUI() {
        tvGameName.setText(gameName);
        tvGamePackage.setText(gamePackage);
        updateAIStatus();
    }
    
    /**
     * Update AI status text and button
     */
    private void updateAIStatus() {
        tvAiModeStatus.setText("AI Mode: " + (aiEnabled ? "Enabled" : "Disabled"));
        btnEnableAI.setText(aiEnabled ? "Disable AI Assistant" : "Enable AI Assistant");
    }
    
    /**
     * Inner class for game statistics
     */
    private static class GameStatistic {
        String label;
        String value;
        
        GameStatistic(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
    
    /**
     * Adapter for game statistics RecyclerView
     */
    private static class GameStatisticsAdapter extends RecyclerView.Adapter<StatisticViewHolder> {
        private final List<GameStatistic> statistics;
        
        GameStatisticsAdapter(List<GameStatistic> statistics) {
            this.statistics = statistics;
        }
        
        @NonNull
        @Override
        public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new StatisticViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
            GameStatistic stat = statistics.get(position);
            holder.bind(stat);
        }
        
        @Override
        public int getItemCount() {
            return statistics.size();
        }
    }
    
    /**
     * ViewHolder for statistics items
     */
    private static class StatisticViewHolder extends RecyclerView.ViewHolder {
        private final TextView text1;
        private final TextView text2;
        
        StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
        
        void bind(GameStatistic statistic) {
            text1.setText(statistic.label);
            text2.setText(statistic.value);
        }
    }
}
