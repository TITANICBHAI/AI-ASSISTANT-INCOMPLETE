package com.aiassistant.ai.features.resource;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Smart Resource Management Feature
 * - Tracks and optimizes in-game resource usage
 * - Provides resource allocation recommendations
 * - Forecasts resource needs based on game state
 * - Maintains resource usage history and statistics
 */
public class ResourceManagementFeature extends BaseFeature {
    private static final String TAG = "ResourceManagement";
    private static final String FEATURE_NAME = "smart_resource_management";
    
    // Current game resources
    private final Map<String, ResourceTracker> resources;
    
    // Resource categories
    private final Map<String, ResourceCategory> categories;
    
    // Resource producers
    private final Map<String, ResourceProducer> producers;
    
    // Resource consumers
    private final Map<String, ResourceConsumer> consumers;
    
    // Resource allocations
    private final List<ResourceAllocation> allocations;
    
    // Resource history
    private final Map<String, List<ResourceHistoryEntry>> resourceHistory;
    
    // Listeners for resource events
    private final List<ResourceManagementListener> listeners;
    
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 2000;
    
    // Last update timestamp
    private long lastUpdateTime;
    
    // Current tracking state
    private boolean isTracking;
    
    // Current game ID
    private String currentGameId;
    
    /**
     * Constructor
     * @param context Application context
     */
    public ResourceManagementFeature(Context context) {
        super(context, FEATURE_NAME);
        this.resources = new ConcurrentHashMap<>();
        this.categories = new ConcurrentHashMap<>();
        this.producers = new ConcurrentHashMap<>();
        this.consumers = new ConcurrentHashMap<>();
        this.allocations = new CopyOnWriteArrayList<>();
        this.resourceHistory = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.lastUpdateTime = 0;
        this.isTracking = false;
        this.currentGameId = null;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Set up default resource categories
                setupDefaultCategories();
                
                Log.d(TAG, "Resource management system initialized with " +
                      categories.size() + " categories");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize resource management", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled() || !isTracking) return;
        
        // Check if update is needed
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update resource trackers
            updateResources();
            
            // Update resource history
            updateResourceHistory();
            
            // Update allocations
            updateResourceAllocations();
            
            // Update timestamp
            lastUpdateTime = currentTime;
        } catch (Exception e) {
            Log.e(TAG, "Error updating resource management", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Stop tracking
        stopTracking();
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start resource tracking for a game
     * @param gameId Game identifier
     */
    public void startTracking(String gameId) {
        if (!isEnabled() || isTracking) return;
        
        // Set game ID
        this.currentGameId = gameId;
        
        // Start tracking
        isTracking = true;
        
        Log.d(TAG, "Started resource tracking for game: " + gameId);
        
        // Notify listeners
        for (ResourceManagementListener listener : listeners) {
            listener.onTrackingStarted(gameId);
        }
    }
    
    /**
     * Stop resource tracking
     */
    public void stopTracking() {
        if (!isTracking) return;
        
        // Stop tracking
        isTracking = false;
        
        Log.d(TAG, "Stopped resource tracking");
        
        // Notify listeners
        for (ResourceManagementListener listener : listeners) {
            listener.onTrackingStopped();
        }
    }
    
    /**
     * Check if tracking is active
     * @return true if tracking
     */
    public boolean isTracking() {
        return isTracking;
    }
    
    /**
     * Add a resource category
     * @param category Resource category
     */
    public void addCategory(ResourceCategory category) {
        if (category != null) {
            categories.put(category.getId(), category);
            Log.d(TAG, "Added resource category: " + category.getName());
        }
    }
    
    /**
     * Get a resource category
     * @param categoryId Category ID
     * @return Resource category or null if not found
     */
    public ResourceCategory getCategory(String categoryId) {
        return categories.get(categoryId);
    }
    
    /**
     * Get all resource categories
     * @return Map of all categories
     */
    public Map<String, ResourceCategory> getAllCategories() {
        return new HashMap<>(categories);
    }
    
    /**
     * Create or update a resource
     * @param resourceId Resource ID
     * @param name Resource name
     * @param categoryId Category ID
     * @param value Current value
     * @param maxValue Maximum value (use 0 for unlimited)
     * @return The resource tracker
     */
    public ResourceTracker trackResource(String resourceId, String name, 
                                        String categoryId, float value, float maxValue) {
        // Get existing resource if available
        ResourceTracker tracker = resources.get(resourceId);
        
        if (tracker == null) {
            // Create new tracker
            tracker = new ResourceTracker(resourceId, name, categoryId);
            resources.put(resourceId, tracker);
            
            // Initialize history
            resourceHistory.put(resourceId, new ArrayList<>());
            
            Log.d(TAG, "Created new resource tracker: " + name);
        }
        
        // Update values
        tracker.setValue(value);
        tracker.setMaxValue(maxValue);
        
        // Add history entry
        addHistoryEntry(resourceId, value);
        
        // Notify listeners
        for (ResourceManagementListener listener : listeners) {
            listener.onResourceUpdated(tracker);
        }
        
        return tracker;
    }
    
    /**
     * Get a resource tracker
     * @param resourceId Resource ID
     * @return Resource tracker or null if not found
     */
    public ResourceTracker getResource(String resourceId) {
        return resources.get(resourceId);
    }
    
    /**
     * Get all resource trackers
     * @return Map of all resources
     */
    public Map<String, ResourceTracker> getAllResources() {
        return new HashMap<>(resources);
    }
    
    /**
     * Get resources by category
     * @param categoryId Category ID
     * @return List of resources in the category
     */
    public List<ResourceTracker> getResourcesByCategory(String categoryId) {
        List<ResourceTracker> result = new ArrayList<>();
        
        for (ResourceTracker tracker : resources.values()) {
            if (tracker.getCategoryId().equals(categoryId)) {
                result.add(tracker);
            }
        }
        
        return result;
    }
    
    /**
     * Add a resource producer
     * @param producer Resource producer
     */
    public void addProducer(ResourceProducer producer) {
        if (producer != null) {
            producers.put(producer.getId(), producer);
            Log.d(TAG, "Added resource producer: " + producer.getName());
            
            // Notify listeners
            for (ResourceManagementListener listener : listeners) {
                listener.onProducerAdded(producer);
            }
        }
    }
    
    /**
     * Get a resource producer
     * @param producerId Producer ID
     * @return Resource producer or null if not found
     */
    public ResourceProducer getProducer(String producerId) {
        return producers.get(producerId);
    }
    
    /**
     * Get all resource producers
     * @return Map of all producers
     */
    public Map<String, ResourceProducer> getAllProducers() {
        return new HashMap<>(producers);
    }
    
    /**
     * Add a resource consumer
     * @param consumer Resource consumer
     */
    public void addConsumer(ResourceConsumer consumer) {
        if (consumer != null) {
            consumers.put(consumer.getId(), consumer);
            Log.d(TAG, "Added resource consumer: " + consumer.getName());
            
            // Notify listeners
            for (ResourceManagementListener listener : listeners) {
                listener.onConsumerAdded(consumer);
            }
        }
    }
    
    /**
     * Get a resource consumer
     * @param consumerId Consumer ID
     * @return Resource consumer or null if not found
     */
    public ResourceConsumer getConsumer(String consumerId) {
        return consumers.get(consumerId);
    }
    
    /**
     * Get all resource consumers
     * @return Map of all consumers
     */
    public Map<String, ResourceConsumer> getAllConsumers() {
        return new HashMap<>(consumers);
    }
    
    /**
     * Add a resource allocation
     * @param allocation Resource allocation
     */
    public void addAllocation(ResourceAllocation allocation) {
        if (allocation != null) {
            allocations.add(allocation);
            Log.d(TAG, "Added resource allocation: " + allocation.getResourceId() + 
                  " to " + allocation.getConsumerId());
            
            // Notify listeners
            for (ResourceManagementListener listener : listeners) {
                listener.onAllocationAdded(allocation);
            }
        }
    }
    
    /**
     * Remove a resource allocation
     * @param resourceId Resource ID
     * @param consumerId Consumer ID
     * @return true if allocation was removed
     */
    public boolean removeAllocation(String resourceId, String consumerId) {
        for (int i = 0; i < allocations.size(); i++) {
            ResourceAllocation allocation = allocations.get(i);
            if (allocation.getResourceId().equals(resourceId) && 
                allocation.getConsumerId().equals(consumerId)) {
                
                ResourceAllocation removed = allocations.remove(i);
                
                // Notify listeners
                for (ResourceManagementListener listener : listeners) {
                    listener.onAllocationRemoved(removed);
                }
                
                Log.d(TAG, "Removed resource allocation: " + resourceId + 
                      " from " + consumerId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all resource allocations
     * @return List of all allocations
     */
    public List<ResourceAllocation> getAllAllocations() {
        return new ArrayList<>(allocations);
    }
    
    /**
     * Get allocations for a resource
     * @param resourceId Resource ID
     * @return List of allocations for the resource
     */
    public List<ResourceAllocation> getAllocationsForResource(String resourceId) {
        List<ResourceAllocation> result = new ArrayList<>();
        
        for (ResourceAllocation allocation : allocations) {
            if (allocation.getResourceId().equals(resourceId)) {
                result.add(allocation);
            }
        }
        
        return result;
    }
    
    /**
     * Get allocations for a consumer
     * @param consumerId Consumer ID
     * @return List of allocations for the consumer
     */
    public List<ResourceAllocation> getAllocationsForConsumer(String consumerId) {
        List<ResourceAllocation> result = new ArrayList<>();
        
        for (ResourceAllocation allocation : allocations) {
            if (allocation.getConsumerId().equals(consumerId)) {
                result.add(allocation);
            }
        }
        
        return result;
    }
    
    /**
     * Get total allocated value for a resource
     * @param resourceId Resource ID
     * @return Total allocated value
     */
    public float getTotalAllocated(String resourceId) {
        float total = 0.0f;
        
        for (ResourceAllocation allocation : allocations) {
            if (allocation.getResourceId().equals(resourceId)) {
                total += allocation.getValue();
            }
        }
        
        return total;
    }
    
    /**
     * Get available (unallocated) value for a resource
     * @param resourceId Resource ID
     * @return Available value
     */
    public float getAvailableValue(String resourceId) {
        ResourceTracker tracker = resources.get(resourceId);
        if (tracker == null) {
            return 0.0f;
        }
        
        float total = tracker.getValue();
        float allocated = getTotalAllocated(resourceId);
        
        return Math.max(0.0f, total - allocated);
    }
    
    /**
     * Get resource history
     * @param resourceId Resource ID
     * @return List of history entries or empty list if not found
     */
    public List<ResourceHistoryEntry> getResourceHistory(String resourceId) {
        return resourceHistory.getOrDefault(resourceId, new ArrayList<>());
    }
    
    /**
     * Get resource production rate
     * @param resourceId Resource ID
     * @return Production rate (units per second)
     */
    public float getProductionRate(String resourceId) {
        float rate = 0.0f;
        
        for (ResourceProducer producer : producers.values()) {
            if (producer.getOutputResourceId().equals(resourceId)) {
                rate += producer.getProductionRate();
            }
        }
        
        return rate;
    }
    
    /**
     * Get resource consumption rate
     * @param resourceId Resource ID
     * @return Consumption rate (units per second)
     */
    public float getConsumptionRate(String resourceId) {
        float rate = 0.0f;
        
        for (ResourceConsumer consumer : consumers.values()) {
            if (consumer.getInputResourceId().equals(resourceId)) {
                rate += consumer.getConsumptionRate();
            }
        }
        
        return rate;
    }
    
    /**
     * Get resource net rate (production - consumption)
     * @param resourceId Resource ID
     * @return Net rate (units per second)
     */
    public float getNetRate(String resourceId) {
        return getProductionRate(resourceId) - getConsumptionRate(resourceId);
    }
    
    /**
     * Calculate time until a resource is depleted
     * @param resourceId Resource ID
     * @return Time in seconds, or Float.POSITIVE_INFINITY if never depleted
     */
    public float getTimeUntilDepleted(String resourceId) {
        ResourceTracker tracker = resources.get(resourceId);
        if (tracker == null) {
            return 0.0f;
        }
        
        float available = getAvailableValue(resourceId);
        float netRate = getNetRate(resourceId);
        
        if (netRate >= 0.0f) {
            // Resource is not being depleted
            return Float.POSITIVE_INFINITY;
        }
        
        // Calculate time until depletion
        return Math.abs(available / netRate);
    }
    
    /**
     * Calculate time until a resource is filled
     * @param resourceId Resource ID
     * @return Time in seconds, or Float.POSITIVE_INFINITY if never filled
     */
    public float getTimeUntilFilled(String resourceId) {
        ResourceTracker tracker = resources.get(resourceId);
        if (tracker == null) {
            return 0.0f;
        }
        
        if (tracker.getMaxValue() <= 0.0f) {
            // Unlimited resource
            return Float.POSITIVE_INFINITY;
        }
        
        float available = getAvailableValue(resourceId);
        float remaining = tracker.getMaxValue() - tracker.getValue();
        float netRate = getNetRate(resourceId);
        
        if (netRate <= 0.0f) {
            // Resource is not being filled
            return Float.POSITIVE_INFINITY;
        }
        
        // Calculate time until filled
        return remaining / netRate;
    }
    
    /**
     * Get optimal resource allocations
     * @return List of recommended allocations
     */
    public List<ResourceAllocationRecommendation> getOptimalAllocations() {
        List<ResourceAllocationRecommendation> recommendations = new ArrayList<>();
        
        // Analyze each consumer
        for (ResourceConsumer consumer : consumers.values()) {
            String resourceId = consumer.getInputResourceId();
            ResourceTracker tracker = resources.get(resourceId);
            
            if (tracker == null) {
                continue;
            }
            
            // Get current allocation
            float currentAllocation = 0.0f;
            for (ResourceAllocation allocation : allocations) {
                if (allocation.getResourceId().equals(resourceId) && 
                    allocation.getConsumerId().equals(consumer.getId())) {
                    currentAllocation = allocation.getValue();
                    break;
                }
            }
            
            // Calculate optimal allocation
            float available = getAvailableValue(resourceId) + currentAllocation;
            float optimal = Math.min(available, consumer.getOptimalConsumption());
            
            if (Math.abs(optimal - currentAllocation) > 0.01f) {
                // Recommendation needed
                ResourceAllocationRecommendation recommendation = 
                    new ResourceAllocationRecommendation(
                        resourceId,
                        consumer.getId(),
                        tracker.getName(),
                        consumer.getName(),
                        currentAllocation,
                        optimal,
                        consumer.getPriority()
                    );
                
                recommendations.add(recommendation);
            }
        }
        
        // Sort by priority
        Collections.sort(recommendations, (r1, r2) -> 
            Integer.compare(r1.getPriority(), r2.getPriority()));
        
        return recommendations;
    }
    
    /**
     * Get resource usage efficiency
     * @param resourceId Resource ID
     * @return Efficiency rating (0.0-1.0)
     */
    public float getResourceEfficiency(String resourceId) {
        ResourceTracker tracker = resources.get(resourceId);
        if (tracker == null) {
            return 0.0f;
        }
        
        // Get allocations
        List<ResourceAllocation> resourceAllocations = 
            getAllocationsForResource(resourceId);
        
        if (resourceAllocations.isEmpty()) {
            return 1.0f; // Nothing allocated, assume efficient
        }
        
        // Calculate total allocated
        float totalAllocated = 0.0f;
        float totalOptimal = 0.0f;
        
        for (ResourceAllocation allocation : resourceAllocations) {
            totalAllocated += allocation.getValue();
            
            // Get consumer
            ResourceConsumer consumer = consumers.get(allocation.getConsumerId());
            if (consumer != null) {
                totalOptimal += consumer.getOptimalConsumption();
            }
        }
        
        if (totalOptimal <= 0.0f) {
            return 1.0f; // No optimal consumption, assume efficient
        }
        
        // Calculate efficiency
        return Math.min(1.0f, totalAllocated / totalOptimal);
    }
    
    /**
     * Add a resource management listener
     * @param listener Listener to add
     */
    public void addListener(ResourceManagementListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a resource management listener
     * @param listener Listener to remove
     */
    public void removeListener(ResourceManagementListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Set up default resource categories
     */
    private void setupDefaultCategories() {
        // Create default categories
        
        // Primary resources (gold, wood, etc.)
        ResourceCategory primary = new ResourceCategory("primary", "Primary Resources");
        primary.setDescription("Basic resources used for production and building");
        primary.setColor(0xFFFFD700); // Gold color
        categories.put(primary.getId(), primary);
        
        // Energy resources (mana, energy, etc.)
        ResourceCategory energy = new ResourceCategory("energy", "Energy Resources");
        energy.setDescription("Energy resources used for abilities and special actions");
        energy.setColor(0xFF00FFFF); // Cyan color
        categories.put(energy.getId(), energy);
        
        // Health resources (health, shields, etc.)
        ResourceCategory health = new ResourceCategory("health", "Health Resources");
        health.setDescription("Health and protection resources");
        health.setColor(0xFF00FF00); // Green color
        categories.put(health.getId(), health);
        
        // Special resources (special materials, rare items, etc.)
        ResourceCategory special = new ResourceCategory("special", "Special Resources");
        special.setDescription("Rare and special resources");
        special.setColor(0xFFFF00FF); // Magenta color
        categories.put(special.getId(), special);
        
        // Time resources (cooldowns, timers, etc.)
        ResourceCategory time = new ResourceCategory("time", "Time Resources");
        time.setDescription("Time-based resources and cooldowns");
        time.setColor(0xFFFF6A00); // Orange color
        categories.put(time.getId(), time);
    }
    
    /**
     * Update resources based on production and consumption
     */
    private void updateResources() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        
        if (deltaTime <= 0.0f) {
            return;
        }
        
        // Update resources from producers
        for (ResourceProducer producer : producers.values()) {
            String resourceId = producer.getOutputResourceId();
            ResourceTracker tracker = resources.get(resourceId);
            
            if (tracker != null && producer.isActive()) {
                float produced = producer.getProductionRate() * deltaTime;
                float newValue = tracker.getValue() + produced;
                
                // Cap at max value if needed
                if (tracker.getMaxValue() > 0.0f) {
                    newValue = Math.min(newValue, tracker.getMaxValue());
                }
                
                // Update tracker
                tracker.setValue(newValue);
                
                // Add history entry
                addHistoryEntry(resourceId, newValue);
                
                // Notify listeners
                for (ResourceManagementListener listener : listeners) {
                    listener.onResourceUpdated(tracker);
                }
            }
        }
        
        // Update resources from consumers
        for (ResourceConsumer consumer : consumers.values()) {
            String resourceId = consumer.getInputResourceId();
            ResourceTracker tracker = resources.get(resourceId);
            
            if (tracker != null && consumer.isActive()) {
                // Get current allocation
                float allocated = 0.0f;
                for (ResourceAllocation allocation : allocations) {
                    if (allocation.getResourceId().equals(resourceId) && 
                        allocation.getConsumerId().equals(consumer.getId())) {
                        allocated = allocation.getValue();
                        break;
                    }
                }
                
                // Calculate consumption
                float consumptionRate = consumer.getConsumptionRate();
                float consumed = Math.min(allocated, consumptionRate * deltaTime);
                float newValue = tracker.getValue() - consumed;
                
                // Ensure non-negative
                newValue = Math.max(0.0f, newValue);
                
                // Update tracker
                tracker.setValue(newValue);
                
                // Add history entry
                addHistoryEntry(resourceId, newValue);
                
                // Notify listeners
                for (ResourceManagementListener listener : listeners) {
                    listener.onResourceUpdated(tracker);
                }
            }
        }
    }
    
    /**
     * Update resource history
     */
    private void updateResourceHistory() {
        // Prune history if needed
        for (List<ResourceHistoryEntry> history : resourceHistory.values()) {
            // Keep only last 100 entries
            while (history.size() > 100) {
                history.remove(0);
            }
        }
    }
    
    /**
     * Add a history entry for a resource
     * @param resourceId Resource ID
     * @param value Resource value
     */
    private void addHistoryEntry(String resourceId, float value) {
        List<ResourceHistoryEntry> history = resourceHistory.get(resourceId);
        
        if (history != null) {
            ResourceHistoryEntry entry = new ResourceHistoryEntry(
                System.currentTimeMillis(), value);
            history.add(entry);
        }
    }
    
    /**
     * Update resource allocations
     */
    private void updateResourceAllocations() {
        // Check for allocation changes
        List<ResourceAllocationRecommendation> recommendations = getOptimalAllocations();
        
        if (!recommendations.isEmpty()) {
            // Notify listeners of recommendations
            for (ResourceManagementListener listener : listeners) {
                listener.onAllocationRecommendationsUpdated(recommendations);
            }
        }
    }
    
    /**
     * Resource Category class
     * Defines a category of resources
     */
    public static class ResourceCategory {
        private final String id;
        private String name;
        private String description;
        private int color;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Category ID
         * @param name Category name
         */
        public ResourceCategory(String id, String name) {
            this.id = id;
            this.name = name;
            this.description = "";
            this.color = 0xFFFFFFFF; // White by default
            this.properties = new HashMap<>();
        }
        
        /**
         * Get category ID
         * @return Category ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get category name
         * @return Category name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set category name
         * @param name Category name
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
         * Get category color
         * @return Color (ARGB)
         */
        public int getColor() {
            return color;
        }
        
        /**
         * Set category color
         * @param color Color (ARGB)
         */
        public void setColor(int color) {
            this.color = color;
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
     * Resource Tracker class
     * Tracks a specific resource's value and properties
     */
    public static class ResourceTracker {
        private final String id;
        private String name;
        private final String categoryId;
        private float value;
        private float maxValue;
        private float changeRate;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Resource ID
         * @param name Resource name
         * @param categoryId Category ID
         */
        public ResourceTracker(String id, String name, String categoryId) {
            this.id = id;
            this.name = name;
            this.categoryId = categoryId;
            this.value = 0.0f;
            this.maxValue = 0.0f;
            this.changeRate = 0.0f;
            this.properties = new HashMap<>();
        }
        
        /**
         * Get resource ID
         * @return Resource ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get resource name
         * @return Resource name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set resource name
         * @param name Resource name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get category ID
         * @return Category ID
         */
        public String getCategoryId() {
            return categoryId;
        }
        
        /**
         * Get current value
         * @return Current value
         */
        public float getValue() {
            return value;
        }
        
        /**
         * Set current value
         * @param value Current value
         */
        public void setValue(float value) {
            this.value = Math.max(0.0f, value);
        }
        
        /**
         * Get maximum value
         * @return Maximum value (0 for unlimited)
         */
        public float getMaxValue() {
            return maxValue;
        }
        
        /**
         * Set maximum value
         * @param maxValue Maximum value (0 for unlimited)
         */
        public void setMaxValue(float maxValue) {
            this.maxValue = Math.max(0.0f, maxValue);
        }
        
        /**
         * Get percentage filled
         * @return Percentage (0.0-1.0) or 0.0 if unlimited
         */
        public float getPercentage() {
            if (maxValue <= 0.0f) {
                return 0.0f; // Unlimited
            }
            
            return Math.min(1.0f, value / maxValue);
        }
        
        /**
         * Get change rate
         * @return Change rate (units per second)
         */
        public float getChangeRate() {
            return changeRate;
        }
        
        /**
         * Set change rate
         * @param changeRate Change rate (units per second)
         */
        public void setChangeRate(float changeRate) {
            this.changeRate = changeRate;
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
            if (maxValue <= 0.0f) {
                return name + ": " + value;
            } else {
                return name + ": " + value + "/" + maxValue;
            }
        }
    }
    
    /**
     * Resource Producer class
     * Represents an entity that produces resources
     */
    public static class ResourceProducer {
        private final String id;
        private String name;
        private final String outputResourceId;
        private float productionRate;
        private boolean active;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Producer ID
         * @param name Producer name
         * @param outputResourceId Output resource ID
         * @param productionRate Production rate (units per second)
         */
        public ResourceProducer(String id, String name, 
                               String outputResourceId, float productionRate) {
            this.id = id;
            this.name = name;
            this.outputResourceId = outputResourceId;
            this.productionRate = Math.max(0.0f, productionRate);
            this.active = true;
            this.properties = new HashMap<>();
        }
        
        /**
         * Get producer ID
         * @return Producer ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get producer name
         * @return Producer name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set producer name
         * @param name Producer name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get output resource ID
         * @return Output resource ID
         */
        public String getOutputResourceId() {
            return outputResourceId;
        }
        
        /**
         * Get production rate
         * @return Production rate (units per second)
         */
        public float getProductionRate() {
            return productionRate;
        }
        
        /**
         * Set production rate
         * @param productionRate Production rate (units per second)
         */
        public void setProductionRate(float productionRate) {
            this.productionRate = Math.max(0.0f, productionRate);
        }
        
        /**
         * Check if producer is active
         * @return true if active
         */
        public boolean isActive() {
            return active;
        }
        
        /**
         * Set active state
         * @param active Active state
         */
        public void setActive(boolean active) {
            this.active = active;
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
            return name + " (" + productionRate + "/sec)";
        }
    }
    
    /**
     * Resource Consumer class
     * Represents an entity that consumes resources
     */
    public static class ResourceConsumer {
        private final String id;
        private String name;
        private final String inputResourceId;
        private float consumptionRate;
        private float optimalConsumption;
        private int priority;
        private boolean active;
        private final Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Consumer ID
         * @param name Consumer name
         * @param inputResourceId Input resource ID
         * @param consumptionRate Consumption rate (units per second)
         * @param priority Priority (lower number = higher priority)
         */
        public ResourceConsumer(String id, String name, String inputResourceId,
                               float consumptionRate, int priority) {
            this.id = id;
            this.name = name;
            this.inputResourceId = inputResourceId;
            this.consumptionRate = Math.max(0.0f, consumptionRate);
            this.optimalConsumption = consumptionRate * 5.0f; // Default to 5 seconds worth
            this.priority = priority;
            this.active = true;
            this.properties = new HashMap<>();
        }
        
        /**
         * Get consumer ID
         * @return Consumer ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get consumer name
         * @return Consumer name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set consumer name
         * @param name Consumer name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get input resource ID
         * @return Input resource ID
         */
        public String getInputResourceId() {
            return inputResourceId;
        }
        
        /**
         * Get consumption rate
         * @return Consumption rate (units per second)
         */
        public float getConsumptionRate() {
            return consumptionRate;
        }
        
        /**
         * Set consumption rate
         * @param consumptionRate Consumption rate (units per second)
         */
        public void setConsumptionRate(float consumptionRate) {
            this.consumptionRate = Math.max(0.0f, consumptionRate);
        }
        
        /**
         * Get optimal consumption level
         * @return Optimal consumption
         */
        public float getOptimalConsumption() {
            return optimalConsumption;
        }
        
        /**
         * Set optimal consumption level
         * @param optimalConsumption Optimal consumption
         */
        public void setOptimalConsumption(float optimalConsumption) {
            this.optimalConsumption = Math.max(0.0f, optimalConsumption);
        }
        
        /**
         * Get priority
         * @return Priority (lower number = higher priority)
         */
        public int getPriority() {
            return priority;
        }
        
        /**
         * Set priority
         * @param priority Priority (lower number = higher priority)
         */
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        /**
         * Check if consumer is active
         * @return true if active
         */
        public boolean isActive() {
            return active;
        }
        
        /**
         * Set active state
         * @param active Active state
         */
        public void setActive(boolean active) {
            this.active = active;
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
            return name + " (" + consumptionRate + "/sec, priority " + priority + ")";
        }
    }
    
    /**
     * Resource Allocation class
     * Represents an allocation of a resource to a consumer
     */
    public static class ResourceAllocation {
        private final String resourceId;
        private final String consumerId;
        private float value;
        private long timestamp;
        
        /**
         * Constructor
         * @param resourceId Resource ID
         * @param consumerId Consumer ID
         * @param value Allocated value
         */
        public ResourceAllocation(String resourceId, String consumerId, float value) {
            this.resourceId = resourceId;
            this.consumerId = consumerId;
            this.value = Math.max(0.0f, value);
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Get resource ID
         * @return Resource ID
         */
        public String getResourceId() {
            return resourceId;
        }
        
        /**
         * Get consumer ID
         * @return Consumer ID
         */
        public String getConsumerId() {
            return consumerId;
        }
        
        /**
         * Get allocated value
         * @return Allocated value
         */
        public float getValue() {
            return value;
        }
        
        /**
         * Set allocated value
         * @param value Allocated value
         */
        public void setValue(float value) {
            this.value = Math.max(0.0f, value);
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Get timestamp
         * @return Timestamp when allocation was created or updated
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return resourceId + " -> " + consumerId + ": " + value;
        }
    }
    
    /**
     * Resource History Entry class
     * Represents a historical value of a resource
     */
    public static class ResourceHistoryEntry {
        private final long timestamp;
        private final float value;
        
        /**
         * Constructor
         * @param timestamp Entry timestamp
         * @param value Resource value
         */
        public ResourceHistoryEntry(long timestamp, float value) {
            this.timestamp = timestamp;
            this.value = value;
        }
        
        /**
         * Get timestamp
         * @return Entry timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Get resource value
         * @return Resource value
         */
        public float getValue() {
            return value;
        }
    }
    
    /**
     * Resource Allocation Recommendation class
     * Represents a recommended change to resource allocation
     */
    public static class ResourceAllocationRecommendation {
        private final String resourceId;
        private final String consumerId;
        private final String resourceName;
        private final String consumerName;
        private final float currentValue;
        private final float recommendedValue;
        private final int priority;
        
        /**
         * Constructor
         * @param resourceId Resource ID
         * @param consumerId Consumer ID
         * @param resourceName Resource name
         * @param consumerName Consumer name
         * @param currentValue Current allocation value
         * @param recommendedValue Recommended allocation value
         * @param priority Priority (lower number = higher priority)
         */
        public ResourceAllocationRecommendation(String resourceId, String consumerId,
                                              String resourceName, String consumerName,
                                              float currentValue, float recommendedValue,
                                              int priority) {
            this.resourceId = resourceId;
            this.consumerId = consumerId;
            this.resourceName = resourceName;
            this.consumerName = consumerName;
            this.currentValue = currentValue;
            this.recommendedValue = recommendedValue;
            this.priority = priority;
        }
        
        /**
         * Get resource ID
         * @return Resource ID
         */
        public String getResourceId() {
            return resourceId;
        }
        
        /**
         * Get consumer ID
         * @return Consumer ID
         */
        public String getConsumerId() {
            return consumerId;
        }
        
        /**
         * Get resource name
         * @return Resource name
         */
        public String getResourceName() {
            return resourceName;
        }
        
        /**
         * Get consumer name
         * @return Consumer name
         */
        public String getConsumerName() {
            return consumerName;
        }
        
        /**
         * Get current allocation value
         * @return Current value
         */
        public float getCurrentValue() {
            return currentValue;
        }
        
        /**
         * Get recommended allocation value
         * @return Recommended value
         */
        public float getRecommendedValue() {
            return recommendedValue;
        }
        
        /**
         * Get priority
         * @return Priority (lower number = higher priority)
         */
        public int getPriority() {
            return priority;
        }
        
        /**
         * Get difference between recommended and current
         * @return Difference value
         */
        public float getDifference() {
            return recommendedValue - currentValue;
        }
        
        /**
         * Check if recommendation is to increase allocation
         * @return true if increase recommended
         */
        public boolean isIncrease() {
            return recommendedValue > currentValue;
        }
        
        @Override
        public String toString() {
            String direction = isIncrease() ? "Increase" : "Decrease";
            return direction + " " + resourceName + " allocation to " + 
                   consumerName + " from " + currentValue + " to " + recommendedValue;
        }
    }
    
    /**
     * Resource Management Listener interface
     * For receiving resource management events
     */
    public interface ResourceManagementListener {
        /**
         * Called when tracking starts
         * @param gameId Game ID
         */
        void onTrackingStarted(String gameId);
        
        /**
         * Called when tracking stops
         */
        void onTrackingStopped();
        
        /**
         * Called when a resource is updated
         * @param resource Updated resource
         */
        void onResourceUpdated(ResourceTracker resource);
        
        /**
         * Called when a producer is added
         * @param producer Added producer
         */
        void onProducerAdded(ResourceProducer producer);
        
        /**
         * Called when a consumer is added
         * @param consumer Added consumer
         */
        void onConsumerAdded(ResourceConsumer consumer);
        
        /**
         * Called when an allocation is added
         * @param allocation Added allocation
         */
        void onAllocationAdded(ResourceAllocation allocation);
        
        /**
         * Called when an allocation is removed
         * @param allocation Removed allocation
         */
        void onAllocationRemoved(ResourceAllocation allocation);
        
        /**
         * Called when allocation recommendations are updated
         * @param recommendations Updated recommendations
         */
        void onAllocationRecommendationsUpdated(
            List<ResourceAllocationRecommendation> recommendations);
    }
}
