package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.HeatCycle;
import com.example.taskperf1.database.HeatCycleDao;
import com.example.taskperf1.database.PawgressDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeatCycleViewModel extends AndroidViewModel {
    private HeatCycleDao dao;
    private ExecutorService executorService;

    public HeatCycleViewModel(@NonNull Application application) {
        super(application);
        dao = PawgressDatabase.getInstance(application).heatCycleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HeatCycle>> getAllHeatCycles() {
        return dao.getAllHeatCycles();
    }

    public LiveData<List<HeatCycle>> getHeatCyclesByPet(int petId) {
        return dao.getHeatCyclesByPet(petId);
    }

    public LiveData<HeatCycle> getHeatCycleById(int cycleId) {
        return dao.getHeatCycleById(cycleId);
    }

    public LiveData<HeatCycle> getLatestHeatCycleForPet(int petId) {
        return dao.getLatestHeatCycleForPet(petId);
    }

    public void insert(HeatCycle cycle) {
        if (cycle.getCreatedAt() == null) {
            cycle.setCreatedAt(new Date());
        }
        executorService.execute(() -> dao.insert(cycle));
    }

    public void update(HeatCycle cycle) {
        executorService.execute(() -> dao.update(cycle));
    }

    public void delete(HeatCycle cycle) {
        executorService.execute(() -> dao.delete(cycle));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}