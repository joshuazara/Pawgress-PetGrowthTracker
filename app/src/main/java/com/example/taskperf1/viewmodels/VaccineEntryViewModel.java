package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.VaccineEntry;
import com.example.taskperf1.database.VaccineEntryDao;
import com.example.taskperf1.database.PawgressDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaccineEntryViewModel extends AndroidViewModel {
    private VaccineEntryDao dao;
    private ExecutorService executorService;

    public VaccineEntryViewModel(@NonNull Application application) {
        super(application);
        dao = PawgressDatabase.getInstance(application).vaccineEntryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<VaccineEntry>> getAllVaccineEntries() {
        return dao.getAllVaccineEntries();
    }

    public LiveData<List<VaccineEntry>> getVaccineEntriesByPet(int petId) {
        return dao.getVaccineEntriesByPet(petId);
    }

    public LiveData<VaccineEntry> getVaccineEntryById(int vaccineId) {
        return dao.getVaccineEntryById(vaccineId);
    }

    public LiveData<List<VaccineEntry>> getUpcomingVaccinesForPet(int petId) {
        return dao.getUpcomingVaccinesForPet(petId, new Date());
    }

    public void insert(VaccineEntry entry) {
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(new Date());
        }
        executorService.execute(() -> dao.insert(entry));
    }

    public void update(VaccineEntry entry) {
        executorService.execute(() -> dao.update(entry));
    }

    public void delete(VaccineEntry entry) {
        executorService.execute(() -> dao.delete(entry));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}