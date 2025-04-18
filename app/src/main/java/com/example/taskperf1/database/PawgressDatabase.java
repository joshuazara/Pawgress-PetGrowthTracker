package com.example.taskperf1.database;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;




@Database(
        entities = {
                Pet.class,
                GrowthEntry.class,
                VaccineEntry.class,
                HeatCycle.class,
                PhotoEntry.class,
                Note.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class PawgressDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "pawgress_db";
    private static PawgressDatabase instance;


    public abstract PetDao petDao();
    public abstract GrowthEntryDao growthEntryDao();
    public abstract VaccineEntryDao vaccineEntryDao();
    public abstract HeatCycleDao heatCycleDao();
    public abstract PhotoEntryDao photoEntryDao();
    public abstract NoteDao noteDao();


    public static synchronized PawgressDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PawgressDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
