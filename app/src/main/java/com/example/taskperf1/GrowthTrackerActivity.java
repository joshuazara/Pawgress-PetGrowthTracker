package com.example.taskperf1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    
    private TextView petNameTextView;
    private TextView petBreedTextView;
    private ShapeableImageView petImageView;

    
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

        
        currentPetId = getIntent().getIntExtra("pet_id", -1);

        if (currentPetId == -1) {
            Toast.makeText(this, "Error: No pet selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        growthEntryViewModel = new ViewModelProvider(this).get(GrowthEntryViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        
        initializeViews();
        setupBackButton();
        setupAddEntryButtons();
        setupViewAllButton();

        
        loadPetData();
    }

    private void initializeViews() {
        
        petNameTextView = findViewById(R.id.petName);
        petBreedTextView = findViewById(R.id.petBreed);
        petImageView = findViewById(R.id.petImage);

        
        currentWeightValue = findViewById(R.id.currentWeightValue);
        currentHeightValue = findViewById(R.id.heightValue);
        currentLengthValue = findViewById(R.id.lengthValue);
        weightGainValue = findViewById(R.id.weightGainValue);
        heightGainValue = findViewById(R.id.heightGainValue);
        lengthGainValue = findViewById(R.id.lengthGainValue);

        
        weightChart = findViewById(R.id.weightChart);
        if (weightChart == null) {
            weightChart = new LineChart(this);
            findViewById(R.id.chartContainer).setVisibility(View.GONE); 
            ((android.widget.FrameLayout) findViewById(R.id.chartContainer)).addView(weightChart);
        }

        
        setupEmptyChart();

        
        RecyclerView recyclerView = findViewById(R.id.growthHistoryRecyclerView);
        adapter = new GrowthEntryAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupEmptyChart() {
        weightChart.clear();
        weightChart.getDescription().setEnabled(false);
        weightChart.setNoDataText("No growth data available yet");
        weightChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.gray_500));
        weightChart.setDrawGridBackground(false);
        weightChart.setDrawBorders(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setExtraOffsets(15, 15, 15, 15);


        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);


        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);


        weightChart.getAxisRight().setEnabled(false);


        weightChart.getLegend().setEnabled(false);


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

                    
                    loadGrowthEntries();

                    
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
                    
                    if (pet.getInitialWeight() > 0 || pet.getInitialHeight() > 0) {
                        GrowthEntry initialEntry = new GrowthEntry();
                        initialEntry.setPetId(pet.getPetId());
                        initialEntry.setEntryDate(pet.getBirthDate() != null ? pet.getBirthDate() : new Date());
                        initialEntry.setWeight(pet.getInitialWeight());
                        initialEntry.setHeight(pet.getInitialHeight());
                        initialEntry.setLength(0); 
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

        
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                android.net.Uri imageUri = android.net.Uri.parse(pet.getProfilePicture());
                com.bumptech.glide.Glide.with(this)
                        .load(imageUri)
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
                        if (weightChart != null) {
                            weightChart.clear();
                            weightChart.fitScreen();
                        }

                        updateGrowthMetrics(entries);
                        updateChart(entries);

                        List<GrowthEntry> recentEntries = entries.size() > 3 ?
                                entries.subList(0, 3) : new ArrayList<>(entries);

                        if (adapter != null) {
                            adapter.setGrowthEntries(recentEntries);
                            if (currentPet != null) {
                                adapter.setPet(currentPet);
                            }
                        }
                    }
                }
            }
        });
    }


    private void updateGrowthMetrics(List<GrowthEntry> entries) {
        if (entries == null || entries.isEmpty()) return;

        
        GrowthEntry latestEntry = entries.get(0); 

        
        currentWeightValue.setText(String.format(Locale.getDefault(), "%.1f kg", latestEntry.getWeight()));
        currentHeightValue.setText(String.format(Locale.getDefault(), "%.1f cm", latestEntry.getHeight()));
        currentLengthValue.setText(String.format(Locale.getDefault(), "%.1f cm", latestEntry.getLength()));

        
        if (entries.size() > 1) {
            GrowthEntry previousEntry = entries.get(1);

            float weightGain = latestEntry.getWeight() - previousEntry.getWeight();
            float heightGain = latestEntry.getHeight() - previousEntry.getHeight();
            float lengthGain = latestEntry.getLength() - previousEntry.getLength();

            
            weightGainValue.setText(String.format(Locale.getDefault(), "%+.1f kg", weightGain));
            heightGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", heightGain));
            lengthGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", lengthGain));

            
            weightGainValue.setTextColor(ContextCompat.getColor(this, weightGain >= 0 ? R.color.brand_green : R.color.red));
            heightGainValue.setTextColor(ContextCompat.getColor(this,heightGain >= 0 ? R.color.brand_green : R.color.red));
            lengthGainValue.setTextColor(ContextCompat.getColor(this,lengthGain >= 0 ? R.color.brand_green : R.color.red));
        } else {
            
            weightGainValue.setText(String.format(Locale.getDefault(), "%+.1f kg", latestEntry.getWeight()));
            heightGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", latestEntry.getHeight()));
            lengthGainValue.setText(String.format(Locale.getDefault(), "%+.1f cm", latestEntry.getLength()));

            
            weightGainValue.setTextColor(ContextCompat.getColor(this,R.color.brand_green));
            heightGainValue.setTextColor(ContextCompat.getColor(this,R.color.brand_green));
            lengthGainValue.setTextColor(ContextCompat.getColor(this,R.color.brand_green));
        }
    }

    private void updateChart(List<GrowthEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            setupEmptyChart();
            return;
        }

        weightChart.clear();
        weightChart.getXAxis().resetAxisMaximum();
        weightChart.getXAxis().resetAxisMinimum();
        weightChart.getAxisLeft().resetAxisMaximum();
        weightChart.getAxisLeft().resetAxisMinimum();
        weightChart.getAxisRight().resetAxisMaximum();
        weightChart.getAxisRight().resetAxisMinimum();


        List<GrowthEntry> sortedEntries = new ArrayList<>(entries);
        Collections.reverse(sortedEntries);


        List<Entry> weightEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();


        SimpleDateFormat chartDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());


        for (int i = 0; i < sortedEntries.size(); i++) {
            GrowthEntry entry = sortedEntries.get(i);
            weightEntries.add(new Entry(i, entry.getWeight()));
            labels.add(chartDateFormat.format(entry.getEntryDate()));
        }


        LineDataSet dataSet = new LineDataSet(weightEntries, "Weight (kg)");
        styleDataSet(dataSet);


        LineData lineData = new LineData(dataSet);
        weightChart.setData(lineData);


        XAxis xAxis = weightChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);


        if (sortedEntries.size() == 1) {

            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(0.5f);
        } else {

            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(sortedEntries.size() - 0.5f);
        }


        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);


        if (sortedEntries.size() == 1) {

            float weight = sortedEntries.get(0).getWeight();
            float padding = Math.max(weight * 0.2f, 1.0f); // At least 1kg padding
            leftAxis.setAxisMinimum(Math.max(0, weight - padding));
            leftAxis.setAxisMaximum(weight + padding);
        } else {

            float minWeight = Float.MAX_VALUE;
            float maxWeight = Float.MIN_VALUE;

            for (GrowthEntry entry : sortedEntries) {
                minWeight = Math.min(minWeight, entry.getWeight());
                maxWeight = Math.max(maxWeight, entry.getWeight());
            }

            float range = maxWeight - minWeight;
            float padding = Math.max(range * 0.1f, 0.5f); // 10% padding, minimum 0.5kg

            leftAxis.setAxisMinimum(Math.max(0, minWeight - padding));
            leftAxis.setAxisMaximum(maxWeight + padding);
        }


        weightChart.getAxisRight().setEnabled(false);


        weightChart.getLegend().setEnabled(false);
        weightChart.getDescription().setEnabled(false);
        weightChart.setDrawGridBackground(false);
        weightChart.setDrawBorders(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setExtraOffsets(15, 15, 15, 15);


        weightChart.notifyDataSetChanged();
        weightChart.invalidate();


        weightChart.animateX(300);
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

        
        if (dataSet.getEntryCount() <= 3) {
            dataSet.setValueTextSize(12f);
        }
    }

    private void showAddEntryDialog() {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_growth_entry);

        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        
        final TextInputEditText dateInput = dialog.findViewById(R.id.dateInput);
        final TextInputEditText weightInput = dialog.findViewById(R.id.weightInput);
        final TextInputEditText heightInput = dialog.findViewById(R.id.heightInput);
        final TextInputEditText lengthInput = dialog.findViewById(R.id.lengthInput);
        final TextInputEditText notesInput = dialog.findViewById(R.id.notesInput);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);

        
        dateInput.setText(dateFormatter.format(selectedDate.getTime()));

        
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

                
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

                datePickerDialog.show();

                
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(GrowthTrackerActivity.this, R.color.brand_green));
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(GrowthTrackerActivity.this, R.color.gray_600));
            }
        });

        
        if (!growthEntries.isEmpty()) {
            GrowthEntry latestEntry = growthEntries.get(0);
            weightInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getWeight()));
            heightInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getHeight()));
            lengthInput.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getLength()));
        } else if (currentPet != null) {
            
            if (currentPet.getInitialWeight() > 0) {
                weightInput.setText(String.format(Locale.getDefault(), "%.1f", currentPet.getInitialWeight()));
            }
            if (currentPet.getInitialHeight() > 0) {
                heightInput.setText(String.format(Locale.getDefault(), "%.1f", currentPet.getInitialHeight()));
            }
        }

        
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
        newEntry.setCreatedAt(new Date());

        growthEntryViewModel.insert(newEntry);
        Toast.makeText(this, "Growth entry saved successfully!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> {
            if (growthEntryViewModel != null) {

                growthEntryViewModel.getGrowthEntriesByPet(currentPetId).removeObservers(this);

                growthEntryViewModel.getGrowthEntriesByPet(currentPetId).observe(this, entries -> {
                    if (entries != null) {
                        growthEntries = entries;

                        if (!entries.isEmpty()) {

                            if (weightChart != null) {
                                weightChart.clear();
                                weightChart.fitScreen();
                            }

                            updateGrowthMetrics(entries);
                            updateChart(entries);

                            List<GrowthEntry> recentEntries = entries.size() > 3 ?
                                    entries.subList(0, 3) : new ArrayList<>(entries);

                            if (adapter != null) {
                                adapter.setGrowthEntries(recentEntries);
                                if (currentPet != null) {
                                    adapter.setPet(currentPet);
                                }
                            }
                        }
                    }
                });
            }
        }, 300);
    }
}