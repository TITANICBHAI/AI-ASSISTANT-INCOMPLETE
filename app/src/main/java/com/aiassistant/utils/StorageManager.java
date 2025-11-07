package com.aiassistant.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class StorageManager {
    
    private static final String TAG = "StorageManager";
    
    private static final String LEARNING_DATA_DIR = "learning_data";
    private static final String VOICE_SAMPLES_DIR = "voice_samples";
    private static final String IMAGE_SAMPLES_DIR = "image_samples";
    private static final String MODELS_DIR = "models";
    
    private static final String AUDIO_FILE_EXTENSION = ".wav";
    private static final String IMAGE_FILE_EXTENSION = ".jpg";
    private static final int IMAGE_QUALITY = 90;
    
    private final Context context;
    private final File learningDataDir;
    private final File voiceSamplesDir;
    private final File imageSamplesDir;
    private final File modelsDir;
    
    public StorageManager(Context context) {
        this.context = context.getApplicationContext();
        
        this.learningDataDir = new File(context.getFilesDir(), LEARNING_DATA_DIR);
        this.voiceSamplesDir = new File(learningDataDir, VOICE_SAMPLES_DIR);
        this.imageSamplesDir = new File(learningDataDir, IMAGE_SAMPLES_DIR);
        this.modelsDir = new File(learningDataDir, MODELS_DIR);
        
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        createDirectoryIfNotExists(learningDataDir);
        createDirectoryIfNotExists(voiceSamplesDir);
        createDirectoryIfNotExists(imageSamplesDir);
        createDirectoryIfNotExists(modelsDir);
    }
    
    private void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Log.d(TAG, "Created directory: " + directory.getAbsolutePath());
            } else {
                Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
            }
        }
    }
    
    public String saveAudioSample(byte[] audioData, String label) {
        String filename = generateAudioFilename(label);
        File audioFile = new File(voiceSamplesDir, filename);
        
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(audioData);
            Log.d(TAG, "Audio sample saved: " + audioFile.getAbsolutePath());
            return audioFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving audio sample", e);
            return null;
        }
    }
    
    public String saveAudioSampleFromFile(File sourceFile, String label) {
        String filename = generateAudioFilename(label);
        File destFile = new File(voiceSamplesDir, filename);
        
        if (FileUtils.copyFile(sourceFile, destFile)) {
            Log.d(TAG, "Audio file copied: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } else {
            Log.e(TAG, "Error copying audio file");
            return null;
        }
    }
    
    public byte[] loadAudioSample(String audioPath) {
        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist: " + audioPath);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            byte[] data = new byte[(int) audioFile.length()];
            int bytesRead = fis.read(data);
            if (bytesRead == data.length) {
                return data;
            } else {
                Log.e(TAG, "Could not read entire audio file");
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading audio sample", e);
            return null;
        }
    }
    
    public String saveImageSample(Bitmap bitmap, String labelId) {
        String filename = generateImageFilename(labelId);
        File imageFile = new File(imageSamplesDir, filename);
        
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fos);
            Log.d(TAG, "Image sample saved: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image sample", e);
            return null;
        }
    }
    
    public String saveImageSampleFromFile(File sourceFile, String labelId) {
        String filename = generateImageFilename(labelId);
        File destFile = new File(imageSamplesDir, filename);
        
        if (FileUtils.copyFile(sourceFile, destFile)) {
            Log.d(TAG, "Image file copied: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } else {
            Log.e(TAG, "Error copying image file");
            return null;
        }
    }
    
    public Bitmap loadImageSample(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.e(TAG, "Image file does not exist: " + imagePath);
            return null;
        }
        
        try {
            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image sample", e);
            return null;
        }
    }
    
    public String saveModel(byte[] modelData, String modelName) {
        String filename = generateModelFilename(modelName);
        File modelFile = new File(modelsDir, filename);
        
        try (FileOutputStream fos = new FileOutputStream(modelFile)) {
            fos.write(modelData);
            Log.d(TAG, "Model saved: " + modelFile.getAbsolutePath());
            return modelFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving model", e);
            return null;
        }
    }
    
    public byte[] loadModel(String modelPath) {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            Log.e(TAG, "Model file does not exist: " + modelPath);
            return null;
        }
        
        return FileUtils.readBinaryFile(modelFile);
    }
    
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Deleted file: " + filePath);
            } else {
                Log.e(TAG, "Failed to delete file: " + filePath);
            }
            return deleted;
        }
        return false;
    }
    
    public boolean deleteAudioSample(String audioPath) {
        return deleteFile(audioPath);
    }
    
    public boolean deleteImageSample(String imagePath) {
        return deleteFile(imagePath);
    }
    
    public boolean deleteModel(String modelPath) {
        return deleteFile(modelPath);
    }
    
    public void cleanupOldFiles(long olderThanTimestamp) {
        cleanupDirectory(voiceSamplesDir, olderThanTimestamp);
        cleanupDirectory(imageSamplesDir, olderThanTimestamp);
    }
    
    private void cleanupDirectory(File directory, long olderThanTimestamp) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        int deletedCount = 0;
        for (File file : files) {
            if (file.isFile() && file.lastModified() < olderThanTimestamp) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
        }
        
        Log.d(TAG, "Cleaned up " + deletedCount + " files from " + directory.getName());
    }
    
    public long getStorageUsage() {
        return getDirectorySize(learningDataDir);
    }
    
    public long getVoiceSamplesStorageUsage() {
        return getDirectorySize(voiceSamplesDir);
    }
    
    public long getImageSamplesStorageUsage() {
        return getDirectorySize(imageSamplesDir);
    }
    
    public long getModelsStorageUsage() {
        return getDirectorySize(modelsDir);
    }
    
    private long getDirectorySize(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySize(file);
                }
            }
        }
        return size;
    }
    
    public int getVoiceSampleCount() {
        return getFileCount(voiceSamplesDir);
    }
    
    public int getImageSampleCount() {
        return getFileCount(imageSamplesDir);
    }
    
    public int getModelCount() {
        return getFileCount(modelsDir);
    }
    
    private int getFileCount(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        File[] files = directory.listFiles();
        return files != null ? files.length : 0;
    }
    
    private String generateAudioFilename(String label) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedLabel = sanitizeFilename(label);
        return sanitizedLabel + "_" + timestamp + "_" + uuid + AUDIO_FILE_EXTENSION;
    }
    
    private String generateImageFilename(String labelId) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedLabelId = sanitizeFilename(labelId);
        return sanitizedLabelId + "_" + timestamp + "_" + uuid + IMAGE_FILE_EXTENSION;
    }
    
    private String generateModelFilename(String modelName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String sanitizedName = sanitizeFilename(modelName);
        return sanitizedName + "_" + timestamp + ".tflite";
    }
    
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "sample";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    public File getVoiceSamplesDirectory() {
        return voiceSamplesDir;
    }
    
    public File getImageSamplesDirectory() {
        return imageSamplesDir;
    }
    
    public File getModelsDirectory() {
        return modelsDir;
    }
    
    public File getLearningDataDirectory() {
        return learningDataDir;
    }
    
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
}
