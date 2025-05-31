package com.example.taskperf1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.adapters.GrowthEntryAdapter;
import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.GrowthEntryViewModel;
import com.example.taskperf1.viewmodels.PetViewModel;

import java.util.List;

public class AllGrowthEntriesActivity extends AppCompatActivity {

    private GrowthEntryViewModel growthEntryViewModel;
    private PetViewModel petViewModel;
    private GrowthEntryAdapter adapter;
    private int petId;
    private Pet currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_growth_entries);

        
        petId = getIntent().getIntExtra("pet_id", -1);

        if (petId == -1) {
            finish();
            return;
        }

        
        growthEntryViewModel = new ViewModelProvider(this).get(GrowthEntryViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        
        setupToolbar();
        setupRecyclerView();

        
        loadPetInfo();
        loadGrowthEntries();
    }

    private void setupToolbar() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.growthEntriesRecyclerView);
        adapter = new GrowthEntryAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadPetInfo() {
        petViewModel.getPetById(petId).observe(this, new Observer<Pet>() {
            @Override
            public void onChanged(Pet pet) {
                if (pet != null) {
                    currentPet = pet;
                    TextView petNameTextView = findViewById(R.id.petName);
                    if (petNameTextView != null) {
                        petNameTextView.setText(pet.getName() + "'s Growth History");
                    }

                    
                    adapter.setPet(pet);
                }
            }
        });
    }

    private void loadGrowthEntries() {
        growthEntryViewModel.getGrowthEntriesByPet(petId).observe(this, new Observer<List<GrowthEntry>>() {
            @Override
            public void onChanged(List<GrowthEntry> entries) {
                if (entries != null) {
                    adapter.setGrowthEntries(entries);

                    
                    updateEmptyState(entries.isEmpty());
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        View emptyView = findViewById(R.id.emptyStateContainer);
        RecyclerView recyclerView = findViewById(R.id.growthEntriesRecyclerView);

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
}