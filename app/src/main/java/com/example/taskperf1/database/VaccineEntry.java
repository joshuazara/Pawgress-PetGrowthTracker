package com.example.taskperf1.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(
        tableName = "vaccine_entries",
        foreignKeys = @ForeignKey(
                entity = Pet.class,
                parentColumns = "petId",
                childColumns = "petId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("petId")}
)
public class VaccineEntry {
    @PrimaryKey(autoGenerate = true)
    private int vaccineId;

    private int petId;

    @NonNull
    private String vaccineType;

    private Date administeredDate;

    private Date nextDueDate;

    private String veterinarian;

    private String clinic;

    private String batchNumber;

    private String notes;

    private boolean reminderSet;

    private Date createdAt;

    public int getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(int vaccineId) {
        this.vaccineId = vaccineId;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    @NonNull
    public String getVaccineType() {
        return vaccineType;
    }

    public void setVaccineType(@NonNull String vaccineType) {
        this.vaccineType = vaccineType;
    }

    public Date getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(Date administeredDate) {
        this.administeredDate = administeredDate;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(Date nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getVeterinarian() {
        return veterinarian;
    }

    public void setVeterinarian(String veterinarian) {
        this.veterinarian = veterinarian;
    }

    public String getClinic() {
        return clinic;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}