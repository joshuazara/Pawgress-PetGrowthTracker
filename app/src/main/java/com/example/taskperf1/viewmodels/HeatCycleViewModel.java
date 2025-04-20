package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.HeatCycle;
import com.example.taskperf1.repositories.HeatCycleRepository;

import java.util.List;

public class HeatCycleViewModel extends AndroidViewModel {
    private HeatCycleRepository repository;
    private LiveData<List<HeatCycle>> allHeatCycles;

    public HeatCycleViewModel(@NonNull Application application) {
        super(application);
        repository = new HeatCycleRepository(application);
        allHeatCycles = repository.getAllHeatCycles();
    }

    public LiveData<List<HeatCycle>> getAllHeatCycles() {
        return allHeatCycles;
    }

    public LiveData<List<HeatCycle>> getHeatCyclesByPet(int petId) {
        return repository.getHeatCyclesByPet(petId);
    }

    public LiveData<HeatCycle> getHeatCycleById(int cycleId) {
        return repository.getHeatCycleById(cycleId);
    }

    public LiveData<HeatCycle> getLatestHeatCycleForPet(int petId) {
        return repository.getLatestHeatCycleForPet(petId);
    }

    public void insert(HeatCycle cycle) {
        repository.insert(cycle);
    }

    public void update(HeatCycle cycle) {
        repository.update(cycle);
    }

    public void delete(HeatCycle cycle) {
        repository.delete(cycle);
    }
}