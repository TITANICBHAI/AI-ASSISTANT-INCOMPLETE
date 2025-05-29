package com.aiassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.adapters.GameAdapter;
import com.aiassistant.data.models.Game;
import com.aiassistant.ui.viewmodels.AppManagementViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for selecting and managing supported games
 */
public class GameSelectFragment extends Fragment implements GameAdapter.GameClickListener {
    
    private AppManagementViewModel viewModel;
    private RecyclerView gamesRecyclerView;
    private GameAdapter gameAdapter;
    private SearchView searchView;
    private Button scanGamesButton;
    
    public GameSelectFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AppManagementViewModel.class);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_select, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        gamesRecyclerView = view.findViewById(R.id.gamesRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        scanGamesButton = view.findViewById(R.id.scanGamesButton);
        
        // Set up RecyclerView
        gamesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        gameAdapter = new GameAdapter(new ArrayList<>(), this);
        gamesRecyclerView.setAdapter(gameAdapter);
        
        // Set up button listeners
        setupButtonListeners();
        
        // Set up search functionality
        setupSearch();
        
        // Observe ViewModel data
        observeViewModelData();
    }
    
    private void setupButtonListeners() {
        scanGamesButton.setOnClickListener(v -> {
            viewModel.scanForInstalledGames();
        });
    }
    
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterGames(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filterGames(newText);
                return true;
            }
        });
    }
    
    private void filterGames(String query) {
        List<Game> allGames = viewModel.getGamesLiveData().getValue();
        if (allGames == null) return;
        
        List<Game> filteredGames = new ArrayList<>();
        for (Game game : allGames) {
            if (game.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredGames.add(game);
            }
        }
        
        gameAdapter.updateGames(filteredGames);
    }
    
    private void observeViewModelData() {
        // Observe games list
        viewModel.getGamesLiveData().observe(getViewLifecycleOwner(), games -> {
            gameAdapter.updateGames(games);
        });
        
        // Observe scanning status
        viewModel.getScanningStatusLiveData().observe(getViewLifecycleOwner(), isScanning -> {
            scanGamesButton.setEnabled(!isScanning);
            scanGamesButton.setText(isScanning ? R.string.scanning_games : R.string.scan_for_games);
        });
    }
    
    @Override
    public void onGameClicked(Game game) {
        viewModel.selectGame(game);
    }
    
    @Override
    public void onGameLongClicked(Game game) {
        // Show options for this game (edit profile, delete, etc.)
        // This is a placeholder for future implementation
    }
}
