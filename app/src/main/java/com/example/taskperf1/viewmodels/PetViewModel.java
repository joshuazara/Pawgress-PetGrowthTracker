package com.example.taskperf1.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskperf1.database.Pet;
import com.example.taskperf1.repositories.PetRepository;

import java.util.List;

public class PetViewModel extends AndroidViewModel {
    private PetRepository repository;
    private LiveData<List<Pet>> allPets;

    public PetViewModel(@NonNull Application application) {
        super(application);
        repository = new PetRepository(application);
        allPets = repository.getAllPets();
    }

    public LiveData<List<Pet>> getAllPets() {
        return allPets;
    }

    public LiveData<Pet> getPetById(int petId) {
        return repository.getPetById(petId);
    }

    public void insert(Pet pet) {
        repository.insert(pet);
    }

    public void update(Pet pet) {
        repository.update(pet);
    }

    public void delete(Pet pet) {
        repository.delete(pet);
    }
}