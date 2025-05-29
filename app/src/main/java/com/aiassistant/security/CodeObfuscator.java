package com.aiassistant.security;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Code and data obfuscation utility
 */
public class CodeObfuscator {
    
    private static final String TAG = "CodeObfuscator";
    private static final boolean ENABLE_LOGGING = false;
    
    // Singleton instance
    private static CodeObfuscator instance;
    
    // Crypto utilities
    private final SecureRandom secureRandom;
    private final byte[] masterKey;
    private final AtomicLong operationCounter;
    
    // Obfuscation configuration
    private boolean complexObfuscationEnabled = true;
    private int obfuscationStrength = 2; // 0-3, with 3 being strongest
    
    /**
     * Get the singleton instance
     * 
     * @return The CodeObfuscator instance
     */
    public static synchronized CodeObfuscator getInstance() {
        if (instance == null) {
            instance = new CodeObfuscator();
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private CodeObfuscator() {
        secureRandom = new SecureRandom();
        masterKey = generateMasterKey();
        operationCounter = new AtomicLong(System.currentTimeMillis());
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "CodeObfuscator initialized");
        }
    }
    
    /**
     * Generate a master key for encryption operations
     * 
     * @return The generated master key
     */
    private byte[] generateMasterKey() {
        byte[] key = new byte[32]; // 256-bit key
        secureRandom.nextBytes(key);
        return key;
    }
    
    /**
     * Obfuscate a string with polymorphic encryption
     * 
     * @param input The input string
     * @return The obfuscated string
     */
    public String obfuscateString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        try {
            // Convert string to bytes
            byte[] inputBytes = input.getBytes("UTF-8");
            
            // Apply polymorphic encryption
            byte[] obfuscated = obfuscateBytes(inputBytes);
            
            // Encode result as Base64
            return Base64.encodeToString(obfuscated, Base64.NO_WRAP);
        } catch (Exception e) {
            if (ENABLE_LOGGING) {
                Log.e(TAG, "Error obfuscating string: " + e.getMessage());
            }
            return input; // Return original on error
        }
    }
    
    /**
     * Deobfuscate a previously obfuscated string
     * 
     * @param obfuscatedInput The obfuscated string
     * @return The original string
     */
    public String deobfuscateString(String obfuscatedInput) {
        if (obfuscatedInput == null || obfuscatedInput.isEmpty()) {
            return obfuscatedInput;
        }
        
        try {
            // Decode Base64
            byte[] obfuscatedBytes = Base64.decode(obfuscatedInput, Base64.NO_WRAP);
            
            // Apply deobfuscation
            byte[] original = deobfuscateBytes(obfuscatedBytes);
            
            // Convert back to string
            return new String(original, "UTF-8");
        } catch (Exception e) {
            if (ENABLE_LOGGING) {
                Log.e(TAG, "Error deobfuscating string: " + e.getMessage());
            }
            return obfuscatedInput; // Return obfuscated on error
        }
    }
    
    /**
     * Obfuscate a byte array
     * 
     * @param input The input bytes
     * @return The obfuscated bytes
     */
    public byte[] obfuscateBytes(byte[] input) {
        if (input == null || input.length == 0) {
            return input;
        }
        
        // Get current operation counter and increment
        long counter = operationCounter.getAndIncrement();
        
        // Generate a session key
        byte[] sessionKey = generateSessionKey(counter);
        
        // Create output with header
        // Format: [4 bytes counter][N bytes data]
        byte[] output = new byte[input.length + 4];
        ByteBuffer buffer = ByteBuffer.wrap(output);
        buffer.putInt((int)counter);
        
        // Apply encryption
        for (int i = 0; i < input.length; i++) {
            int keyIndex = i % sessionKey.length;
            byte keyByte = sessionKey[keyIndex];
            byte counterByte = (byte)(counter >> ((i % 4) * 8));
            
            // XOR with key, counter byte, and position
            output[i + 4] = (byte)(input[i] ^ keyByte ^ counterByte ^ (byte)i);
            
            // For higher obfuscation strengths, apply additional transformations
            if (complexObfuscationEnabled && obfuscationStrength >= 2) {
                // Apply additional bit shifting and rotation based on position
                byte temp = output[i + 4];
                int shift = i % 7; // Shift by 0-6 bits
                output[i + 4] = (byte)((temp << shift) | ((temp & 0xFF) >>> (8 - shift)));
            }
        }
        
        return output;
    }
    
    /**
     * Deobfuscate a previously obfuscated byte array
     * 
     * @param input The obfuscated bytes
     * @return The original bytes
     */
    public byte[] deobfuscateBytes(byte[] input) {
        if (input == null || input.length <= 4) {
            return input;
        }
        
        // Extract counter from header
        ByteBuffer buffer = ByteBuffer.wrap(input, 0, 4);
        int counter = buffer.getInt();
        
        // Generate the same session key
        byte[] sessionKey = generateSessionKey(counter);
        
        // Create output buffer
        byte[] output = new byte[input.length - 4];
        
        // Apply decryption
        for (int i = 0; i < output.length; i++) {
            byte encryptedByte = input[i + 4];
            
            // For higher obfuscation strengths, undo additional transformations
            if (complexObfuscationEnabled && obfuscationStrength >= 2) {
                // Undo bit shifting and rotation
                byte temp = encryptedByte;
                int shift = i % 7; // Shift by 0-6 bits
                encryptedByte = (byte)(((temp & 0xFF) >>> shift) | (temp << (8 - shift)));
            }
            
            int keyIndex = i % sessionKey.length;
            byte keyByte = sessionKey[keyIndex];
            byte counterByte = (byte)(counter >> ((i % 4) * 8));
            
            // XOR with same values to reverse encryption
            output[i] = (byte)(encryptedByte ^ keyByte ^ counterByte ^ (byte)i);
        }
        
        return output;
    }
    
    /**
     * Generate a session key based on master key and counter
     * 
     * @param counter The operation counter
     * @return The session key
     */
    private byte[] generateSessionKey(long counter) {
        try {
            // Create a digest of master key + counter
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(masterKey);
            digest.update(ByteBuffer.allocate(8).putLong(counter).array());
            
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            // Fallback if SHA-256 is not available
            byte[] derivedKey = new byte[32];
            new Random(counter ^ Arrays.hashCode(masterKey)).nextBytes(derivedKey);
            return derivedKey;
        }
    }
    
    /**
     * Compute a secure hash of data
     * 
     * @param data The data to hash
     * @return The hash value as hex string
     */
    public String computeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            
            // Convert to hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            if (ENABLE_LOGGING) {
                Log.e(TAG, "Error computing hash: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Verify a hash against data
     * 
     * @param data The data to verify
     * @param expectedHash The expected hash
     * @return Whether the hash matches
     */
    public boolean verifyHash(byte[] data, String expectedHash) {
        String actualHash = computeHash(data);
        return actualHash != null && actualHash.equals(expectedHash);
    }
    
    /**
     * Set whether complex obfuscation is enabled
     * 
     * @param enabled Whether complex obfuscation is enabled
     */
    public void setComplexObfuscationEnabled(boolean enabled) {
        this.complexObfuscationEnabled = enabled;
    }
    
    /**
     * Set the obfuscation strength
     * 
     * @param strength The obfuscation strength (0-3)
     */
    public void setObfuscationStrength(int strength) {
        if (strength >= 0 && strength <= 3) {
            this.obfuscationStrength = strength;
        }
    }
    
    /**
     * Get the obfuscation strength
     * 
     * @return The obfuscation strength
     */
    public int getObfuscationStrength() {
        return this.obfuscationStrength;
    }
    
    /**
     * Generate a secure random token
     * 
     * @param length The token length in bytes
     * @return The token as a hex string
     */
    public String generateSecureToken(int length) {
        byte[] token = new byte[length];
        secureRandom.nextBytes(token);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : token) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}