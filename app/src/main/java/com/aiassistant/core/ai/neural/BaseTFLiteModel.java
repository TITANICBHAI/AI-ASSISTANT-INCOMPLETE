package com.aiassistant.core.ai.neural;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Base class for TensorFlow Lite models
 * Handles loading and basic inference for TFLite models
 */
public abstract class BaseTFLiteModel {
    private static final String TAG = "BaseTFLiteModel";
    
    // TensorFlow Lite interpreter
    protected Interpreter interpreter;
    
    // Model configuration
    protected String modelPath;
    protected int inputSize;
    protected boolean quantized;
    protected boolean modelLoaded;
    
    // Application context
    protected final Context context;
    
    /**
     * Constructor
     * @param context Application context
     * @param modelPath Path to model file in assets
     * @param inputSize Input size for the model
     * @param quantized Whether the model is quantized
     */
    public BaseTFLiteModel(Context context, String modelPath, int inputSize, boolean quantized) {
        this.context = context.getApplicationContext();
        this.modelPath = modelPath;
        this.inputSize = inputSize;
        this.quantized = quantized;
        this.modelLoaded = false;
        
        Log.d(TAG, "BaseTFLiteModel initialized for " + modelPath);
    }
    
    /**
     * Load model from assets
     * @return True if model loaded successfully
     */
    public boolean loadModel() {
        try {
            interpreter = new Interpreter(loadModelFile());
            modelLoaded = true;
            Log.d(TAG, "Model loaded successfully: " + modelPath);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if model is loaded
     * @return True if model is loaded
     */
    public boolean isModelLoaded() {
        return modelLoaded;
    }
    
    /**
     * Close interpreter to free resources
     */
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            modelLoaded = false;
            Log.d(TAG, "Model closed: " + modelPath);
        }
    }
    
    /**
     * Load model file from assets
     * @return MappedByteBuffer containing model
     * @throws IOException If model file cannot be read
     */
    protected MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    
    /**
     * Allocate input buffer for float model
     * @param size Buffer size in bytes
     * @return Allocated ByteBuffer
     */
    protected ByteBuffer allocateFloatBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }
    
    /**
     * Allocate input buffer for quantized model
     * @param size Buffer size in bytes
     * @return Allocated ByteBuffer
     */
    protected ByteBuffer allocateByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }
    
    /**
     * Normalize pixel value for float model
     * @param value Pixel value (0-255)
     * @return Normalized value (-1.0 to 1.0)
     */
    protected float normalizePixel(int value) {
        return (value - 127.5f) / 127.5f;
    }
}
