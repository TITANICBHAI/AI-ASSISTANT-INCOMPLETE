package com.aiassistant.ui.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated This is being replaced by new ViewModels
 */
@Deprecated
public class AppManagementViewModel extends AndroidViewModel {
    private static final String TAG = "AppManagementViewModel";
    
    /**
     * Constructor
     * @param application Application
     */
    public AppManagementViewModel(Application application) {
        super(application);
    }
}
