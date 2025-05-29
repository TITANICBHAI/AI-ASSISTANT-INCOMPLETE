package com.aiassistant.ai.features.gaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a profile for a specific game
 */
public class GameProfile {
    private String name;
    private String packageName;
    private List<String> gameTips;
    private Random random;
    
    /**
     * Constructor
     * @param name Game name
     * @param packageName Game package name
     */
    public GameProfile(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
        this.gameTips = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Get the game name
     * @return Game name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the game package name
     * @return Package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Add a game tip
     * @param tip Tip text
     */
    public void addGameTip(String tip) {
        gameTips.add(tip);
    }
    
    /**
     * Get a random game tip
     * @return Random tip or null if none available
     */
    public String getRandomTip() {
        if (gameTips.isEmpty()) {
            return null;
        }
        
        int index = random.nextInt(gameTips.size());
        return gameTips.get(index);
    }
    
    /**
     * Get all game tips
     * @return List of all tips
     */
    public List<String> getAllTips() {
        return new ArrayList<>(gameTips);
    }
}
