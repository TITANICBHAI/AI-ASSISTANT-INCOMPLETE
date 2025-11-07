package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.UserProfile;

import java.util.List;

@Dao
public interface UserProfileDao {
    
    @Insert
    long insert(UserProfile profile);
    
    @Update
    void update(UserProfile profile);
    
    @Delete
    void delete(UserProfile profile);
    
    @Query("SELECT * FROM user_profile")
    List<UserProfile> getAll();
    
    @Query("SELECT * FROM user_profile")
    LiveData<List<UserProfile>> getAllLive();
    
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    UserProfile getById(int id);
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getCurrentProfile();
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    LiveData<UserProfile> getCurrentProfileLive();
    
    @Query("UPDATE user_profile SET experiencePoints = :xp WHERE id = :id")
    void updateExperiencePoints(int id, int xp);
    
    @Query("UPDATE user_profile SET currentLevel = :level WHERE id = :id")
    void updateLevel(int id, int level);
    
    @Query("UPDATE user_profile SET sessionCount = sessionCount + 1 WHERE id = :id")
    void incrementSessionCount(int id);
    
    @Query("UPDATE user_profile SET tasksCompleted = tasksCompleted + 1 WHERE id = :id")
    void incrementTasksCompleted(int id);
    
    @Query("UPDATE user_profile SET totalPlayTime = totalPlayTime + :duration WHERE id = :id")
    void addPlayTime(int id, long duration);
    
    @Query("DELETE FROM user_profile")
    void deleteAll();
}
