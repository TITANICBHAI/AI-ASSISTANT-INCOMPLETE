package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ContactEntity;

import java.util.List;

@Dao
public interface ContactDao {
    
    @Insert
    long insert(ContactEntity contact);
    
    @Update
    void update(ContactEntity contact);
    
    @Delete
    void delete(ContactEntity contact);
    
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    List<ContactEntity> getAll();
    
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    LiveData<List<ContactEntity>> getAllLive();
    
    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    ContactEntity getById(long id);
    
    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    ContactEntity getByPhoneNumber(String phoneNumber);
    
    @Query("SELECT * FROM contacts WHERE email = :email LIMIT 1")
    ContactEntity getByEmail(String email);
    
    @Query("SELECT * FROM contacts WHERE isBusiness = 1 ORDER BY name ASC")
    List<ContactEntity> getBusinessContacts();
    
    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    List<ContactEntity> searchByName(String searchQuery);
    
    @Query("SELECT * FROM contacts ORDER BY lastContactTime DESC LIMIT :limit")
    List<ContactEntity> getRecentContacts(int limit);
    
    @Query("DELETE FROM contacts")
    void deleteAll();
    
    @Query("DELETE FROM contacts WHERE id = :id")
    void deleteById(long id);
}
