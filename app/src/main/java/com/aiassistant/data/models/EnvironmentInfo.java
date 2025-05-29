package com.aiassistant.data.models;

/**
 * Information about the game environment
 */
public class EnvironmentInfo {
    
    // Terrain type constants
    public static final int TERRAIN_URBAN = 0;
    public static final int TERRAIN_FOREST = 1;
    public static final int TERRAIN_DESERT = 2;
    public static final int TERRAIN_INDOOR = 3;
    public static final int TERRAIN_SNOW = 4;
    public static final int TERRAIN_WATER = 5;
    
    // Weather condition constants
    public static final int WEATHER_CLEAR = 0;
    public static final int WEATHER_RAIN = 1;
    public static final int WEATHER_FOG = 2;
    public static final int WEATHER_NIGHT = 3;
    public static final int WEATHER_STORM = 4;
    
    // Environment properties
    private int terrainType;
    private int weatherCondition;
    private boolean isDayTime;
    private int obstaclePercentage; // 0-100, percentage of screen covered by obstacles
    
    /**
     * Default constructor
     */
    public EnvironmentInfo() {
        this.terrainType = TERRAIN_URBAN;
        this.weatherCondition = WEATHER_CLEAR;
        this.isDayTime = true;
        this.obstaclePercentage = 0;
    }

    /**
     * Get terrain type
     * @return The terrain type
     */
    public int getTerrainType() {
        return terrainType;
    }

    /**
     * Set terrain type
     * @param terrainType The terrain type
     */
    public void setTerrainType(int terrainType) {
        this.terrainType = terrainType;
    }

    /**
     * Get weather condition
     * @return The weather condition
     */
    public int getWeatherCondition() {
        return weatherCondition;
    }

    /**
     * Set weather condition
     * @param weatherCondition The weather condition
     */
    public void setWeatherCondition(int weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    /**
     * Check if it's daytime
     * @return True if daytime, false if nighttime
     */
    public boolean isDayTime() {
        return isDayTime;
    }

    /**
     * Set whether it's daytime
     * @param dayTime True if daytime, false if nighttime
     */
    public void setDayTime(boolean dayTime) {
        isDayTime = dayTime;
    }

    /**
     * Get obstacle percentage
     * @return Percentage of screen covered by obstacles (0-100)
     */
    public int getObstaclePercentage() {
        return obstaclePercentage;
    }

    /**
     * Set obstacle percentage
     * @param obstaclePercentage Percentage of screen covered by obstacles (0-100)
     */
    public void setObstaclePercentage(int obstaclePercentage) {
        this.obstaclePercentage = Math.min(100, Math.max(0, obstaclePercentage));
    }
    
    /**
     * Get a string representation of the terrain type
     * @return The terrain type as a string
     */
    public String getTerrainTypeString() {
        switch (terrainType) {
            case TERRAIN_URBAN:
                return "Urban";
            case TERRAIN_FOREST:
                return "Forest";
            case TERRAIN_DESERT:
                return "Desert";
            case TERRAIN_INDOOR:
                return "Indoor";
            case TERRAIN_SNOW:
                return "Snow";
            case TERRAIN_WATER:
                return "Water";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get a string representation of the weather condition
     * @return The weather condition as a string
     */
    public String getWeatherConditionString() {
        switch (weatherCondition) {
            case WEATHER_CLEAR:
                return "Clear";
            case WEATHER_RAIN:
                return "Rain";
            case WEATHER_FOG:
                return "Fog";
            case WEATHER_NIGHT:
                return "Night";
            case WEATHER_STORM:
                return "Storm";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return "EnvironmentInfo{" +
                "terrain=" + getTerrainTypeString() +
                ", weather=" + getWeatherConditionString() +
                ", time=" + (isDayTime ? "Day" : "Night") +
                ", obstacles=" + obstaclePercentage + "%" +
                '}';
    }
}
