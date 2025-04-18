package com.example.taskperf1.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "photo_entries",
        foreignKeys = @ForeignKey(
                entity = Pet.class,
                parentColumns = "petId",
                childColumns = "petId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("petId")}
)
public class PhotoEntry {
    @PrimaryKey(autoGenerate = true)
    private int photoId;

    private int petId;

    private String filePath;

    private Date captureDate;

    private String caption;

    private boolean isFavorite;

    private Date uploadedAt;


    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}