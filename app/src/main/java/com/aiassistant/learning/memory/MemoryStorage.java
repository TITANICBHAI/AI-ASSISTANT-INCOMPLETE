package com.aiassistant.learning.memory;

import android.content.Context;
import android.util.Log;

import com.aiassistant.security.AccessControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Secure persistent memory storage system for the adaptive learning architecture
 */
public class MemoryStorage {
    private static final String TAG = "MemoryStorage";
    private static final String MEMORY_FILE_NAME = "ai_memory_storage.dat";
    
    private final Context context;
    private final AccessControl accessControl;
    
    // Memory storage, organized by domain
    private final Map<String, List<MemoryItem>> memoryByDomain;
    
    // Memory indexing for faster retrieval
    private final Map<String, MemoryItem> memoryById;
    
    // Thread safety
    private final ReadWriteLock memoryLock = new ReentrantReadWriteLock();
    
    // Statistics
    private int totalMemoryItems = 0;
    private long lastSaveTime = 0;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control system
     */
    public MemoryStorage(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.memoryByDomain = new ConcurrentHashMap<>();
        this.memoryById = new ConcurrentHashMap<>();
        
        // Load saved memories
        loadMemories();
    }
    
    /**
     * Store a memory item
     * @param item Memory item to store
     * @return True if stored successfully
     */
    public boolean storeMemory(MemoryItem item) {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.e(TAG, "Security permission denied for memory storage");
            return false;
        }
        
        if (item == null || item.getId() == null || item.getContent() == null) {
            return false;
        }
        
        try {
            memoryLock.writeLock().lock();
            
            // Add to domain map
            List<MemoryItem> domainMemories = memoryByDomain.computeIfAbsent(
                    item.getDomainId(), k -> new ArrayList<>());
            domainMemories.add(item);
            
            // Add to ID index
            memoryById.put(item.getId(), item);
            
            // Update statistics
            totalMemoryItems++;
            
            Log.d(TAG, "Stored memory item: " + item.getId() + " in domain: " + item.getDomainId());
            
            // Save periodically (every 10 items or at least 5 minutes since last save)
            if (totalMemoryItems % 10 == 0 || 
                    System.currentTimeMillis() - lastSaveTime > 5 * 60 * 1000) {
                saveMemories();
            }
            
            return true;
        } finally {
            memoryLock.writeLock().unlock();
        }
    }
    
    /**
     * Retrieve memory by ID
     * @param memoryId Memory ID
     * @return Memory item or null if not found
     */
    public MemoryItem getMemoryById(String memoryId) {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Security permission denied for memory retrieval");
            return null;
        }
        
        try {
            memoryLock.readLock().lock();
            return memoryById.get(memoryId);
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve memories by domain
     * @param domainId Domain ID
     * @return List of memory items
     */
    public List<MemoryItem> getMemoriesByDomain(String domainId) {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Security permission denied for memory retrieval");
            return Collections.emptyList();
        }
        
        try {
            memoryLock.readLock().lock();
            List<MemoryItem> memories = memoryByDomain.get(domainId);
            if (memories == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(memories);
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Search memories using text query
     * @param query Search query
     * @param maxResults Maximum number of results to return
     * @return List of matching memory items
     */
    public List<MemoryItem> searchMemories(String query, int maxResults) {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Security permission denied for memory search");
            return Collections.emptyList();
        }
        
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Normalize query
        String normalizedQuery = query.toLowerCase().trim();
        
        try {
            memoryLock.readLock().lock();
            
            // Search across all domains
            List<MemoryItem> results = new ArrayList<>();
            
            for (List<MemoryItem> domainMemories : memoryByDomain.values()) {
                for (MemoryItem item : domainMemories) {
                    if (item.getContent().toLowerCase().contains(normalizedQuery)) {
                        results.add(item);
                        
                        if (maxResults > 0 && results.size() >= maxResults) {
                            return results;
                        }
                    }
                }
            }
            
            return results;
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Delete a memory item
     * @param memoryId Memory ID to delete
     * @return True if deleted successfully
     */
    public boolean deleteMemory(String memoryId) {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.FULL_ACCESS)) {
            Log.e(TAG, "Security permission denied for memory deletion");
            return false;
        }
        
        try {
            memoryLock.writeLock().lock();
            
            // Get memory item
            MemoryItem item = memoryById.remove(memoryId);
            if (item == null) {
                return false;
            }
            
            // Remove from domain list
            List<MemoryItem> domainMemories = memoryByDomain.get(item.getDomainId());
            if (domainMemories != null) {
                domainMemories.remove(item);
            }
            
            // Update statistics
            totalMemoryItems--;
            
            Log.d(TAG, "Deleted memory item: " + memoryId);
            
            // Save changes
            saveMemories();
            
            return true;
        } finally {
            memoryLock.writeLock().unlock();
        }
    }
    
    /**
     * Get memory count for a domain
     * @param domainId Domain ID
     * @return Number of memories in the domain
     */
    public int getMemoryCount(String domainId) {
        try {
            memoryLock.readLock().lock();
            List<MemoryItem> memories = memoryByDomain.get(domainId);
            return memories != null ? memories.size() : 0;
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Get total memory count
     * @return Total number of memory items
     */
    public int getTotalMemoryCount() {
        try {
            memoryLock.readLock().lock();
            return totalMemoryItems;
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Get memory domains
     * @return Set of domain IDs with memories
     */
    public List<String> getMemoryDomains() {
        try {
            memoryLock.readLock().lock();
            return new ArrayList<>(memoryByDomain.keySet());
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Save memories to persistent storage
     */
    public void saveMemories() {
        // Verify security permissions
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.FULL_ACCESS)) {
            Log.e(TAG, "Security permission denied for memory save operation");
            return;
        }
        
        try {
            memoryLock.readLock().lock();
            
            File file = new File(context.getFilesDir(), MEMORY_FILE_NAME);
            
            try (FileOutputStream fos = new FileOutputStream(file);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                
                // Save memory items grouped by domain
                oos.writeObject(new HashMap<>(memoryByDomain));
                
                lastSaveTime = System.currentTimeMillis();
                Log.d(TAG, "Saved " + totalMemoryItems + " memory items to storage");
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving memories", e);
            }
            
        } finally {
            memoryLock.readLock().unlock();
        }
    }
    
    /**
     * Load memories from persistent storage
     */
    @SuppressWarnings("unchecked")
    private void loadMemories() {
        File file = new File(context.getFilesDir(), MEMORY_FILE_NAME);
        if (!file.exists()) {
            Log.d(TAG, "No memory file found, starting with empty memory");
            return;
        }
        
        try {
            memoryLock.writeLock().lock();
            
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                
                // Load memory items
                Map<String, List<MemoryItem>> loadedMemories = 
                        (Map<String, List<MemoryItem>>) ois.readObject();
                
                // Clear current memories
                memoryByDomain.clear();
                memoryById.clear();
                
                // Copy loaded memories
                for (Map.Entry<String, List<MemoryItem>> entry : loadedMemories.entrySet()) {
                    List<MemoryItem> memoryList = new ArrayList<>(entry.getValue());
                    memoryByDomain.put(entry.getKey(), memoryList);
                    
                    // Rebuild ID index
                    for (MemoryItem item : memoryList) {
                        memoryById.put(item.getId(), item);
                    }
                }
                
                // Update statistics
                totalMemoryItems = memoryById.size();
                
                Log.d(TAG, "Loaded " + totalMemoryItems + " memory items from storage");
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading memories", e);
                
                // Reset on error
                memoryByDomain.clear();
                memoryById.clear();
                totalMemoryItems = 0;
            }
            
        } finally {
            memoryLock.writeLock().unlock();
        }
    }
    
    /**
     * Force commit all memory to persistent storage
     */
    public void forceCommit() {
        saveMemories();
    }
}
    
    /**
     * Store an interaction for learning
     * @param type Type of interaction
     * @param description Brief description
     * @param details Detailed content
     */
    public void storeInteraction(String type, String description, String details) {
        // Create a memory item for this interaction
        MemoryItem item = new MemoryItem(
                "interaction_" + System.currentTimeMillis(),
                type,
                description,
                details,
                System.currentTimeMillis());
        
        // Store in memory
        storeMemory(item);
        
        Log.d(TAG, "Stored interaction: " + description);
    }
