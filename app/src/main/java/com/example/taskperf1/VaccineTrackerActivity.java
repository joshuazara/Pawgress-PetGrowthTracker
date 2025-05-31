package com.example.taskperf1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.taskperf1.adapters.VaccineEntryAdapter;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.database.VaccineEntry;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.example.taskperf1.viewmodels.VaccineEntryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VaccineTrackerActivity extends AppCompatActivity {

    private VaccineEntryViewModel vaccineEntryViewModel;
    private PetViewModel petViewModel;
    private int currentPetId;
    private Pet currentPet;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    
    private ShapeableImageView petImage;
    private TextView petName;
    private TextView petBreed;
    private RecyclerView upcomingVaccinesRecyclerView;
    private RecyclerView vaccineHistoryRecyclerView;
    private VaccineEntryAdapter upcomingAdapter;
    private VaccineEntryAdapter historyAdapter;
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_tracker);

        
        currentPetId = getIntent().getIntExtra("pet_id", -1);

        if (currentPetId == -1) {
            Toast.makeText(this, "Error: No pet selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        vaccineEntryViewModel = new ViewModelProvider(this).get(VaccineEntryViewModel.class);
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        
        initializeViews();
        setupBackButton();
        setupAddVaccineButtons();
        setupViewAllButton();

        
        loadPetData();
    }

    private void initializeViews() {
        
        petImage = findViewById(R.id.petImage);
        petName = findViewById(R.id.petName);
        petBreed = findViewById(R.id.petBreed);
        emptyStateView = findViewById(R.id.emptyStateContainer);

        
        upcomingVaccinesRecyclerView = findViewById(R.id.upcomingVaccinesRecyclerView);
        if (upcomingVaccinesRecyclerView != null) {
            upcomingAdapter = new VaccineEntryAdapter(this, true);
            upcomingVaccinesRecyclerView.setAdapter(upcomingAdapter);
            upcomingVaccinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        vaccineHistoryRecyclerView = findViewById(R.id.vaccineHistoryRecyclerView);
        if (vaccineHistoryRecyclerView != null) {
            historyAdapter = new VaccineEntryAdapter(this, false);
            vaccineHistoryRecyclerView.setAdapter(historyAdapter);
            vaccineHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
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

    private void setupAddVaccineButtons() {
        ImageButton addVaccineButton = findViewById(R.id.addVaccineButton);
        FloatingActionButton addVaccineFab = findViewById(R.id.addVaccineFab);

        View.OnClickListener addVaccineClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccineDialog();
            }
        };

        addVaccineButton.setOnClickListener(addVaccineClickListener);
        addVaccineFab.setOnClickListener(addVaccineClickListener);
    }

    private void setupViewAllButton() {
        MaterialButton viewAllButton = findViewById(R.id.viewAllVaccinesButton);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VaccineTrackerActivity.this, AllVaccineEntriesActivity.class);
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

                    
                    loadVaccineEntries();
                }
            }
        });
    }

    private void updatePetInfo(Pet pet) {
        petName.setText(pet.getName());

        
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

        
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                android.net.Uri imageUri = android.net.Uri.parse(pet.getProfilePicture());
                Glide.with(this)
                        .load(imageUri)
                        .centerCrop()
                        .into(petImage);
            } catch (Exception e) {
                petImage.setImageResource(R.drawable.dogpic);
            }
        } else {
            petImage.setImageResource(R.drawable.dogpic);
        }
    }

    private void loadVaccineEntries() {
        
        vaccineEntryViewModel.getUpcomingVaccinesForPet(currentPetId).observe(this, new Observer<List<VaccineEntry>>() {
            @Override
            public void onChanged(List<VaccineEntry> upcomingEntries) {
                if (upcomingEntries != null) {
                    upcomingAdapter.setVaccineEntries(upcomingEntries);

                    
                    if (upcomingEntries.isEmpty()) {
                        findViewById(R.id.upcomingVaccinesCard).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.upcomingVaccinesCard).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        
        vaccineEntryViewModel.getVaccineEntriesByPet(currentPetId).observe(this, new Observer<List<VaccineEntry>>() {
            @Override
            public void onChanged(List<VaccineEntry> entries) {
                if (entries != null) {
                    
                    List<VaccineEntry> historyEntries = new ArrayList<>();
                    Date today = new Date();

                    for (VaccineEntry entry : entries) {
                        if (entry.getAdministeredDate() != null && entry.getAdministeredDate().before(today)) {
                            historyEntries.add(entry);
                        }
                    }

                    historyAdapter.setVaccineEntries(historyEntries);

                    
                    updateEmptyState(entries.isEmpty());

                    
                    if (historyEntries.isEmpty()) {
                        findViewById(R.id.vaccineHistoryCard).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.vaccineHistoryCard).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateView != null) {
            if (isEmpty) {
                emptyStateView.setVisibility(View.VISIBLE);
            } else {
                emptyStateView.setVisibility(View.GONE);
            }
        }
    }

    private void showAddVaccineDialog() {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_vaccine);

        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        
        final TextInputEditText vaccineTypeInput = dialog.findViewById(R.id.vaccineTypeInput);
        final TextInputEditText dateInput = dialog.findViewById(R.id.dateInput);
        final TextInputEditText vetInput = dialog.findViewById(R.id.vetInput);
        final TextInputEditText clinicInput = dialog.findViewById(R.id.clinicInput);
        final TextInputEditText batchInput = dialog.findViewById(R.id.batchInput);
        final TextInputEditText nextDueDateInput = dialog.findViewById(R.id.nextDueDateInput);
        final TextInputEditText notesInput = dialog.findViewById(R.id.notesInput);
        final Switch reminderSwitch = dialog.findViewById(R.id.reminderSwitch);

        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        Button saveButton = dialog.findViewById(R.id.saveButton);

        
        final Calendar adminCalendar = Calendar.getInstance();
        dateInput.setText(inputDateFormat.format(adminCalendar.getTime()));

        
        final Calendar nextDueCalendar = Calendar.getInstance();
        nextDueCalendar.add(Calendar.YEAR, 1);
        nextDueDateInput.setText(inputDateFormat.format(nextDueCalendar.getTime()));

        
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateInput, adminCalendar);
            }
        });

        
        nextDueDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(nextDueDateInput, nextDueCalendar);
            }
        });

        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs(vaccineTypeInput, dateInput, nextDueDateInput)) {
                    saveVaccineEntry(
                            vaccineTypeInput.getText().toString(),
                            parseDate(dateInput.getText().toString()),
                            parseDate(nextDueDateInput.getText().toString()),
                            vetInput.getText() != null ? vetInput.getText().toString() : "",
                            clinicInput.getText() != null ? clinicInput.getText().toString() : "",
                            batchInput.getText() != null ? batchInput.getText().toString() : "",
                            notesInput.getText() != null ? notesInput.getText().toString() : "",
                            reminderSwitch.isChecked()
                    );
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void showDatePickerDialog(final TextInputEditText dateField, final Calendar calendar) {
        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        try {
                            calendar.set(year, month, dayOfMonth);
                            dateField.setText(inputDateFormat.format(calendar.getTime()));
                        } catch (Exception e) {
                            Log.e("VaccineTracker", "Error setting date: " + e.getMessage());
                            Toast.makeText(this, "Error setting date", Toast.LENGTH_SHORT).show();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            
            datePickerDialog.setOnShowListener(dialog -> {
                try {
                    datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(this, R.color.brand_green));
                    datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(this, R.color.gray_600));
                } catch (Exception e) {
                    Log.e("VaccineTracker", "Error styling dialog buttons: " + e.getMessage());
                }
            });

            datePickerDialog.show();
        } catch (Exception e) {
            Log.e("VaccineTracker", "Error showing date picker: " + e.getMessage());
            Toast.makeText(this, "Error showing date picker", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(TextInputEditText vaccineTypeInput, TextInputEditText dateInput,
                                   TextInputEditText nextDueDateInput) {
        boolean isValid = true;

        if (TextUtils.isEmpty(vaccineTypeInput.getText())) {
            vaccineTypeInput.setError("Vaccine type is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(dateInput.getText())) {
            dateInput.setError("Administered date is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(nextDueDateInput.getText())) {
            nextDueDateInput.setError("Next due date is required");
            isValid = false;
        }

        return isValid;
    }

    private Date parseDate(String dateStr) {
        try {
            return inputDateFormat.parse(dateStr);
        } catch (ParseException e) {
            return new Date(); 
        }
    }

    private void saveVaccineEntry(String vaccineType, Date administeredDate, Date nextDueDate,
                                  String veterinarian, String clinic, String batchNumber,
                                  String notes, boolean reminderSet) {
        VaccineEntry newEntry = new VaccineEntry();
        newEntry.setPetId(currentPetId);
        newEntry.setVaccineType(vaccineType);
        newEntry.setAdministeredDate(administeredDate);
        newEntry.setNextDueDate(nextDueDate);
        newEntry.setVeterinarian(veterinarian);
        newEntry.setClinic(clinic);
        newEntry.setBatchNumber(batchNumber);
        newEntry.setNotes(notes);
        newEntry.setReminderSet(reminderSet);
        newEntry.setCreatedAt(new Date()); 

        vaccineEntryViewModel.insert(newEntry);
        Toast.makeText(this, "Vaccine record saved successfully!", Toast.LENGTH_SHORT).show();
    }
}