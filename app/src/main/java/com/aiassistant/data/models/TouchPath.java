package com.aiassistant.data.models;

import android.graphics.PointF;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity class for touch paths in the database
 */
@Entity(tableName = "touch_paths")
@TypeConverters(DateConverter.class)
public class TouchPath {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String pathId;
    private String gameId;
    private String screenId;
    private String pathType;
    private String pointsJson;
    private Date timestamp;
    private boolean isRecorded;

    @Ignore
    private List<PointF> points;

    /**
     * Default constructor
     */
    public TouchPath() {
        // Required by Room
        points = new ArrayList<>();
    }

    /**
     * Full constructor
     * @param pathId Path ID
     * @param gameId Game ID
     * @param screenId Screen ID
     * @param pathType Path type
     * @param pointsJson Points as JSON
     * @param timestamp Timestamp
     * @param isRecorded Recording status
     */
    public TouchPath(String pathId, String gameId, String screenId, String pathType,
                    String pointsJson, Date timestamp, boolean isRecorded) {
        this.pathId = pathId;
        this.gameId = gameId;
        this.screenId = screenId;
        this.pathType = pathType;
        this.pointsJson = pointsJson;
        this.timestamp = timestamp;
        this.isRecorded = isRecorded;
        this.points = new ArrayList<>();
    }

    /**
     * Add point to path
     * @param point Point to add
     */
    @Ignore
    public void addPoint(PointF point) {
        if (points == null) {
            points = new ArrayList<>();
        }
        points.add(point);
    }

    /**
     * Get point at index
     * @param index Index
     * @return Point at index
     */
    @Ignore
    public PointF getPointAt(int index) {
        if (points != null && index >= 0 && index < points.size()) {
            return points.get(index);
        }
        return null;
    }

    /**
     * Get points
     * @return Points list
     */
    @Ignore
    public List<PointF> getPoints() {
        return points;
    }

    /**
     * Set points
     * @param points New points list
     */
    @Ignore
    public void setPoints(List<PointF> points) {
        this.points = points;
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
     * Get path ID
     * @return Path ID
     */
    public String getPathId() {
        return pathId;
    }

    /**
     * Set path ID
     * @param pathId New path ID
     */
    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    /**
     * Get game ID
     * @return Game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Set game ID
     * @param gameId New game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
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
     * Get path type
     * @return Path type
     */
    public String getPathType() {
        return pathType;
    }

    /**
     * Set path type
     * @param pathType New path type
     */
    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    /**
     * Get points JSON
     * @return Points JSON
     */
    public String getPointsJson() {
        return pointsJson;
    }

    /**
     * Set points JSON
     * @param pointsJson New points JSON
     */
    public void setPointsJson(String pointsJson) {
        this.pointsJson = pointsJson;
    }

    /**
     * Get timestamp
     * @return Timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp
     * @param timestamp New timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Check if recorded
     * @return True if recorded
     */
    public boolean isRecorded() {
        return isRecorded;
    }

    /**
     * Set recorded status
     * @param recorded New recorded status
     */
    public void setRecorded(boolean recorded) {
        isRecorded = recorded;
    }
}
