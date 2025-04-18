package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.repositories.GrowthEntryRepository;

import java.util.List;

public class GrowthEntryViewModel extends AndroidViewModel {
    private GrowthEntryRepository repository;
    private LiveData<List<GrowthEntry>> allGrowthEntries;

    public GrowthEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new GrowthEntryRepository(application);
        allGrowthEntries = repository.getAllGrowthEntries();
    }

    public LiveData<List<GrowthEntry>> getAllGrowthEntries() {
        return allGrowthEntries;
    }

    public LiveData<List<GrowthEntry>> getGrowthEntriesByPet(int petId) {
        return repository.getGrowthEntriesByPet(petId);
    }

    public LiveData<GrowthEntry> getGrowthEntryById(int growthId) {
        return repository.getGrowthEntryById(growthId);
    }

    public LiveData<GrowthEntry> getLatestGrowthEntryForPet(int petId) {
        return repository.getLatestGrowthEntryForPet(petId);
    }

    public void insert(GrowthEntry entry) {
        repository.insert(entry);
    }

    public void update(GrowthEntry entry) {
        repository.update(entry);
    }

    public void delete(GrowthEntry entry) {
        repository.delete(entry);
    }
}
