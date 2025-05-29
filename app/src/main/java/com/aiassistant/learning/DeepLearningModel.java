package com.aiassistant.learning;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for TensorFlow Lite model inference
 */
public class DeepLearningModel {
    private static final String TAG = "DeepLearningModel";
    
    // TensorFlow interpreter
    private Interpreter interpreter;
    private GpuDelegate gpuDelegate;
    
    // Model dimensions
    private int inputWidth;
    private int inputHeight;
    private int inputChannels;
    private int outputSize;
    
    // Input and output buffers
    private ByteBuffer inputBuffer;
    private float[][] outputBuffer;
    
    // Performance metrics
    private long averageInferenceTime;
    private long inferenceCount;
    
    /**
     * Constructor
     * 
     * @param context Application context
     * @param modelName Name of the model file in assets
     * @param inputWidth Input width
     * @param inputHeight Input height
     * @param inputChannels Input channels
     * @param outputSize Output size
     * @param useGpu Whether to use GPU acceleration if available
     */
    public DeepLearningModel(Context context, String modelName, int inputWidth, int inputHeight, 
                            int inputChannels, int outputSize, boolean useGpu) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.inputChannels = inputChannels;
        this.outputSize = outputSize;
        
        try {
            // Load model
            MappedByteBuffer modelBuffer = loadModelFile(context, modelName);
            
            // Set up interpreter
            Interpreter.Options options = new Interpreter.Options();
            
            // Set up GPU delegate if requested and available
            if (useGpu) {
                CompatibilityList compatList = new CompatibilityList();
                if (compatList.isDelegateSupportedOnThisDevice()) {
                    GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
                    gpuDelegate = new GpuDelegate(delegateOptions);
                    options.addDelegate(gpuDelegate);
                    Log.d(TAG, "GPU acceleration enabled for model: " + modelName);
                } else {
                    Log.d(TAG, "GPU acceleration not supported for model: " + modelName);
                }
            }
            
            // Create interpreter
            interpreter = new Interpreter(modelBuffer, options);
            
            // Initialize buffers
            inputBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * inputChannels * 4);
            inputBuffer.order(ByteOrder.nativeOrder());
            
            outputBuffer = new float[1][outputSize];
            
            Log.d(TAG, "Model loaded: " + modelName);
        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + modelName, e);
        }
    }
    
    /**
     * Load model file from assets
     * 
     * @param context Application context
     * @param modelName Model name
     * @return MappedByteBuffer containing the model
     * @throws IOException If the model cannot be loaded
     */
    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        File file = new File(context.getFilesDir(), modelName);
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = 0;
            long declaredLength = fileChannel.size();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } else {
            throw new IOException("Model file not found: " + modelName);
        }
    }
    
    /**
     * Prepare input buffer with normalized float data
     * 
     * @param inputData Input float array
     */
    private void prepareInput(float[] inputData) {
        inputBuffer.rewind();
        
        // Copy input data to buffer
        for (float value : inputData) {
            inputBuffer.putFloat(value);
        }
    }
    
    /**
     * Run inference on input data
     * 
     * @param inputData Input data as float array
     * @return Output as float array
     */
    public float[] runInference(float[] inputData) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is null");
            return new float[outputSize];
        }
        
        try {
            // Prepare input
            prepareInput(inputData);
            
            // Run inference
            long startTime = SystemClock.elapsedRealtimeNanos();
            interpreter.run(inputBuffer, outputBuffer);
            long endTime = SystemClock.elapsedRealtimeNanos();
            
            // Update performance metrics
            long inferenceTime = (endTime - startTime) / 1000000; // Convert to ms
            updatePerformanceMetrics(inferenceTime);
            
            // Convert output to array
            float[] output = new float[outputSize];
            for (int i = 0; i < outputSize; i++) {
                output[i] = outputBuffer[0][i];
            }
            
            return output;
        } catch (Exception e) {
            Log.e(TAG, "Error running inference", e);
            return new float[outputSize];
        }
    }
    
    /**
     * Run inference on input data with custom options
     * 
     * @param inputData Input data
     * @param options Custom inference options
     * @return Output as float array
     */
    public float[] runInferenceWithOptions(float[] inputData, Map<String, Object> options) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is null");
            return new float[outputSize];
        }
        
        try {
            // Prepare input
            prepareInput(inputData);
            
            // Prepare output
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, outputBuffer);
            
            // Run inference
            long startTime = SystemClock.elapsedRealtimeNanos();
            interpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputs);
            long endTime = SystemClock.elapsedRealtimeNanos();
            
            // Update performance metrics
            long inferenceTime = (endTime - startTime) / 1000000; // Convert to ms
            updatePerformanceMetrics(inferenceTime);
            
            // Convert output to array
            float[] output = new float[outputSize];
            for (int i = 0; i < outputSize; i++) {
                output[i] = outputBuffer[0][i];
            }
            
            return output;
        } catch (Exception e) {
            Log.e(TAG, "Error running inference with options", e);
            return new float[outputSize];
        }
    }
    
    /**
     * Update performance metrics
     * 
     * @param inferenceTime Inference time in ms
     */
    private void updatePerformanceMetrics(long inferenceTime) {
        if (inferenceCount == 0) {
            averageInferenceTime = inferenceTime;
        } else {
            averageInferenceTime = (averageInferenceTime * inferenceCount + inferenceTime) / (inferenceCount + 1);
        }
        inferenceCount++;
        
        if (inferenceCount % 100 == 0) {
            Log.d(TAG, "Average inference time after " + inferenceCount + " inferences: " + 
                  averageInferenceTime + " ms");
        }
    }
    
    /**
     * Get average inference time
     * 
     * @return Average inference time in ms
     */
    public long getAverageInferenceTime() {
        return averageInferenceTime;
    }
    
    /**
     * Get inference count
     * 
     * @return Number of inferences run
     */
    public long getInferenceCount() {
        return inferenceCount;
    }
    
    /**
     * Close the model and release resources
     */
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
        
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        
        Log.d(TAG, "Model closed");
    }
}
