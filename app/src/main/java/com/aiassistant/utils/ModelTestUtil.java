package com.aiassistant.utils;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for testing TensorFlow Lite models
 */
public class ModelTestUtil {
    private static final String TAG = "ModelTestUtil";
    
    /**
     * Test model loading and basic inference
     * 
     * @param context Application context
     * @return True if successful, false otherwise
     */
    public static boolean testModelLoading(Context context) {
        Log.d(TAG, "Testing TensorFlow Lite model loading");
        
        try {
            // Load model
            Interpreter interpreter = loadModelFile(context, Constants.MODEL_GENERAL_DETECTION);
            
            if (interpreter == null) {
                Log.e(TAG, "Failed to load model");
                return false;
            }
            
            // Get input/output shapes
            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();
            
            Log.d(TAG, "Model loaded successfully");
            Log.d(TAG, "Input shape: [" + inputShape[0] + ", " + inputShape[1] + ", " + 
                       inputShape[2] + ", " + inputShape[3] + "]");
            Log.d(TAG, "Output shape: [" + outputShape[0] + ", " + outputShape[1] + "]");
            
            // Create dummy input
            ByteBuffer inputBuffer = createDummyInput(inputShape);
            
            // Create output container
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, new float[outputShape[0]][outputShape[1]]);
            
            // Run inference
            long startTime = System.currentTimeMillis();
            interpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputs);
            long endTime = System.currentTimeMillis();
            
            Log.d(TAG, "Inference completed in " + (endTime - startTime) + "ms");
            
            // Clean up
            interpreter.close();
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error testing model", e);
            return false;
        }
    }
    
    /**
     * Load a TensorFlow Lite model file
     * 
     * @param context Application context
     * @param modelPath Model file path
     * @return Interpreter instance
     */
    private static Interpreter loadModelFile(Context context, String modelPath) {
        try {
            MappedByteBuffer modelBuffer = loadModelBuffer(context, modelPath);
            
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            
            // Try to enable GPU acceleration if available
            try {
                org.tensorflow.lite.gpu.GpuDelegate gpuDelegate = new org.tensorflow.lite.gpu.GpuDelegate();
                options.addDelegate(gpuDelegate);
                Log.d(TAG, "GPU delegation enabled");
            } catch (Exception e) {
                Log.w(TAG, "GPU acceleration not available: " + e.getMessage());
            }
            
            return new Interpreter(modelBuffer, options);
        } catch (Exception e) {
            Log.e(TAG, "Error loading model file", e);
            return null;
        }
    }
    
    /**
     * Load a model file into a buffer
     * 
     * @param context Application context
     * @param modelPath Model file path
     * @return Mapped byte buffer
     */
    private static MappedByteBuffer loadModelBuffer(Context context, String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getFilesDir() + "/" + modelPath);
        
        try {
            FileChannel fileChannel = fileInputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        } finally {
            fileInputStream.close();
        }
    }
    
    /**
     * Create a dummy input buffer for testing
     * 
     * @param inputShape Input shape
     * @return Input buffer
     */
    private static ByteBuffer createDummyInput(int[] inputShape) {
        // Calculate buffer size
        int bufferSize = 1;
        for (int dim : inputShape) {
            bufferSize *= dim;
        }
        bufferSize *= 4; // 4 bytes per float
        
        // Create buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        
        // Fill with dummy data
        while (buffer.hasRemaining()) {
            buffer.putFloat(0.0f);
        }
        
        // Reset position
        buffer.rewind();
        
        return buffer;
    }
}
