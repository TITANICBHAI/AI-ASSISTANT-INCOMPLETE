    
    public int getGamePhase() {
        return gamePhase;
    }
    
    public void setGamePhase(int gamePhase) {
        this.gamePhase = gamePhase;
    }
    
    public GamePhase getGamePhaseEnum() {
        return GamePhase.fromValue(gamePhase);
    }
    
    public void setGamePhaseEnum(GamePhase gamePhase) {
        this.gamePhase = gamePhase.getValue();
    }
