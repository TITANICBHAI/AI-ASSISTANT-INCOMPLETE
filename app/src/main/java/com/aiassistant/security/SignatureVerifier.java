package com.aiassistant.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Verifies application signatures and integrity.
 * Part of the custom anti-tampering implementation.
 */
public class SignatureVerifier {
    private static final String TAG = "SignatureVerifier";
    
    private final Context context;
    private final Map<String, String> classChecksums = new HashMap<>();
    
    // Store known safe signatures
    private String expectedSignature;
    
    /**
     * Constructor
     * @param context Application context
     */
    public SignatureVerifier(Context context) {
        this.context = context;
        
        // Initialize signatures during construction
        initializeSignatures();
        
        // Initialize critical class checksums
        initializeClassChecksums();
    }
    
    /**
     * Initialize signature verification
     */
    private void initializeSignatures() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                // Store the app's signature during first run on a device
                expectedSignature = getSignatureString(packageInfo.signatures[0]);
                Log.d(TAG, "Initialized signature: " + obfuscateString(expectedSignature));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing signatures", e);
        }
    }
    
    /**
     * Initialize checksums for critical classes
     */
    private void initializeClassChecksums() {
        // In a production app, you would compute these values during build
        // and store them in a protected format
        
        // This is just a demonstration - in reality you would have actual
        // checksums of compiled classes or code patterns
        classChecksums.put("AIStateManager", "d8e8fca2dc0f896fd7cb4cb0031ba249");
        classChecksums.put("AIState", "7b52009b64fd0a2a49e6d8a939753077");
        classChecksums.put("AntiDetectionManager", "bd307a3ec329e10a2cff8fb87480823d");
        classChecksums.put("SecurityProtectionSystem", "fa35e192121eabf3dabf9f5ea6abdbcb");
    }
    
    /**
     * Verify the application signature
     * @return True if signature matches
     */
    public boolean verifyAppSignature() {
        if (expectedSignature == null || expectedSignature.isEmpty()) {
            Log.e(TAG, "No expected signature available");
            return false;
        }
        
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                String currentSignature = getSignatureString(packageInfo.signatures[0]);
                
                // Compare with expected signature
                boolean result = expectedSignature.equals(currentSignature);
                if (!result) {
                    Log.e(TAG, "Signature verification failed! Tampering detected!");
                    // In a real app, you might take more drastic action here
                }
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying app signature", e);
        }
        
        return false;
    }
    
    /**
     * Verify the integrity of a critical class
     * @param className Name of class to verify
     * @param classBytes Bytes of class
     * @return True if integrity is preserved
     */
    public boolean verifyClassIntegrity(String className, byte[] classBytes) {
        if (!classChecksums.containsKey(className)) {
            Log.w(TAG, "No checksum available for class: " + className);
            return true; // Don't fail for classes we don't check
        }
        
        String expectedChecksum = classChecksums.get(className);
        String actualChecksum = calculateChecksum(classBytes);
        
        boolean result = expectedChecksum.equals(actualChecksum);
        if (!result) {
            Log.e(TAG, "Class integrity verification failed for: " + className);
        }
        
        return result;
    }
    
    /**
     * Verify the integrity of a critical method
     * @param className Name of containing class
     * @param methodName Name of method
     * @param methodBytes Bytes of method
     * @return True if integrity is preserved
     */
    public boolean verifyMethodIntegrity(String className, String methodName, byte[] methodBytes) {
        // In a real implementation, we would have method-level checksums
        // This is a simplified version that just checks if the method exists
        
        String checksum = calculateChecksum(methodBytes);
        Log.d(TAG, "Method checksum for " + className + "." + methodName + ": " + checksum);
        
        // For this demo, we'll return true but log the check
        return true;
    }
    
    /**
     * Convert signature to string representation
     * @param sig Signature to convert
     * @return String representation of signature
     */
    private String getSignatureString(Signature sig) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] signatureBytes = sig.toByteArray();
            digest.update(signatureBytes);
            byte[] digestBytes = digest.digest();
            return bytesToHexString(digestBytes);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error processing signature", e);
            return "";
        }
    }
    
    /**
     * Calculate checksum for class bytes
     * @param data Bytes to checksum
     * @return Checksum as string
     */
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] digestBytes = digest.digest(data);
            return bytesToHexString(digestBytes);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error calculating checksum", e);
            return "";
        }
    }
    
    /**
     * Convert bytes to hex string
     * @param bytes Bytes to convert
     * @return Hex string
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Obfuscate a string for logging
     * @param input String to obfuscate
     * @return Obfuscated string
     */
    private String obfuscateString(String input) {
        if (input == null || input.length() < 8) {
            return "***";
        }
        
        // Just show first and last characters
        return input.substring(0, 4) + "..." + input.substring(input.length() - 4);
    }
    
    /**
     * Get the integrity status of the application
     * @return True if integrity is intact
     */
    public boolean getIntegrityStatus() {
        // Perform app signature verification
        boolean signatureValid = verifyAppSignature();
        
        // This would be extended with other integrity checks in a real app
        
        return signatureValid;
    }
}
