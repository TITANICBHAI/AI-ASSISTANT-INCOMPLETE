package com.aiassistant.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.SentientVoiceSystem;

/**
 * Service to handle voice-initiated outgoing calls,
 * allowing the AI to make calls on behalf of the user.
 */
public class AICallInitiationService {
    private static final String TAG = "AICallInitiationService";
    private static AICallInitiationService instance;
    
    private Context context;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    
    private AICallInitiationService(Context context) {
        this.context = context.getApplicationContext();
        this.aiStateManager = AIStateManager.getInstance(context);
        this.voiceSystem = SentientVoiceSystem.getInstance(context);
    }
    
    public static synchronized AICallInitiationService getInstance(Context context) {
        if (instance == null) {
            instance = new AICallInitiationService(context);
        }
        return instance;
    }
    
    /**
     * Make a call with AI conversation handling
     * 
     * @param phoneNumber The number to call
     * @param contactName The name of the contact (if known)
     * @param speakOnBehalf Whether the AI should speak on behalf of the user
     * @return True if call was successfully initiated
     */
    public boolean makeCall(String phoneNumber, String contactName, boolean speakOnBehalf) {
        Log.d(TAG, "Making call to: " + phoneNumber + " (AI speaking: " + speakOnBehalf + ")");
        
        try {
            // Enable AI call handling if requested
            if (speakOnBehalf) {
                enableAICallHandling();
                
                // Inform user
                voiceSystem.speak("I'll call " + (contactName != null ? contactName : phoneNumber) + 
                                 " and speak with them on your behalf.", "helpful", 0.8f);
            }
            
            // Initiate the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
            
            return true;
        } catch (SecurityException se) {
            Log.e(TAG, "Permission denied for making call: " + se.getMessage());
            voiceSystem.speak("I don't have permission to make calls. Please grant call permission in settings.", "concerned", 0.7f);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error making call: " + e.getMessage());
            voiceSystem.speak("I couldn't make the call due to an error.", "concerned", 0.7f);
            return false;
        }
    }
    
    /**
     * Enable AI call handling for the next call
     */
    private void enableAICallHandling() {
        aiStateManager.setUserPreference("ai_call_handling", "true");
    }
    
    /**
     * Find a contact by name or partial name match
     * 
     * @param name The name to search for
     * @return Contact phone number or null if not found
     */
    public String findContactByName(String name) {
        // In a real implementation, this would query the contacts database
        // For now, implement a basic mock for testing
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        // Check if a contact with this name exists in user preferences
        // Format is "contact_name|phone_number,contact_name2|phone_number2"
        String contactsStr = aiStateManager.getUserPreference("saved_contacts", "");
        String[] contacts = contactsStr.split(",");
        
        for (String contact : contacts) {
            String[] parts = contact.split("\\|");
            if (parts.length == 2) {
                String contactName = parts[0].toLowerCase();
                String contactPhone = parts[1];
                
                if (contactName.contains(name.toLowerCase()) || 
                    name.toLowerCase().contains(contactName)) {
                    return contactPhone;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Save a contact for future reference
     * 
     * @param name Contact name
     * @param phoneNumber Contact phone number
     */
    public void saveContact(String name, String phoneNumber) {
        if (name == null || name.isEmpty() || phoneNumber == null || phoneNumber.isEmpty()) {
            return;
        }
        
        // Get existing contacts
        String contactsStr = aiStateManager.getUserPreference("saved_contacts", "");
        
        // Add new contact
        if (!contactsStr.isEmpty()) {
            contactsStr += ",";
        }
        contactsStr += name + "|" + phoneNumber;
        
        // Save updated contacts
        aiStateManager.setUserPreference("saved_contacts", contactsStr);
        
        Log.d(TAG, "Saved contact: " + name + " - " + phoneNumber);
    }
}