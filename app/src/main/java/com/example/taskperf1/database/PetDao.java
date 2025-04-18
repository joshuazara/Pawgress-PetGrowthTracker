package com.example.taskperf1.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.Pet;

import java.util.List;

@Dao
public interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Pet pet);

    @Update
    void update(Pet pet);

    @Delete
    void delete(Pet pet);

    @Query("SELECT * FROM pets WHERE petId = :petId")
    LiveData<Pet> getPetById(int petId);

    @Query("SELECT * FROM pets ORDER BY name ASC")
    LiveData<List<Pet>> getAllPets();
}
