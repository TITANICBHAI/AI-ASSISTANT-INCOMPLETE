package com.aiassistant.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for HomeFragment
 */
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> status;
    private final MutableLiveData<String> currentMode;
    private final MutableLiveData<String> currentGame;
    private final MutableLiveData<Integer> frameCount;
    private final MutableLiveData<Integer> detectionCount;
    private final MutableLiveData<Integer> confidence;

    public HomeViewModel() {
        status = new MutableLiveData<>();
        currentMode = new MutableLiveData<>();
        currentGame = new MutableLiveData<>();
        frameCount = new MutableLiveData<>();
        detectionCount = new MutableLiveData<>();
        confidence = new MutableLiveData<>();
        
        // Set default values
        status.setValue("Inactive");
        currentMode.setValue("None");
        currentGame.setValue("None");
        frameCount.setValue(0);
        detectionCount.setValue(0);
        confidence.setValue(0);
    }

    public LiveData<String> getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status.setValue(value);
    }

    public LiveData<String> getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String value) {
        currentMode.setValue(value);
    }

    public LiveData<String> getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String value) {
        currentGame.setValue(value);
    }

    public LiveData<Integer> getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int value) {
        frameCount.setValue(value);
    }

    public LiveData<Integer> getDetectionCount() {
        return detectionCount;
    }

    public void setDetectionCount(int value) {
        detectionCount.setValue(value);
    }

    public LiveData<Integer> getConfidence() {
        return confidence;
    }

    public void setConfidence(int value) {
        confidence.setValue(value);
    }
}
