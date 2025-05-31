package com.example.taskperf1;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PhotoEntry;
import com.example.taskperf1.database.PhotoEntryDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoDetailActivity extends AppCompatActivity {

    private static final String TAG = "PhotoDetailActivity";
    private int photoId = -1;
    private PhotoEntryDao photoEntryDao;
    private PhotoEntry currentPhoto;
    private ExecutorService executorService;
    private ImageView photoImageView;
    private TextView dateTextView;
    private FloatingActionButton favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        
        photoId = getIntent().getIntExtra("photo_id", -1);
        if (photoId == -1) {
            Toast.makeText(this, "Error: Photo not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        photoEntryDao = PawgressDatabase.getInstance(this).photoEntryDao();
        executorService = Executors.newSingleThreadExecutor();

        
        photoImageView = findViewById(R.id.fullPhotoView);
        dateTextView = findViewById(R.id.photoDateText);
        favoriteButton = findViewById(R.id.favoriteButton);

        
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> deletePhoto());

        
        loadPhotoData();

    }

    private void loadPhotoData() {
        photoEntryDao.getPhotoEntryById(photoId).observe(this, photoEntry -> {
            if (photoEntry != null) {
                currentPhoto = photoEntry;
                loadImage();
                updateUI();
            } else {
                Toast.makeText(this, "Error: Photo not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadImage() {
        try {
            Uri photoUri = Uri.parse(currentPhoto.getFilePath());

            
            Glide.with(this)
                    .load(photoUri)
                    .into(photoImageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading photo: " + e.getMessage());
            photoImageView.setImageResource(R.drawable.ic_camera);
        }
    }

    private void updateUI() {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(currentPhoto.getCaptureDate()));

        
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (currentPhoto.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_outline);
        }
    }

    private void toggleFavorite() {
        currentPhoto.setFavorite(!currentPhoto.isFavorite());
        executorService.execute(() -> photoEntryDao.update(currentPhoto));
        updateFavoriteButton();
        Toast.makeText(this,
                currentPhoto.isFavorite() ? "Added to favorites" : "Removed from favorites",
                Toast.LENGTH_SHORT).show();
    }

    private void deletePhoto() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> {
                        photoEntryDao.delete(currentPhoto);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}