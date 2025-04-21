package com.example.taskperf1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PhotoEntry;
import com.example.taskperf1.database.PhotoEntryDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
    private FrameLayout textOverlayContainer;
    private FloatingActionButton favoriteButton;
    private FloatingActionButton addTextButton;
    private Bitmap originalBitmap;
    private Bitmap editedBitmap;
    private boolean isEditMode = false;
    private List<TextOverlay> textOverlays = new ArrayList<>();
    private TextOverlay activeTextOverlay = null;
    private ScaleGestureDetector scaleGestureDetector;

    // Mode constants
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;
    private int mode = MODE_NONE;

    // Last touch position
    private float lastTouchX, lastTouchY;

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
        textOverlayContainer = findViewById(R.id.textOverlayContainer);
        favoriteButton = findViewById(R.id.favoriteButton);
        addTextButton = findViewById(R.id.addTextButton);

        // Setup scale detector
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Add text button
        addTextButton.setOnClickListener(v -> {
            if (isEditMode) {
                // Exit edit mode and save changes
                isEditMode = false;
                addTextButton.setImageResource(R.drawable.ic_edit);
                saveEditedImage();
            } else {
                // Enter edit mode
                isEditMode = true;
                addTextButton.setImageResource(R.drawable.ic_check);
                showAddTextDialog();
            }
        });

        // Favorite button
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // Delete button
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> deletePhoto());

        // Load photo data
        loadPhotoData();
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            // Ask user if they want to save changes
            new AlertDialog.Builder(this)
                    .setTitle("Save Changes")
                    .setMessage("Do you want to save your changes?")
                    .setPositiveButton("Save", (dialog, which) -> {
                        saveEditedImage();
                        finish();
                    })
                    .setNegativeButton("Discard", (dialog, which) -> finish())
                    .show();
        } else {
            super.onBackPressed();
        }
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

            // Load with Glide to handle large images efficiently
            Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            originalBitmap = bitmap;
                            editedBitmap = bitmap.copy(bitmap.getConfig(), true);
                            photoImageView.setImageBitmap(editedBitmap);

                            // Load existing text overlays if any (from saved caption)
                            loadSavedTextOverlays();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading photo: " + e.getMessage());
            photoImageView.setImageResource(R.drawable.ic_camera);
        }
    }

    private void loadSavedTextOverlays() {
        textOverlays.clear();

        if (currentPhoto.getCaption() != null && !currentPhoto.getCaption().isEmpty()) {
            try {
                // In a real app, you would store text overlay data in a structured format
                // like JSON. For this example, we'll just create a sample text overlay
                // based on the existing caption

                String caption = currentPhoto.getCaption();
                float centerX = photoImageView.getWidth() / 2f;
                float centerY = photoImageView.getHeight() / 2f;

                TextOverlay textOverlay = new TextOverlay(
                        caption,
                        centerX,
                        centerY,
                        1.0f,  // Scale
                        Color.WHITE
                );

                textOverlays.add(textOverlay);
                renderTextOverlays();
            } catch (Exception e) {
                Log.e(TAG, "Error loading saved text overlays: " + e.getMessage());
            }
        }
    }

    private void updateUI() {
        // Format and set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(currentPhoto.getCaptureDate()));

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

    private void showAddTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Text");

        // Create EditText for input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Setup buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                // Create and add new text overlay
                float centerX = photoImageView.getWidth() / 2f;
                float centerY = photoImageView.getHeight() / 2f;

                TextOverlay newOverlay = new TextOverlay(
                        text,
                        centerX,
                        centerY,
                        1.0f,
                        Color.WHITE
                );

                textOverlays.add(newOverlay);
                activeTextOverlay = newOverlay;  // Set as active for editing

                // Show the text on screen
                renderTextOverlays();

                // Show toast with instructions
                Toast.makeText(this, "Drag to position, pinch to resize", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Text Color", (dialog, which) -> {
            // This will be overridden below to prevent dialog dismissal
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the color button click to show color picker
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            showColorPickerDialog(input.getText().toString());
        });

        // Show keyboard automatically
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showColorPickerDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Text Color");

        // Define color choices - you can expand this
        final int[] colors = {
                Color.WHITE,
                Color.BLACK,
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA
        };

        final String[] colorNames = {
                "White",
                "Black",
                "Red",
                "Green",
                "Blue",
                "Yellow",
                "Cyan",
                "Magenta"
        };

        builder.setItems(colorNames, (dialog, which) -> {
            int selectedColor = colors[which];

            if (!text.isEmpty()) {
                float centerX = photoImageView.getWidth() / 2f;
                float centerY = photoImageView.getHeight() / 2f;

                TextOverlay newOverlay = new TextOverlay(
                        text,
                        centerX,
                        centerY,
                        1.0f,
                        selectedColor
                );

                textOverlays.add(newOverlay);
                activeTextOverlay = newOverlay;

                // Show the text on screen
                renderTextOverlays();
            }
        });

        builder.show();
    }

    private void renderTextOverlays() {
        if (textOverlayContainer == null || photoImageView == null) return;

        // Clear existing views
        textOverlayContainer.removeAllViews();

        // Create a TextView for each overlay
        for (TextOverlay overlay : textOverlays) {
            TextView textView = new TextView(this);
            textView.setText(overlay.getText());
            textView.setTextColor(overlay.getColor());
            textView.setTextSize(20 * overlay.getScale());  // Base size * scale

            // Position the text view
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );

            textView.setLayoutParams(params);
            textView.setX(overlay.getX() - textView.getWidth() / 2f);
            textView.setY(overlay.getY() - textView.getHeight() / 2f);

            // Handle dragging when in edit mode
            textView.setOnTouchListener((v, event) -> {
                if (!isEditMode) return false;

                // Make this the active overlay
                activeTextOverlay = overlay;

                // Handle touch events
                return handleTouch(v, event);
            });

            textOverlayContainer.addView(textView);
        }
    }

    private boolean handleTouch(View view, MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events
        scaleGestureDetector.onTouchEvent(event);

        final float x = event.getRawX();
        final float y = event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Start drag
                mode = MODE_DRAG;
                lastTouchX = x;
                lastTouchY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_DRAG) {
                    // Calculate movement
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    // Update position of view
                    view.setX(view.getX() + dx);
                    view.setY(view.getY() + dy);

                    // Update overlay data
                    if (activeTextOverlay != null) {
                        activeTextOverlay.setX(activeTextOverlay.getX() + dx);
                        activeTextOverlay.setY(activeTextOverlay.getY() + dy);
                    }

                    // Save touch position
                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NONE;
                break;
        }

        return true;
    }

    // Scale listener for pinch-to-zoom text
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (activeTextOverlay == null) return false;

            float scaleFactor = detector.getScaleFactor();

            // Don't let the text get too small or too large
            activeTextOverlay.setScale(
                    Math.max(0.5f, Math.min(activeTextOverlay.getScale() * scaleFactor, 3.0f))
            );

            // Re-render the text at new scale
            renderTextOverlays();

            return true;
        }
    }

    private void saveEditedImage() {
        if (editedBitmap == null) return;

        try {
            // Create a new bitmap to draw on
            Bitmap finalBitmap = editedBitmap.copy(editedBitmap.getConfig(), true);
            Canvas canvas = new Canvas(finalBitmap);

            // Create paint for drawing text
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);

            // Calculate scale factors between view and bitmap
            float scaleX = (float) finalBitmap.getWidth() / photoImageView.getWidth();
            float scaleY = (float) finalBitmap.getHeight() / photoImageView.getHeight();

            // Draw each text overlay onto the bitmap
            for (TextOverlay overlay : textOverlays) {
                paint.setColor(overlay.getColor());
                paint.setTextSize(50 * overlay.getScale() * scaleX); // Scale the text size

                // Calculate position in bitmap coordinates
                float x = overlay.getX() * scaleX;
                float y = overlay.getY() * scaleY;

                // Draw text
                canvas.drawText(overlay.getText(), x, y, paint);
            }

            // Save the bitmap to a temporary file
            File outputFile = new File(getCacheDir(), "temp_edited_photo.jpg");
            FileOutputStream fos = new FileOutputStream(outputFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Update the photo entry with the new file path and caption
            Uri newUri = Uri.fromFile(outputFile);

            // Create caption from text overlays (simplified)
            StringBuilder caption = new StringBuilder();
            for (TextOverlay overlay : textOverlays) {
                if (caption.length() > 0) caption.append(" | ");
                caption.append(overlay.getText());
            }

            // Update photo entry
            currentPhoto.setFilePath(newUri.toString());
            currentPhoto.setCaption(caption.toString());

            // Save to database
            executorService.execute(() -> photoEntryDao.update(currentPhoto));

            // Update UI
            photoImageView.setImageBitmap(finalBitmap);
            textOverlayContainer.setVisibility(View.GONE);

            Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving edited image: " + e.getMessage());
            Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show();
        }
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

    // Class to represent a text overlay
    private static class TextOverlay {
        private String text;
        private float x, y;
        private float scale;
        private int color;

        public TextOverlay(String text, float x, float y, float scale, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.color = color;
        }

        public String getText() { return text; }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getScale() { return scale; }
        public int getColor() { return color; }

        public void setX(float x) { this.x = x; }
        public void setY(float y) { this.y = y; }
        public void setScale(float scale) { this.scale = scale; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}