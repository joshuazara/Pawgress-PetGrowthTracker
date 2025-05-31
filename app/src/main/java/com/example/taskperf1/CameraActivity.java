package com.example.taskperf1;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PhotoEntry;
import com.example.taskperf1.database.PhotoEntryDao;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private Uri currentPhotoUri;
    private int petId = -1;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ExecutorService executorService;
    private PhotoEntryDao photoEntryDao;
    private String currentPhotoPath;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        
        petId = getIntent().getIntExtra("pet_id", -1);

        
        photoEntryDao = PawgressDatabase.getInstance(this).photoEntryDao();
        executorService = Executors.newSingleThreadExecutor();

        
        checkPermissions();

        
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        
                        savePhotoEntry();
                        Toast.makeText(CameraActivity.this, "Photo saved to gallery!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CameraActivity.this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        
        MaterialButton takePhotoButton = findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        
        MaterialButton viewGalleryButton = findViewById(R.id.viewGalleryButton);
        viewGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, GalleryActivity.class);
                intent.putExtra("pet_id", petId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.taskperf1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                takePictureLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  
                ".jpg",         
                storageDir      
        );

        
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePhotoEntry() {
        if (currentPhotoUri != null && petId != -1) {
            executorService.execute(() -> {
                
                PhotoEntry photoEntry = new PhotoEntry();
                photoEntry.setPetId(petId);
                photoEntry.setFilePath(currentPhotoUri.toString());
                photoEntry.setCaptureDate(new Date());
                photoEntry.setUploadedAt(new Date());
                photoEntry.setCaption(""); 
                photoEntry.setFavorite(false);

                
                photoEntryDao.insert(photoEntry);
            });
        }
    }

    private void checkPermissions() {
        
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                checkPermissions();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                Toast.makeText(this, "All permissions granted, you can now take photos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission is required to save photos", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}