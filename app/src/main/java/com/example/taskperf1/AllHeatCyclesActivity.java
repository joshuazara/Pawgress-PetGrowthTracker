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

import com.example.taskperf1.adapters.HeatCycleAdapter;
import com.example.taskperf1.database.HeatCycle;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.HeatCycleViewModel;
import com.example.taskperf1.viewmodels.PetViewModel;

import java.util.List;

public class AllHeatCyclesActivity extends AppCompatActivity {

    private HeatCycleViewModel heatCycleViewModel;
    private PetViewModel petViewModel;
    private HeatCycleAdapter adapter;
    private int petId;
    private Pet currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_heat_cycles);

        // Get pet ID from intent
        petId = getIntent().getIntExtra("pet_id", -1);

        if (petId == -1) {
            finish();
            return;
        }

        // Initialize view models
        heatCycleViewModel = new ViewModelProvider(this).get(HeatCycleViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        // Setup UI
        setupToolbar();
        setupRecyclerView();

        // Load data
        loadPetInfo();
        loadHeatCycles();
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
        RecyclerView recyclerView = findViewById(R.id.heatCyclesRecyclerView);
        adapter = new HeatCycleAdapter(this);
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
                        petNameTextView.setText(pet.getName() + "'s Heat Cycles");
                    }

                    // Verify gender is female
                    if (pet.getGender() == null || !pet.getGender().equals("Female")) {
                        finish();
                        return;
                    }
                }
            }
        });
    }

    private void loadHeatCycles() {
        heatCycleViewModel.getHeatCyclesByPet(petId).observe(this, new Observer<List<HeatCycle>>() {
            @Override
            public void onChanged(List<HeatCycle> cycles) {
                if (cycles != null) {
                    // Sort cycles by date if needed
                    cycles.sort((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()));

                    adapter.setHeatCycles(cycles);

                    // Update UI if empty
                    updateEmptyState(cycles.isEmpty());
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        View emptyView = findViewById(R.id.emptyStateContainer);
        RecyclerView recyclerView = findViewById(R.id.heatCyclesRecyclerView);

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