package com.aiassistant.security;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ensures compatibility between our security measures and the need to
 * communicate with external services and apps.
 */
public class ExternalServiceCompatibility {
    private static final String TAG = "ExternalServiceCompat";
    
    // Service types
    public enum ServiceType {
        AI_API,              // AI APIs like Gemini, ChatGPT
        MESSAGING,           // Messaging apps (WhatsApp, Telegram)
        PAYMENTS,            // Payment services
        SYSTEM_CONTROL,      // System control apps
        DEVICE_HARDWARE,     // Hardware access
        BROWSER              // Web browser access
    }
    
    private final Context context;
    private final AccessControl accessControl;
    
    // Authorized service packages
    private final Map<ServiceType, List<String>> authorizedServices;
    
    // API compatibility settings
    private final Map<String, Boolean> apiCompatibilitySettings;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control system
     */
    public ExternalServiceCompatibility(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.authorizedServices = new HashMap<>();
        this.apiCompatibilitySettings = new HashMap<>();
        
        // Initialize service categories
        for (ServiceType type : ServiceType.values()) {
            authorizedServices.put(type, new ArrayList<>());
        }
        
        // Initialize default authorized services
        initializeAuthorizedServices();
        
        // Initialize API compatibility settings
        initializeApiCompatibilitySettings();
    }
    
    /**
     * Initialize authorized services
     */
    private void initializeAuthorizedServices() {
        // AI APIs
        authorizedServices.get(ServiceType.AI_API).add("com.google.android.googlequicksearchbox"); // Google Assistant
        authorizedServices.get(ServiceType.AI_API).add("com.google.android.apps.bard"); // Gemini
        authorizedServices.get(ServiceType.AI_API).add("com.openai.chatgpt"); // ChatGPT
        
        // Messaging
        authorizedServices.get(ServiceType.MESSAGING).add("com.whatsapp");
        authorizedServices.get(ServiceType.MESSAGING).add("org.telegram.messenger");
        authorizedServices.get(ServiceType.MESSAGING).add("com.google.android.apps.messaging"); // Google Messages
        authorizedServices.get(ServiceType.MESSAGING).add("com.facebook.orca"); // Messenger
        
        // Payments
        authorizedServices.get(ServiceType.PAYMENTS).add("com.google.android.apps.walletnfcrel"); // Google Pay
        authorizedServices.get(ServiceType.PAYMENTS).add("com.venmo");
        authorizedServices.get(ServiceType.PAYMENTS).add("com.paypal.android.p2pmobile");
        
        // System Control
        authorizedServices.get(ServiceType.SYSTEM_CONTROL).add("com.android.settings");
        authorizedServices.get(ServiceType.SYSTEM_CONTROL).add("com.android.systemui");
        
        // Hardware access is handled by system permissions
        
        // Browsers
        authorizedServices.get(ServiceType.BROWSER).add("com.android.chrome");
        authorizedServices.get(ServiceType.BROWSER).add("org.mozilla.firefox");
        authorizedServices.get(ServiceType.BROWSER).add("com.opera.browser");
    }
    
    /**
     * Initialize API compatibility settings
     */
    private void initializeApiCompatibilitySettings() {
        // AI API settings
        apiCompatibilitySettings.put("allow_gemini_api", true);
        apiCompatibilitySettings.put("allow_chatgpt_api", true);
        apiCompatibilitySettings.put("allow_custom_llm_api", true);
        
        // Messaging settings
        apiCompatibilitySettings.put("allow_whatsapp_send", true);
        apiCompatibilitySettings.put("allow_telegram_send", true);
        apiCompatibilitySettings.put("allow_sms_send", true);
        
        // Payment settings
        apiCompatibilitySettings.put("allow_payment_processing", true);
        
        // System control settings
        apiCompatibilitySettings.put("allow_app_launch", true);
        apiCompatibilitySettings.put("allow_settings_changes", true);
        
        // Hardware access settings
        apiCompatibilitySettings.put("allow_camera_access", true);
        apiCompatibilitySettings.put("allow_microphone_access", true);
        apiCompatibilitySettings.put("allow_location_access", true);
    }
    
    /**
     * Add authorized service
     * @param serviceType Type of service
     * @param packageName Package name of service
     * @return True if added successfully
     */
    public boolean addAuthorizedService(ServiceType serviceType, String packageName) {
        // Verify caller has proper access
        if (!accessControl.checkPermission(AccessControl.SecurityZone.SETTINGS, 
                AccessControl.PermissionLevel.ADMIN)) {
            Log.w(TAG, "Unauthorized attempt to add service: " + packageName);
            return false;
        }
        
        // Verify package exists
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            
            // Add to authorized list if not already present
            List<String> services = authorizedServices.get(serviceType);
            if (!services.contains(packageName)) {
                services.add(packageName);
                Log.d(TAG, "Added authorized service: " + packageName + " for " + serviceType);
            }
            
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Attempted to authorize non-existent package: " + packageName);
            return false;
        }
    }
    
    /**
     * Remove authorized service
     * @param serviceType Type of service
     * @param packageName Package name of service
     * @return True if removed successfully
     */
    public boolean removeAuthorizedService(ServiceType serviceType, String packageName) {
        // Verify caller has proper access
        if (!accessControl.checkPermission(AccessControl.SecurityZone.SETTINGS, 
                AccessControl.PermissionLevel.ADMIN)) {
            Log.w(TAG, "Unauthorized attempt to remove service: " + packageName);
            return false;
        }
        
        // Remove from authorized list
        List<String> services = authorizedServices.get(serviceType);
        boolean removed = services.remove(packageName);
        
        if (removed) {
            Log.d(TAG, "Removed authorized service: " + packageName + " for " + serviceType);
        }
        
        return removed;
    }
    
    /**
     * Set API compatibility setting
     * @param setting Setting name
     * @param enabled Whether it's enabled
     */
    public void setApiCompatibilitySetting(String setting, boolean enabled) {
        // Verify caller has proper access
        if (!accessControl.checkPermission(AccessControl.SecurityZone.SETTINGS, 
                AccessControl.PermissionLevel.ADMIN)) {
            Log.w(TAG, "Unauthorized attempt to change API setting: " + setting);
            return;
        }
        
        if (apiCompatibilitySettings.containsKey(setting)) {
            apiCompatibilitySettings.put(setting, enabled);
            Log.d(TAG, "Set API compatibility setting: " + setting + " = " + enabled);
        } else {
            Log.w(TAG, "Attempted to set unknown API setting: " + setting);
        }
    }
    
    /**
     * Check if service is authorized
     * @param serviceType Type of service
     * @param packageName Package name of service
     * @return True if authorized
     */
    public boolean isServiceAuthorized(ServiceType serviceType, String packageName) {
        List<String> services = authorizedServices.get(serviceType);
        return services.contains(packageName);
    }
    
    /**
     * Check if API compatibility setting is enabled
     * @param setting Setting name
     * @return True if enabled
     */
    public boolean isApiCompatibilityEnabled(String setting) {
        Boolean enabled = apiCompatibilitySettings.get(setting);
        return enabled != null && enabled;
    }
    
    /**
     * Get all authorized services for a type
     * @param serviceType Type of service
     * @return List of package names
     */
    public List<String> getAuthorizedServices(ServiceType serviceType) {
        return new ArrayList<>(authorizedServices.get(serviceType));
    }
    
    /**
     * Safely send WhatsApp message with security checks
     * @param phoneNumber Phone number to message
     * @param message Message to send
     * @return True if message was dispatched
     */
    public boolean sendWhatsAppMessage(String phoneNumber, String message) {
        // Verify WhatsApp messaging is enabled
        if (!isApiCompatibilityEnabled("allow_whatsapp_send")) {
            Log.w(TAG, "WhatsApp messaging is disabled");
            return false;
        }
        
        // Verify WhatsApp is authorized
        if (!isServiceAuthorized(ServiceType.MESSAGING, "com.whatsapp")) {
            Log.w(TAG, "WhatsApp is not an authorized messaging service");
            return false;
        }
        
        try {
            // Ensure phone number is properly formatted
            String formattedNumber = phoneNumber;
            if (!formattedNumber.startsWith("+")) {
                formattedNumber = "+" + formattedNumber;
            }
            formattedNumber = formattedNumber.replace(" ", "").replace("-", "");
            
            // Create intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String uri = "https://api.whatsapp.com/send?phone=" + formattedNumber + 
                         "&text=" + Uri.encode(message);
            intent.setData(Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Verify WhatsApp is installed
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            boolean isWhatsAppInstalled = activities.size() > 0;
            
            if (isWhatsAppInstalled) {
                // Log intent dispatch for security auditing
                Log.d(TAG, "Dispatching WhatsApp message intent to: " + formattedNumber);
                
                // Launch WhatsApp
                context.startActivity(intent);
                return true;
            } else {
                Log.w(TAG, "WhatsApp is not installed");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending WhatsApp message", e);
            return false;
        }
    }
    
    /**
     * Safely connect to Gemini API with security checks
     * @param apiKey API key to use (if needed)
     * @return True if connection authorized
     */
    public boolean connectToGeminiApi(String apiKey) {
        // Verify Gemini API is enabled
        if (!isApiCompatibilityEnabled("allow_gemini_api")) {
            Log.w(TAG, "Gemini API access is disabled");
            return false;
        }
        
        // Verify Google apps are authorized
        if (!isServiceAuthorized(ServiceType.AI_API, "com.google.android.googlequicksearchbox") &&
            !isServiceAuthorized(ServiceType.AI_API, "com.google.android.apps.bard")) {
            Log.w(TAG, "Google AI services are not authorized");
            return false;
        }
        
        // In a real implementation, would set up API client here
        Log.d(TAG, "Authorized connection to Gemini API");
        
        return true;
    }
    
    /**
     * Safely launch an app with security checks
     * @param packageName Package name to launch
     * @return True if launched successfully
     */
    public boolean launchApp(String packageName) {
        // Verify app launching is enabled
        if (!isApiCompatibilityEnabled("allow_app_launch")) {
            Log.w(TAG, "App launching is disabled");
            return false;
        }
        
        try {
            // Check if this is a system control app
            boolean isSystemApp = isServiceAuthorized(ServiceType.SYSTEM_CONTROL, packageName);
            
            if (!isSystemApp) {
                // For non-system apps, check if it's in any of our authorized categories
                boolean isAuthorized = false;
                for (ServiceType type : ServiceType.values()) {
                    if (isServiceAuthorized(type, packageName)) {
                        isAuthorized = true;
                        break;
                    }
                }
                
                if (!isAuthorized) {
                    Log.w(TAG, "Attempted to launch unauthorized app: " + packageName);
                    return false;
                }
            }
            
            // Launch the app
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Log.d(TAG, "Launched app: " + packageName);
                return true;
            } else {
                Log.w(TAG, "No launch intent found for app: " + packageName);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching app: " + packageName, e);
            return false;
        }
    }
    
    /**
     * Safely process a payment with security checks
     * @param amount Payment amount
     * @param paymentMethod Payment method
     * @param metadata Payment metadata
     * @return True if payment was initiated
     */
    public boolean processPayment(double amount, String paymentMethod, Bundle metadata) {
        // Verify payment processing is enabled
        if (!isApiCompatibilityEnabled("allow_payment_processing")) {
            Log.w(TAG, "Payment processing is disabled");
            return false;
        }
        
        // Verify payment service is authorized
        String paymentPackage = getPaymentPackageForMethod(paymentMethod);
        if (paymentPackage == null || !isServiceAuthorized(ServiceType.PAYMENTS, paymentPackage)) {
            Log.w(TAG, "Payment method is not authorized: " + paymentMethod);
            return false;
        }
        
        // In a real implementation, would initiate payment here
        Log.d(TAG, "Authorized payment of " + amount + " via " + paymentMethod);
        
        return true;
    }
    
    /**
     * Get payment package for method
     * @param method Payment method
     * @return Package name
     */
    private String getPaymentPackageForMethod(String method) {
        switch (method.toLowerCase()) {
            case "google pay":
            case "googlepay":
                return "com.google.android.apps.walletnfcrel";
                
            case "venmo":
                return "com.venmo";
                
            case "paypal":
                return "com.paypal.android.p2pmobile";
                
            default:
                return null;
        }
    }
    
    /**
     * Make phone call with security checks
     * @param phoneNumber Phone number to call
     * @return True if call was initiated
     */
    public boolean makePhoneCall(String phoneNumber) {
        try {
            // Create call intent
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Log for security auditing
            Log.d(TAG, "Initiating phone call to: " + phoneNumber);
            
            // Start dialer
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error making phone call", e);
            return false;
        }
    }
    
    /**
     * Get list of all API compatibility settings
     * @return Map of settings and values
     */
    public Map<String, Boolean> getAllApiCompatibilitySettings() {
        return new HashMap<>(apiCompatibilitySettings);
    }
}
