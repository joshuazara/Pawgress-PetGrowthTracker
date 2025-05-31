package com.example.taskperf1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.adapters.PhotoAdapter;
import com.example.taskperf1.database.PawgressDatabase;
import com.example.taskperf1.database.PhotoEntry;
import com.example.taskperf1.database.PhotoEntryDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private int petId = -1;
    private RecyclerView photoGrid;
    private PhotoAdapter photoAdapter;
    private PhotoEntryDao photoEntryDao;
    private boolean showFavoritesOnly = false;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        
        petId = getIntent().getIntExtra("pet_id", -1);

        
        photoEntryDao = PawgressDatabase.getInstance(this).photoEntryDao();

        
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        
        FloatingActionButton takePictureButton = findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GalleryActivity.this, CameraActivity.class);
                intent.putExtra("pet_id", petId);
                startActivity(intent);
                finish();
            }
        });

        
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    
                    showFavoritesOnly = false;
                    loadPhotos();
                } else {
                    
                    showFavoritesOnly = true;
                    loadPhotos();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        
        setupPhotoGrid();
    }

    private void setupPhotoGrid() {
        photoGrid = findViewById(R.id.photoGrid);

        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        photoGrid.setLayoutManager(layoutManager);

        
        photoAdapter = new PhotoAdapter(this);
        photoGrid.setAdapter(photoAdapter);

        
        loadPhotos();
    }

    private void loadPhotos() {
        if (petId != -1) {
            if (showFavoritesOnly) {
                photoEntryDao.getFavoritePhotosByPet(petId).observe(this, photoEntries -> {
                    photoAdapter.setPhotos(photoEntries);
                    updateEmptyState(photoEntries.isEmpty());
                });
            } else {
                photoEntryDao.getPhotoEntriesByPet(petId).observe(this, photoEntries -> {
                    photoAdapter.setPhotos(photoEntries);
                    updateEmptyState(photoEntries.isEmpty());
                });
            }
        } else {
            
            if (showFavoritesOnly) {
                photoEntryDao.getAllPhotoEntries().observe(this, photoEntries -> {
                    List<PhotoEntry> favorites = new ArrayList<>();
                    for (PhotoEntry entry : photoEntries) {
                        if (entry.isFavorite()) {
                            favorites.add(entry);
                        }
                    }
                    photoAdapter.setPhotos(favorites);
                    updateEmptyState(favorites.isEmpty());
                });
            } else {
                photoEntryDao.getAllPhotoEntries().observe(this, photoEntries -> {
                    photoAdapter.setPhotos(photoEntries);
                    updateEmptyState(photoEntries.isEmpty());
                });
            }
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        View emptyView = findViewById(R.id.emptyStateContainer);
        RecyclerView recyclerView = findViewById(R.id.photoGrid);

        if (emptyView != null && recyclerView != null) {
            if (isEmpty) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        
        loadPhotos();
    }
}