package com.example.taskperf1.database;

import androidx.room.Entity;

import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "pets")
public class Pet {
    @PrimaryKey(autoGenerate = true)
    private int petId;

    @NonNull
    private String name;

    private String breed;


    private Date birthDate;

    private String gender;

    private float initialWeight;

    private float initialHeight;

    private float initialLength;

    private String profilePicture;


    private Date createdAt;

    private Date updatedAt;


    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }


    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(float initialWeight) {
        this.initialWeight = initialWeight;
    }

    public float getInitialHeight() {
        return initialHeight;
    }

    public void setInitialHeight(float initialHeight) {
        this.initialHeight = initialHeight;
    }

    public float getInitialLength() {
        return initialLength;
    }

    public void setInitialLength(float initialLength) {
        this.initialLength = initialLength;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }



    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


}
