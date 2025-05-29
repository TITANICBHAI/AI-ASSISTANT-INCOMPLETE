package com.aiassistant.core.ai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for TensorFlow Lite models
 */
public class TFLiteModelManager {
    private static final String TAG = "TFLiteModelManager";
    
    private Context context;
    private Map<String, Interpreter> models;
    private Map<String, Integer[]> inputShapes;
    private Map<String, Integer[]> outputShapes;
    private boolean useGPU;
    
    /**
     * Constructor
     * @param context Application context
     */
    public TFLiteModelManager(Context context) {
        this.context = context;
        this.models = new HashMap<>();
        this.inputShapes = new HashMap<>();
        this.outputShapes = new HashMap<>();
        
        // Check GPU compatibility
        CompatibilityList compatList = new CompatibilityList();
        this.useGPU = compatList.isDelegateSupportedOnThisDevice();
        Log.d(TAG, "GPU Acceleration supported: " + useGPU);
    }
    
    /**
     * Load model from assets
     * @param modelName Model name
     * @param modelPath Path to model in assets
     * @param inputShape Input shape
     * @param outputShape Output shape
     * @return True if successful
     */
    public boolean loadModel(String modelName, String modelPath, Integer[] inputShape, Integer[] outputShape) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            
            // Use GPU if available
            if (useGPU) {
                GpuDelegate gpuDelegate = new GpuDelegate();
                options.addDelegate(gpuDelegate);
            }
            
            // Load model
            MappedByteBuffer modelBuffer = loadModelFile(modelPath);
            Interpreter interpreter = new Interpreter(modelBuffer, options);
            
            // Store model
            models.put(modelName, interpreter);
            inputShapes.put(modelName, inputShape);
            outputShapes.put(modelName, outputShape);
            
            Log.d(TAG, "Model " + modelName + " loaded successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load model " + modelName, e);
            return false;
        }
    }
    
    /**
     * Run inference on model
     * @param modelName Model name
     * @param input Input data
     * @return Output data
     */
    public float[][] runInference(String modelName, float[][] input) {
        Interpreter model = models.get(modelName);
        Integer[] outputShape = outputShapes.get(modelName);
        
        if (model == null || outputShape == null) {
            Log.e(TAG, "Model " + modelName + " not found");
            return null;
        }
        
        try {
            // Prepare output buffer
            float[][] output = new float[outputShape[0]][outputShape[1]];
            
            // Run inference
            model.run(input, output);
            
            return output;
        } catch (Exception e) {
            Log.e(TAG, "Error running inference on model " + modelName, e);
            return null;
        }
    }
    
    /**
     * Run inference on model with ByteBuffer input
     * @param modelName Model name
     * @param inputBuffer Input buffer
     * @return Output data
     */
    public float[] runInference(String modelName, ByteBuffer inputBuffer) {
        Interpreter model = models.get(modelName);
        Integer[] outputShape = outputShapes.get(modelName);
        
        if (model == null || outputShape == null) {
            Log.e(TAG, "Model " + modelName + " not found");
            return null;
        }
        
        try {
            // Prepare output buffer
            float[] output = new float[outputShape[0] * outputShape[1]];
            
            // Run inference
            model.run(inputBuffer, output);
            
            return output;
        } catch (Exception e) {
            Log.e(TAG, "Error running inference on model " + modelName, e);
            return null;
        }
    }
    
    /**
     * Load model file from assets
     * @param modelPath Path to model in assets
     * @return Mapped byte buffer with model data
     * @throws IOException If model cannot be loaded
     */
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    
    /**
     * Create input ByteBuffer for model
     * @param modelName Model name
     * @return Input ByteBuffer
     */
    public ByteBuffer createInputBuffer(String modelName) {
        Integer[] inputShape = inputShapes.get(modelName);
        
        if (inputShape == null) {
            Log.e(TAG, "Model " + modelName + " not found");
            return null;
        }
        
        // Calculate buffer size
        int bufferSize = 1;
        for (int dim : inputShape) {
            bufferSize *= dim;
        }
        bufferSize *= 4; // 4 bytes per float
        
        // Create buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        
        return buffer;
    }
    
    /**
     * Close all models
     */
    public void close() {
        for (Interpreter model : models.values()) {
            model.close();
        }
        models.clear();
    }
}
