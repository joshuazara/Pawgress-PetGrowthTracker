package com.example.taskperf1.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.HeatCycle;

import java.util.List;

@Dao
public interface HeatCycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HeatCycle cycle);

    @Update
    void update(HeatCycle cycle);

    @Delete
    void delete(HeatCycle cycle);

    @Query("SELECT * FROM heat_cycles WHERE cycleId = :cycleId")
    LiveData<HeatCycle> getHeatCycleById(int cycleId);

    @Query("SELECT * FROM heat_cycles WHERE petId = :petId ORDER BY startDate DESC")
    LiveData<List<HeatCycle>> getHeatCyclesByPet(int petId);

    @Query("SELECT * FROM heat_cycles WHERE petId = :petId ORDER BY startDate DESC LIMIT 1")
    LiveData<HeatCycle> getLatestHeatCycleForPet(int petId);

    @Query("SELECT * FROM heat_cycles ORDER BY startDate DESC")
    LiveData<List<HeatCycle>> getAllHeatCycles();
}
