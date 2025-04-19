package com.example.taskperf1.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.VaccineEntryDao;
import com.example.taskperf1.database.VaccineEntry;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaccineEntryRepository {
    private VaccineEntryDao vaccineEntryDao;
    private LiveData<List<VaccineEntry>> allVaccineEntries;
    private ExecutorService executorService;

    public VaccineEntryRepository(Application application) {
        PawgressDatabase database = PawgressDatabase.getInstance(application);
        vaccineEntryDao = database.vaccineEntryDao();
        allVaccineEntries = vaccineEntryDao.getAllVaccineEntries();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<VaccineEntry>> getAllVaccineEntries() {
        return allVaccineEntries;
    }

    public LiveData<List<VaccineEntry>> getVaccineEntriesByPet(int petId) {
        return vaccineEntryDao.getVaccineEntriesByPet(petId);
    }

    public LiveData<VaccineEntry> getVaccineEntryById(int vaccineId) {
        return vaccineEntryDao.getVaccineEntryById(vaccineId);
    }

    public LiveData<List<VaccineEntry>> getUpcomingVaccinesForPet(int petId, Date currentDate) {
        return vaccineEntryDao.getUpcomingVaccinesForPet(petId, currentDate);
    }

    public void insert(VaccineEntry entry) {
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(new Date());
        }
        executorService.execute(() -> vaccineEntryDao.insert(entry));
    }

    public void update(VaccineEntry entry) {
        executorService.execute(() -> vaccineEntryDao.update(entry));
    }

    public void delete(VaccineEntry entry) {
        executorService.execute(() -> vaccineEntryDao.delete(entry));
    }
}