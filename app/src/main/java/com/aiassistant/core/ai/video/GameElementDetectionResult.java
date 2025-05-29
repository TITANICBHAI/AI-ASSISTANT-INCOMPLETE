package com.aiassistant.core.ai.video;

public class GameElementDetectionResult {
    private String gameMode;
    private List<GameElement> elements;
    
    public GameElementDetectionResult(String gameMode, List<GameElement> elements) {
        this.gameMode = gameMode;
        this.elements = elements;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public List<GameElement> getElements() {
        return elements;
    }
}
