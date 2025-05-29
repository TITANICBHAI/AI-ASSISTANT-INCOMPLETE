package com.aiassistant.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;

import java.nio.ByteBuffer;

/**
 * Utility class for bitmap operations
 */
public class BitmapUtils {
    
    /**
     * Convert an Android Media.Image to Bitmap
     * @param image Image to convert
     * @return Bitmap representation of the image
     */
    public static Bitmap imageToBitmap(Image image) {
        if (image == null) {
            return null;
        }
        
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);
        
        // Crop if needed due to rowPadding
        if (rowPadding > 0) {
            return Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
        }
        
        return bitmap;
    }
    
    /**
     * Convert a bitmap to grayscale
     * @param source Source bitmap
     * @return Grayscale version of the bitmap
     */
    public static Bitmap toGrayscale(Bitmap source) {
        if (source == null) {
            return null;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }
    
    /**
     * Resize a bitmap to a specified width and height
     * @param source Source bitmap
     * @param newWidth New width
     * @param newHeight New height
     * @return Resized bitmap
     */
    public static Bitmap resize(Bitmap source, int newWidth, int newHeight) {
        if (source == null) {
            return null;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        
        return Bitmap.createBitmap(source, 0, 0, width, height, matrix, false);
    }
    
    /**
     * Calculate the average color of a region in a bitmap
     * @param bitmap Source bitmap
     * @param rect Region to analyze
     * @return Average color (ARGB int)
     */
    public static int getAverageColor(Bitmap bitmap, Rect rect) {
        if (bitmap == null || rect == null) {
            return Color.BLACK;
        }
        
        int left = Math.max(0, rect.left);
        int top = Math.max(0, rect.top);
        int right = Math.min(bitmap.getWidth(), rect.right);
        int bottom = Math.min(bitmap.getHeight(), rect.bottom);
        
        if (right <= left || bottom <= top) {
            return Color.BLACK;
        }
        
        int width = right - left;
        int height = bottom - top;
        int pixelCount = width * height;
        
        if (pixelCount <= 0) {
            return Color.BLACK;
        }
        
        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                int pixel = bitmap.getPixel(x, y);
                totalRed += Color.red(pixel);
                totalGreen += Color.green(pixel);
                totalBlue += Color.blue(pixel);
            }
        }
        
        return Color.rgb(
                (int)(totalRed / pixelCount),
                (int)(totalGreen / pixelCount),
                (int)(totalBlue / pixelCount)
        );
    }
    
    /**
     * Calculate color difference between two colors
     * @param color1 First color
     * @param color2 Second color
     * @return Difference value (0-765, with 0 being identical and 765 being max difference)
     */
    public static int getColorDifference(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }
}