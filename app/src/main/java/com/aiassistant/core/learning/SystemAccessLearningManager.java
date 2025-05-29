package com.aiassistant.core.learning;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.aiassistant.security.AccessControl;
import com.aiassistant.security.ExternalServiceCompatibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * System Access Learning Manager
 * 
 * This component leverages the AI's unique system access to learn from user interactions
 * across different applications, build contextual understanding from real-world usage patterns,
 * and connect theoretical knowledge with practical applications.
 */
public class SystemAccessLearningManager {
    private static final String TAG = "SystemAccessLearning";
    
    // Interaction types
    public enum InteractionType {
        APP_USAGE,          // Application usage patterns
        UI_INTERACTION,     // UI element interactions
        WORKFLOW_PATTERN,   // User workflow patterns
        CONTENT_ACCESS,     // Content access patterns
        FEATURE_USAGE,      // Feature usage within apps
        CONTEXT_SWITCH      // Context switching between apps
    }
    
    // Observation sources
    public enum ObservationSource {
        ACCESSIBILITY_SERVICE,  // From accessibility service
        USAGE_STATS,           // From usage statistics
        FOREGROUND_SERVICE,    // From foreground service
        USER_INITIATED,        // Explicitly initiated by user
        SYSTEM_EVENTS          // From system events
    }
    
    // Insight categories
    public enum InsightCategory {
        USAGE_PATTERN,      // User usage pattern insights
        EFFICIENCY,         // Efficiency improvement insights
        PREFERENCE,         // User preference insights
        BEHAVIOR,           // User behavior insights
        KNOWLEDGE_NEED      // Knowledge requirement insights
    }
    
    // User interaction observation class
    public static class UserInteractionObservation {
        private final String id;
        private final InteractionType type;
        private final ObservationSource source;
        private final long timestamp;
        private String packageName;
        private String activityName;
        private String description;
        private final Map<String, Object> metadata;
        
        public UserInteractionObservation(InteractionType type, ObservationSource source) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
            this.packageName = "";
            this.activityName = "";
            this.description = "";
            this.metadata = new HashMap<>();
        }
        
        // Getters
        public String getId() { return id; }
        public InteractionType getType() { return type; }
        public ObservationSource getSource() { return source; }
        public long getTimestamp() { return timestamp; }
        public String getPackageName() { return packageName; }
        public String getActivityName() { return activityName; }
        public String getDescription() { return description; }
        
        // Setters
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }
        public void setDescription(String description) { this.description = description; }
        
        // Metadata management
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
        public Object getMetadata(String key) { return metadata.get(key); }
        public boolean hasMetadata(String key) { return metadata.containsKey(key); }
        public Map<String, Object> getAllMetadata() { return new HashMap<>(metadata); }
        
        @Override
        public String toString() {
            return "UserInteractionObservation{" +
                    "id='" + id + '\'' +
                    ", type=" + type +
                    ", source=" + source +
                    ", packageName='" + packageName + '\'' +
                    ", activityName='" + activityName + '\'' +
                    '}';
        }
    }
    
    // User behavior insight class
    public static class UserBehaviorInsight {
        private final String id;
        private final InsightCategory category;
        private final long discoveryTime;
        private String title;
        private String description;
        private double confidence;
        private final List<String> relatedObservationIds;
        private final Map<String, Object> metadata;
        
        public UserBehaviorInsight(String title, InsightCategory category, String description) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.category = category;
            this.description = description;
            this.discoveryTime = System.currentTimeMillis();
            this.confidence = 0.5; // Default confidence
            this.relatedObservationIds = new ArrayList<>();
            this.metadata = new HashMap<>();
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public InsightCategory getCategory() { return category; }
        public String getDescription() { return description; }
        public long getDiscoveryTime() { return discoveryTime; }
        public double getConfidence() { return confidence; }
        
        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setConfidence(double confidence) { 
            this.confidence = Math.max(0.0, Math.min(1.0, confidence)); 
        }
        
        // Related observations management
        public void addRelatedObservation(String observationId) {
            if (!relatedObservationIds.contains(observationId)) {
                relatedObservationIds.add(observationId);
            }
        }
        public List<String> getRelatedObservationIds() { return new ArrayList<>(relatedObservationIds); }
        
        // Metadata management
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
        public Object getMetadata(String key) { return metadata.get(key); }
        public boolean hasMetadata(String key) { return metadata.containsKey(key); }
        public Map<String, Object> getAllMetadata() { return new HashMap<>(metadata); }
        
        @Override
        public String toString() {
            return "UserBehaviorInsight{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", category=" + category +
                    ", confidence=" + String.format("%.2f", confidence) +
                    '}';
        }
    }
    
    // App usage pattern class
    public static class AppUsagePattern {
        private final String packageName;
        private String appName;
        private int usageCount;
        private long totalUsageTime;
        private long firstUsageTime;
        private long lastUsageTime;
        private final Map<String, Integer> relatedApps;
        private final Map<String, Integer> precedingApps;
        private final Map<String, Integer> followingApps;
        private final Map<Integer, Integer> timeOfDayUsage; // Hour (0-23) -> count
        
        public AppUsagePattern(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
            this.usageCount = 0;
            this.totalUsageTime = 0;
            this.firstUsageTime = System.currentTimeMillis();
            this.lastUsageTime = this.firstUsageTime;
            this.relatedApps = new HashMap<>();
            this.precedingApps = new HashMap<>();
            this.followingApps = new HashMap<>();
            this.timeOfDayUsage = new HashMap<>();
            
            // Initialize time of day usage
            for (int i = 0; i < 24; i++) {
                timeOfDayUsage.put(i, 0);
            }
        }
        
        // Getters
        public String getPackageName() { return packageName; }
        public String getAppName() { return appName; }
        public int getUsageCount() { return usageCount; }
        public long getTotalUsageTime() { return totalUsageTime; }
        public long getFirstUsageTime() { return firstUsageTime; }
        public long getLastUsageTime() { return lastUsageTime; }
        
        // Setters
        public void setAppName(String appName) { this.appName = appName; }
        
        // Usage tracking
        public void recordUsage(long usageTime) {
            usageCount++;
            totalUsageTime += usageTime;
            lastUsageTime = System.currentTimeMillis();
            
            // Record time of day
            int hour = getHourOfDay(System.currentTimeMillis());
            timeOfDayUsage.put(hour, timeOfDayUsage.get(hour) + 1);
        }
        
        // Related apps
        public void addRelatedApp(String relatedPackage) {
            relatedApps.put(relatedPackage, relatedApps.getOrDefault(relatedPackage, 0) + 1);
        }
        
        public void addPrecedingApp(String precedingPackage) {
            precedingApps.put(precedingPackage, precedingApps.getOrDefault(precedingPackage, 0) + 1);
        }
        
        public void addFollowingApp(String followingPackage) {
            followingApps.put(followingPackage, followingApps.getOrDefault(followingPackage, 0) + 1);
        }
        
        // Get most common related apps
        public List<Map.Entry<String, Integer>> getMostCommonRelatedApps(int limit) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(relatedApps.entrySet());
            entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            return entries.subList(0, Math.min(limit, entries.size()));
        }
        
        // Get most common preceding apps
        public List<Map.Entry<String, Integer>> getMostCommonPrecedingApps(int limit) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(precedingApps.entrySet());
            entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            return entries.subList(0, Math.min(limit, entries.size()));
        }
        
        // Get most common following apps
        public List<Map.Entry<String, Integer>> getMostCommonFollowingApps(int limit) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(followingApps.entrySet());
            entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            return entries.subList(0, Math.min(limit, entries.size()));
        }
        
        // Get peak usage hours
        public List<Integer> getPeakUsageHours(int limit) {
            List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(timeOfDayUsage.entrySet());
            entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            
            List<Integer> peakHours = new ArrayList<>();
            int count = 0;
            for (Map.Entry<Integer, Integer> entry : entries) {
                if (count >= limit) break;
                if (entry.getValue() > 0) {
                    peakHours.add(entry.getKey());
                    count++;
                }
            }
            
            return peakHours;
        }
        
        // Helper to get hour of day (0-23)
        private int getHourOfDay(long timestamp) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            return cal.get(java.util.Calendar.HOUR_OF_DAY);
        }
        
        @Override
        public String toString() {
            return "AppUsagePattern{" +
                    "packageName='" + packageName + '\'' +
                    ", appName='" + appName + '\'' +
                    ", usageCount=" + usageCount +
                    ", totalUsageTime=" + (totalUsageTime / 1000 / 60) + " minutes" +
                    '}';
        }
    }
    
    private final Context context;
    private final AccessControl accessControl;
    private final ExternalServiceCompatibility serviceCompatibility;
    private final StructuredKnowledgeSystem knowledgeSystem;
    private final SelfDirectedLearningSystem learningSystem;
    
    // Storage
    private final List<UserInteractionObservation> recentObservations;
    private final Map<String, UserBehaviorInsight> behaviorInsights;
    private final Map<String, AppUsagePattern> appUsagePatterns;
    
    // Last known state
    private String lastForegroundPackage;
    private long lastPackageStartTime;
    
    // Listeners
    private final List<SystemAccessLearningListener> listeners;
    
    // Scheduler
    private final ScheduledExecutorService scheduler;
    private final Handler mainHandler;
    
    // Learning status
    private boolean isLearningActive = false;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control system
     * @param serviceCompatibility External service compatibility manager
     * @param knowledgeSystem Structured knowledge system
     * @param learningSystem Self-directed learning system
     */
    public SystemAccessLearningManager(Context context, AccessControl accessControl,
            ExternalServiceCompatibility serviceCompatibility,
            StructuredKnowledgeSystem knowledgeSystem,
            SelfDirectedLearningSystem learningSystem) {
        this.context = context;
        this.accessControl = accessControl;
        this.serviceCompatibility = serviceCompatibility;
        this.knowledgeSystem = knowledgeSystem;
        this.learningSystem = learningSystem;
        
        this.recentObservations = new ArrayList<>();
        this.behaviorInsights = new HashMap<>();
        this.appUsagePatterns = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        this.lastForegroundPackage = "";
        this.lastPackageStartTime = 0;
        
        // Initialize scheduler
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        Log.d(TAG, "System Access Learning Manager initialized");
    }
    
    /**
     * Start learning from system access
     */
    public void startLearning() {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for starting system access learning");
            return;
        }
        
        if (isLearningActive) {
            return;
        }
        
        Log.d(TAG, "Starting system access learning");
        
        // Schedule periodic pattern analysis
        scheduler.scheduleWithFixedDelay(
                this::analyzeAppUsagePatterns,
                300, // 5 minutes delay
                7200, // Every 2 hours
                TimeUnit.SECONDS);
        
        // Schedule periodic insight generation
        scheduler.scheduleWithFixedDelay(
                this::generateBehaviorInsights,
                600, // 10 minutes delay
                14400, // Every 4 hours
                TimeUnit.SECONDS);
        
        isLearningActive = true;
    }
    
    /**
     * Stop learning from system access
     */
    public void stopLearning() {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for stopping system access learning");
            return;
        }
        
        if (!isLearningActive) {
            return;
        }
        
        Log.d(TAG, "Stopping system access learning");
        
        // Shutdown scheduler
        scheduler.shutdown();
        
        isLearningActive = false;
    }
    
    /**
     * Process accessibility event
     * @param event Accessibility event
     */
    public void processAccessibilityEvent(AccessibilityEvent event) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for processing accessibility event");
            return;
        }
        
        if (event == null) {
            return;
        }
        
        try {
            // Get package name from event
            String packageName = String.valueOf(event.getPackageName());
            
            // Skip events from our own package
            if (packageName.equals(context.getPackageName())) {
                return;
            }
            
            // Process event based on type
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    processWindowStateChanged(event, packageName);
                    break;
                    
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    processViewClicked(event, packageName);
                    break;
                    
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                    processTextChanged(event, packageName);
                    break;
                    
                // Add more event types as needed
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing accessibility event", e);
        }
    }
    
    /**
     * Process window state changed event
     * @param event Accessibility event
     * @param packageName Package name
     */
    private void processWindowStateChanged(AccessibilityEvent event, String packageName) {
        // Track app switching
        if (!packageName.equals(lastForegroundPackage) && !lastForegroundPackage.isEmpty()) {
            // Record app usage time for previous app
            long usageTime = System.currentTimeMillis() - lastPackageStartTime;
            if (usageTime > 1000) { // Ignore very short switches
                recordAppUsage(lastForegroundPackage, usageTime);
                
                // Record app switching pattern
                if (appUsagePatterns.containsKey(lastForegroundPackage)) {
                    AppUsagePattern pattern = appUsagePatterns.get(lastForegroundPackage);
                    pattern.addFollowingApp(packageName);
                }
                
                if (appUsagePatterns.containsKey(packageName)) {
                    AppUsagePattern pattern = appUsagePatterns.get(packageName);
                    pattern.addPrecedingApp(lastForegroundPackage);
                }
            }
        }
        
        // Update current state
        lastForegroundPackage = packageName;
        lastPackageStartTime = System.currentTimeMillis();
        
        // Create observation
        UserInteractionObservation observation = new UserInteractionObservation(
                InteractionType.CONTEXT_SWITCH, ObservationSource.ACCESSIBILITY_SERVICE);
        observation.setPackageName(packageName);
        
        // Try to get activity name
        CharSequence className = event.getClassName();
        if (className != null) {
            observation.setActivityName(className.toString());
        }
        
        // Set description
        observation.setDescription("Window state changed to " + packageName);
        
        // Add observation
        addObservation(observation);
    }
    
    /**
     * Process view clicked event
     * @param event Accessibility event
     * @param packageName Package name
     */
    private void processViewClicked(AccessibilityEvent event, String packageName) {
        // Create observation
        UserInteractionObservation observation = new UserInteractionObservation(
                InteractionType.UI_INTERACTION, ObservationSource.ACCESSIBILITY_SERVICE);
        observation.setPackageName(packageName);
        
        // Try to get view info
        CharSequence className = event.getClassName();
        if (className != null) {
            observation.setActivityName(className.toString());
        }
        
        // Try to get text from event
        CharSequence text = "";
        if (event.getText() != null && !event.getText().isEmpty()) {
            text = event.getText().get(0);
        }
        
        // Set description
        observation.setDescription("Clicked view: " + (text.length() > 0 ? text : className));
        
        // Add metadata
        if (event.getContentDescription() != null) {
            observation.setMetadata("content_description", event.getContentDescription().toString());
        }
        
        // Add observation
        addObservation(observation);
    }
    
    /**
     * Process text changed event
     * @param event Accessibility event
     * @param packageName Package name
     */
    private void processTextChanged(AccessibilityEvent event, String packageName) {
        // Create observation
        UserInteractionObservation observation = new UserInteractionObservation(
                InteractionType.UI_INTERACTION, ObservationSource.ACCESSIBILITY_SERVICE);
        observation.setPackageName(packageName);
        
        // Try to get view info
        CharSequence className = event.getClassName();
        if (className != null) {
            observation.setActivityName(className.toString());
        }
        
        // Set description
        observation.setDescription("Text input in " + packageName);
        
        // We don't store the actual text for privacy reasons
        
        // Add observation
        addObservation(observation);
    }
    
    /**
     * Add a user interaction observation
     * @param observation Observation to add
     */
    private void addObservation(UserInteractionObservation observation) {
        // Add to recent observations list (limiting size)
        synchronized (recentObservations) {
            recentObservations.add(observation);
            
            // Limit size to 1000 recent observations
            while (recentObservations.size() > 1000) {
                recentObservations.remove(0);
            }
        }
        
        // Notify listeners
        notifyObservationAdded(observation);
    }
    
    /**
     * Record app usage
     * @param packageName Package name
     * @param usageTime Usage time in milliseconds
     */
    private void recordAppUsage(String packageName, long usageTime) {
        // Skip very short usages
        if (usageTime < 1000) {
            return;
        }
        
        // Get or create app usage pattern
        AppUsagePattern pattern = appUsagePatterns.get(packageName);
        if (pattern == null) {
            String appName = getAppName(packageName);
            pattern = new AppUsagePattern(packageName, appName);
            appUsagePatterns.put(packageName, pattern);
        }
        
        // Record usage
        pattern.recordUsage(usageTime);
        
        // Create observation for longer usages
        if (usageTime > 60000) { // 1 minute
            UserInteractionObservation observation = new UserInteractionObservation(
                    InteractionType.APP_USAGE, ObservationSource.USAGE_STATS);
            observation.setPackageName(packageName);
            observation.setDescription("Used " + pattern.getAppName() + " for " + 
                    (usageTime / 1000 / 60) + " minutes");
            observation.setMetadata("usage_time_ms", usageTime);
            
            addObservation(observation);
        }
    }
    
    /**
     * Get app name from package name
     * @param packageName Package name
     * @return App name or package name if not found
     */
    private String getAppName(String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            return packageName;
        }
    }
    
    /**
     * Analyze app usage patterns periodically
     */
    private void analyzeAppUsagePatterns() {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for analyzing app usage patterns");
            return;
        }
        
        Log.d(TAG, "Analyzing app usage patterns");
        
        try {
            // Get usage stats manager
            UsageStatsManager usageStatsManager = (UsageStatsManager) 
                    context.getSystemService(Context.USAGE_STATS_SERVICE);
            
            if (usageStatsManager == null) {
                Log.w(TAG, "UsageStatsManager not available");
                return;
            }
            
            // Get events from last 24 hours
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (24 * 60 * 60 * 1000);
            
            UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();
            
            String currentPackage = null;
            long currentPackageStartTime = 0;
            
            // Process events
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    // New app came to foreground
                    
                    // Record previous app usage if there was one
                    if (currentPackage != null) {
                        long usageTime = event.getTimeStamp() - currentPackageStartTime;
                        recordAppUsage(currentPackage, usageTime);
                    }
                    
                    // Update current app
                    currentPackage = event.getPackageName();
                    currentPackageStartTime = event.getTimeStamp();
                }
            }
            
            // Record last app if still active
            if (currentPackage != null) {
                long usageTime = System.currentTimeMillis() - currentPackageStartTime;
                recordAppUsage(currentPackage, usageTime);
            }
            
            // Analyze app relationships
            analyzeAppRelationships();
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing app usage patterns", e);
        }
    }
    
    /**
     * Analyze relationships between apps
     */
    private void analyzeAppRelationships() {
        Log.d(TAG, "Analyzing app relationships");
        
        try {
            // Find apps with frequent switching patterns
            for (AppUsagePattern pattern : appUsagePatterns.values()) {
                if (pattern.getUsageCount() < 5) {
                    continue; // Skip rarely used apps
                }
                
                // Get most common preceding and following apps
                List<Map.Entry<String, Integer>> precedingApps = 
                        pattern.getMostCommonPrecedingApps(3);
                List<Map.Entry<String, Integer>> followingApps = 
                        pattern.getMostCommonFollowingApps(3);
                
                // Connect related apps
                for (Map.Entry<String, Integer> entry : precedingApps) {
                    if (entry.getValue() >= 3) { // At least 3 switches
                        AppUsagePattern otherPattern = appUsagePatterns.get(entry.getKey());
                        if (otherPattern != null) {
                            pattern.addRelatedApp(entry.getKey());
                            otherPattern.addRelatedApp(pattern.getPackageName());
                        }
                    }
                }
                
                for (Map.Entry<String, Integer> entry : followingApps) {
                    if (entry.getValue() >= 3) { // At least 3 switches
                        AppUsagePattern otherPattern = appUsagePatterns.get(entry.getKey());
                        if (otherPattern != null) {
                            pattern.addRelatedApp(entry.getKey());
                            otherPattern.addRelatedApp(pattern.getPackageName());
                        }
                    }
                }
            }
            
            // Generate workflow insights from frequent patterns
            for (AppUsagePattern pattern : appUsagePatterns.values()) {
                if (pattern.getUsageCount() < 10) {
                    continue; // Skip rarely used apps
                }
                
                // Get peak usage hours
                List<Integer> peakHours = pattern.getPeakUsageHours(3);
                
                // If there are clear peak hours, create insight
                if (!peakHours.isEmpty() && 
                        pattern.getMostCommonRelatedApps(3).size() >= 2) {
                    StringBuilder peakTimeStr = new StringBuilder();
                    for (int i = 0; i < peakHours.size(); i++) {
                        int hour = peakHours.get(i);
                        peakTimeStr.append(formatHour(hour));
                        if (i < peakHours.size() - 1) {
                            peakTimeStr.append(", ");
                        }
                    }
                    
                    // Create insight about usage pattern
                    String title = "Usage pattern: " + pattern.getAppName();
                    StringBuilder description = new StringBuilder();
                    description.append("Regular usage of ").append(pattern.getAppName())
                            .append(" detected at ").append(peakTimeStr).append(".\n");
                    
                    // Add related apps info
                    List<Map.Entry<String, Integer>> relatedApps = 
                            pattern.getMostCommonRelatedApps(3);
                    if (!relatedApps.isEmpty()) {
                        description.append("Frequently used with: ");
                        for (int i = 0; i < relatedApps.size(); i++) {
                            String relatedPackage = relatedApps.get(i).getKey();
                            AppUsagePattern relatedPattern = appUsagePatterns.get(relatedPackage);
                            String relatedName = relatedPattern != null ? 
                                    relatedPattern.getAppName() : relatedPackage;
                            
                            description.append(relatedName);
                            if (i < relatedApps.size() - 1) {
                                description.append(", ");
                            }
                        }
                    }
                    
                    // Create insight
                    createBehaviorInsight(title, InsightCategory.USAGE_PATTERN, 
                            description.toString(), 0.75);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing app relationships", e);
        }
    }
    
    /**
     * Format hour for display
     * @param hour Hour (0-23)
     * @return Formatted hour string
     */
    private String formatHour(int hour) {
        if (hour == 0) {
            return "12 AM";
        } else if (hour < 12) {
            return hour + " AM";
        } else if (hour == 12) {
            return "12 PM";
        } else {
            return (hour - 12) + " PM";
        }
    }
    
    /**
     * Generate behavior insights from observations
     */
    private void generateBehaviorInsights() {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for generating behavior insights");
            return;
        }
        
        Log.d(TAG, "Generating behavior insights");
        
        try {
            // Get observations that haven't been analyzed yet
            List<UserInteractionObservation> observations;
            synchronized (recentObservations) {
                observations = new ArrayList<>(recentObservations);
            }
            
            if (observations.isEmpty()) {
                return;
            }
            
            // Group observations by package name
            Map<String, List<UserInteractionObservation>> observationsByPackage = new HashMap<>();
            for (UserInteractionObservation observation : observations) {
                String packageName = observation.getPackageName();
                if (!observationsByPackage.containsKey(packageName)) {
                    observationsByPackage.put(packageName, new ArrayList<>());
                }
                observationsByPackage.get(packageName).add(observation);
            }
            
            // Analyze each package with sufficient observations
            for (Map.Entry<String, List<UserInteractionObservation>> entry : 
                    observationsByPackage.entrySet()) {
                if (entry.getValue().size() < 10) {
                    continue; // Skip packages with few observations
                }
                
                analyzePackageObservations(entry.getKey(), entry.getValue());
            }
            
            // Detect efficiency patterns
            detectEfficiencyInsights(observations);
            
            // Detect knowledge needs
            detectKnowledgeNeedInsights(observations);
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating behavior insights", e);
        }
    }
    
    /**
     * Analyze observations for a specific package
     * @param packageName Package name
     * @param observations List of observations
     */
    private void analyzePackageObservations(String packageName, 
            List<UserInteractionObservation> observations) {
        Log.d(TAG, "Analyzing observations for package: " + packageName);
        
        try {
            // Count interaction types
            Map<InteractionType, Integer> interactionCounts = new HashMap<>();
            for (UserInteractionObservation observation : observations) {
                InteractionType type = observation.getType();
                interactionCounts.put(type, interactionCounts.getOrDefault(type, 0) + 1);
            }
            
            // Get app name
            String appName = getAppName(packageName);
            
            // Detect heavy UI interaction pattern
            if (interactionCounts.getOrDefault(InteractionType.UI_INTERACTION, 0) > 
                    observations.size() * 0.7) {
                String title = "High interaction with " + appName;
                String description = "User frequently interacts with UI elements in " + 
                        appName + ", suggesting it's an actively used application.";
                
                createBehaviorInsight(title, InsightCategory.USAGE_PATTERN, description, 0.8);
            }
            
            // Detect content consumption pattern
            if (interactionCounts.getOrDefault(InteractionType.CONTENT_ACCESS, 0) > 
                    observations.size() * 0.5) {
                String title = "Content consumption in " + appName;
                String description = "User primarily consumes content in " + appName + 
                        " with minimal UI interaction, suggesting it's used for reading or viewing.";
                
                createBehaviorInsight(title, InsightCategory.USAGE_PATTERN, description, 0.7);
            }
            
            // Check if app is in a recognized category
            String category = getCategoryForPackage(packageName);
            if (category != null) {
                // Record domain-specific insights
                if (category.equals("Productivity") && 
                        interactionCounts.getOrDefault(InteractionType.UI_INTERACTION, 0) > 20) {
                    String title = "Productivity tool usage pattern";
                    String description = "User regularly uses " + appName + 
                            " as a productivity tool with frequent interactions.";
                    
                    createBehaviorInsight(title, InsightCategory.BEHAVIOR, description, 0.75);
                    
                    // Add to knowledge system
                    addToKnowledgeSystem("User regularly uses productivity tool: " + appName,
                            StructuredKnowledgeSystem.KnowledgeDomain.USER_SPECIFIC,
                            StructuredKnowledgeSystem.KnowledgeSource.SYSTEM_OBSERVATION);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing package observations", e);
        }
    }
    
    /**
     * Detect efficiency insights from observations
     * @param observations List of observations
     */
    private void detectEfficiencyInsights(List<UserInteractionObservation> observations) {
        Log.d(TAG, "Detecting efficiency insights");
        
        try {
            // Extract timing patterns between related apps
            Map<String, Map<String, List<Long>>> appSwitchTimings = new HashMap<>();
            
            String lastPackage = null;
            long lastTimestamp = 0;
            
            // Sort observations by timestamp
            observations.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
            
            // Analyze switching patterns
            for (UserInteractionObservation observation : observations) {
                if (observation.getType() == InteractionType.CONTEXT_SWITCH) {
                    String currentPackage = observation.getPackageName();
                    long currentTimestamp = observation.getTimestamp();
                    
                    if (lastPackage != null && !lastPackage.equals(currentPackage)) {
                        // Record switch timing
                        long switchTime = currentTimestamp - lastTimestamp;
                        
                        if (!appSwitchTimings.containsKey(lastPackage)) {
                            appSwitchTimings.put(lastPackage, new HashMap<>());
                        }
                        
                        Map<String, List<Long>> targetTimings = appSwitchTimings.get(lastPackage);
                        if (!targetTimings.containsKey(currentPackage)) {
                            targetTimings.put(currentPackage, new ArrayList<>());
                        }
                        
                        targetTimings.get(currentPackage).add(switchTime);
                    }
                    
                    lastPackage = currentPackage;
                    lastTimestamp = currentTimestamp;
                }
            }
            
            // Analyze for frequent, rapid switching patterns
            for (Map.Entry<String, Map<String, List<Long>>> sourceEntry : 
                    appSwitchTimings.entrySet()) {
                String sourcePackage = sourceEntry.getKey();
                
                for (Map.Entry<String, List<Long>> targetEntry : 
                        sourceEntry.getValue().entrySet()) {
                    String targetPackage = targetEntry.getKey();
                    List<Long> switchTimes = targetEntry.getValue();
                    
                    if (switchTimes.size() >= 5) {
                        // Calculate average switch time
                        long totalTime = 0;
                        for (Long time : switchTimes) {
                            totalTime += time;
                        }
                        long avgTime = totalTime / switchTimes.size();
                        
                        // If average switch time is short (< 10 seconds)
                        if (avgTime < 10000 && avgTime > 500) {
                            String sourceApp = getAppName(sourcePackage);
                            String targetApp = getAppName(targetPackage);
                            
                            String title = "Frequent switching: " + sourceApp + " → " + targetApp;
                            String description = "User frequently switches between " + 
                                    sourceApp + " and " + targetApp + " (avg. " + 
                                    (avgTime / 1000) + " seconds). This may indicate " +
                                    "a workflow that could be optimized.";
                            
                            createBehaviorInsight(title, InsightCategory.EFFICIENCY, 
                                    description, 0.7);
                            
                            // Create learning objective for workflow optimization
                            String objectiveTitle = "Optimize workflow between " + 
                                    sourceApp + " and " + targetApp;
                            String objectiveDesc = "User frequently switches between these apps. " +
                                    "Investigate if there are ways to streamline this workflow " +
                                    "or provide integrated functionality.";
                            
                            learningSystem.createLearningObjective(objectiveTitle, objectiveDesc,
                                    SelfDirectedLearningSystem.ImportanceLevel.MEDIUM,
                                    SelfDirectedLearningSystem.LearningSource.SYSTEM_ANALYSIS);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error detecting efficiency insights", e);
        }
    }
    
    /**
     * Detect knowledge need insights from observations
     * @param observations List of observations
     */
    private void detectKnowledgeNeedInsights(List<UserInteractionObservation> observations) {
        Log.d(TAG, "Detecting knowledge need insights");
        
        try {
            // Group observations by package name
            Map<String, List<UserInteractionObservation>> observationsByPackage = new HashMap<>();
            for (UserInteractionObservation observation : observations) {
                String packageName = observation.getPackageName();
                if (!observationsByPackage.containsKey(packageName)) {
                    observationsByPackage.put(packageName, new ArrayList<>());
                }
                observationsByPackage.get(packageName).add(observation);
            }
            
            // Find packages with high usage but limited features used
            for (Map.Entry<String, List<UserInteractionObservation>> entry : 
                    observationsByPackage.entrySet()) {
                String packageName = entry.getKey();
                List<UserInteractionObservation> packageObservations = entry.getValue();
                
                if (packageObservations.size() < 20) {
                    continue; // Skip packages with few observations
                }
                
                // Get app usage pattern
                AppUsagePattern pattern = appUsagePatterns.get(packageName);
                if (pattern == null || pattern.getUsageCount() < 10) {
                    continue; // Skip rarely used apps
                }
                
                // Analyze variety of interactions
                Set<String> uniqueActivities = new HashSet<>();
                for (UserInteractionObservation obs : packageObservations) {
                    if (obs.getActivityName() != null && !obs.getActivityName().isEmpty()) {
                        uniqueActivities.add(obs.getActivityName());
                    }
                }
                
                // If limited variety of screens/activities used in a frequently used app
                if (uniqueActivities.size() < 3 && pattern.getUsageCount() >= 15) {
                    String appName = getAppName(packageName);
                    String category = getCategoryForPackage(packageName);
                    
                    if (category != null) {
                        String title = "Limited feature usage in " + appName;
                        String description = "User regularly uses " + appName + 
                                " but appears to use a limited set of features. " +
                                "They might benefit from knowledge about additional capabilities.";
                        
                        createBehaviorInsight(title, InsightCategory.KNOWLEDGE_NEED, 
                                description, 0.65);
                        
                        // Create learning objective
                        String objectiveTitle = "Explore " + appName + " capabilities";
                        String objectiveDesc = "User frequently uses " + appName + 
                                " but with limited feature usage. Learn about additional " +
                                "capabilities to potentially assist the user.";
                        
                        learningSystem.createLearningObjective(objectiveTitle, objectiveDesc,
                                SelfDirectedLearningSystem.ImportanceLevel.MEDIUM,
                                SelfDirectedLearningSystem.LearningSource.SYSTEM_ANALYSIS);
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error detecting knowledge need insights", e);
        }
    }
    
    /**
     * Get app category for package
     * @param packageName Package name
     * @return Category or null if unknown
     */
    private String getCategoryForPackage(String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            
            List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo info : apps) {
                if (info.activityInfo.packageName.equals(packageName)) {
                    // This is simplified - in a real app, would use more sophisticated
                    // categorization based on Play Store categories or trained classifier
                    
                    if (packageName.contains("office") || 
                            packageName.contains("docs") ||
                            packageName.contains("sheets") ||
                            packageName.contains("slides") ||
                            packageName.contains("note")) {
                        return "Productivity";
                    } else if (packageName.contains("game") ||
                            packageName.contains("play")) {
                        return "Games";
                    } else if (packageName.contains("map") ||
                            packageName.contains("navigation") ||
                            packageName.contains("travel")) {
                        return "Travel";
                    } else if (packageName.contains("music") ||
                            packageName.contains("video") ||
                            packageName.contains("media") ||
                            packageName.contains("photo")) {
                        return "Media";
                    } else if (packageName.contains("message") ||
                            packageName.contains("chat") ||
                            packageName.contains("mail") ||
                            packageName.contains("comm")) {
                        return "Communication";
                    }
                    
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting app category", e);
        }
        
        return null;
    }
    
    /**
     * Create behavior insight
     * @param title Insight title
     * @param category Insight category
     * @param description Insight description
     * @param confidence Confidence level (0.0-1.0)
     * @return Created insight ID
     */
    private String createBehaviorInsight(String title, InsightCategory category,
            String description, double confidence) {
        // Check for duplicate insights
        for (UserBehaviorInsight insight : behaviorInsights.values()) {
            if (insight.getTitle().equals(title)) {
                // Update existing insight instead
                insight.setDescription(description);
                insight.setConfidence(confidence);
                
                notifyInsightUpdated(insight);
                return insight.getId();
            }
        }
        
        // Create new insight
        UserBehaviorInsight insight = new UserBehaviorInsight(title, category, description);
        insight.setConfidence(confidence);
        
        // Add to insights
        behaviorInsights.put(insight.getId(), insight);
        
        // Notify listeners
        notifyInsightCreated(insight);
        
        Log.d(TAG, "Created behavior insight: " + insight.getId() + " - " + title);
        
        // Add to knowledge system if confidence is high enough
        if (confidence >= 0.7) {
            addToKnowledgeSystem(title + ": " + description,
                    StructuredKnowledgeSystem.KnowledgeDomain.USER_SPECIFIC,
                    StructuredKnowledgeSystem.KnowledgeSource.SYSTEM_OBSERVATION);
        }
        
        return insight.getId();
    }
    
    /**
     * Add insight to knowledge system
     * @param content Content to add
     * @param domain Knowledge domain
     * @param source Knowledge source
     */
    private void addToKnowledgeSystem(String content, 
            StructuredKnowledgeSystem.KnowledgeDomain domain,
            StructuredKnowledgeSystem.KnowledgeSource source) {
        try {
            knowledgeSystem.addKnowledgeEntry(content, domain, source);
        } catch (Exception e) {
            Log.e(TAG, "Error adding to knowledge system", e);
        }
    }
    
    /**
     * Get behavior insight
     * @param insightId Insight ID
     * @return Behavior insight or null if not found
     */
    public UserBehaviorInsight getInsight(String insightId) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting behavior insight");
            return null;
        }
        
        return behaviorInsights.get(insightId);
    }
    
    /**
     * Get insights by category
     * @param category Insight category
     * @return List of insights in that category
     */
    public List<UserBehaviorInsight> getInsightsByCategory(InsightCategory category) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting insights by category");
            return new ArrayList<>();
        }
        
        List<UserBehaviorInsight> insights = new ArrayList<>();
        for (UserBehaviorInsight insight : behaviorInsights.values()) {
            if (insight.getCategory() == category) {
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    /**
     * Get app usage pattern
     * @param packageName Package name
     * @return App usage pattern or null if not found
     */
    public AppUsagePattern getAppUsagePattern(String packageName) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting app usage pattern");
            return null;
        }
        
        return appUsagePatterns.get(packageName);
    }
    
    /**
     * Get most used apps
     * @param limit Maximum number of apps to return
     * @return List of most used app patterns
     */
    public List<AppUsagePattern> getMostUsedApps(int limit) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting most used apps");
            return new ArrayList<>();
        }
        
        // Convert to list and sort by usage count
        List<AppUsagePattern> patterns = new ArrayList<>(appUsagePatterns.values());
        patterns.sort((p1, p2) -> Integer.compare(p2.getUsageCount(), p1.getUsageCount()));
        
        // Return top patterns (up to limit)
        return patterns.subList(0, Math.min(limit, patterns.size()));
    }
    
    /**
     * Get recent observations
     * @param limit Maximum number of observations to return
     * @return List of recent observations
     */
    public List<UserInteractionObservation> getRecentObservations(int limit) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting recent observations");
            return new ArrayList<>();
        }
        
        synchronized (recentObservations) {
            int size = recentObservations.size();
            if (size <= limit) {
                return new ArrayList<>(recentObservations);
            } else {
                return new ArrayList<>(recentObservations.subList(size - limit, size));
            }
        }
    }
    
    /**
     * Get recent observations for package
     * @param packageName Package name
     * @param limit Maximum number of observations to return
     * @return List of recent observations for package
     */
    public List<UserInteractionObservation> getRecentObservationsForPackage(
            String packageName, int limit) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for getting package observations");
            return new ArrayList<>();
        }
        
        List<UserInteractionObservation> packageObservations = new ArrayList<>();
        synchronized (recentObservations) {
            for (int i = recentObservations.size() - 1; i >= 0; i--) {
                UserInteractionObservation observation = recentObservations.get(i);
                if (observation.getPackageName().equals(packageName)) {
                    packageObservations.add(observation);
                    if (packageObservations.size() >= limit) {
                        break;
                    }
                }
            }
        }
        
        return packageObservations;
    }
    
    /**
     * Add system access learning listener
     * @param listener Listener to add
     */
    public void addLearningListener(SystemAccessLearningListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove system access learning listener
     * @param listener Listener to remove
     */
    public void removeLearningListener(SystemAccessLearningListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners of observation added
     * @param observation Added observation
     */
    private void notifyObservationAdded(UserInteractionObservation observation) {
        for (SystemAccessLearningListener listener : listeners) {
            listener.onObservationAdded(observation);
        }
    }
    
    /**
     * Notify listeners of insight created
     * @param insight Created insight
     */
    private void notifyInsightCreated(UserBehaviorInsight insight) {
        for (SystemAccessLearningListener listener : listeners) {
            listener.onInsightCreated(insight);
        }
    }
    
    /**
     * Notify listeners of insight updated
     * @param insight Updated insight
     */
    private void notifyInsightUpdated(UserBehaviorInsight insight) {
        for (SystemAccessLearningListener listener : listeners) {
            listener.onInsightUpdated(insight);
        }
    }
    
    /**
     * System access learning listener interface
     */
    public interface SystemAccessLearningListener {
        /**
         * Called when an observation is added
         * @param observation Added observation
         */
        void onObservationAdded(UserInteractionObservation observation);
        
        /**
         * Called when an insight is created
         * @param insight Created insight
         */
        void onInsightCreated(UserBehaviorInsight insight);
        
        /**
         * Called when an insight is updated
         * @param insight Updated insight
         */
        void onInsightUpdated(UserBehaviorInsight insight);
    }
}
