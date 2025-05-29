    /**
     * Enum for game phases
     */
    public enum GamePhase {
        UNKNOWN(0),
        MENU(1),
        LOADING(2),
        LOBBY(3),
        MATCH_PREPARATION(4),
        IN_GAME(5),
        COMBAT(6),
        LOOTING(7),
        HEALING(8),
        MOVEMENT(9),
        GAME_OVER(10),
        SPECTATING(11),
        RESULTS(12);
        
        private final int value;
        
        GamePhase(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static GamePhase fromValue(int value) {
            for (GamePhase phase : values()) {
                if (phase.value == value) {
                    return phase;
                }
            }
            return UNKNOWN;
        }
    }
