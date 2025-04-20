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

import com.bumptech.glide.Glide;
import com.example.taskperf1.adapters.HeatCycleAdapter;
import com.example.taskperf1.database.HeatCycle;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.HeatCycleViewModel;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HeatTrackerActivity extends AppCompatActivity {

    private HeatCycleViewModel heatCycleViewModel;
    private PetViewModel petViewModel;
    private int currentPetId;
    private Pet currentPet;
    private HeatCycleAdapter adapter;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    // UI elements
    private TextView petName;
    private TextView petBreed;
    private ShapeableImageView petImage;
    private TextView nextHeatDate;
    private TextView daysUntilNextHeat;
    private TextView lastHeatDate;
    private TextView lastHeatDuration;
    private RecyclerView historyRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_tracker);

        // Get pet ID from intent
        currentPetId = getIntent().getIntExtra("pet_id", -1);

        if (currentPetId == -1) {
            Toast.makeText(this, "Error: No pet selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModels
        heatCycleViewModel = new ViewModelProvider(this).get(HeatCycleViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        // Initialize UI elements
        initializeViews();
        setupBackButton();
        setupAddHeatCycleButtons();
        setupViewAllButton();

        // Load pet data
        loadPetData();
    }

    private void initializeViews() {
        // Pet info views
        petName = findViewById(R.id.petName);
        petBreed = findViewById(R.id.petBreed);
        petImage = findViewById(R.id.petImage);

        // Heat cycle info views
        nextHeatDate = findViewById(R.id.nextHeatDate);
        daysUntilNextHeat = findViewById(R.id.daysUntilNextHeat);
        lastHeatDate = findViewById(R.id.lastHeatDate);
        lastHeatDuration = findViewById(R.id.lastHeatDuration);

        // Setup recycler view for heat cycle history
        historyRecyclerView = findViewById(R.id.heatCycleHistoryRecyclerView);
        adapter = new HeatCycleAdapter(this);
        historyRecyclerView.setAdapter(adapter);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupAddHeatCycleButtons() {
        ImageButton addHeatCycleButton = findViewById(R.id.addHeatCycleButton);
        FloatingActionButton addHeatCycleFab = findViewById(R.id.addHeatCycleFab);

        View.OnClickListener addHeatCycleClickListener = v -> showAddHeatCycleDialog();

        addHeatCycleButton.setOnClickListener(addHeatCycleClickListener);
        addHeatCycleFab.setOnClickListener(addHeatCycleClickListener);
    }

    private void setupViewAllButton() {
        MaterialButton viewAllButton = findViewById(R.id.viewAllCyclesButton);
        viewAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HeatTrackerActivity.this, AllHeatCyclesActivity.class);
            intent.putExtra("pet_id", currentPetId);
            startActivity(intent);
        });
    }

    private void loadPetData() {
        petViewModel.getPetById(currentPetId).observe(this, pet -> {
            if (pet != null) {
                currentPet = pet;
                updatePetInfo(pet);

                // Verify the pet is female
                if (pet.getGender() == null || !pet.getGender().equals("Female")) {
                    Toast.makeText(this, "Heat tracking is only available for female pets", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Load heat cycles
                loadHeatCycles();
            }
        });
    }

    private void updatePetInfo(Pet pet) {
        petName.setText(pet.getName());

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

        petBreed.setText(pet.getBreed() + " • " + ageText);

        // Load profile picture if available
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                android.net.Uri imageUri = android.net.Uri.parse(pet.getProfilePicture());
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.dogpic)
                        .error(R.drawable.dogpic)
                        .centerCrop()
                        .into(petImage);
            } catch (Exception e) {
                petImage.setImageResource(R.drawable.dogpic);
            }
        } else {
            petImage.setImageResource(R.drawable.dogpic);
        }
    }

    private void loadHeatCycles() {
        heatCycleViewModel.getHeatCyclesByPet(currentPetId).observe(this, heatCycles -> {
            if (heatCycles != null && !heatCycles.isEmpty()) {
                // Sort by date (latest first) if needed
                heatCycles.sort((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()));

                // Update adapter with all heat cycles
                adapter.setHeatCycles(heatCycles);

                // Get the latest heat cycle
                HeatCycle latestCycle = heatCycles.get(0);

                // Update last heat cycle information
                if (latestCycle.getStartDate() != null) {
                    lastHeatDate.setText(displayDateFormat.format(latestCycle.getStartDate()));
                }

                lastHeatDuration.setText("Duration: " + latestCycle.getDuration() + " days");

                // Calculate and set next estimated heat cycle
                Calendar nextEstimatedHeat = Calendar.getInstance();
                nextEstimatedHeat.setTime(latestCycle.getStartDate());
                nextEstimatedHeat.add(Calendar.MONTH, 6); // Typically every 6 months

                nextHeatDate.setText("Estimated: " + displayDateFormat.format(nextEstimatedHeat.getTime()));

                // Calculate days until next heat
                long daysUntil = getDaysUntil(nextEstimatedHeat.getTime());
                daysUntilNextHeat.setText(daysUntil + " days from now");
            } else {
                // No heat cycles yet - show empty state
                adapter.setHeatCycles(heatCycles);

                // Set default text for no cycles
                lastHeatDate.setText("No previous cycles");
                lastHeatDuration.setText("Duration: N/A");
                nextHeatDate.setText("Add a heat cycle to see predictions");
                daysUntilNextHeat.setText("No data yet");
            }
        });
    }

    private long getDaysUntil(Date futureDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffMillis = futureDate.getTime() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    private void showAddHeatCycleDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_heat_cycle);

        // Configure dialog window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);
            window.setAttributes(layoutParams);
        }

        // Initialize date inputs
        final TextInputEditText startDateInput = dialog.findViewById(R.id.startDateInput);
        final TextInputEditText endDateInput = dialog.findViewById(R.id.endDateInput);
        final TextInputEditText durationInput = dialog.findViewById(R.id.durationInput);
        final TextInputEditText intensityInput = dialog.findViewById(R.id.intensityInput);
        final TextInputEditText symptomsInput = dialog.findViewById(R.id.symptomsInput);
        final TextInputEditText notesInput = dialog.findViewById(R.id.notesInput);
        final SwitchMaterial reminderSwitch = dialog.findViewById(R.id.reminderSwitch);

        // Set current date for start date
        final Calendar startCalendar = Calendar.getInstance();
        startDateInput.setText(dateFormatter.format(startCalendar.getTime()));

        // Set end date 21 days later (typical heat cycle duration)
        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.DAY_OF_MONTH, 21);
        endDateInput.setText(dateFormatter.format(endCalendar.getTime()));

        // Calculate and set duration
        updateDuration(startCalendar.getTime(), endCalendar.getTime(), durationInput);

        // Handle date selection for start date
        startDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    HeatTrackerActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        startCalendar.set(year, month, dayOfMonth);
                        startDateInput.setText(dateFormatter.format(startCalendar.getTime()));

                        // Update duration when start date changes
                        try {
                            Date endDate = dateFormatter.parse(endDateInput.getText().toString());
                            updateDuration(startCalendar.getTime(), endDate, durationInput);
                        } catch (ParseException e) {
                            // Ignore parsing error
                        }
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set the maximum date as today
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();
        });

        // Handle date selection for end date
        endDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    HeatTrackerActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        endCalendar.set(year, month, dayOfMonth);
                        endDateInput.setText(dateFormatter.format(endCalendar.getTime()));

                        // Update duration when end date changes
                        try {
                            Date startDate = dateFormatter.parse(startDateInput.getText().toString());
                            updateDuration(startDate, endCalendar.getTime(), durationInput);
                        } catch (ParseException e) {
                            // Ignore parsing error
                        }
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        // Handle buttons
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            // Validate required fields
            if (validateInputs(startDateInput, durationInput)) {
                saveHeatCycle(
                        parseDate(startDateInput.getText().toString()),
                        parseDate(endDateInput.getText().toString()),
                        parseDuration(durationInput.getText().toString()),
                        intensityInput.getText() != null ? intensityInput.getText().toString() : "",
                        symptomsInput.getText() != null ? symptomsInput.getText().toString() : "",
                        notesInput.getText() != null ? notesInput.getText().toString() : "",
                        reminderSwitch.isChecked()
                );
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateDuration(Date startDate, Date endDate, TextInputEditText durationInput) {
        if (startDate != null && endDate != null) {
            long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            durationInput.setText(String.valueOf(diff));
        }
    }

    private boolean validateInputs(TextInputEditText startDateInput, TextInputEditText durationInput) {
        boolean isValid = true;

        if (TextUtils.isEmpty(startDateInput.getText())) {
            startDateInput.setError("Start date is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(durationInput.getText())) {
            durationInput.setError("Duration is required");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationInput.getText().toString());
                if (duration <= 0) {
                    durationInput.setError("Duration must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                durationInput.setError("Invalid duration");
                isValid = false;
            }
        }

        return isValid;
    }

    private Date parseDate(String dateStr) {
        try {
            return dateFormatter.parse(dateStr);
        } catch (ParseException e) {
            return new Date(); // Default to current date if parsing fails
        }
    }

    private int parseDuration(String durationStr) {
        try {
            return Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            return 0; // Default to 0 if parsing fails
        }
    }

    private void saveHeatCycle(Date startDate, Date endDate, int duration,
                               String intensity, String symptoms, String notes, boolean reminderSet) {
        HeatCycle newCycle = new HeatCycle();
        newCycle.setPetId(currentPetId);
        newCycle.setStartDate(startDate);
        newCycle.setEndDate(endDate);
        newCycle.setDuration(duration);
        newCycle.setIntensity(intensity);
        newCycle.setSymptoms(symptoms);
        newCycle.setNotes(notes);
        newCycle.setReminderSet(reminderSet);
        newCycle.setCreatedAt(new Date()); // Current timestamp

        heatCycleViewModel.insert(newCycle);
        Toast.makeText(this, "Heat cycle saved successfully!", Toast.LENGTH_SHORT).show();
    }
}