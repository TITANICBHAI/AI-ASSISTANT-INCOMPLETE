package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.AIActionReward;

import java.util.List;

@Dao
public interface AIActionRewardDao {
    
    @Insert
    long insert(AIActionReward reward);
    
    @Update
    void update(AIActionReward reward);
    
    @Delete
    void delete(AIActionReward reward);
    
    @Query("SELECT * FROM ai_action_rewards ORDER BY timestamp DESC")
    List<AIActionReward> getAll();
    
    @Query("SELECT * FROM ai_action_rewards ORDER BY timestamp DESC")
    LiveData<List<AIActionReward>> getAllLive();
    
    @Query("SELECT * FROM ai_action_rewards WHERE id = :id LIMIT 1")
    AIActionReward getById(long id);
    
    @Query("SELECT * FROM ai_action_rewards WHERE actionId = :actionId ORDER BY timestamp DESC")
    List<AIActionReward> getByActionId(long actionId);
    
    @Query("SELECT AVG(value) FROM ai_action_rewards WHERE actionId = :actionId")
    Float getAverageRewardForAction(long actionId);
    
    @Query("SELECT * FROM ai_action_rewards WHERE source = :source ORDER BY timestamp DESC")
    List<AIActionReward> getBySource(String source);
    
    @Query("SELECT * FROM ai_action_rewards WHERE value > 0 ORDER BY value DESC LIMIT :limit")
    List<AIActionReward> getTopPositiveRewards(int limit);
    
    @Query("DELETE FROM ai_action_rewards")
    void deleteAll();
    
    @Query("DELETE FROM ai_action_rewards WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM ai_action_rewards WHERE actionId = :actionId")
    void deleteByActionId(long actionId);
}
