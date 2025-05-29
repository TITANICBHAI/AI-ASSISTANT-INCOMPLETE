package com.aiassistant.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Validates the application signature to ensure the app hasn't been tampered with
 * or repackaged with malicious code.
 */
public class SignatureValidator {
    private static final String TAG = "SignatureValidator";
    private static final boolean DEBUG = false;
    
    // Expected signature hash - would be set to the actual value in production
    // This is just a placeholder
    private static final String EXPECTED_SIGNATURE_HASH = 
        "01:23:45:67:89:AB:CD:EF:01:23:45:67:89:AB:CD:EF:01:23:45:67";
    
    // Native method declarations
    private native boolean nativeVerifySignature(byte[] signature);
    private native boolean nativeCheckIntegrity();
    private native String nativeGetExpectedSignatureHash();

    private final Context mContext;
    private final String mPackageName;
    private boolean mIsValidated;
    private String mCurrentSignatureHash;

    /**
     * Constructor initializes the signature validator
     * @param context Application context
     */
    public SignatureValidator(Context context) {
        mContext = context;
        mPackageName = context.getPackageName();
        mIsValidated = false;
        mCurrentSignatureHash = null;
    }

    /**
     * Validates the application signature
     * @return True if signature is valid, false otherwise
     */
    public boolean validateSignature() {
        try {
            // First try native validation for better security
            boolean nativeValidation = false;
            try {
                nativeValidation = nativeCheckIntegrity();
                if (nativeValidation) {
                    mIsValidated = true;
                    return true;
                }
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native signature validation unavailable");
                }
            }
            
            // Fall back to Java implementation
            PackageManager pm = mContext.getPackageManager();
            
            // Get package info with signatures
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = pm.getPackageInfo(mPackageName, 
                                            PackageManager.GET_SIGNING_CERTIFICATES);
                
                if (packageInfo.signingInfo == null) {
                    return false;
                }
                
                // Get signature array
                Signature[] signatures;
                if (packageInfo.signingInfo.hasMultipleSigners()) {
                    signatures = packageInfo.signingInfo.getApkContentsSigners();
                } else {
                    signatures = packageInfo.signingInfo.getSigningCertificateHistory();
                }
                
                if (signatures == null || signatures.length == 0) {
                    return false;
                }
                
                // Validate signatures
                boolean isValid = validateSignatures(signatures);
                mIsValidated = isValid;
                return isValid;
                
            } else {
                packageInfo = pm.getPackageInfo(mPackageName, 
                                            PackageManager.GET_SIGNATURES);
                
                if (packageInfo.signatures == null || 
                    packageInfo.signatures.length == 0) {
                    return false;
                }
                
                // Validate signatures
                boolean isValid = validateSignatures(packageInfo.signatures);
                mIsValidated = isValid;
                return isValid;
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error validating signature", e);
            }
            return false;
        }
    }

    /**
     * Validates an array of signatures
     * @param signatures Signatures to validate
     * @return True if any signature is valid
     */
    private boolean validateSignatures(Signature[] signatures) {
        try {
            for (Signature signature : signatures) {
                // Try native validation first
                try {
                    if (nativeVerifySignature(signature.toByteArray())) {
                        return true;
                    }
                } catch (UnsatisfiedLinkError e) {
                    if (DEBUG) {
                        Log.d(TAG, "Native signature verification unavailable");
                    }
                }
                
                // Get signature hash
                String signatureHash = getSignatureHash(signature);
                mCurrentSignatureHash = signatureHash;
                
                // Get expected hash - first try native method for better security
                String expectedHash = EXPECTED_SIGNATURE_HASH;
                try {
                    String nativeHash = nativeGetExpectedSignatureHash();
                    if (nativeHash != null && !nativeHash.isEmpty()) {
                        expectedHash = nativeHash;
                    }
                } catch (UnsatisfiedLinkError e) {
                    if (DEBUG) {
                        Log.d(TAG, "Native expected hash unavailable");
                    }
                }
                
                // Compare hashes
                if (expectedHash.equals(signatureHash)) {
                    return true;
                }
            }
            
            // No valid signatures found
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error validating signatures", e);
            }
            return false;
        }
    }

    /**
     * Gets a hash string for the given signature
     * @param signature Signature to hash
     * @return String representation of signature hash
     */
    private String getSignatureHash(Signature signature) {
        try {
            // Convert signature to certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream stream = new ByteArrayInputStream(signature.toByteArray());
            X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
            
            // Get SHA-256 hash of the certificate
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(cert.getEncoded());
            
            // Convert hash to string representation
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < hashBytes.length; i++) {
                if (i > 0 && i % 2 == 0) {
                    hash.append(':');
                }
                hash.append(String.format("%02X", hashBytes[i]));
            }
            
            return hash.toString();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error getting signature hash", e);
            }
            return "";
        }
    }

    /**
     * Checks if the signature has been validated
     * @return True if signature was validated
     */
    public boolean isValidated() {
        return mIsValidated;
    }

    /**
     * Gets the current signature hash
     * @return String representation of current signature hash
     */
    public String getCurrentSignatureHash() {
        return mCurrentSignatureHash;
    }
    
    /**
     * Checks if the signature is valid for the given security level
     * @param securityLevel Current security level (0-3)
     * @return True if the signature is valid for the security level
     */
    public boolean isValidForSecurityLevel(int securityLevel) {
        // For security level 0, we're more permissive
        if (securityLevel == 0) {
            return true;
        }
        
        // For security level 1, we validate but accept if unavailable
        if (securityLevel == 1) {
            return validateSignature() || mCurrentSignatureHash == null;
        }
        
        // For higher security levels, we require validation
        return validateSignature();
    }
    
    /**
     * Performs a deep validation of the application package
     * @return True if the package passes deep validation
     */
    public boolean performDeepValidation() {
        try {
            // First try native validation
            try {
                return nativeCheckIntegrity();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native deep validation unavailable");
                }
            }
            
            // Fall back to basic signature validation
            return validateSignature();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error in deep validation", e);
            }
            return false;
        }
    }
}
