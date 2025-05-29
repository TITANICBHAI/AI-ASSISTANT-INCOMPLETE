package com.aiassistant.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.data.models.LearningSession;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for learning functionalities
 */
public class LearningViewModel extends AndroidViewModel {
    private static final String TAG = "LearningViewModel";
    
    private final MutableLiveData<Boolean> isLearningActive = new MutableLiveData<>(false);
    private final MutableLiveData<List<LearningSession>> sessions = new MutableLiveData<>(new ArrayList<>());
    
    /**
     * Constructor
     * @param application Application
     */
    public LearningViewModel(Application application) {
        super(application);
    }
    
    /**
     * Get is learning active
     * @return Is learning active LiveData
     */
    public LiveData<Boolean> getIsLearningActive() {
        return isLearningActive;
    }
    
    /**
     * Set is learning active
     * @param active New active status
     */
    public void setLearningActive(boolean active) {
        this.isLearningActive.setValue(active);
        Log.d(TAG, "Learning active status set to: " + active);
    }
    
    /**
     * Get learning sessions
     * @return Learning sessions LiveData
     */
    public LiveData<List<LearningSession>> getSessions() {
        return sessions;
    }
    
    /**
     * Update learning sessions
     * @param newSessions New learning sessions
     */
    public void updateSessions(List<LearningSession> newSessions) {
        this.sessions.setValue(newSessions);
    }
    
    /**
     * Add learning session
     * @param session New learning session
     */
    public void addSession(LearningSession session) {
        List<LearningSession> currentSessions = sessions.getValue();
        
        if (currentSessions != null) {
            currentSessions.add(session);
            sessions.setValue(currentSessions);
        }
    }
}
