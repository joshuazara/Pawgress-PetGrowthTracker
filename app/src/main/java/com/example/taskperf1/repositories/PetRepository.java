package com.example.taskperf1.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PetDao;
import com.example.taskperf1.database.Pet;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PetRepository {
    private PetDao petDao;
    private LiveData<List<Pet>> allPets;
    private ExecutorService executorService;

    public PetRepository(Application application) {
        PawgressDatabase database = PawgressDatabase.getInstance(application);
        petDao = database.petDao();
        allPets = petDao.getAllPets();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Pet>> getAllPets() {
        return allPets;
    }

    public LiveData<Pet> getPetById(int petId) {
        return petDao.getPetById(petId);
    }

    public void insert(Pet pet) {
        pet.setCreatedAt(new Date());
        pet.setUpdatedAt(new Date());
        executorService.execute(() -> petDao.insert(pet));
    }

    public long insertSync(Pet pet) {
        if (pet.getCreatedAt() == null) {
            pet.setCreatedAt(new Date());
        }
        if (pet.getUpdatedAt() == null) {
            pet.setUpdatedAt(new Date());
        }
        return petDao.insert(pet);
    }

    public void update(Pet pet) {
        pet.setUpdatedAt(new Date());
        executorService.execute(() -> petDao.update(pet));
    }

    public void delete(Pet pet) {
        executorService.execute(() -> petDao.delete(pet));
    }
}