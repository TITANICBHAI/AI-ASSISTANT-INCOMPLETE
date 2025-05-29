package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity class for screen actions in the database
 */
@Entity(tableName = "screen_actions")
public class ScreenActionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String actionType;
    private String targetElementId;
    private String screenId;
    private String actionParams;
    private long timestamp;
    private boolean successful;

    /**
     * Default constructor
     */
    public ScreenActionEntity() {
        // Required by Room
    }

    /**
     * Full constructor
     * @param actionType Action type
     * @param targetElementId Target element ID
     * @param screenId Screen ID
     * @param actionParams Action parameters
     * @param timestamp Timestamp
     * @param successful Success status
     */
    public ScreenActionEntity(String actionType, String targetElementId, String screenId,
                             String actionParams, long timestamp, boolean successful) {
        this.actionType = actionType;
        this.targetElementId = targetElementId;
        this.screenId = screenId;
        this.actionParams = actionParams;
        this.timestamp = timestamp;
        this.successful = successful;
    }

    /**
     * Factory method for tap action
     * @param targetElementId Target element ID
     * @param screenId Screen ID
     * @return ScreenActionEntity for tap
     */
    @Ignore
    public static ScreenActionEntity createTapAction(String targetElementId, String screenId) {
        return new ScreenActionEntity("TAP", targetElementId, screenId, null, 
                                     System.currentTimeMillis(), false);
    }

    /**
     * Factory method for text input action
     * @param targetElementId Target element ID
     * @param screenId Screen ID
     * @param text Text to input
     * @return ScreenActionEntity for text input
     */
    @Ignore
    public static ScreenActionEntity createTextInputAction(String targetElementId, String screenId, String text) {
        return new ScreenActionEntity("TEXT_INPUT", targetElementId, screenId, text,
                                     System.currentTimeMillis(), false);
    }

    /**
     * Factory method for swipe action
     * @param targetElementId Target element ID
     * @param screenId Screen ID
     * @param direction Swipe direction
     * @return ScreenActionEntity for swipe
     */
    @Ignore
    public static ScreenActionEntity createSwipeAction(String targetElementId, String screenId, String direction) {
        return new ScreenActionEntity("SWIPE", targetElementId, screenId, direction,
                                     System.currentTimeMillis(), false);
    }

    // Getters and setters

    /**
     * Get ID
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Set ID
     * @param id New ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get action type
     * @return Action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Set action type
     * @param actionType New action type
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * Get target element ID
     * @return Target element ID
     */
    public String getTargetElementId() {
        return targetElementId;
    }

    /**
     * Set target element ID
     * @param targetElementId New target element ID
     */
    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }

    /**
     * Get screen ID
     * @return Screen ID
     */
    public String getScreenId() {
        return screenId;
    }

    /**
     * Set screen ID
     * @param screenId New screen ID
     */
    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    /**
     * Get action parameters
     * @return Action parameters
     */
    public String getActionParams() {
        return actionParams;
    }

    /**
     * Set action parameters
     * @param actionParams New action parameters
     */
    public void setActionParams(String actionParams) {
        this.actionParams = actionParams;
    }

    /**
     * Get timestamp
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp
     * @param timestamp New timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Check if successful
     * @return True if successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Set successful status
     * @param successful New successful status
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
