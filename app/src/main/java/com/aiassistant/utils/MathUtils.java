package com.aiassistant.utils;

/**
 * Utility class with optimized math functions for performance-critical operations
 * in the advanced AI components.
 */
public class MathUtils {
    
    /**
     * Calculate 2D distance between two points
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate 3D distance between two points
     */
    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Fast approximation of inverse square root (Quake III algorithm)
     */
    public static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x);
        return x;
    }
    
    /**
     * Clamp a value between min and max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamp a value between 0 and 1
     */
    public static float clamp01(float value) {
        return clamp(value, 0.0f, 1.0f);
    }
    
    /**
     * Linear interpolation between two values
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }
    
    /**
     * Smooth step interpolation
     */
    public static float smoothStep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3 - 2 * t);
    }
    
    /**
     * Convert degrees to radians
     */
    public static float toRadians(float degrees) {
        return degrees * (float) Math.PI / 180.0f;
    }
    
    /**
     * Convert radians to degrees
     */
    public static float toDegrees(float radians) {
        return radians * 180.0f / (float) Math.PI;
    }
    
    /**
     * Fast sine approximation
     */
    public static float fastSin(float x) {
        // Normalize to [0, 2*PI]
        x = x % (2 * (float) Math.PI);
        
        // Convert to [0, 4]
        float y = x * (float) (2.0 / Math.PI);
        
        // This approximation works in [0, PI/2]
        // For other quadrants, we use symmetry
        int quadrant = (int) y;
        boolean odd = (quadrant & 1) != 0;
        if (odd) {
            y = 4 - y;
        }
        y -= quadrant;
        
        // Approximation based on parabola
        float y2 = y * y;
        float p = 0.225f * (y2 - y) + y;
        
        return (quadrant & 2) != 0 ? -p : p;
    }
    
    /**
     * Fast cosine approximation
     */
    public static float fastCos(float x) {
        return fastSin(x + (float) Math.PI / 2);
    }
    
    /**
     * Fast arctangent approximation
     */
    public static float fastAtan2(float y, float x) {
        if (x == 0) {
            return y > 0 ? (float) Math.PI / 2 : -((float) Math.PI / 2);
        }
        
        float z = y / x;
        float absZ = Math.abs(z);
        
        // Approximation for |z| <= 1
        float result;
        if (absZ <= 1.0f) {
            float z2 = z * z;
            result = z * (1.0f - 0.1784f * z2) / (1.0f + 0.0675f * z2);
        } else {
            // Approximation for |z| > 1
            float invZ = 1.0f / z;
            float invZ2 = invZ * invZ;
            result = (float) Math.PI / 2 - invZ * (1.0f - 0.1784f * invZ2) / (1.0f + 0.0675f * invZ2);
            if (z < 0) {
                result = -result;
            }
        }
        
        // Adjust for quadrant
        if (x < 0) {
            return y >= 0 ? result + (float) Math.PI : result - (float) Math.PI;
        }
        
        return result;
    }
    
    /**
     * Exponential moving average
     */
    public static float expAverage(float current, float newValue, float alpha) {
        return current * (1 - alpha) + newValue * alpha;
    }
    
    /**
     * Calculate dot product of two 3D vectors
     */
    public static float dot(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 * x2 + y1 * y2 + z1 * z2;
    }
    
    /**
     * Calculate cross product of two 3D vectors
     */
    public static float[] cross(float x1, float y1, float z1, float x2, float y2, float z2) {
        return new float[] {
            y1 * z2 - z1 * y2,
            z1 * x2 - x1 * z2,
            x1 * y2 - y1 * x2
        };
    }
    
    /**
     * Calculate magnitude (length) of a 3D vector
     */
    public static float magnitude(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
    
    /**
     * Normalize a 3D vector to unit length
     */
    public static float[] normalize(float x, float y, float z) {
        float mag = magnitude(x, y, z);
        if (mag < 0.0000001f) {
            return new float[] {0, 0, 0};
        }
        float invMag = 1.0f / mag;
        return new float[] {x * invMag, y * invMag, z * invMag};
    }
    
    /**
     * Fast approximation of exponential function e^x
     */
    public static float fastExp(float x) {
        if (x < -88.0f) return 0.0f;
        if (x > 88.0f) return Float.POSITIVE_INFINITY;
        
        // x = a + b with a = int(x) and b = x - a
        float a = (float) Math.floor(x);
        float b = x - a;
        
        // e^x = e^a * e^b
        // e^a = 2^(a/ln(2))
        float ea = (float) Math.pow(2.0, a * 1.442695040f);
        
        // Approximate e^b using a polynomial
        float eb = 1.0f + b * (1.0f + b * (0.5f + b * (0.166666667f + b * 0.0416666667f)));
        
        return ea * eb;
    }
    
    /**
     * Fast approximation of natural logarithm ln(x)
     */
    public static float fastLn(float x) {
        if (x <= 0) return Float.NEGATIVE_INFINITY;
        
        // Get exponent and mantissa
        int bits = Float.floatToIntBits(x);
        int exp = (bits >> 23) - 127;
        
        // Normalize mantissa to [1, 2)
        int mantissa = (bits & 0x7FFFFF) | 0x800000;
        float f = mantissa / 8388608.0f; // 2^23
        
        // Use polynomial approximation for ln(f) in [1, 2)
        float y = (f - 1.0f) / (f + 1.0f);
        float y2 = y * y;
        float lnf = y * (2.0f + y2 * (2.0f/3.0f + y2 * 2.0f/5.0f));
        
        // ln(x) = ln(2^exp * f) = exp*ln(2) + ln(f)
        return exp * 0.693147181f + lnf;
    }
    
    /**
     * Fast approximation of x^y
     */
    public static float fastPow(float x, float y) {
        if (x <= 0) return (y % 2 == 0) ? Math.abs(x) : 0;
        return fastExp(y * fastLn(x));
    }
    
    /**
     * Fast factorial approximation using Stirling's formula
     */
    public static float fastFactorial(int n) {
        if (n <= 1) return 1.0f;
        if (n <= 10) {
            float result = 1.0f;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }
            return result;
        }
        
        // Stirling's approximation for n > 10
        float invN = 1.0f / n;
        return (float) (Math.sqrt(2 * Math.PI * n) * Math.pow(n * invN, n) * Math.exp(-n + 1.0/(12.0*n)));
    }
    
    /**
     * Compute sigmoid function 1/(1+e^(-x))
     */
    public static float sigmoid(float x) {
        if (x < -10.0f) return 0.0f;
        if (x > 10.0f) return 1.0f;
        return 1.0f / (1.0f + fastExp(-x));
    }
    
    /**
     * Compute derivative of sigmoid function
     */
    public static float sigmoidDerivative(float x) {
        float s = sigmoid(x);
        return s * (1 - s);
    }
    
    /**
     * Compute hyperbolic tangent tanh(x)
     */
    public static float tanh(float x) {
        if (x < -3.0f) return -1.0f;
        if (x > 3.0f) return 1.0f;
        float ex = fastExp(x);
        float emx = fastExp(-x);
        return (ex - emx) / (ex + emx);
    }
    
    /**
     * Compute ReLU (Rectified Linear Unit) activation
     */
    public static float relu(float x) {
        return Math.max(0.0f, x);
    }
    
    /**
     * Compute derivative of ReLU
     */
    public static float reluDerivative(float x) {
        return x > 0 ? 1.0f : 0.0f;
    }
    
    /**
     * Compute Leaky ReLU activation
     */
    public static float leakyRelu(float x, float alpha) {
        return x > 0 ? x : alpha * x;
    }
    
    /**
     * Compute Gaussian function
     */
    public static float gaussian(float x, float mean, float stdDev) {
        float variance = stdDev * stdDev;
        float diff = x - mean;
        return (float) (Math.exp(-(diff * diff) / (2 * variance)) / (stdDev * Math.sqrt(2 * Math.PI)));
    }
    
    /**
     * Compute softmax function for array
     */
    public static float[] softmax(float[] x) {
        float max = Float.NEGATIVE_INFINITY;
        for (float val : x) {
            max = Math.max(max, val);
        }
        
        float sum = 0.0f;
        float[] result = new float[x.length];
        
        for (int i = 0; i < x.length; i++) {
            result[i] = fastExp(x[i] - max);
            sum += result[i];
        }
        
        float invSum = 1.0f / sum;
        for (int i = 0; i < x.length; i++) {
            result[i] *= invSum;
        }
        
        return result;
    }
}
