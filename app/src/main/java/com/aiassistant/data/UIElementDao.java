package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.UIElement;

import java.util.List;

/**
 * Data Access Object for UIElement entities
 */
@Dao
public interface UIElementDao {
    /**
     * Insert UI element
     * @param uiElement UI element to insert
     * @return Inserted ID
     */
    @Insert
    long insert(UIElement uiElement);

    /**
     * Update UI element
     * @param uiElement UI element to update
     */
    @Update
    void update(UIElement uiElement);

    /**
     * Delete UI element
     * @param uiElement UI element to delete
     */
    @Delete
    void delete(UIElement uiElement);

    /**
     * Get all UI elements
     * @return All UI elements
     */
    @Query("SELECT * FROM ui_elements")
    List<UIElement> getAllUIElements();

    /**
     * Get all UI elements as LiveData
     * @return All UI elements as LiveData
     */
    @Query("SELECT * FROM ui_elements")
    LiveData<List<UIElement>> getAllUIElementsLive();

    /**
     * Get UI elements for screen
     * @param screenId Screen ID
     * @return UI elements for specified screen
     */
    @Query("SELECT * FROM ui_elements WHERE screenId = :screenId")
    List<UIElement> getUIElementsForScreen(String screenId);

    /**
     * Get UI element by ID
     * @param elementId Element ID
     * @return UI element with specified ID
     */
    @Query("SELECT * FROM ui_elements WHERE elementId = :elementId LIMIT 1")
    UIElement getUIElementById(String elementId);

    /**
     * Get UI elements by class name
     * @param className Class name
     * @return UI elements with specified class name
     */
    @Query("SELECT * FROM ui_elements WHERE className = :className")
    List<UIElement> getUIElementsByClassName(String className);

    /**
     * Get clickable UI elements
     * @return Clickable UI elements
     */
    @Query("SELECT * FROM ui_elements WHERE isClickable = 1")
    List<UIElement> getClickableUIElements();

    /**
     * Get editable UI elements
     * @return Editable UI elements
     */
    @Query("SELECT * FROM ui_elements WHERE isEditable = 1")
    List<UIElement> getEditableUIElements();

    /**
     * Delete UI elements for screen
     * @param screenId Screen ID
     * @return Number of deleted rows
     */
    @Query("DELETE FROM ui_elements WHERE screenId = :screenId")
    int deleteUIElementsForScreen(String screenId);
}
