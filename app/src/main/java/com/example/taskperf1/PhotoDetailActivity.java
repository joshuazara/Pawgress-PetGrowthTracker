package com.example.taskperf1;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PhotoEntry;
import com.example.taskperf1.database.PhotoEntryDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoDetailActivity extends AppCompatActivity {

    private int photoId = -1;
    private PhotoEntryDao photoEntryDao;
    private PhotoEntry currentPhoto;
    private ExecutorService executorService;
    private ImageView photoImageView;
    private TextView dateTextView;
    private TextView captionTextView;
    private FloatingActionButton favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // Get photo ID from intent
        photoId = getIntent().getIntExtra("photo_id", -1);
        if (photoId == -1) {
            Toast.makeText(this, "Error: Photo not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database access
        photoEntryDao = PawgressDatabase.getInstance(this).photoEntryDao();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize UI components
        photoImageView = findViewById(R.id.fullPhotoView);
        dateTextView = findViewById(R.id.photoDateText);
        captionTextView = findViewById(R.id.photoCaptionText);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Edit caption button
        MaterialButton editCaptionButton = findViewById(R.id.editCaptionButton);
        editCaptionButton.setOnClickListener(v -> showEditCaptionDialog());

        // Favorite button
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // Delete button
        MaterialButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> deletePhoto());

        // Load photo data
        loadPhotoData();
    }

    private void loadPhotoData() {
        photoEntryDao.getPhotoEntryById(photoId).observe(this, photoEntry -> {
            if (photoEntry != null) {
                currentPhoto = photoEntry;
                updateUI();
            } else {
                Toast.makeText(this, "Error: Photo not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        // Load image with Glide
        try {
            Uri photoUri = Uri.parse(currentPhoto.getFilePath());
            Glide.with(this)
                    .load(photoUri)
                    .into(photoImageView);
        } catch (Exception e) {
            // If loading fails, show placeholder
            photoImageView.setImageResource(R.drawable.ic_camera);
        }

        // Format and set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(currentPhoto.getCaptureDate()));

        // Set caption
        if (currentPhoto.getCaption() != null && !currentPhoto.getCaption().isEmpty()) {
            captionTextView.setText(currentPhoto.getCaption());
            captionTextView.setVisibility(View.VISIBLE);
        } else {
            captionTextView.setVisibility(View.GONE);
        }

        // Update favorite button
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

    private void showEditCaptionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Edit Caption");

        // Setup view for editing caption
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setText(currentPhoto.getCaption());
        builder.setView(input);

        // Setup buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String caption = input.getText().toString();
            currentPhoto.setCaption(caption);
            executorService.execute(() -> photoEntryDao.update(currentPhoto));
            updateUI();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deletePhoto() {
        // Show confirmation dialog first
        executorService.execute(() -> {
            photoEntryDao.delete(currentPhoto);
            runOnUiThread(() -> {
                Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}