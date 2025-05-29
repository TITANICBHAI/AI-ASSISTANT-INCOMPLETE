package com.aiassistant.ui.games;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.AIApplication;
import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for GamesFragment
 */
public class GamesViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Game>> games;
    private final ExecutorService executor;
    private final AppDatabase database;

    /**
     * Constructor
     * 
     * @param application The application
     */
    public GamesViewModel(@NonNull Application application) {
        super(application);
        games = new MutableLiveData<>(new ArrayList<>());
        executor = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(application);
        
        loadGames();
    }

    /**
     * Get the games
     * 
     * @return The games
     */
    public LiveData<List<Game>> getGames() {
        return games;
    }

    /**
     * Load games from the database
     */
    public void loadGames() {
        executor.execute(() -> {
            List<Game> gameList = database.gameDao().getAll();
            games.postValue(gameList);
        });
    }

    /**
     * Add a game
     * 
     * @param game The game
     */
    public void addGame(Game game) {
        executor.execute(() -> {
            database.gameDao().insert(game);
            loadGames();
        });
    }

    /**
     * Update a game
     * 
     * @param game The game
     */
    public void updateGame(Game game) {
        executor.execute(() -> {
            database.gameDao().update(game);
            loadGames();
        });
    }

    /**
     * Delete a game
     * 
     * @param game The game
     */
    public void deleteGame(Game game) {
        executor.execute(() -> {
            database.gameDao().delete(game);
            loadGames();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
