package com.aiassistant.data.repositories;

import com.aiassistant.data.models.Game;

/**
 * Wrapper for Game class
 */
public class GameWrapper {
    
    private final Game game;
    
    /**
     * Constructor
     * 
     * @param game The game
     */
    public GameWrapper(Game game) {
        this.game = game;
    }
    
    /**
     * Get the game
     * 
     * @return The game
     */
    public Game getGame() {
        return game;
    }
}
