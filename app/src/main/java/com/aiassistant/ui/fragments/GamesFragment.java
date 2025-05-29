package com.aiassistant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;

public class GamesFragment extends Fragment {

    private RecyclerView gamesRecyclerView;
    private Button addGameButton;
    private Button backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        gamesRecyclerView = view.findViewById(R.id.gamesRecyclerView);
        addGameButton = view.findViewById(R.id.addGameButton);
        backButton = view.findViewById(R.id.backButton);
        
        // Set up RecyclerView
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set up adapter (placeholder)
        
        // Set up button click listeners
        addGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show dialog to add game (placeholder)
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigateUp();
            }
        });
    }
}
