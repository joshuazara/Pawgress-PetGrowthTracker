package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.database.GrowthEntryDao;
import com.example.taskperf1.database.PawgressDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrowthEntryViewModel extends AndroidViewModel {
    private GrowthEntryDao dao;
    private ExecutorService executorService;

    public GrowthEntryViewModel(@NonNull Application application) {
        super(application);
        dao = PawgressDatabase.getInstance(application).growthEntryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<GrowthEntry>> getAllGrowthEntries() {
        return dao.getAllGrowthEntries();
    }

    public LiveData<List<GrowthEntry>> getGrowthEntriesByPet(int petId) {
        return dao.getGrowthEntriesByPet(petId);
    }

    public LiveData<GrowthEntry> getGrowthEntryById(int growthId) {
        return dao.getGrowthEntryById(growthId);
    }

    public LiveData<GrowthEntry> getLatestGrowthEntryForPet(int petId) {
        return dao.getLatestGrowthEntryForPet(petId);
    }

    public void insert(GrowthEntry entry) {
        entry.setCreatedAt(new Date());
        if (entry.getEntryDate() == null) {
            entry.setEntryDate(new Date());
        }
        executorService.execute(() -> dao.insert(entry));
    }

    public void update(GrowthEntry entry) {
        executorService.execute(() -> dao.update(entry));
    }

    public void delete(GrowthEntry entry) {
        executorService.execute(() -> dao.delete(entry));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}