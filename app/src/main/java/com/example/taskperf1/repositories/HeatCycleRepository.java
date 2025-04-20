package com.example.taskperf1.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.HeatCycle;
import com.example.taskperf1.database.HeatCycleDao;
import com.example.taskperf1.database.PawgressDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeatCycleRepository {
    private HeatCycleDao heatCycleDao;
    private LiveData<List<HeatCycle>> allHeatCycles;
    private ExecutorService executorService;

    public HeatCycleRepository(Application application) {
        PawgressDatabase database = PawgressDatabase.getInstance(application);
        heatCycleDao = database.heatCycleDao();
        allHeatCycles = heatCycleDao.getAllHeatCycles();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HeatCycle>> getAllHeatCycles() {
        return allHeatCycles;
    }

    public LiveData<List<HeatCycle>> getHeatCyclesByPet(int petId) {
        return heatCycleDao.getHeatCyclesByPet(petId);
    }

    public LiveData<HeatCycle> getHeatCycleById(int cycleId) {
        return heatCycleDao.getHeatCycleById(cycleId);
    }

    public LiveData<HeatCycle> getLatestHeatCycleForPet(int petId) {
        return heatCycleDao.getLatestHeatCycleForPet(petId);
    }

    public void insert(HeatCycle cycle) {
        if (cycle.getCreatedAt() == null) {
            cycle.setCreatedAt(new Date());
        }
        executorService.execute(() -> heatCycleDao.insert(cycle));
    }

    public void update(HeatCycle cycle) {
        executorService.execute(() -> heatCycleDao.update(cycle));
    }

    public void delete(HeatCycle cycle) {
        executorService.execute(() -> heatCycleDao.delete(cycle));
    }
}