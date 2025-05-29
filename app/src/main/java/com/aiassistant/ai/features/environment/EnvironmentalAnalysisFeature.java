package com.aiassistant.ai.features.environment;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Environmental Analysis Feature
 * - Analyzes game environments and maps
 * - Identifies important locations and objects
 * - Tracks environmental changes over time
 * - Provides strategic insights based on environment
 */
public class EnvironmentalAnalysisFeature extends BaseFeature {
    private static final String TAG = "EnvironmentalAnalysis";
    private static final String FEATURE_NAME = "environmental_analysis";
    
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 500;
    
    // Last update timestamp
    private long lastUpdateTime;
    
    // Current environment data
    private Environment currentEnvironment;
    
    // Known environments
    private final Map<String, Environment> knownEnvironments;
    
    // Environmental elements registry
    private final Map<String, ElementType> elementTypeRegistry;
    
    // Listeners for environment events
    private final List<EnvironmentalAnalysisListener> listeners;
    
    // Analysis state
    private boolean isAnalyzing;
    
    // Current image data for analysis
    private Bitmap currentImage;
    
    // Map markers
    private final List<MapMarker> mapMarkers;
    
    /**
     * Environment type enumeration
     */
    public enum EnvironmentType {
        URBAN,
        RURAL,
        FOREST,
        DESERT,
        MOUNTAIN,
        INDOOR,
        UNDERWATER,
        SPACE,
        CUSTOM
    }
    
    /**
     * Time of day enumeration
     */
    public enum TimeOfDay {
        DAY,
        NIGHT,
        DAWN,
        DUSK,
        UNKNOWN
    }
    
    /**
     * Weather condition enumeration
     */
    public enum WeatherCondition {
        CLEAR,
        CLOUDY,
        RAINY,
        FOGGY,
        SNOWY,
        STORMY,
        WINDY,
        UNKNOWN
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public EnvironmentalAnalysisFeature(Context context) {
        super(context, FEATURE_NAME);
        this.lastUpdateTime = 0;
        this.knownEnvironments = new ConcurrentHashMap<>();
        this.elementTypeRegistry = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.isAnalyzing = false;
        this.currentImage = null;
        this.mapMarkers = new CopyOnWriteArrayList<>();
        this.currentEnvironment = null;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Register default element types
                registerDefaultElementTypes();
                
                // Load known environments
                loadKnownEnvironments();
                
                Log.d(TAG, "Environmental analysis system initialized with " +
                      elementTypeRegistry.size() + " element types and " +
                      knownEnvironments.size() + " known environments");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize environmental analysis", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled()) return;
        
        // Check if update is needed
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update environment analysis
            if (isAnalyzing && currentImage != null) {
                analyzeEnvironment();
            }
            
            // Update timestamp
            lastUpdateTime = currentTime;
        } catch (Exception e) {
            Log.e(TAG, "Error updating environmental analysis", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Stop analysis
        stopAnalysis();
        
        // Clear image data
        if (currentImage != null) {
            currentImage.recycle();
            currentImage = null;
        }
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start environment analysis
     * @param environmentId Environment ID
     * @return Created or existing environment
     */
    public Environment startAnalysis(String environmentId) {
        if (!isEnabled()) return null;
        
        // Check if environment exists
        Environment environment = knownEnvironments.get(environmentId);
        
        // Create new environment if not found
        if (environment == null) {
            environment = new Environment(environmentId);
            knownEnvironments.put(environmentId, environment);
        }
        
        // Set as current environment
        currentEnvironment = environment;
        
        // Set analyzing state
        isAnalyzing = true;
        
        Log.d(TAG, "Started environmental analysis for: " + environmentId);
        
        // Notify listeners
        for (EnvironmentalAnalysisListener listener : listeners) {
            listener.onAnalysisStarted(environment);
        }
        
        return environment;
    }
    
    /**
     * Stop environment analysis
     */
    public void stopAnalysis() {
        if (!isAnalyzing) return;
        
        isAnalyzing = false;
        
        Log.d(TAG, "Stopped environmental analysis");
        
        // Notify listeners
        if (currentEnvironment != null) {
            for (EnvironmentalAnalysisListener listener : listeners) {
                listener.onAnalysisStopped(currentEnvironment);
            }
        }
    }
    
    /**
     * Check if analysis is running
     * @return true if analyzing
     */
    public boolean isAnalyzing() {
        return isAnalyzing;
    }
    
    /**
     * Update environment image for analysis
     * @param image Image bitmap
     */
    public void updateImage(Bitmap image) {
        if (!isEnabled() || !isAnalyzing) return;
        
        // Recycle previous image if exists
        if (currentImage != null) {
            currentImage.recycle();
        }
        
        // Set new image
        currentImage = image;
        
        // Notify image update
        if (currentEnvironment != null) {
            for (EnvironmentalAnalysisListener listener : listeners) {
                listener.onImageUpdated(currentEnvironment, image);
            }
        }
        
        // Trigger immediate analysis
        analyzeEnvironment();
    }
    
    /**
     * Get current environment
     * @return Current environment or null if none
     */
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    /**
     * Get a known environment by ID
     * @param environmentId Environment ID
     * @return Environment or null if not found
     */
    public Environment getEnvironment(String environmentId) {
        return knownEnvironments.get(environmentId);
    }
    
    /**
     * Get all known environments
     * @return Map of all environments
     */
    public Map<String, Environment> getAllEnvironments() {
        return new HashMap<>(knownEnvironments);
    }
    
    /**
     * Register a new element type
     * @param typeId Type ID
     * @param elementType Element type
     */
    public void registerElementType(String typeId, ElementType elementType) {
        if (typeId != null && elementType != null) {
            elementTypeRegistry.put(typeId, elementType);
            Log.d(TAG, "Registered element type: " + typeId);
        }
    }
    
    /**
     * Get an element type by ID
     * @param typeId Type ID
     * @return Element type or null if not found
     */
    public ElementType getElementType(String typeId) {
        return elementTypeRegistry.get(typeId);
    }
    
    /**
     * Get all registered element types
     * @return Map of all element types
     */
    public Map<String, ElementType> getAllElementTypes() {
        return new HashMap<>(elementTypeRegistry);
    }
    
    /**
     * Add a map marker
     * @param marker Map marker
     */
    public void addMapMarker(MapMarker marker) {
        if (marker != null) {
            mapMarkers.add(marker);
            
            // Add to current environment if exists
            if (currentEnvironment != null) {
                currentEnvironment.addMapMarker(marker);
            }
            
            // Notify listeners
            for (EnvironmentalAnalysisListener listener : listeners) {
                listener.onMapMarkerAdded(marker);
            }
            
            Log.d(TAG, "Added map marker: " + marker.getId() + " - " + marker.getName());
        }
    }
    
    /**
     * Remove a map marker
     * @param markerId Marker ID
     * @return true if marker was removed
     */
    public boolean removeMapMarker(String markerId) {
        boolean removed = false;
        
        // Find and remove marker
        MapMarker markerToRemove = null;
        for (MapMarker marker : mapMarkers) {
            if (marker.getId().equals(markerId)) {
                markerToRemove = marker;
                break;
            }
        }
        
        if (markerToRemove != null) {
            mapMarkers.remove(markerToRemove);
            
            // Remove from current environment if exists
            if (currentEnvironment != null) {
                currentEnvironment.removeMapMarker(markerId);
            }
            
            // Notify listeners
            for (EnvironmentalAnalysisListener listener : listeners) {
                listener.onMapMarkerRemoved(markerToRemove);
            }
            
            Log.d(TAG, "Removed map marker: " + markerId);
            removed = true;
        }
        
        return removed;
    }
    
    /**
     * Get all map markers
     * @return List of all markers
     */
    public List<MapMarker> getAllMapMarkers() {
        return new ArrayList<>(mapMarkers);
    }
    
    /**
     * Set environment properties
     * @param type Environment type
     * @param timeOfDay Time of day
     * @param weather Weather condition
     */
    public void setEnvironmentProperties(EnvironmentType type, TimeOfDay timeOfDay, 
                                        WeatherCondition weather) {
        if (currentEnvironment != null) {
            currentEnvironment.setType(type);
            currentEnvironment.setTimeOfDay(timeOfDay);
            currentEnvironment.setWeatherCondition(weather);
            
            // Notify listeners
            for (EnvironmentalAnalysisListener listener : listeners) {
                listener.onEnvironmentPropertiesChanged(currentEnvironment);
            }
            
            Log.d(TAG, "Set environment properties: " + type + ", " + 
                  timeOfDay + ", " + weather);
        }
    }
    
    /**
     * Add an environmental analysis listener
     * @param listener Listener to add
     */
    public void addListener(EnvironmentalAnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove an environmental analysis listener
     * @param listener Listener to remove
     */
    public void removeListener(EnvironmentalAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Register default element types
     */
    private void registerDefaultElementTypes() {
        // Create default element types
        
        // Cover element type
        ElementType cover = new ElementType("cover", "Cover");
        cover.setDescription("Provides protection from attacks");
        cover.setStrategicValue(0.8f);
        elementTypeRegistry.put(cover.getId(), cover);
        
        // Resource element type
        ElementType resource = new ElementType("resource", "Resource");
        resource.setDescription("Collectible resources");
        resource.setStrategicValue(0.7f);
        elementTypeRegistry.put(resource.getId(), resource);
        
        // Hazard element type
        ElementType hazard = new ElementType("hazard", "Hazard");
        hazard.setDescription("Dangerous environmental elements");
        hazard.setStrategicValue(0.6f);
        elementTypeRegistry.put(hazard.getId(), hazard);
        
        // Path element type
        ElementType path = new ElementType("path", "Path");
        path.setDescription("Traversable paths");
        path.setStrategicValue(0.5f);
        elementTypeRegistry.put(path.getId(), path);
        
        // Landmark element type
        ElementType landmark = new ElementType("landmark", "Landmark");
        landmark.setDescription("Notable location markers");
        landmark.setStrategicValue(0.4f);
        elementTypeRegistry.put(landmark.getId(), landmark);
        
        // Spawn element type
        ElementType spawn = new ElementType("spawn", "Spawn Point");
        spawn.setDescription("Player or entity spawn locations");
        spawn.setStrategicValue(0.9f);
        elementTypeRegistry.put(spawn.getId(), spawn);
    }
    
    /**
     * Load known environments
     */
    private void loadKnownEnvironments() {
        // This would load from storage or predefined data
        // For now, leave empty
    }
    
    /**
     * Analyze current environment
     */
    private void analyzeEnvironment() {
        if (currentEnvironment == null || currentImage == null) {
            return;
        }
        
        // In a real implementation, this would perform image analysis
        // For this example, we'll simulate detection of environmental elements
        
        // Update environment data (simulated for this example)
        simulateEnvironmentDetection();
        
        // Notify listeners of update
        for (EnvironmentalAnalysisListener listener : listeners) {
            listener.onEnvironmentAnalyzed(currentEnvironment);
        }
    }
    
    /**
     * Simulate environment detection (for example purposes)
     */
    private void simulateEnvironmentDetection() {
        if (currentEnvironment == null) {
            return;
        }
        
        // Simulate detection of environment type based on image characteristics
        // In a real implementation, this would use computer vision techniques
        
        // For this example, we'll just set some sample values
        if (currentEnvironment.getType() == null) {
            currentEnvironment.setType(EnvironmentType.FOREST);
            currentEnvironment.setTimeOfDay(TimeOfDay.DAY);
            currentEnvironment.setWeatherCondition(WeatherCondition.CLEAR);
        }
        
        // Check if elements already detected
        if (currentEnvironment.getElements().isEmpty()) {
            // Simulate detection of environment elements
            addSimulatedElements();
        }
    }
    
    /**
     * Add simulated environmental elements (for example purposes)
     */
    private void addSimulatedElements() {
        if (currentEnvironment == null) {
            return;
        }
        
        // Add some sample elements
        
        // Add a cover element
        EnvironmentalElement coverElement = new EnvironmentalElement(
            "cover_1", "Large Rock", "cover");
        coverElement.setPosition(100, 200, 0);
        coverElement.setSize(50, 30, 20);
        coverElement.setProperty("protection_level", 0.8f);
        currentEnvironment.addElement(coverElement);
        
        // Add a resource element
        EnvironmentalElement resourceElement = new EnvironmentalElement(
            "resource_1", "Health Pack", "resource");
        resourceElement.setPosition(300, 150, 0);
        resourceElement.setSize(20, 20, 10);
        resourceElement.setProperty("value", 50.0f);
        currentEnvironment.addElement(resourceElement);
        
        // Add a hazard element
        EnvironmentalElement hazardElement = new EnvironmentalElement(
            "hazard_1", "Fire Pit", "hazard");
        hazardElement.setPosition(200, 300, 0);
        hazardElement.setSize(40, 40, 10);
        hazardElement.setProperty("damage", 25.0f);
        currentEnvironment.addElement(hazardElement);
        
        // Add a path element
        EnvironmentalElement pathElement = new EnvironmentalElement(
            "path_1", "Forest Trail", "path");
        pathElement.setPosition(150, 150, 0);
        pathElement.setSize(300, 50, 0);
        pathElement.setProperty("speed_modifier", 1.2f);
        currentEnvironment.addElement(pathElement);
        
        // Add elements to the environment
        Log.d(TAG, "Added " + currentEnvironment.getElements().size() + 
              " elements to environment");
    }
    
    /**
     * Environment class
     * Represents a game environment
     */
    public static class Environment {
        private final String id;
        private String name;
        private EnvironmentType type;
        private TimeOfDay timeOfDay;
        private WeatherCondition weatherCondition;
        private final Map<String, EnvironmentalElement> elements;
        private final List<MapMarker> mapMarkers;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Environment ID
         */
        public Environment(String id) {
            this.id = id;
            this.name = id;
            this.type = null;
            this.timeOfDay = TimeOfDay.UNKNOWN;
            this.weatherCondition = WeatherCondition.UNKNOWN;
            this.elements = new ConcurrentHashMap<>();
            this.mapMarkers = new CopyOnWriteArrayList<>();
            this.properties = new ConcurrentHashMap<>();
        }
        
        /**
         * Get environment ID
         * @return Environment ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get environment name
         * @return Environment name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set environment name
         * @param name Environment name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get environment type
         * @return Environment type
         */
        public EnvironmentType getType() {
            return type;
        }
        
        /**
         * Set environment type
         * @param type Environment type
         */
        public void setType(EnvironmentType type) {
            this.type = type;
        }
        
        /**
         * Get time of day
         * @return Time of day
         */
        public TimeOfDay getTimeOfDay() {
            return timeOfDay;
        }
        
        /**
         * Set time of day
         * @param timeOfDay Time of day
         */
        public void setTimeOfDay(TimeOfDay timeOfDay) {
            this.timeOfDay = timeOfDay;
        }
        
        /**
         * Get weather condition
         * @return Weather condition
         */
        public WeatherCondition getWeatherCondition() {
            return weatherCondition;
        }
        
        /**
         * Set weather condition
         * @param weatherCondition Weather condition
         */
        public void setWeatherCondition(WeatherCondition weatherCondition) {
            this.weatherCondition = weatherCondition;
        }
        
        /**
         * Add an environmental element
         * @param element Element to add
         */
        public void addElement(EnvironmentalElement element) {
            if (element != null) {
                elements.put(element.getId(), element);
            }
        }
        
        /**
         * Remove an environmental element
         * @param elementId Element ID to remove
         * @return true if element was removed
         */
        public boolean removeElement(String elementId) {
            return elements.remove(elementId) != null;
        }
        
        /**
         * Get an environmental element
         * @param elementId Element ID
         * @return Element or null if not found
         */
        public EnvironmentalElement getElement(String elementId) {
            return elements.get(elementId);
        }
        
        /**
         * Get all environmental elements
         * @return List of all elements
         */
        public List<EnvironmentalElement> getElements() {
            return new ArrayList<>(elements.values());
        }
        
        /**
         * Get elements by type
         * @param typeId Type ID
         * @return List of elements of the specified type
         */
        public List<EnvironmentalElement> getElementsByType(String typeId) {
            List<EnvironmentalElement> result = new ArrayList<>();
            
            for (EnvironmentalElement element : elements.values()) {
                if (element.getTypeId().equals(typeId)) {
                    result.add(element);
                }
            }
            
            return result;
        }
        
        /**
         * Add a map marker
         * @param marker Marker to add
         */
        public void addMapMarker(MapMarker marker) {
            if (marker != null && !mapMarkers.contains(marker)) {
                mapMarkers.add(marker);
            }
        }
        
        /**
         * Remove a map marker
         * @param markerId Marker ID to remove
         * @return true if marker was removed
         */
        public boolean removeMapMarker(String markerId) {
            for (int i = 0; i < mapMarkers.size(); i++) {
                if (mapMarkers.get(i).getId().equals(markerId)) {
                    mapMarkers.remove(i);
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Get all map markers
         * @return List of all markers
         */
        public List<MapMarker> getMapMarkers() {
            return new ArrayList<>(mapMarkers);
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
        
        /**
         * Get a property as string
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public String getStringProperty(String key, String defaultValue) {
            Object value = properties.get(key);
            return (value != null) ? value.toString() : defaultValue;
        }
        
        /**
         * Get a property as float
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public float getFloatProperty(String key, float defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return defaultValue;
        }
        
        /**
         * Get a property as boolean
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public boolean getBooleanProperty(String key, boolean defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return defaultValue;
        }
        
        /**
         * Calculate visibility based on time of day and weather
         * @return Visibility factor (0.0-1.0)
         */
        public float calculateVisibility() {
            float visibilityFactor = 1.0f;
            
            // Adjust for time of day
            if (timeOfDay == TimeOfDay.NIGHT) {
                visibilityFactor *= 0.3f;
            } else if (timeOfDay == TimeOfDay.DAWN || timeOfDay == TimeOfDay.DUSK) {
                visibilityFactor *= 0.7f;
            }
            
            // Adjust for weather
            if (weatherCondition == WeatherCondition.FOGGY) {
                visibilityFactor *= 0.5f;
            } else if (weatherCondition == WeatherCondition.RAINY) {
                visibilityFactor *= 0.8f;
            } else if (weatherCondition == WeatherCondition.STORMY) {
                visibilityFactor *= 0.6f;
            }
            
            return Math.max(0.1f, visibilityFactor);
        }
        
        /**
         * Get environment description
         * @return Text description of environment
         */
        public String getDescription() {
            StringBuilder description = new StringBuilder();
            
            if (type != null) {
                description.append(type.toString().toLowerCase());
                description.append(" environment");
            }
            
            if (timeOfDay != TimeOfDay.UNKNOWN) {
                description.append(", ");
                description.append(timeOfDay.toString().toLowerCase());
                description.append(" time");
            }
            
            if (weatherCondition != WeatherCondition.UNKNOWN) {
                description.append(", ");
                description.append(weatherCondition.toString().toLowerCase());
                description.append(" weather");
            }
            
            if (description.length() == 0) {
                return "Unknown environment";
            }
            
            return description.toString();
        }
        
        @Override
        public String toString() {
            return name + " (" + getDescription() + ")";
        }
    }
    
    /**
     * Environmental Element class
     * Represents an element in the environment
     */
    public static class EnvironmentalElement {
        private final String id;
        private String name;
        private final String typeId;
        private float x, y, z;
        private float width, height, depth;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Element ID
         * @param name Element name
         * @param typeId Element type ID
         */
        public EnvironmentalElement(String id, String name, String typeId) {
            this.id = id;
            this.name = name;
            this.typeId = typeId;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.width = 0;
            this.height = 0;
            this.depth = 0;
            this.properties = new HashMap<>();
        }
        
        /**
         * Get element ID
         * @return Element ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get element name
         * @return Element name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set element name
         * @param name Element name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get element type ID
         * @return Type ID
         */
        public String getTypeId() {
            return typeId;
        }
        
        /**
         * Set element position
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         */
        public void setPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        /**
         * Get X coordinate
         * @return X coordinate
         */
        public float getX() {
            return x;
        }
        
        /**
         * Get Y coordinate
         * @return Y coordinate
         */
        public float getY() {
            return y;
        }
        
        /**
         * Get Z coordinate
         * @return Z coordinate
         */
        public float getZ() {
            return z;
        }
        
        /**
         * Set element size
         * @param width Width
         * @param height Height
         * @param depth Depth
         */
        public void setSize(float width, float height, float depth) {
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
        
        /**
         * Get width
         * @return Width
         */
        public float getWidth() {
            return width;
        }
        
        /**
         * Get height
         * @return Height
         */
        public float getHeight() {
            return height;
        }
        
        /**
         * Get depth
         * @return Depth
         */
        public float getDepth() {
            return depth;
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
        
        /**
         * Get a property as string
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public String getStringProperty(String key, String defaultValue) {
            Object value = properties.get(key);
            return (value != null) ? value.toString() : defaultValue;
        }
        
        /**
         * Get a property as float
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public float getFloatProperty(String key, float defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return defaultValue;
        }
        
        /**
         * Get a property as boolean
         * @param key Property key
         * @param defaultValue Default value if not found
         * @return Property value or default value
         */
        public boolean getBooleanProperty(String key, boolean defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return defaultValue;
        }
        
        /**
         * Check if this element contains a point
         * @param px X coordinate
         * @param py Y coordinate
         * @return true if point is inside element
         */
        public boolean containsPoint(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
        
        /**
         * Calculate distance to a point
         * @param px X coordinate
         * @param py Y coordinate
         * @return Distance to point
         */
        public float distanceTo(float px, float py) {
            // Calculate center of element
            float centerX = x + width / 2;
            float centerY = y + height / 2;
            
            // Calculate distance to center
            float dx = px - centerX;
            float dy = py - centerY;
            
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        
        @Override
        public String toString() {
            return name + " (" + typeId + ")";
        }
    }
    
    /**
     * Element Type class
     * Defines a type of environmental element
     */
    public static class ElementType {
        private final String id;
        private String name;
        private String description;
        private float strategicValue;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Type ID
         * @param name Type name
         */
        public ElementType(String id, String name) {
            this.id = id;
            this.name = name;
            this.description = "";
            this.strategicValue = 0.5f;
            this.properties = new HashMap<>();
        }
        
        /**
         * Get type ID
         * @return Type ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get type name
         * @return Type name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set type name
         * @param name Type name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Set description
         * @param description Description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Get strategic value
         * @return Strategic value (0.0-1.0)
         */
        public float getStrategicValue() {
            return strategicValue;
        }
        
        /**
         * Set strategic value
         * @param strategicValue Strategic value (0.0-1.0)
         */
        public void setStrategicValue(float strategicValue) {
            this.strategicValue = Math.max(0.0f, Math.min(1.0f, strategicValue));
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Map Marker class
     * Represents a marker on the map
     */
    public static class MapMarker {
        private final String id;
        private String name;
        private MarkerType type;
        private float x, y;
        private int color;
        private String description;
        private final Map<String, Object> properties;
        
        /**
         * Marker type enumeration
         */
        public enum MarkerType {
            OBJECTIVE,
            POINT_OF_INTEREST,
            DANGER,
            RESOURCE,
            CUSTOM
        }
        
        /**
         * Constructor
         * @param id Marker ID
         * @param name Marker name
         * @param type Marker type
         * @param x X coordinate
         * @param y Y coordinate
         */
        public MapMarker(String id, String name, MarkerType type, float x, float y) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.color = 0xFFFFFFFF; // White by default
            this.description = "";
            this.properties = new HashMap<>();
        }
        
        /**
         * Get marker ID
         * @return Marker ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get marker name
         * @return Marker name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set marker name
         * @param name Marker name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get marker type
         * @return Marker type
         */
        public MarkerType getType() {
            return type;
        }
        
        /**
         * Set marker type
         * @param type Marker type
         */
        public void setType(MarkerType type) {
            this.type = type;
        }
        
        /**
         * Get X coordinate
         * @return X coordinate
         */
        public float getX() {
            return x;
        }
        
        /**
         * Set X coordinate
         * @param x X coordinate
         */
        public void setX(float x) {
            this.x = x;
        }
        
        /**
         * Get Y coordinate
         * @return Y coordinate
         */
        public float getY() {
            return y;
        }
        
        /**
         * Set Y coordinate
         * @param y Y coordinate
         */
        public void setY(float y) {
            this.y = y;
        }
        
        /**
         * Get marker color
         * @return Color (ARGB)
         */
        public int getColor() {
            return color;
        }
        
        /**
         * Set marker color
         * @param color Color (ARGB)
         */
        public void setColor(int color) {
            this.color = color;
        }
        
        /**
         * Get description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Set description
         * @param description Description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
        
        /**
         * Calculate distance to another marker
         * @param other Other marker
         * @return Distance
         */
        public float distanceTo(MapMarker other) {
            float dx = other.x - this.x;
            float dy = other.y - this.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        
        /**
         * Calculate distance to a point
         * @param px X coordinate
         * @param py Y coordinate
         * @return Distance
         */
        public float distanceTo(float px, float py) {
            float dx = px - this.x;
            float dy = py - this.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        
        @Override
        public String toString() {
            return name + " (" + type + ")";
        }
    }
    
    /**
     * Environmental Analysis Listener interface
     * For receiving environment analysis events
     */
    public interface EnvironmentalAnalysisListener {
        /**
         * Called when analysis starts
         * @param environment Environment being analyzed
         */
        void onAnalysisStarted(Environment environment);
        
        /**
         * Called when analysis stops
         * @param environment Environment that was analyzed
         */
        void onAnalysisStopped(Environment environment);
        
        /**
         * Called when environment image is updated
         * @param environment Environment being analyzed
         * @param image New image
         */
        void onImageUpdated(Environment environment, Bitmap image);
        
        /**
         * Called when environment is analyzed
         * @param environment Analyzed environment
         */
        void onEnvironmentAnalyzed(Environment environment);
        
        /**
         * Called when environment properties change
         * @param environment Environment with changed properties
         */
        void onEnvironmentPropertiesChanged(Environment environment);
        
        /**
         * Called when a map marker is added
         * @param marker Added marker
         */
        void onMapMarkerAdded(MapMarker marker);
        
        /**
         * Called when a map marker is removed
         * @param marker Removed marker
         */
        void onMapMarkerRemoved(MapMarker marker);
    }
}
