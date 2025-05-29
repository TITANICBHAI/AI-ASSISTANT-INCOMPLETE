package com.aiassistant.ai.features.resource;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource Management Manager
 * Simplified interface for using the resource management feature
 */
public class ResourceManagementManager implements ResourceManagementFeature.ResourceManagementListener {
    private static final String TAG = "ResourceManager";
    
    private final Context context;
    private final ResourceManagementFeature resourceManagementFeature;
    private final List<ResourceEventListener> eventListeners;
    private final Map<String, ResourceStatus> resourceStatusMap;
    
    /**
     * Constructor
     * @param context Application context
     * @param resourceManagementFeature Resource management feature
     */
    public ResourceManagementManager(Context context, 
                                   ResourceManagementFeature resourceManagementFeature) {
        this.context = context;
        this.resourceManagementFeature = resourceManagementFeature;
        this.eventListeners = new ArrayList<>();
        this.resourceStatusMap = new HashMap<>();
        
        // Register as listener
        resourceManagementFeature.addListener(this);
    }
    
    /**
     * Start resource tracking
     * @param gameId Game ID
     */
    public void startTracking(String gameId) {
        if (resourceManagementFeature.isEnabled() && !resourceManagementFeature.isTracking()) {
            resourceManagementFeature.startTracking(gameId);
        }
    }
    
    /**
     * Stop resource tracking
     */
    public void stopTracking() {
        if (resourceManagementFeature.isEnabled() && resourceManagementFeature.isTracking()) {
            resourceManagementFeature.stopTracking();
        }
    }
    
    /**
     * Check if tracking is active
     * @return true if tracking
     */
    public boolean isTracking() {
        return resourceManagementFeature.isEnabled() && 
               resourceManagementFeature.isTracking();
    }
    
    /**
     * Track a resource
     * @param resourceId Resource ID
     * @param name Resource name
     * @param categoryId Category ID
     * @param value Current value
     * @param maxValue Maximum value (0 for unlimited)
     */
    public void trackResource(String resourceId, String name, 
                            String categoryId, float value, float maxValue) {
        if (resourceManagementFeature.isEnabled() && resourceManagementFeature.isTracking()) {
            resourceManagementFeature.trackResource(
                resourceId, name, categoryId, value, maxValue);
            
            // Update status map
            updateResourceStatus(resourceId);
        }
    }
    
    /**
     * Add a resource producer
     * @param name Producer name
     * @param resourceId Resource ID to produce
     * @param rate Production rate
     * @return Created producer
     */
    public ResourceManagementFeature.ResourceProducer addProducer(
            String name, String resourceId, float rate) {
        if (!resourceManagementFeature.isEnabled() || !resourceManagementFeature.isTracking()) {
            return null;
        }
        
        // Create producer
        String id = "producer_" + System.currentTimeMillis();
        ResourceManagementFeature.ResourceProducer producer = 
            new ResourceManagementFeature.ResourceProducer(id, name, resourceId, rate);
        
        // Add to feature
        resourceManagementFeature.addProducer(producer);
        
        // Update status
        updateResourceStatus(resourceId);
        
        return producer;
    }
    
    /**
     * Add a resource consumer
     * @param name Consumer name
     * @param resourceId Resource ID to consume
     * @param rate Consumption rate
     * @param priority Priority (lower number = higher priority)
     * @return Created consumer
     */
    public ResourceManagementFeature.ResourceConsumer addConsumer(
            String name, String resourceId, float rate, int priority) {
        if (!resourceManagementFeature.isEnabled() || !resourceManagementFeature.isTracking()) {
            return null;
        }
        
        // Create consumer
        String id = "consumer_" + System.currentTimeMillis();
        ResourceManagementFeature.ResourceConsumer consumer = 
            new ResourceManagementFeature.ResourceConsumer(id, name, resourceId, rate, priority);
        
        // Add to feature
        resourceManagementFeature.addConsumer(consumer);
        
        // Update status
        updateResourceStatus(resourceId);
        
        return consumer;
    }
    
    /**
     * Allocate a resource to a consumer
     * @param resourceId Resource ID
     * @param consumerId Consumer ID
     * @param value Amount to allocate
     */
    public void allocateResource(String resourceId, String consumerId, float value) {
        if (!resourceManagementFeature.isEnabled() || !resourceManagementFeature.isTracking()) {
            return;
        }
        
        // Create allocation
        ResourceManagementFeature.ResourceAllocation allocation = 
            new ResourceManagementFeature.ResourceAllocation(resourceId, consumerId, value);
        
        // Add to feature
        resourceManagementFeature.addAllocation(allocation);
        
        // Update status
        updateResourceStatus(resourceId);
    }
    
    /**
     * Apply recommended allocations
     * @return Number of recommendations applied
     */
    public int applyRecommendedAllocations() {
        if (!resourceManagementFeature.isEnabled() || !resourceManagementFeature.isTracking()) {
            return 0;
        }
        
        // Get recommendations
        List<ResourceManagementFeature.ResourceAllocationRecommendation> recommendations = 
            resourceManagementFeature.getOptimalAllocations();
        
        int count = 0;
        
        // Apply each recommendation
        for (ResourceManagementFeature.ResourceAllocationRecommendation recommendation : recommendations) {
            // Remove existing allocation
            resourceManagementFeature.removeAllocation(
                recommendation.getResourceId(), 
                recommendation.getConsumerId());
            
            // Create new allocation
            ResourceManagementFeature.ResourceAllocation allocation = 
                new ResourceManagementFeature.ResourceAllocation(
                    recommendation.getResourceId(),
                    recommendation.getConsumerId(),
                    recommendation.getRecommendedValue()
                );
            
            // Add new allocation
            resourceManagementFeature.addAllocation(allocation);
            
            // Update status
            updateResourceStatus(recommendation.getResourceId());
            
            count++;
        }
        
        if (count > 0) {
            Log.d(TAG, "Applied " + count + " resource allocation recommendations");
        }
        
        return count;
    }
    
    /**
     * Get resource status
     * @param resourceId Resource ID
     * @return Resource status or null if not found
     */
    public ResourceStatus getResourceStatus(String resourceId) {
        return resourceStatusMap.get(resourceId);
    }
    
    /**
     * Get all resource statuses
     * @return Map of resource statuses
     */
    public Map<String, ResourceStatus> getAllResourceStatus() {
        return new HashMap<>(resourceStatusMap);
    }
    
    /**
     * Get critical resources
     * @return List of critical resource statuses
     */
    public List<ResourceStatus> getCriticalResources() {
        List<ResourceStatus> critical = new ArrayList<>();
        
        for (ResourceStatus status : resourceStatusMap.values()) {
            if (status.getStatus() == ResourceStatus.Status.CRITICAL) {
                critical.add(status);
            }
        }
        
        return critical;
    }
    
    /**
     * Get allocation recommendations
     * @return List of allocation recommendations
     */
    public List<ResourceManagementFeature.ResourceAllocationRecommendation> getRecommendations() {
        if (resourceManagementFeature.isEnabled() && resourceManagementFeature.isTracking()) {
            return resourceManagementFeature.getOptimalAllocations();
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Add a resource event listener
     * @param listener Listener to add
     */
    public void addEventListener(ResourceEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Remove a resource event listener
     * @param listener Listener to remove
     */
    public void removeEventListener(ResourceEventListener listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * Update resource status
     * @param resourceId Resource ID
     */
    private void updateResourceStatus(String resourceId) {
        ResourceManagementFeature.ResourceTracker tracker = 
            resourceManagementFeature.getResource(resourceId);
        
        if (tracker == null) {
            return;
        }
        
        // Get resource details
        float value = tracker.getValue();
        float maxValue = tracker.getMaxValue();
        float percentage = tracker.getPercentage();
        
        // Get rates
        float productionRate = resourceManagementFeature.getProductionRate(resourceId);
        float consumptionRate = resourceManagementFeature.getConsumptionRate(resourceId);
        float netRate = productionRate - consumptionRate;
        
        // Get time estimates
        float timeUntilDepleted = resourceManagementFeature.getTimeUntilDepleted(resourceId);
        float timeUntilFilled = resourceManagementFeature.getTimeUntilFilled(resourceId);
        
        // Get efficiency
        float efficiency = resourceManagementFeature.getResourceEfficiency(resourceId);
        
        // Determine status
        ResourceStatus.Status status;
        String statusMessage;
        
        if (maxValue > 0.0f && percentage < 0.2f && netRate < 0.0f) {
            // Critical - low and depleting
            status = ResourceStatus.Status.CRITICAL;
            statusMessage = "Critical: " + timeUntilDepleted + " seconds until depleted";
        } else if (maxValue > 0.0f && percentage < 0.3f) {
            // Warning - low but not depleting
            status = ResourceStatus.Status.WARNING;
            statusMessage = "Low: " + Math.floor(value) + "/" + Math.floor(maxValue);
        } else if (netRate < 0.0f && timeUntilDepleted < 30.0f) {
            // Warning - depleting soon
            status = ResourceStatus.Status.WARNING;
            statusMessage = "Depleting: " + timeUntilDepleted + " seconds remaining";
        } else if (netRate > 0.0f) {
            // Stable or growing
            status = ResourceStatus.Status.GOOD;
            statusMessage = "Stable: " + (maxValue > 0.0f ? 
                                        (Math.floor(value) + "/" + Math.floor(maxValue)) : 
                                        String.valueOf(Math.floor(value)));
        } else {
            // Neutral
            status = ResourceStatus.Status.NEUTRAL;
            statusMessage = "Neutral: " + Math.floor(value);
        }
        
        // Create status object
        ResourceStatus resourceStatus = new ResourceStatus(
            resourceId,
            tracker.getName(),
            tracker.getCategoryId(),
            value,
            maxValue,
            productionRate,
            consumptionRate,
            efficiency,
            status,
            statusMessage
        );
        
        // Store in map
        resourceStatusMap.put(resourceId, resourceStatus);
        
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onResourceStatusUpdated(resourceStatus);
        }
    }
    
    // ResourceManagementListener implementation
    
    @Override
    public void onTrackingStarted(String gameId) {
        // Clear status map
        resourceStatusMap.clear();
        
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onTrackingStarted(gameId);
        }
    }
    
    @Override
    public void onTrackingStopped() {
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onTrackingStopped();
        }
    }
    
    @Override
    public void onResourceUpdated(ResourceManagementFeature.ResourceTracker resource) {
        // Update status for this resource
        updateResourceStatus(resource.getId());
    }
    
    @Override
    public void onProducerAdded(ResourceManagementFeature.ResourceProducer producer) {
        // Update status for the resource
        updateResourceStatus(producer.getOutputResourceId());
        
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onProducerAdded(producer.getName(), producer.getOutputResourceId());
        }
    }
    
    @Override
    public void onConsumerAdded(ResourceManagementFeature.ResourceConsumer consumer) {
        // Update status for the resource
        updateResourceStatus(consumer.getInputResourceId());
        
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onConsumerAdded(consumer.getName(), consumer.getInputResourceId());
        }
    }
    
    @Override
    public void onAllocationAdded(ResourceManagementFeature.ResourceAllocation allocation) {
        // Update status for the resource
        updateResourceStatus(allocation.getResourceId());
    }
    
    @Override
    public void onAllocationRemoved(ResourceManagementFeature.ResourceAllocation allocation) {
        // Update status for the resource
        updateResourceStatus(allocation.getResourceId());
    }
    
    @Override
    public void onAllocationRecommendationsUpdated(
            List<ResourceManagementFeature.ResourceAllocationRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return;
        }
        
        // Notify listeners
        for (ResourceEventListener listener : eventListeners) {
            listener.onRecommendationsUpdated(recommendations.size());
        }
    }
    
    /**
     * Resource Status class
     * Represents the current status of a resource
     */
    public static class ResourceStatus {
        /**
         * Status enumeration
         */
        public enum Status {
            GOOD,
            NEUTRAL,
            WARNING,
            CRITICAL
        }
        
        private final String resourceId;
        private final String name;
        private final String categoryId;
        private final float value;
        private final float maxValue;
        private final float productionRate;
        private final float consumptionRate;
        private final float efficiency;
        private final Status status;
        private final String statusMessage;
        
        /**
         * Constructor
         * @param resourceId Resource ID
         * @param name Resource name
         * @param categoryId Category ID
         * @param value Current value
         * @param maxValue Maximum value
         * @param productionRate Production rate
         * @param consumptionRate Consumption rate
         * @param efficiency Resource efficiency
         * @param status Status
         * @param statusMessage Status message
         */
        public ResourceStatus(String resourceId, String name, String categoryId,
                            float value, float maxValue, float productionRate,
                            float consumptionRate, float efficiency,
                            Status status, String statusMessage) {
            this.resourceId = resourceId;
            this.name = name;
            this.categoryId = categoryId;
            this.value = value;
            this.maxValue = maxValue;
            this.productionRate = productionRate;
            this.consumptionRate = consumptionRate;
            this.efficiency = efficiency;
            this.status = status;
            this.statusMessage = statusMessage;
        }
        
        /**
         * Get resource ID
         * @return Resource ID
         */
        public String getResourceId() {
            return resourceId;
        }
        
        /**
         * Get resource name
         * @return Resource name
         */
        public String getName() {
            return name;
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
         * Get maximum value
         * @return Maximum value
         */
        public float getMaxValue() {
            return maxValue;
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
         * Get production rate
         * @return Production rate
         */
        public float getProductionRate() {
            return productionRate;
        }
        
        /**
         * Get consumption rate
         * @return Consumption rate
         */
        public float getConsumptionRate() {
            return consumptionRate;
        }
        
        /**
         * Get net rate
         * @return Net rate (production - consumption)
         */
        public float getNetRate() {
            return productionRate - consumptionRate;
        }
        
        /**
         * Get efficiency
         * @return Efficiency (0.0-1.0)
         */
        public float getEfficiency() {
            return efficiency;
        }
        
        /**
         * Get status
         * @return Status
         */
        public Status getStatus() {
            return status;
        }
        
        /**
         * Get status message
         * @return Status message
         */
        public String getStatusMessage() {
            return statusMessage;
        }
        
        /**
         * Get status color
         * @return Color (ARGB)
         */
        public int getStatusColor() {
            switch (status) {
                case GOOD:
                    return 0xFF00FF00; // Green
                case NEUTRAL:
                    return 0xFF808080; // Gray
                case WARNING:
                    return 0xFFFFFF00; // Yellow
                case CRITICAL:
                    return 0xFFFF0000; // Red
                default:
                    return 0xFFFFFFFF; // White
            }
        }
        
        @Override
        public String toString() {
            return name + ": " + statusMessage;
        }
    }
    
    /**
     * Resource Event Listener interface
     * For receiving resource events
     */
    public interface ResourceEventListener {
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
         * Called when resource status is updated
         * @param status Updated status
         */
        void onResourceStatusUpdated(ResourceStatus status);
        
        /**
         * Called when a producer is added
         * @param producerName Producer name
         * @param resourceId Resource ID
         */
        void onProducerAdded(String producerName, String resourceId);
        
        /**
         * Called when a consumer is added
         * @param consumerName Consumer name
         * @param resourceId Resource ID
         */
        void onConsumerAdded(String consumerName, String resourceId);
        
        /**
         * Called when recommendations are updated
         * @param count Number of recommendations
         */
        void onRecommendationsUpdated(int count);
    }
}
