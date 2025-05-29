package com.aiassistant.learning.memory.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database for memory storage
 */
@Database(entities = {InteractionEntity.class}, version = 1, exportSchema = false)
public abstract class MemoryDatabase extends RoomDatabase {
    
    /**
     * Get the knowledge DAO
     */
    public abstract KnowledgeDao knowledgeDao();
}
