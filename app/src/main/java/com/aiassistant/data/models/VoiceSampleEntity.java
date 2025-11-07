package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "voice_samples")
@TypeConverters(DateConverter.class)
public class VoiceSampleEntity implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String voiceSampleId;
    
    private String audioDataPath;
    private String transcript;
    private Date timestamp;
    private String label;
    private float confidence;
    
    public VoiceSampleEntity() {
        this.voiceSampleId = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.confidence = 0.0f;
    }
    
    public VoiceSampleEntity(@NonNull String voiceSampleId, String audioDataPath, String transcript, 
                            Date timestamp, String label, float confidence) {
        this.voiceSampleId = voiceSampleId;
        this.audioDataPath = audioDataPath;
        this.transcript = transcript;
        this.timestamp = timestamp;
        this.label = label;
        this.confidence = confidence;
    }
    
    @NonNull
    public String getVoiceSampleId() {
        return voiceSampleId;
    }
    
    public void setVoiceSampleId(@NonNull String voiceSampleId) {
        this.voiceSampleId = voiceSampleId;
    }
    
    public String getAudioDataPath() {
        return audioDataPath;
    }
    
    public void setAudioDataPath(String audioDataPath) {
        this.audioDataPath = audioDataPath;
    }
    
    public String getTranscript() {
        return transcript;
    }
    
    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
