package com.aiassistant.utils;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for cryptography-related functions, including encryption,
 * decryption, hashing, and secure storage of sensitive data.
 */
public class CryptoUtils {
    private static final String TAG = "CryptoUtils";
    
    // Singleton instance
    private static CryptoUtils instance;
    
    // Secret key for symmetric encryption
    private SecretKey secretKey;
    
    // KeyPair for asymmetric encryption
    private KeyPair keyPair;
    
    // Random number generator
    private SecureRandom secureRandom;
    
    // Lists for protected references
    private static final List<ByteBuffer> protectedBuffers = new ArrayList<>();
    private static final List<Object> protectedObjects = new ArrayList<>();
    private static final Map<String, byte[]> sensitiveData = new HashMap<>();
    
    /**
     * Private constructor for singleton pattern
     */
    private CryptoUtils() {
        try {
            // Initialize secure random
            secureRandom = new SecureRandom();
            
            // Generate a symmetric key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, secureRandom);
            secretKey = keyGen.generateKey();
            
            // Generate key pair for asymmetric encryption
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
            keyPairGen.initialize(new ECGenParameterSpec("secp256r1"), secureRandom);
            keyPair = keyPairGen.generateKeyPair();
            
            Log.d(TAG, "CryptoUtils initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing CryptoUtils", e);
        }
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized CryptoUtils getInstance() {
        if (instance == null) {
            instance = new CryptoUtils();
        }
        return instance;
    }
    
    /**
     * Add a ByteBuffer to the protected list
     */
    public static void addToProtectedBuffers(ByteBuffer buffer) {
        synchronized (protectedBuffers) {
            protectedBuffers.add(buffer);
        }
    }
    
    /**
     * Add an object to the protected list
     */
    public static void addToProtectedObjects(Object obj) {
        synchronized (protectedObjects) {
            protectedObjects.add(obj);
        }
    }
    
    /**
     * Store sensitive data with encryption
     */
    public static boolean storeSensitiveData(String key, byte[] data) {
        try {
            CryptoUtils utils = getInstance();
            
            // Generate a random IV
            byte[] iv = new byte[16];
            utils.secureRandom.nextBytes(iv);
            
            // Encrypt the data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, utils.secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(data);
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            // Store the encrypted data
            synchronized (sensitiveData) {
                sensitiveData.put(key, combined);
            }
            
            Log.d(TAG, "Stored sensitive data for key: " + key);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error storing sensitive data", e);
            return false;
        }
    }
    
    /**
     * Retrieve and decrypt sensitive data
     */
    public static byte[] retrieveSensitiveData(String key) {
        try {
            CryptoUtils utils = getInstance();
            
            // Get the stored encrypted data
            byte[] combined;
            synchronized (sensitiveData) {
                combined = sensitiveData.get(key);
            }
            
            if (combined == null) {
                Log.w(TAG, "No sensitive data found for key: " + key);
                return null;
            }
            
            // Extract IV and encrypted data
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] encryptedData = Arrays.copyOfRange(combined, 16, combined.length);
            
            // Decrypt the data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, utils.secretKey, parameterSpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            Log.d(TAG, "Retrieved sensitive data for key: " + key);
            return decryptedData;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving sensitive data", e);
            return null;
        }
    }
    
    /**
     * Encrypt a string using AES encryption
     */
    public static String encryptString(String plaintext) {
        try {
            CryptoUtils utils = getInstance();
            
            // Generate a random IV
            byte[] iv = new byte[16];
            utils.secureRandom.nextBytes(iv);
            
            // Set up cipher for encryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, utils.secretKey, ivParameterSpec);
            
            // Encrypt the data
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            
            // Combine IV and encrypted part
            byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);
            
            // Encode as Base64 string
            return Base64.encodeToString(encryptedIVAndText, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting string", e);
            return null;
        }
    }
    
    /**
     * Decrypt a string that was encrypted with AES
     */
    public static String decryptString(String encryptedText) {
        try {
            CryptoUtils utils = getInstance();
            
            // Decode the Base64 string
            byte[] encryptedIVAndText = Base64.decode(encryptedText, Base64.DEFAULT);
            
            // Extract IV
            byte[] iv = Arrays.copyOfRange(encryptedIVAndText, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(encryptedIVAndText, 16, encryptedIVAndText.length);
            
            // Set up cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, utils.secretKey, ivParameterSpec);
            
            // Decrypt
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting string", e);
            return null;
        }
    }
    
    /**
     * Generate a secure, random string of specified length
     */
    public static String generateRandomString(int length) {
        try {
            CryptoUtils utils = getInstance();
            
            // Characters to use in the random string
            String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
            
            // Generate random string
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int randomIndex = utils.secureRandom.nextInt(allowedChars.length());
                sb.append(allowedChars.charAt(randomIndex));
            }
            
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error generating random string", e);
            return null;
        }
    }
    
    /**
     * Compute SHA-256 hash of data
     */
    public static byte[] sha256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (Exception e) {
            Log.e(TAG, "Error computing SHA-256 hash", e);
            return null;
        }
    }
    
    /**
     * Convert a hash to a hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Generate a secure key based on a passphrase
     */
    public static SecretKey generateKeyFromPassphrase(String passphrase, byte[] salt) {
        try {
            // Use a key derivation function to generate a key from passphrase
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] key = digest.digest(passphrase.getBytes("UTF-8"));
            
            // Create a secret key from the derived bytes
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            Log.e(TAG, "Error generating key from passphrase", e);
            return null;
        }
    }
    
    /**
     * Generate random bytes for use as salt or IV
     */
    public static byte[] generateRandomBytes(int length) {
        try {
            CryptoUtils utils = getInstance();
            
            byte[] bytes = new byte[length];
            utils.secureRandom.nextBytes(bytes);
            return bytes;
        } catch (Exception e) {
            Log.e(TAG, "Error generating random bytes", e);
            return null;
        }
    }
    
    /**
     * Obfuscate a byte array by XORing with a key
     */
    public static byte[] obfuscate(byte[] data, byte[] key) {
        if (data == null || key == null || key.length == 0) {
            return data;
        }
        
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        
        return result;
    }
    
    /**
     * Deobfuscate a byte array that was XORed with a key
     */
    public static byte[] deobfuscate(byte[] data, byte[] key) {
        // XOR is symmetric, so obfuscate and deobfuscate are the same operation
        return obfuscate(data, key);
    }
    
    /**
     * Generate a secure token for authentication
     */
    public static String generateSecureToken() {
        // Generate 32 random bytes
        byte[] randomBytes = generateRandomBytes(32);
        
        // Convert to Base64 string
        return Base64.encodeToString(randomBytes, Base64.URL_SAFE | Base64.NO_PADDING);
    }
    
    /**
     * Securely compare two strings in constant time to prevent timing attacks
     */
    public static boolean secureCompare(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        
        // Convert strings to byte arrays
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        
        // Check if lengths are different
        if (aBytes.length != bBytes.length) {
            return false;
        }
        
        // Perform constant-time comparison
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        
        return result == 0;
    }
    
    /**
     * Clear all protected buffers and objects (used during cleanup)
     */
    public static void clearProtectedData() {
        synchronized (protectedBuffers) {
            for (ByteBuffer buffer : protectedBuffers) {
                // Clear the buffer data
                buffer.clear();
                
                // Fill with zeros for extra security
                int size = buffer.capacity();
                buffer.position(0);
                byte[] zeros = new byte[size];
                buffer.put(zeros);
            }
            protectedBuffers.clear();
        }
        
        synchronized (protectedObjects) {
            protectedObjects.clear();
        }
        
        synchronized (sensitiveData) {
            sensitiveData.clear();
        }
        
        Log.d(TAG, "Cleared all protected data");
    }
}
