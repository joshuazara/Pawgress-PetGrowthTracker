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

import com.example.taskperf1.adapters.VaccineEntryAdapter;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.database.VaccineEntry;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.example.taskperf1.viewmodels.VaccineEntryViewModel;

import java.util.List;

public class AllVaccineEntriesActivity extends AppCompatActivity {

    private VaccineEntryViewModel vaccineEntryViewModel;
    private PetViewModel petViewModel;
    private VaccineEntryAdapter adapter;
    private int petId;
    private Pet currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_vaccine_entries);

        // Get pet ID from intent
        petId = getIntent().getIntExtra("pet_id", -1);

        if (petId == -1) {
            finish();
            return;
        }

        // Initialize view models
        vaccineEntryViewModel = new ViewModelProvider(this).get(VaccineEntryViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        // Setup UI
        setupToolbar();
        setupRecyclerView();

        // Load data
        loadPetInfo();
        loadVaccineEntries();
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
        RecyclerView recyclerView = findViewById(R.id.vaccineEntriesRecyclerView);
        adapter = new VaccineEntryAdapter(this, false); // false = show all entries, not just upcoming
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
                        petNameTextView.setText(pet.getName() + "'s Vaccination History");
                    }
                }
            }
        });
    }

    private void loadVaccineEntries() {
        vaccineEntryViewModel.getVaccineEntriesByPet(petId).observe(this, new Observer<List<VaccineEntry>>() {
            @Override
            public void onChanged(List<VaccineEntry> entries) {
                if (entries != null) {
                    adapter.setVaccineEntries(entries);

                    // Update UI if empty
                    updateEmptyState(entries.isEmpty());
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        View emptyView = findViewById(R.id.emptyStateContainer);
        RecyclerView recyclerView = findViewById(R.id.vaccineEntriesRecyclerView);

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