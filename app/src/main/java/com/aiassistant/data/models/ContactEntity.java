package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Contact entity for database storage
 */
@Entity(tableName = "contacts")
public class ContactEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String phoneNumber;
    private String email;
    private String company;
    private String relationship;
    private boolean isBusiness;
    private String notes;
    private long lastContactTime;
    private int contactFrequency;
    private String preferredContactMethod;
    
    /**
     * Default constructor
     */
    public ContactEntity() {
    }
    
    /**
     * Constructor with main fields
     */
    public ContactEntity(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.lastContactTime = System.currentTimeMillis();
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public boolean isBusiness() {
        return isBusiness;
    }
    
    public void setBusiness(boolean business) {
        isBusiness = business;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public long getLastContactTime() {
        return lastContactTime;
    }
    
    public void setLastContactTime(long lastContactTime) {
        this.lastContactTime = lastContactTime;
    }
    
    public int getContactFrequency() {
        return contactFrequency;
    }
    
    public void setContactFrequency(int contactFrequency) {
        this.contactFrequency = contactFrequency;
    }
    
    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }
    
    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }
    
    /**
     * Convert to CallerInfo
     */
    public CallerInfo toCallerInfo() {
        CallerInfo callerInfo = new CallerInfo(phoneNumber, name);
        callerInfo.setBusiness(isBusiness);
        callerInfo.setCompany(company);
        callerInfo.setRelationshipType(relationship);
        callerInfo.setCallFrequency(contactFrequency);
        callerInfo.setLastCallTimestamp(lastContactTime);
        callerInfo.setNotes(notes);
        return callerInfo;
    }
}
