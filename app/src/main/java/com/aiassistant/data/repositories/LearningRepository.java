package com.aiassistant.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.GestureSampleDao;
import com.aiassistant.data.ImageSampleDao;
import com.aiassistant.data.LabelDefinitionDao;
import com.aiassistant.data.ModelInfoDao;
import com.aiassistant.data.VoiceSampleDao;
import com.aiassistant.data.models.GestureSampleEntity;
import com.aiassistant.data.models.ImageSampleEntity;
import com.aiassistant.data.models.LabelDefinitionEntity;
import com.aiassistant.data.models.ModelInfoEntity;
import com.aiassistant.data.models.VoiceSampleEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LearningRepository {
    
    private static final String TAG = "LearningRepository";
    
    private final VoiceSampleDao voiceSampleDao;
    private final GestureSampleDao gestureSampleDao;
    private final ImageSampleDao imageSampleDao;
    private final LabelDefinitionDao labelDefinitionDao;
    private final ModelInfoDao modelInfoDao;
    
    private final ExecutorService executorService;
    
    public LearningRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        this.voiceSampleDao = database.voiceSampleDao();
        this.gestureSampleDao = database.gestureSampleDao();
        this.imageSampleDao = database.imageSampleDao();
        this.labelDefinitionDao = database.labelDefinitionDao();
        this.modelInfoDao = database.modelInfoDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    public void insertVoiceSample(VoiceSampleEntity sample, OnInsertCallback callback) {
        executorService.execute(() -> {
            try {
                long id = voiceSampleDao.insert(sample);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting voice sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void insertGestureSample(GestureSampleEntity sample, OnInsertCallback callback) {
        executorService.execute(() -> {
            try {
                long id = gestureSampleDao.insert(sample);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting gesture sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void insertImageSample(ImageSampleEntity sample, OnInsertCallback callback) {
        executorService.execute(() -> {
            try {
                long id = imageSampleDao.insert(sample);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting image sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void insertLabel(LabelDefinitionEntity label, OnInsertCallback callback) {
        executorService.execute(() -> {
            try {
                long id = labelDefinitionDao.insert(label);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting label", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void insertModelInfo(ModelInfoEntity modelInfo, OnInsertCallback callback) {
        executorService.execute(() -> {
            try {
                long id = modelInfoDao.insert(modelInfo);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting model info", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public LiveData<List<VoiceSampleEntity>> getVoiceSamplesByLabel(String label) {
        return voiceSampleDao.getByLabelLive(label);
    }
    
    public LiveData<List<GestureSampleEntity>> getGestureSamplesByLabel(String label) {
        return gestureSampleDao.getByLabelLive(label);
    }
    
    public LiveData<List<ImageSampleEntity>> getImageSamplesByLabelId(String labelId) {
        return imageSampleDao.getByLabelIdLive(labelId);
    }
    
    public LiveData<List<LabelDefinitionEntity>> getAllLabels() {
        return labelDefinitionDao.getAllLive();
    }
    
    public LiveData<List<LabelDefinitionEntity>> getLabelsByCategory(String category) {
        return labelDefinitionDao.getByCategoryLive(category);
    }
    
    public LiveData<List<ModelInfoEntity>> getModelsByLabelId(String labelId) {
        return modelInfoDao.getByLabelIdLive(labelId);
    }
    
    public LiveData<List<ModelInfoEntity>> getModelsByStatus(String status) {
        return modelInfoDao.getByStatusLive(status);
    }
    
    public void getLabelByName(String name, OnLabelCallback callback) {
        executorService.execute(() -> {
            try {
                LabelDefinitionEntity label = labelDefinitionDao.getByName(name);
                if (callback != null) {
                    callback.onLabelRetrieved(label);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting label by name", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getBestModelForLabel(String labelId, OnModelCallback callback) {
        executorService.execute(() -> {
            try {
                ModelInfoEntity model = modelInfoDao.getBestModelForLabel(labelId);
                if (callback != null) {
                    callback.onModelRetrieved(model);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting best model for label", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getLatestModelForLabel(String labelId, OnModelCallback callback) {
        executorService.execute(() -> {
            try {
                ModelInfoEntity model = modelInfoDao.getLatestModelForLabel(labelId);
                if (callback != null) {
                    callback.onModelRetrieved(model);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting latest model for label", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void updateModelStatus(String modelId, String status, OnUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                modelInfoDao.updateStatus(modelId, status);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating model status", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void updateModelAccuracy(String modelId, float accuracy, OnUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                modelInfoDao.updateAccuracy(modelId, accuracy);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating model accuracy", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void incrementLabelUsage(String labelId, OnUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                labelDefinitionDao.incrementUsageCount(labelId);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error incrementing label usage", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getVoiceSampleCount(String label, OnCountCallback callback) {
        executorService.execute(() -> {
            try {
                int count = voiceSampleDao.countByLabel(label);
                if (callback != null) {
                    callback.onCountRetrieved(count);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting voice sample count", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getImageSampleCount(String labelId, OnCountCallback callback) {
        executorService.execute(() -> {
            try {
                int count = imageSampleDao.countByLabelId(labelId);
                if (callback != null) {
                    callback.onCountRetrieved(count);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting image sample count", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getGestureSampleCount(String label, OnCountCallback callback) {
        executorService.execute(() -> {
            try {
                int count = gestureSampleDao.countByLabel(label);
                if (callback != null) {
                    callback.onCountRetrieved(count);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting gesture sample count", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void deleteVoiceSample(VoiceSampleEntity sample, OnDeleteCallback callback) {
        executorService.execute(() -> {
            try {
                voiceSampleDao.delete(sample);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting voice sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void deleteImageSample(ImageSampleEntity sample, OnDeleteCallback callback) {
        executorService.execute(() -> {
            try {
                imageSampleDao.delete(sample);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting image sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void deleteGestureSample(GestureSampleEntity sample, OnDeleteCallback callback) {
        executorService.execute(() -> {
            try {
                gestureSampleDao.delete(sample);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting gesture sample", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public interface OnInsertCallback {
        void onSuccess(long id);
        void onError(Exception e);
    }
    
    public interface OnUpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface OnDeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface OnLabelCallback {
        void onLabelRetrieved(LabelDefinitionEntity label);
        void onError(Exception e);
    }
    
    public interface OnModelCallback {
        void onModelRetrieved(ModelInfoEntity model);
        void onError(Exception e);
    }
    
    public interface OnCountCallback {
        void onCountRetrieved(int count);
        void onError(Exception e);
    }
}
