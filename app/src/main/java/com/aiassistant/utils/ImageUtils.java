package com.aiassistant.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Utility class for image processing
 */
public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    
    /**
     * Convert bitmap to byte array
     * 
     * @param bitmap The bitmap
     * @return The byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    
    /**
     * Convert byte array to bitmap
     * 
     * @param byteArray The byte array
     * @return The bitmap
     */
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    
    /**
     * Convert bitmap to base64 string
     * 
     * @param bitmap The bitmap
     * @return The base64 string
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    
    /**
     * Convert base64 string to bitmap
     * 
     * @param base64 The base64 string
     * @return The bitmap
     */
    public static Bitmap base64ToBitmap(String base64) {
        if (base64 == null) {
            return null;
        }
        
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    
    /**
     * Resize a bitmap
     * 
     * @param bitmap The bitmap
     * @param width The target width
     * @param height The target height
     * @return The resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }
        
        float scaleWidth = ((float) width) / bitmap.getWidth();
        float scaleHeight = ((float) height) / bitmap.getHeight();
        
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }
    
    /**
     * Convert bitmap to float array
     * 
     * @param bitmap The bitmap
     * @return The float array
     */
    public static float[] bitmapToFloatArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height * 3; // RGB channels
        
        float[] floatArray = new float[size];
        int[] intValues = new int[width * height];
        
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);
        
        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            floatArray[i * 3] = ((val >> 16) & 0xFF) / 255.0f; // R
            floatArray[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f; // G
            floatArray[i * 3 + 2] = (val & 0xFF) / 255.0f; // B
        }
        
        return floatArray;
    }
    
    /**
     * Convert bitmap to ByteBuffer
     * 
     * @param bitmap The bitmap
     * @return The ByteBuffer
     */
    public static ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height * 3; // RGB channels
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size * 4); // 4 bytes per float
        int[] intValues = new int[width * height];
        
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);
        
        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f); // R
            byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f); // G
            byteBuffer.putFloat((val & 0xFF) / 255.0f); // B
        }
        
        return byteBuffer;
    }
    
    /**
     * Draw rectangles on a bitmap
     * 
     * @param bitmap The bitmap
     * @param rects The rectangles
     * @param labels The labels
     * @param colors The colors
     * @return The bitmap with rectangles
     */
    public static Bitmap drawRectangles(Bitmap bitmap, Rect[] rects, String[] labels, int[] colors) {
        if (bitmap == null || rects == null) {
            return bitmap;
        }
        
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        
        for (int i = 0; i < rects.length; i++) {
            Rect rect = rects[i];
            int color = (colors != null && i < colors.length) ? colors[i] : Color.GREEN;
            String label = (labels != null && i < labels.length) ? labels[i] : "";
            
            paint.setColor(color);
            canvas.drawRect(rect, paint);
            
            if (!label.isEmpty()) {
                canvas.drawText(label, rect.left, rect.top - 10, textPaint);
            }
        }
        
        return mutableBitmap;
    }
    
    /**
     * Get average color in a region
     * 
     * @param bitmap The bitmap
     * @param rect The rectangle
     * @return The average color
     */
    public static int getAverageColor(Bitmap bitmap, Rect rect) {
        if (bitmap == null || rect == null) {
            return Color.BLACK;
        }
        
        int x = rect.left;
        int y = rect.top;
        int width = rect.width();
        int height = rect.height();
        
        // Make sure the rectangle is within the bitmap bounds
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > bitmap.getWidth()) width = bitmap.getWidth() - x;
        if (y + height > bitmap.getHeight()) height = bitmap.getHeight() - y;
        
        int pixelCount = width * height;
        if (pixelCount <= 0) {
            return Color.BLACK;
        }
        
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, width, x, y, width, height);
        
        long redSum = 0;
        long greenSum = 0;
        long blueSum = 0;
        
        for (int pixel : pixels) {
            redSum += (pixel >> 16) & 0xFF;
            greenSum += (pixel >> 8) & 0xFF;
            blueSum += pixel & 0xFF;
        }
        
        int red = (int) (redSum / pixelCount);
        int green = (int) (greenSum / pixelCount);
        int blue = (int) (blueSum / pixelCount);
        
        return Color.rgb(red, green, blue);
    }
}
