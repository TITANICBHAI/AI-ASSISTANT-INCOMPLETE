package com.aiassistant.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

/**
 * Utility for analyzing images
 */
public class ImageAnalyzer {
    private static final String TAG = "ImageAnalyzer";
    
    private Context context;
    private RenderScript renderScript;
    private ScriptIntrinsicBlur blurScript;
    private ScriptIntrinsicYuvToRGB yuvToRgbScript;
    
    /**
     * Constructor
     * @param context Application context
     */
    public ImageAnalyzer(Context context) {
        this.context = context;
        initializeRenderScript();
    }
    
    /**
     * Initialize RenderScript
     */
    private void initializeRenderScript() {
        this.renderScript = RenderScript.create(context);
        this.blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        this.yuvToRgbScript = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
    }
    
    /**
     * Apply blur to bitmap
     * @param input Input bitmap
     * @param radius Blur radius
     * @return Blurred bitmap
     */
    public Bitmap applyBlur(Bitmap input, float radius) {
        if (input == null) {
            return null;
        }
        
        try {
            Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            
            Allocation allIn = Allocation.createFromBitmap(renderScript, input);
            Allocation allOut = Allocation.createFromBitmap(renderScript, output);
            
            blurScript.setRadius(radius);
            blurScript.setInput(allIn);
            blurScript.forEach(allOut);
            
            allOut.copyTo(output);
            
            allIn.destroy();
            allOut.destroy();
            
            return output;
        } catch (Exception e) {
            Log.e(TAG, "Error applying blur", e);
            return input;
        }
    }
    
    /**
     * Release resources
     */
    public void close() {
        if (blurScript != null) {
            blurScript.destroy();
            blurScript = null;
        }
        
        if (yuvToRgbScript != null) {
            yuvToRgbScript.destroy();
            yuvToRgbScript = null;
        }
        
        if (renderScript != null) {
            renderScript.destroy();
            renderScript = null;
        }
    }
}
