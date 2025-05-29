package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.UIElement;

import java.util.List;

/**
 * DAO for UIElement
 */
@Dao
public interface UIElementDao {
    
    /**
     * Get all UI elements
     * 
     * @return The UI elements
     */
    @Query("SELECT * FROM ui_elements")
    List<UIElement> getAll();
    
    /**
     * Get UI element by ID
     * 
     * @param id The ID
     * @return The UI element
     */
    @Query("SELECT * FROM ui_elements WHERE id = :id")
    UIElement getById(long id);
    
    /**
     * Get UI elements by game state ID
     * 
     * @param gameStateId The game state ID
     * @return The UI elements
     */
    @Query("SELECT * FROM ui_elements WHERE gameStateId = :gameStateId")
    List<UIElement> getByGameStateId(long gameStateId);
    
    /**
     * Insert a UI element
     * 
     * @param uiElement The UI element
     * @return The inserted ID
     */
    @Insert
    long insert(UIElement uiElement);
    
    /**
     * Insert multiple UI elements
     * 
     * @param uiElements The UI elements
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<UIElement> uiElements);
    
    /**
     * Update a UI element
     * 
     * @param uiElement The UI element
     */
    @Update
    void update(UIElement uiElement);
    
    /**
     * Delete a UI element
     * 
     * @param uiElement The UI element
     */
    @Delete
    void delete(UIElement uiElement);
    
    /**
     * Delete UI elements by game state ID
     * 
     * @param gameStateId The game state ID
     */
    @Query("DELETE FROM ui_elements WHERE gameStateId = :gameStateId")
    void deleteByGameStateId(long gameStateId);
}
