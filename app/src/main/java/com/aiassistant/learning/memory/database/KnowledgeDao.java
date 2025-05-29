package com.aiassistant.learning.memory.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO for interacting with the memory database
 */
@Dao
public interface KnowledgeDao {
    
    /**
     * Insert a new knowledge entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InteractionEntity entity);
    
    /**
     * Find a knowledge entry by domain and key
     */
    @Query("SELECT * FROM interactions WHERE domain = :domain AND key = :key LIMIT 1")
    InteractionEntity findByDomainAndKey(String domain, String key);
    
    /**
     * Find all knowledge entries for a domain
     */
    @Query("SELECT * FROM interactions WHERE domain = :domain")
    List<InteractionEntity> findAllByDomain(String domain);
    
    /**
     * Delete all entries for a domain
     */
    @Query("DELETE FROM interactions WHERE domain = :domain")
    void deleteAllByDomain(String domain);
    
    /**
     * Get all interactions, sorted by timestamp (newest first)
     */
    @Query("SELECT * FROM interactions ORDER BY timestamp DESC")
    List<InteractionEntity> getAllSortedByTimestamp();
}
