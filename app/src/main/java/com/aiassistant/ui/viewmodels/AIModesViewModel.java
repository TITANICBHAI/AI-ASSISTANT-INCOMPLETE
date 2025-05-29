package com.aiassistant.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.data.models.AIAction;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing AI modes and actions
 */
public class AIModesViewModel extends AndroidViewModel {
    private static final String TAG = "AIModesViewModel";
    
    private final MutableLiveData<String> currentMode = new MutableLiveData<>("manual");
    private final MutableLiveData<Boolean> isActive = new MutableLiveData<>(false);
    private final MutableLiveData<List<AIAction>> suggestedActions = new MutableLiveData<>(new ArrayList<>());
    
    /**
     * Constructor
     * @param application Application
     */
    public AIModesViewModel(Application application) {
        super(application);
    }
    
    /**
     * Get current mode
     * @return Current mode LiveData
     */
    public LiveData<String> getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Set current mode
     * @param mode New mode
     */
    public void setMode(String mode) {
        this.currentMode.setValue(mode);
        Log.d(TAG, "Mode set to: " + mode);
    }
    
    /**
     * Get is active
     * @return Is active LiveData
     */
    public LiveData<Boolean> getIsActive() {
        return isActive;
    }
    
    /**
     * Set is active
     * @param active New active status
     */
    public void setActive(boolean active) {
        this.isActive.setValue(active);
        Log.d(TAG, "Active status set to: " + active);
    }
    
    /**
     * Get suggested actions
     * @return Suggested actions LiveData
     */
    public LiveData<List<AIAction>> getSuggestedActions() {
        return suggestedActions;
    }
    
    /**
     * Update suggested actions
     * @param actions New suggested actions
     */
    public void updateSuggestedActions(List<AIAction> actions) {
        this.suggestedActions.setValue(actions);
    }
    
    /**
     * Toggle active status
     */
    public void toggleActive() {
        Boolean currentActive = isActive.getValue();
        
        if (currentActive != null) {
            setActive(!currentActive);
        } else {
            setActive(true);
        }
    }
}
