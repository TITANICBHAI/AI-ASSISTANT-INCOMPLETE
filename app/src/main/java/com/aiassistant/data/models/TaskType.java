package com.aiassistant.data.models;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Enumeration of different task types that the AI assistant can perform.
 */
public enum TaskType {
    GAME_AUTOMATION,    // Tasks related to automating gameplay
    SEND_MESSAGE,       // Tasks for sending messages on behalf of the user
    APP_INTERACTION,    // General tasks interacting with apps
    SYSTEM_ACTION,      // Tasks that perform system operations
    CUSTOM              // Custom defined tasks
}