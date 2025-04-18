package com.example.taskperf1.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.GrowthEntryDao;
import com.example.taskperf1.database.GrowthEntry;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrowthEntryRepository {
    private GrowthEntryDao growthEntryDao;
    private LiveData<List<GrowthEntry>> allGrowthEntries;
    private ExecutorService executorService;

    public GrowthEntryRepository(Application application) {
        PawgressDatabase database = PawgressDatabase.getInstance(application);
        growthEntryDao = database.growthEntryDao();
        allGrowthEntries = growthEntryDao.getAllGrowthEntries();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<GrowthEntry>> getAllGrowthEntries() {
        return allGrowthEntries;
    }

    public LiveData<List<GrowthEntry>> getGrowthEntriesByPet(int petId) {
        return growthEntryDao.getGrowthEntriesByPet(petId);
    }

    public LiveData<GrowthEntry> getGrowthEntryById(int growthId) {
        return growthEntryDao.getGrowthEntryById(growthId);
    }

    public LiveData<GrowthEntry> getLatestGrowthEntryForPet(int petId) {
        return growthEntryDao.getLatestGrowthEntryForPet(petId);
    }

    public void insert(GrowthEntry entry) {
        entry.setCreatedAt(new Date());
        if (entry.getEntryDate() == null) {
            entry.setEntryDate(new Date());
        }
        executorService.execute(() -> growthEntryDao.insert(entry));
    }

    public void update(GrowthEntry entry) {
        executorService.execute(() -> growthEntryDao.update(entry));
    }

    public void delete(GrowthEntry entry) {
        executorService.execute(() -> growthEntryDao.delete(entry));
    }
}
