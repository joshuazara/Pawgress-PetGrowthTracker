package com.example.taskperf1.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.PhotoEntry;

import java.util.List;

@Dao
public interface PhotoEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PhotoEntry photo);

    @Update
    void update(PhotoEntry photo);

    @Delete
    void delete(PhotoEntry photo);

    @Query("SELECT * FROM photo_entries WHERE photoId = :photoId")
    LiveData<PhotoEntry> getPhotoEntryById(int photoId);

    @Query("SELECT * FROM photo_entries WHERE petId = :petId ORDER BY captureDate DESC")
    LiveData<List<PhotoEntry>> getPhotoEntriesByPet(int petId);

    @Query("SELECT * FROM photo_entries WHERE petId = :petId AND isFavorite = 1 ORDER BY captureDate DESC")
    LiveData<List<PhotoEntry>> getFavoritePhotosByPet(int petId);

    @Query("SELECT * FROM photo_entries ORDER BY captureDate DESC")
    LiveData<List<PhotoEntry>> getAllPhotoEntries();
}