package com.aiassistant.security;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Obfuscates runtime data in memory to prevent analysis and detection.
 * This class implements various techniques to protect sensitive data
 * such as strings, data structures, and algorithms from memory scanning.
 */
public class RuntimeDataObfuscator {
    private static final String TAG = "RuntimeDataObfuscator";
    private static final boolean DEBUG = false;
    
    // Encryption constants
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 256;
    
    // Native method declarations
    private native void nativeInitDataObfuscator();
    private native byte[] nativeObfuscateData(byte[] data);
    private native byte[] nativeDeobfuscateData(byte[] obfuscatedData);
    private native void nativeObfuscateMemoryRegion(long address, int size);
    private native void nativeClearSensitiveData(Object obj);

    private final SecureRandom mRandom;
    private final ConcurrentHashMap<String, byte[]> mKeyCache;
    private final ConcurrentHashMap<String, byte[]> mObfuscatedData;
    private SecretKey mCurrentKey;
    private byte[] mCurrentIV;

    /**
     * Constructor initializes the runtime data obfuscator
     */
    public RuntimeDataObfuscator() {
        mRandom = new SecureRandom();
        mKeyCache = new ConcurrentHashMap<>();
        mObfuscatedData = new ConcurrentHashMap<>();
        
        // Initialize encryption
        initEncryption();
        
        // Initialize native component
        try {
            nativeInitDataObfuscator();
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to initialize native data obfuscator", e);
            }
        }
    }

    /**
     * Initializes encryption components
     */
    private void initEncryption() {
        try {
            // Generate a random AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_LENGTH, mRandom);
            mCurrentKey = keyGen.generateKey();
            
            // Generate a random IV
            mCurrentIV = new byte[12]; // 96 bits, standard for GCM
            mRandom.nextBytes(mCurrentIV);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error initializing encryption", e);
            }
        }
    }

    /**
     * Obfuscates a string to protect it in memory
     * @param input String to obfuscate
     * @return Obfuscated string identifier
     */
    public String obfuscateString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        try {
            String identifier = generateIdentifier(input);
            
            // Try native obfuscation first
            try {
                byte[] obfuscated = nativeObfuscateData(input.getBytes(StandardCharsets.UTF_8));
                mObfuscatedData.put(identifier, obfuscated);
                return identifier;
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native data obfuscation unavailable");
                }
            }
            
            // Fall back to Java implementation
            byte[] encrypted = encryptData(input.getBytes(StandardCharsets.UTF_8));
            mObfuscatedData.put(identifier, encrypted);
            
            return identifier;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error obfuscating string", e);
            }
            return input; // Fallback to original string
        }
    }

    /**
     * Deobfuscates a previously obfuscated string
     * @param identifier Identifier of the obfuscated string
     * @return Original string, or empty if not found
     */
    public String deobfuscateString(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "";
        }
        
        try {
            byte[] obfuscated = mObfuscatedData.get(identifier);
            if (obfuscated == null) {
                return "";
            }
            
            // Try native deobfuscation first
            try {
                byte[] deobfuscated = nativeDeobfuscateData(obfuscated);
                return new String(deobfuscated, StandardCharsets.UTF_8);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native data deobfuscation unavailable");
                }
            }
            
            // Fall back to Java implementation
            byte[] decrypted = decryptData(obfuscated);
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error deobfuscating string", e);
            }
            return "";
        }
    }

    /**
     * Obfuscates byte array data
     * @param data Byte array to obfuscate
     * @return Obfuscated data byte array
     */
    public byte[] obfuscateData(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        
        try {
            // Try native obfuscation first
            try {
                return nativeObfuscateData(data);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native data obfuscation unavailable");
                }
            }
            
            // Fall back to Java implementation
            return encryptData(data);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error obfuscating data", e);
            }
            return data;
        }
    }

    /**
     * Deobfuscates previously obfuscated byte array data
     * @param obfuscatedData Obfuscated data to deobfuscate
     * @return Original data byte array
     */
    public byte[] deobfuscateData(byte[] obfuscatedData) {
        if (obfuscatedData == null || obfuscatedData.length == 0) {
            return new byte[0];
        }
        
        try {
            // Try native deobfuscation first
            try {
                return nativeDeobfuscateData(obfuscatedData);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native data deobfuscation unavailable");
                }
            }
            
            // Fall back to Java implementation
            return decryptData(obfuscatedData);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error deobfuscating data", e);
            }
            return obfuscatedData;
        }
    }

    /**
     * Clears sensitive data from an object
     * @param obj Object containing sensitive data
     */
    public void clearSensitiveData(Object obj) {
        if (obj == null) {
            return;
        }
        
        try {
            // Try native clearing first
            try {
                nativeClearSensitiveData(obj);
                return;
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native data clearing unavailable");
                }
            }
            
            // Fall back to Java implementation
            if (obj instanceof String) {
                secureOverwrite((String) obj);
            } else if (obj instanceof byte[]) {
                secureOverwrite((byte[]) obj);
            } else if (obj instanceof char[]) {
                secureOverwrite((char[]) obj);
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error clearing sensitive data", e);
            }
        }
    }

    /**
     * Securely overwrites a string with random data
     * @param str String to overwrite
     */
    private void secureOverwrite(String str) {
        try {
            // Unfortunately, Java strings are immutable
            // This doesn't directly overwrite the string but can help
            // with garbage collection
            
            // Convert to char array and overwrite that
            char[] chars = str.toCharArray();
            secureOverwrite(chars);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error overwriting string", e);
            }
        }
    }

    /**
     * Securely overwrites a byte array with random data
     * @param data Byte array to overwrite
     */
    private void secureOverwrite(byte[] data) {
        try {
            // Overwrite with random data
            mRandom.nextBytes(data);
            
            // Overwrite again with zeros
            Arrays.fill(data, (byte) 0);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error overwriting byte array", e);
            }
        }
    }

    /**
     * Securely overwrites a char array with random data
     * @param chars Char array to overwrite
     */
    private void secureOverwrite(char[] chars) {
        try {
            // Overwrite with random data
            for (int i = 0; i < chars.length; i++) {
                chars[i] = (char) mRandom.nextInt(Character.MAX_VALUE);
            }
            
            // Overwrite again with zeros
            Arrays.fill(chars, (char) 0);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error overwriting char array", e);
            }
        }
    }

    /**
     * Generates a unique identifier for obfuscated data
     * @param input Input string to generate identifier for
     * @return Unique identifier
     */
    private String generateIdentifier(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Add some randomness to prevent hash matching
            byte[] randomBytes = new byte[8];
            mRandom.nextBytes(randomBytes);
            
            // Combine hash and random bytes
            byte[] combined = new byte[hash.length + randomBytes.length];
            System.arraycopy(hash, 0, combined, 0, hash.length);
            System.arraycopy(randomBytes, 0, combined, hash.length, randomBytes.length);
            
            // Use Base64 to create a string representation
            return Base64.encodeToString(combined, Base64.NO_WRAP);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error generating identifier", e);
            }
            
            // Fallback to a random identifier
            byte[] random = new byte[16];
            mRandom.nextBytes(random);
            return Base64.encodeToString(random, Base64.NO_WRAP);
        }
    }

    /**
     * Encrypts data using AES-GCM
     * @param data Data to encrypt
     * @return Encrypted data
     */
    private byte[] encryptData(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, mCurrentIV);
            cipher.init(Cipher.ENCRYPT_MODE, mCurrentKey, spec);
            
            return cipher.doFinal(data);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error encrypting data", e);
            }
            
            // Fallback to simple XOR if encryption fails
            return xorEncrypt(data);
        }
    }

    /**
     * Decrypts data using AES-GCM
     * @param encryptedData Encrypted data to decrypt
     * @return Decrypted data
     */
    private byte[] decryptData(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, mCurrentIV);
            cipher.init(Cipher.DECRYPT_MODE, mCurrentKey, spec);
            
            return cipher.doFinal(encryptedData);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error decrypting data", e);
            }
            
            // Fallback to simple XOR if decryption fails
            return xorDecrypt(encryptedData);
        }
    }

    /**
     * Simple XOR encryption as fallback method
     * @param data Data to encrypt
     * @return XOR encrypted data
     */
    private byte[] xorEncrypt(byte[] data) {
        byte[] key = mCurrentKey.getEncoded();
        byte[] encrypted = new byte[data.length];
        
        for (int i = 0; i < data.length; i++) {
            encrypted[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        
        return encrypted;
    }

    /**
     * Simple XOR decryption as fallback method
     * @param encryptedData Data to decrypt
     * @return XOR decrypted data
     */
    private byte[] xorDecrypt(byte[] encryptedData) {
        // XOR is symmetric, so encryption and decryption are the same
        return xorEncrypt(encryptedData);
    }
    
    /**
     * Rotates encryption keys periodically for enhanced security
     */
    public void rotateKeys() {
        try {
            // Generate a new key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_LENGTH, mRandom);
            SecretKey newKey = keyGen.generateKey();
            
            // Generate a new IV
            byte[] newIV = new byte[12]; // 96 bits, standard for GCM
            mRandom.nextBytes(newIV);
            
            // Re-encrypt all obfuscated data with the new key
            Map<String, byte[]> newObfuscatedData = new HashMap<>();
            
            for (Map.Entry<String, byte[]> entry : mObfuscatedData.entrySet()) {
                // Decrypt with old key
                byte[] decrypted;
                try {
                    decrypted = decryptData(entry.getValue());
                } catch (Exception e) {
                    // Skip this entry if it can't be decrypted
                    continue;
                }
                
                // Save the old key temporarily
                SecretKey oldKey = mCurrentKey;
                byte[] oldIV = mCurrentIV;
                
                // Set the new key and encrypt
                mCurrentKey = newKey;
                mCurrentIV = newIV;
                byte[] newEncrypted = encryptData(decrypted);
                
                // Store the new encrypted data
                newObfuscatedData.put(entry.getKey(), newEncrypted);
                
                // Securely wipe the decrypted data
                secureOverwrite(decrypted);
            }
            
            // Set the new key as current
            mCurrentKey = newKey;
            mCurrentIV = newIV;
            
            // Replace the old obfuscated data with the new one
            mObfuscatedData.clear();
            mObfuscatedData.putAll(newObfuscatedData);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error rotating keys", e);
            }
        }
    }
}
