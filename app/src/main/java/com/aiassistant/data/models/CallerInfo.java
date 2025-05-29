package com.aiassistant.data.models;

/**
 * Class to store information about a caller
 */
public class CallerInfo {
    private String phoneNumber;
    private String displayName;
    private String contactURI;
    private String emailAddress;
    private boolean knownContact;
    private boolean previouslyCalled;
    private long lastCallTime;
    private int callCount;
    
    /**
     * Create a new CallerInfo with just a phone number
     */
    public CallerInfo(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.displayName = formatPhoneNumber(phoneNumber);
        this.knownContact = false;
        this.previouslyCalled = false;
        this.callCount = 0;
    }
    
    /**
     * Create a new CallerInfo with phone number and name
     */
    public CallerInfo(String phoneNumber, String displayName) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.knownContact = true;
        this.previouslyCalled = false;
        this.callCount = 0;
    }
    
    /**
     * Format phone number for display
     */
    private String formatPhoneNumber(String number) {
        // Simple formatting - in a real app, use proper phone formatting
        if (number.length() >= 10) {
            return number.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
        }
        return number;
    }
    
    // Getters and setters
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getContactURI() {
        return contactURI;
    }
    
    public void setContactURI(String contactURI) {
        this.contactURI = contactURI;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public boolean isKnownContact() {
        return knownContact;
    }
    
    public void setKnownContact(boolean knownContact) {
        this.knownContact = knownContact;
    }
    
    public boolean isPreviouslyCalled() {
        return previouslyCalled;
    }
    
    public void setPreviouslyCalled(boolean previouslyCalled) {
        this.previouslyCalled = previouslyCalled;
    }
    
    public long getLastCallTime() {
        return lastCallTime;
    }
    
    public void setLastCallTime(long lastCallTime) {
        this.lastCallTime = lastCallTime;
    }
    
    public int getCallCount() {
        return callCount;
    }
    
    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
    
    /**
     * Increment call count
     */
    public void incrementCallCount() {
        this.callCount++;
    }
}
