package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.Pet;
import com.example.taskperf1.database.PetDao;
import com.example.taskperf1.database.PawgressDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PetViewModel extends AndroidViewModel {
    private PetDao dao;
    private ExecutorService executorService;

    public PetViewModel(@NonNull Application application) {
        super(application);
        dao = PawgressDatabase.getInstance(application).petDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Pet>> getAllPets() {
        return dao.getAllPets();
    }

    public LiveData<Pet> getPetById(int petId) {
        return dao.getPetById(petId);
    }

    public void insert(Pet pet) {
        pet.setCreatedAt(new Date());
        pet.setUpdatedAt(new Date());
        executorService.execute(() -> dao.insert(pet));
    }

    public long insertSync(Pet pet) {
        if (pet.getCreatedAt() == null) {
            pet.setCreatedAt(new Date());
        }
        if (pet.getUpdatedAt() == null) {
            pet.setUpdatedAt(new Date());
        }
        return dao.insert(pet);
    }

    public void update(Pet pet) {
        pet.setUpdatedAt(new Date());
        executorService.execute(() -> dao.update(pet));
    }

    public void delete(Pet pet) {
        executorService.execute(() -> dao.delete(pet));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}