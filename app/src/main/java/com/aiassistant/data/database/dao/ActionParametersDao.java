package com.aiassistant.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ActionParameters;

import java.util.List;

/**
 * Data Access Object for ActionParameters
 */
@Dao
public interface ActionParametersDao {

    /**
     * Insert a new set of action parameters into the database
     * 
     * @param actionParameters The action parameters to insert
     * @return The ID of the inserted action parameters
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ActionParameters actionParameters);

    /**
     * Update existing action parameters in the database
     * 
     * @param actionParameters The action parameters to update
     */
    @Update
    void update(ActionParameters actionParameters);

    /**
     * Delete action parameters from the database
     * 
     * @param actionParameters The action parameters to delete
     */
    @Delete
    void delete(ActionParameters actionParameters);

    /**
     * Get action parameters by ID
     * 
     * @param id The ID of the action parameters
     * @return The requested action parameters
     */
    @Query("SELECT * FROM action_parameters WHERE id = :id")
    ActionParameters getById(long id);

    /**
     * Get all action parameters for a specific action
     * 
     * @param actionId The ID of the action
     * @return LiveData list of all matching action parameters
     */
    @Query("SELECT * FROM action_parameters WHERE actionId = :actionId")
    LiveData<List<ActionParameters>> getAllForAction(long actionId);

    /**
     * Get all action parameters
     * 
     * @return LiveData list of all action parameters
     */
    @Query("SELECT * FROM action_parameters")
    LiveData<List<ActionParameters>> getAll();

    /**
     * Delete all action parameters for a specific action
     * 
     * @param actionId The ID of the action
     */
    @Query("DELETE FROM action_parameters WHERE actionId = :actionId")
    void deleteAllForAction(long actionId);

    /**
     * Delete all action parameters
     */
    @Query("DELETE FROM action_parameters")
    void deleteAll();
}
