package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ActionSequence;

import java.util.List;

@Dao
public interface ActionSequenceDao {
    
    @Insert
    long insert(ActionSequence sequence);
    
    @Update
    void update(ActionSequence sequence);
    
    @Delete
    void delete(ActionSequence sequence);
    
    @Query("SELECT * FROM action_sequences ORDER BY createdAt DESC")
    List<ActionSequence> getAll();
    
    @Query("SELECT * FROM action_sequences ORDER BY createdAt DESC")
    LiveData<List<ActionSequence>> getAllLive();
    
    @Query("SELECT * FROM action_sequences WHERE id = :id LIMIT 1")
    ActionSequence getById(long id);
    
    @Query("SELECT * FROM action_sequences WHERE targetAppPackage = :packageName ORDER BY createdAt DESC")
    List<ActionSequence> getByPackage(String packageName);
    
    @Query("SELECT * FROM action_sequences WHERE isCompleteSequence = 1 ORDER BY successCount DESC")
    List<ActionSequence> getCompleteSequences();
    
    @Query("SELECT * FROM action_sequences WHERE name LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    List<ActionSequence> searchByName(String searchQuery);
    
    @Query("DELETE FROM action_sequences")
    void deleteAll();
    
    @Query("DELETE FROM action_sequences WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT * FROM action_sequences ORDER BY lastUsedAt DESC LIMIT :limit")
    List<ActionSequence> getRecentlyUsed(int limit);
}
