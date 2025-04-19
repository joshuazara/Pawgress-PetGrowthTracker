package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.VaccineEntry;
import com.example.taskperf1.repositories.VaccineEntryRepository;

import java.util.Date;
import java.util.List;

public class VaccineEntryViewModel extends AndroidViewModel {
    private VaccineEntryRepository repository;
    private LiveData<List<VaccineEntry>> allVaccineEntries;

    public VaccineEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new VaccineEntryRepository(application);
        allVaccineEntries = repository.getAllVaccineEntries();
    }

    public LiveData<List<VaccineEntry>> getAllVaccineEntries() {
        return allVaccineEntries;
    }

    public LiveData<List<VaccineEntry>> getVaccineEntriesByPet(int petId) {
        return repository.getVaccineEntriesByPet(petId);
    }

    public LiveData<VaccineEntry> getVaccineEntryById(int vaccineId) {
        return repository.getVaccineEntryById(vaccineId);
    }

    public LiveData<List<VaccineEntry>> getUpcomingVaccinesForPet(int petId) {
        return repository.getUpcomingVaccinesForPet(petId, new Date());
    }

    public void insert(VaccineEntry entry) {
        repository.insert(entry);
    }

    public void update(VaccineEntry entry) {
        repository.update(entry);
    }

    public void delete(VaccineEntry entry) {
        repository.delete(entry);
    }
}