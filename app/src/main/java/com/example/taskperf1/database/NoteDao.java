package com.example.taskperf1.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskperf1.database.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    LiveData<Note> getNoteById(int noteId);

    @Query("SELECT * FROM notes WHERE petId = :petId ORDER BY noteDate DESC")
    LiveData<List<Note>> getNotesByPet(int petId);

    @Query("SELECT * FROM notes ORDER BY noteDate DESC")
    LiveData<List<Note>> getAllNotes();
}