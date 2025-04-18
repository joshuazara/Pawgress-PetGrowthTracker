package com.example.taskperf1.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.GrowthEntry;

import java.util.List;

@Dao
public interface GrowthEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GrowthEntry entry);

    @Update
    void update(GrowthEntry entry);

    @Delete
    void delete(GrowthEntry entry);

    @Query("SELECT * FROM growth_entries WHERE growthId = :growthId")
    LiveData<GrowthEntry> getGrowthEntryById(int growthId);

    @Query("SELECT * FROM growth_entries WHERE petId = :petId ORDER BY entryDate DESC")
    LiveData<List<GrowthEntry>> getGrowthEntriesByPet(int petId);

    @Query("SELECT * FROM growth_entries ORDER BY entryDate DESC")
    LiveData<List<GrowthEntry>> getAllGrowthEntries();

    @Query("SELECT * FROM growth_entries WHERE petId = :petId ORDER BY entryDate DESC LIMIT 1")
    LiveData<GrowthEntry> getLatestGrowthEntryForPet(int petId);
}
