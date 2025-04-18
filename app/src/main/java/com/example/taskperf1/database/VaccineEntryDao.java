package com.example.taskperf1.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.VaccineEntry;

import java.util.Date;
import java.util.List;

@Dao
public interface VaccineEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(VaccineEntry entry);

    @Update
    void update(VaccineEntry entry);

    @Delete
    void delete(VaccineEntry entry);

    @Query("SELECT * FROM vaccine_entries WHERE vaccineId = :vaccineId")
    LiveData<VaccineEntry> getVaccineEntryById(int vaccineId);

    @Query("SELECT * FROM vaccine_entries WHERE petId = :petId ORDER BY administeredDate DESC")
    LiveData<List<VaccineEntry>> getVaccineEntriesByPet(int petId);

    @Query("SELECT * FROM vaccine_entries WHERE petId = :petId AND nextDueDate > :currentDate ORDER BY nextDueDate ASC")
    LiveData<List<VaccineEntry>> getUpcomingVaccinesForPet(int petId, Date currentDate);

    @Query("SELECT * FROM vaccine_entries ORDER BY administeredDate DESC")
    LiveData<List<VaccineEntry>> getAllVaccineEntries();
}
