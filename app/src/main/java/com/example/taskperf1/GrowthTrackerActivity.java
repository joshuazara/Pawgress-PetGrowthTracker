package com.example.taskperf1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.adapters.GrowthEntryAdapter;
import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.GrowthEntryViewModel;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GrowthTrackerActivity extends AppCompatActivity {

    private GrowthEntryViewModel growthEntryViewModel;
    private PetViewModel petViewModel;
    private int currentPetId;
    private Pet currentPet;
    private List<GrowthEntry> growthEntries = new ArrayList<>();
    private GrowthEntryAdapter adapter;
    private LineChart weightChart;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // UI elements for pet info
    private TextView petNameTextView;
    private TextView petBreedTextView;
    private ShapeableImageView petImageView;

    // UI elements for current metrics
    private TextView currentWeightValue;
    private TextView currentHeightValue;
    private TextView currentLengthValue;
    private TextView weightGainValue;
    private TextView heightGainValue;
    private TextView lengthGainValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_growth_tracker);

        // Get pet ID from intent
        currentPetId = getIntent().getIntExtra("pet_id", -1);

        if (currentPetId == -1) {
            Toast.makeText(this, "Error: No pet selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModels
        growthEntryViewModel = new ViewModelProvider(this).get(GrowthEntryViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        // Initialize UI elements
        initializeViews();
        setupBackButton();
        setupAddEntryButtons();
        setupViewAllButton();

        // Load pet data
        loadPetData();
    }

    private void initializeViews() {
        // Pet info views
        petNameTextView = findViewById(R.id.petName);
        petBreedTextView = findViewById(R.id.petBreed);
        petImageView = findViewById(R.id.petImage);

        // Metrics views
        currentWeightValue = findViewById(R.id.currentWeightValue);
        currentHeightValue = findViewById(R.id.heightValue);
        currentLengthValue = findViewById(R.id.lengthValue);
        weightGainValue = findViewById(R.id.weightGainValue);
        heightGainValue = findViewById(R.id.heightGainValue);
        lengthGainValue = findViewById(R.id.lengthGainValue);

        // Weight chart
        weightChart = findViewById(R.id.weightChart);
        if (weightChart == null) {
            weightChart = new LineChart(this);
            findViewById(R.id.chartContainer).setVisibility(View.GONE); // Hide placeholder
            ((android.widget.FrameLayout) findViewById(R.id.chartContainer)).addView(weightChart);
        }

        // Configure chart
        setupEmptyChart();

        // Growth history RecyclerView
        RecyclerView recyclerView = findViewById(R.id.growthHistoryRecyclerView);
        adapter = new GrowthEntryAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupEmptyChart() {
        weightChart.getDescription().setEnabled(false);
        weightChart.setNoDataText("No growth data available yet");
        weightChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.gray_500));
        weightChart.setDrawGridBackground(false);
        weightChart.setDrawBorders(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setExtraOffsets(10, 10, 10, 10);

        // X-axis setup
        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Y-axis setup
        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f); // Always start from 0

        // Disable right Y-axis
        weightChart.getAxisRight().setEnabled(false);

        // Legend setup
        weightChart.getLegend().setEnabled(false);

        // Set empty data
        weightChart.invalidate();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupAddEntryButtons() {
        ImageButton addEntryButton = findViewById(R.id.addEntryButton);
        FloatingActionButton addGrowthEntryFab = findViewById(R.id.addGrowthEntryFab);

        View.OnClickListener addEntryClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEntryDialog();
            }
        };

        addEntryButton.setOnClickListener(addEntryClickListener);
        addGrowthEntryFab.setOnClickListener(addEntryClickListener);
    }

    private void setupViewAllButton() {
        MaterialButton viewAllButton = findViewById(R.id.viewAllButton);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to view all growth entries
                Intent intent = new Intent(GrowthTrackerActivity.this, AllGrowthEntriesActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });
    }

    private void loadPetData() {
        petViewModel.getPetById(currentPetId).observe(this, new Observer<Pet>() {
            @Override
            public void onChanged(Pet pet) {
                if (pet != null) {
                    currentPet = pet;
                    updatePetInfo(pet);

                    // Now load growth entries after we have the pet info
                    loadGrowthEntries();

                    // If there are no growth entries, create an initial one based on pet data
                    checkForInitialGrowthEntry(pet);
                }
            }
        });
    }

    private void checkForInitialGrowthEntry(final Pet pet) {
        growthEntryViewModel.getGrowthEntriesByPet(currentPetId).observe(this, new Observer<List<GrowthEntry>>() {
            @Override
            public void onChanged(List<GrowthEntry> entries) {
                if (entries == null || entries.isEmpty()) {
                    // No growth entries yet - create an initial one based on pet's initial values
                    if (pet.getInitialWeight() > 0 || pet.getInitialHeight() > 0) {
                        GrowthEntry initialEntry = new GrowthEntry();
                        initialEntry.setPetId(pet.getPetId());
                        initialEntry.setEntryDate(pet.getBirthDate() != null ? pet.getBirthDate() : new Date());
                        initialEntry.setWeight(pet.getInitialWeight());
                        initialEntry.setHeight(pet.getInitialHeight());
                        initialEntry.setLength(0); // Default value
                        initialEntry.setCreatedAt(new Date());
                        initialEntry.setNotes("Initial measurement");

                        growthEntryViewModel.insert(initialEntry);
                    }
                }
            }
        });
    }

    private void updatePetInfo(Pet pet) {
        petNameTextView.setText(pet.getName());

        // Format the age based on birth date
        String ageText = "";
        if (pet.getBirthDate() != null) {
            Calendar dob = Calendar.getInstance();
            dob.setTime(pet.getBirthDate());
            Calendar today = Calendar.getInstance();

            int ageYears = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            int ageMonths = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);

            if (ageMonths < 0) {
                ageYears--;
                ageMonths += 12;
            }

            if (ageYears > 0) {
                ageText = ageYears + (ageYears == 1 ? " year" : " years");
            } else {
                ageText = ageMonths + (ageMonths == 1 ? " month" : " months");
            }
        }

        petBreedTextView.setText(pet.getBreed() + " • " + ageText);

        // Load profile picture if available
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                android.net.Uri imageUri = android.net.Uri.parse(pet.getProfilePicture());
                com.bumptech.glide.Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.dogpic)
                        .error(R.drawable.dogpic)
                        .centerCrop()
                        .into(petImageView);
            } catch (Exception e) {
                petImageView.setImageResource(R.drawable.dogpic);
            }
        } else {
            petImageView.setImageResource(R.drawable.dogpic);
        }
    }

    private void loadGrowthEntries() {
        growthEntryViewModel.getGrowthEntriesByPet(currentPetId).observe(this, new Observer<List<GrowthEntry>>() {
            @Override
            public void onChanged(List<GrowthEntry> entries) {
                if (entries != null) {
                    growthEntries = entries;

                    if (!entries.isEmpty()) {
                        updateGrowthMetrics(entries);
                        updateChart(entries);

                        // Update the recycler view with the latest 3 entries
                        List<GrowthEntry> recentEntries = entries.size() > 3 ?
                                entries.subList(0, 3) : new ArrayList<>(entries);

                        if (adapter != null) {
                            adapter.setGrowthEntries(recentEntries);
                            if (currentPet != null) {
                                adapter.setPet(currentPet);
                            }
                        }
                    } else {
                        // If we have a pet with initial measurements but no entries yet,
                        // those will be added by checkForInitialGrowthEntry
                    }
                }
            }
        });
    }

    private void updateGrowthMetrics(List<GrowthEntry> entries) {
        if (entries == null || entries.isEmpty()) return;

        // Get the latest entry
        GrowthEntry latestEntry = entries.get(0); // Entries should be sorted by date desc

        // Set current values
        currentWeightValue.setText(String.format(Locale.getDefault(), "%.1f kg", latestEntry.getWeight()));
        currentHeightValue.setText(String.format(Locale.getDefault(), "%.1f cm", latestEntry.getHeight()));
        currentLengthValue.setText(String.format(Locale.getDefault(), "%.1f cm", latestEntry.getLength()));

        // If there are at least 2 entries, calculate and show the gains
        if (entries.size() > 1) {
            GrowthEntry previousEntry = entries.get(1);

            float weightGain = latestEntry.getWeight() - previousEntry.getWeight();
            float heightGain = latestEntry.getHeight() - previousEntry.getHeight();
            float lengthGain = latestEntry.getLength() - previousEntry.getLength();

            // Update UI with gain values
            weightGainValue.setText(String.format(Locale.getDefault(), "%+.1f kg", weightGain));
            heightGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", heightGain));
            lengthGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", lengthGain));

            // Set text color based on gain (green for positive, red for negative)
            weightGainValue.setTextColor(getResources().getColor(weightGain >= 0 ? R.color.brand_green : R.color.red));
            heightGainValue.setTextColor(getResources().getColor(heightGain >= 0 ? R.color.brand_green : R.color.red));
            lengthGainValue.setTextColor(getResources().getColor(lengthGain >= 0 ? R.color.brand_green : R.color.red));
        } else {
            // If only one entry, show initial values as gains
            weightGainValue.setText(String.format(Locale.getDefault(), "%+.1f kg", latestEntry.getWeight()));
            heightGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", latestEntry.getHeight()));
            lengthGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", latestEntry.getLength()));

            // All values are gains (positive)
            weightGainValue.setTextColor(getResources().getColor(R.color.brand_green));
            heightGainValue.setTextColor(getResources().getColor(R.color.brand_green));
            lengthGainValue.setTextColor(getResources().getColor(R.color.brand_green));
        }
    }

    private void updateChart(List<GrowthEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            setupEmptyChart();
            return;
        }

        // Create a copy of entries and sort from oldest to newest for the chart
        List<GrowthEntry> sortedEntries = new ArrayList<>(entries);
        Collections.reverse(sortedEntries);

        // Create entries for the chart
        List<Entry> weightEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Format for displaying dates on chart
        SimpleDateFormat chartDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        // Add data points
        for (int i = 0; i < sortedEntries.size(); i++) {
            GrowthEntry entry = sortedEntries.get(i);
            weightEntries.add(new Entry(i, entry.getWeight()));
            labels.add(chartDateFormat.format(entry.getEntryDate()));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(weightEntries, "Weight (kg)");
        styleDataSet(dataSet);

        // Set chart data
        LineData lineData = new LineData(dataSet);
        weightChart.setData(lineData);

        // Set X-axis labels
        XAxis xAxis = weightChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // Handle single point - adjust Y axis for better visibility
        if (sortedEntries.size() == 1) {
            float weight = sortedEntries.get(0).getWeight();
            YAxis leftAxis = weightChart.getAxisLeft();
            leftAxis.setAxisMinimum(Math.max(0, weight - (weight * 0.2f))); // 20% below current weight, but not below 0
            leftAxis.setAxisMaximum(weight + (weight * 0.2f)); // 20% above current weight

            // Add padding for single point
            weightChart.setExtraOffsets(20, 20, 20, 20);
        }

        // Refresh the chart
        weightChart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        dataSet.setColor(ContextCompat.getColor(this, R.color.brand_green));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.brand_green));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.brand_green_light));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);

        // For single or few data points, make sure values are shown
        if (dataSet.getEntryCount() <= 3) {
            dataSet.setValueTextSize(12f);
        }
    }

    private void showAddEntryDialog() {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_growth_entry);

        // Configure dialog window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        // Initialize dialog views
        final TextInputEditText dateInput = dialog.findViewById(R.id.dateInput);
        final TextInputEditText weightInput = dialog.findViewById(R.id.weightInput);
        final TextInputEditText heightInput = dialog.findViewById(R.id.heightInput);
        final TextInputEditText lengthInput = dialog.findViewById(R.id.lengthInput);
        final TextInputEditText notesInput = dialog.findViewById(R.id.notesInput);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);

        // Set current date
        dateInput.setText(dateFormatter.format(selectedDate.getTime()));

        // Handle date selection
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        GrowthTrackerActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                selectedDate.set(year, month, dayOfMonth);
                                dateInput.setText(dateFormatter.format(selectedDate.getTime()));
                            }
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                );

                // Set the maximum date as today
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

                datePickerDialog.show();

                // Style the date picker dialog buttons
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(GrowthTrackerActivity.this, R.color.brand_green));
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(GrowthTrackerActivity.this, R.color.gray_600));
            }
        });

        // Pre-fill with latest values if available
        if (!growthEntries.isEmpty()) {
            GrowthEntry latestEntry = growthEntries.get(0);
            weightInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getWeight()));
            heightInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getHeight()));
            lengthInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getLength()));
        } else if (currentPet != null) {
            // Use initial values from pet if no entries yet
            if (currentPet.getInitialWeight() > 0) {
                weightInput.setText(String.format(Locale.getDefault(), "%.1f", currentPet.getInitialWeight()));
            }
            if (currentPet.getInitialHeight() > 0) {
                heightInput.setText(String.format(Locale.getDefault(), "%.1f", currentPet.getInitialHeight()));
            }
        }

        // Handle button clicks
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs(weightInput, heightInput, lengthInput)) {
                    saveGrowthEntry(
                            selectedDate.getTime(),
                            Float.parseFloat(weightInput.getText().toString()),
                            Float.parseFloat(heightInput.getText().toString()),
                            Float.parseFloat(lengthInput.getText().toString()),
                            notesInput.getText() != null ? notesInput.getText().toString() : ""
                    );
                    dialog.dismiss();
                }
            }
        });

        // Make sure buttons use brand colors
        saveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_green));
        cancelButton.setTextColor(ContextCompat.getColor(this, R.color.gray_600));

        dialog.show();
    }

    private boolean validateInputs(TextInputEditText weightInput, TextInputEditText heightInput,
                                   TextInputEditText lengthInput) {
        boolean isValid = true;

        if (TextUtils.isEmpty(weightInput.getText())) {
            weightInput.setError("Weight is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(heightInput.getText())) {
            heightInput.setError("Height is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(lengthInput.getText())) {
            lengthInput.setError("Length is required");
            isValid = false;
        }

        return isValid;
    }

    private void saveGrowthEntry(Date entryDate, float weight, float height, float length, String notes) {
        GrowthEntry newEntry = new GrowthEntry();
        newEntry.setPetId(currentPetId);
        newEntry.setEntryDate(entryDate);
        newEntry.setWeight(weight);
        newEntry.setHeight(height);
        newEntry.setLength(length);
        newEntry.setNotes(notes);
        newEntry.setCreatedAt(new Date()); // Current timestamp

        growthEntryViewModel.insert(newEntry);
        Toast.makeText(this, "Growth entry saved successfully!", Toast.LENGTH_SHORT).show();
    }
}