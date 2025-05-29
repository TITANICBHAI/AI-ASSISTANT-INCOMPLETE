package com.aiassistant.data.converters;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Room database converter for Date objects
 */
public class DateConverter {
    /**
     * Convert timestamp to Date
     * @param timestamp Timestamp in milliseconds
     * @return Date object
     */
    @TypeConverter
    public static Date fromTimestamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    /**
     * Convert Date to timestamp
     * @param date Date object
     * @return Timestamp in milliseconds
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
