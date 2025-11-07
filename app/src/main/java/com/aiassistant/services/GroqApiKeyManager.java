package com.aiassistant.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class GroqApiKeyManager {
    private static final String TAG = "GroqApiKeyManager";
    
    private static final String PREFS_NAME = "groq_api_prefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_IV = "api_key_iv";
    private static final String KEYSTORE_ALIAS = "GroqApiKeyAlias";
    
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    
    private static GroqApiKeyManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private boolean encryptionAvailable;
    
    private GroqApiKeyManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.encryptionAvailable = initializeEncryption();
    }
    
    public static synchronized GroqApiKeyManager getInstance(Context context) {
        if (instance == null) {
            instance = new GroqApiKeyManager(context);
        }
        return instance;
    }
    
    private boolean initializeEncryption() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                );
                
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build();
                
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Encryption initialization failed, falling back to plain storage: " + e.getMessage());
            return false;
        }
    }
    
    public void setApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            clearApiKey();
            return;
        }
        
        SharedPreferences.Editor editor = preferences.edit();
        
        if (encryptionAvailable) {
            try {
                String[] encrypted = encryptApiKey(apiKey);
                editor.putString(KEY_API_KEY, encrypted[0]);
                editor.putString(KEY_IV, encrypted[1]);
            } catch (Exception e) {
                Log.e(TAG, "Encryption failed, storing plain text: " + e.getMessage());
                editor.putString(KEY_API_KEY, apiKey);
                editor.remove(KEY_IV);
            }
        } else {
            editor.putString(KEY_API_KEY, apiKey);
        }
        
        editor.apply();
        Log.d(TAG, "API key saved " + (encryptionAvailable ? "(encrypted)" : "(plain text)"));
    }
    
    public String getApiKey() {
        String encryptedKey = preferences.getString(KEY_API_KEY, null);
        
        if (encryptedKey == null) {
            return null;
        }
        
        String iv = preferences.getString(KEY_IV, null);
        
        if (encryptionAvailable && iv != null) {
            try {
                return decryptApiKey(encryptedKey, iv);
            } catch (Exception e) {
                Log.e(TAG, "Decryption failed: " + e.getMessage());
                return null;
            }
        } else {
            return encryptedKey;
        }
    }
    
    public void clearApiKey() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_API_KEY);
        editor.remove(KEY_IV);
        editor.apply();
        Log.d(TAG, "API key cleared");
    }
    
    public boolean hasApiKey() {
        return preferences.contains(KEY_API_KEY);
    }
    
    private String[] encryptApiKey(String apiKey) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(apiKey.getBytes("UTF-8"));
        
        String encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
        String ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT);
        
        return new String[]{encryptedBase64, ivBase64};
    }
    
    private String decryptApiKey(String encryptedKey, String ivString) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
        
        byte[] encrypted = Base64.decode(encryptedKey, Base64.DEFAULT);
        byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
    
    public boolean isEncryptionAvailable() {
        return encryptionAvailable;
    }
}
