package com.example.taskperf1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HeatTrackerActivity extends AppCompatActivity {

    private static final String TAG = "HeatTrackerActivity";
    private static final int AVERAGE_HEAT_CYCLE_DAYS = 180; // ~6 months between cycles
    private static final int AVERAGE_HEAT_DURATION_DAYS = 21; // ~3 weeks duration
    private static final int FERTILE_WINDOW_START_DAY = 9; // Day in cycle when fertility begins
    private static final int FERTILE_WINDOW_DURATION_DAYS = 5; // Duration of fertile windowCYCLE_DAYS = 180; // ~6 months between cycles


    private HeatCycleViewModel heatCycleViewModel;
    private PetViewModel petViewModel;
    private int currentPetId;
    private Pet currentPet;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    // UI elements
    private ShapeableImageView petImage;
    private TextView petName;
    private TextView petBreed;
    private TextView nextHeatDate;
    private TextView daysUntilNextHeat;
    private TextView lastHeatDate;
    private TextView lastHeatDuration;
    private RecyclerView heatCycleHistoryRecyclerView;
    private HeatCycleAdapter adapter;
    private TextView currentMonthTextView;
    private MaterialCardView currentStatusCard;
    private LinearLayout fertileWindowIndicator;
    private TextView fertileWindowDates;
    private LinearLayout upcomingEventsContainer;
    private CalendarView calendarView;

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
        setupCalendarView();

        // Load pet data
        loadPetData();
    }

    private void initializeViews() {
        // Pet info views
        petImage = findViewById(R.id.petImage);
        petName = findViewById(R.id.petName);
        petBreed = findViewById(R.id.petBreed);

        // Status views
        nextHeatDate = findViewById(R.id.nextHeatDate);
        daysUntilNextHeat = findViewById(R.id.daysUntilNextHeat);
        lastHeatDate = findViewById(R.id.lastHeatDate);
        lastHeatDuration = findViewById(R.id.lastHeatDuration);
        fertileWindowIndicator = findViewById(R.id.fertileWindowIndicator);
        fertileWindowDates = findViewById(R.id.fertileWindowDates);
        upcomingEventsContainer = findViewById(R.id.upcomingEventsContainer);

        // Calendar view
        calendarView = findViewById(R.id.calendarView);

        // Status card
        currentStatusCard = findViewById(R.id.currentStatusCard);

        // History views
        heatCycleHistoryRecyclerView = findViewById(R.id.heatCycleHistoryRecyclerView);
        adapter = new HeatCycleAdapter(this);
        heatCycleHistoryRecyclerView.setAdapter(adapter);
        heatCycleHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    private void setupCalendarView() {
        if (calendarView != null) {
            // Set a listener to detect date selection
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth);
                checkDateForHeatEvents(selectedCal.getTime());
            });

        }
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
            if (heatCycles != null) {
                // Sort by date (latest first)
                heatCycles.sort((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()));

                // Update adapter with all heat cycles
                adapter.setHeatCycles(heatCycles);

                // Toggle visibility of empty state and content containers
                View emptyStateView = findViewById(R.id.emptyStateContainer);
                View contentContainer = findViewById(R.id.contentContainer);

                if (emptyStateView != null && contentContainer != null) {
                    boolean isEmpty = heatCycles.isEmpty();
                    emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    contentContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                }

                // Only update UI if we have data
                if (!heatCycles.isEmpty()) {
                    // Update status card and event indicators
                    updateStatusCard(heatCycles);
                    updateCurrentCycleEvents(heatCycles);
                }
            }
        });
    }

    private void updateCurrentCycleEvents(List<HeatCycle> heatCycles) {
        // Clear existing indicators
        upcomingEventsContainer.removeAllViews();

        if (heatCycles == null || heatCycles.isEmpty()) {
            // Hide the events card if no data
            View eventsCard = findViewById(R.id.currentCycleEventsCard);
            if (eventsCard != null) eventsCard.setVisibility(View.GONE);
            return;
        }

        // Show the events card
        View eventsCard = findViewById(R.id.currentCycleEventsCard);
        if (eventsCard != null) eventsCard.setVisibility(View.VISIBLE);

        // Get the latest heat cycle
        HeatCycle latestCycle = heatCycles.get(0);

        // Add current heat cycle start
        addEventIndicator(upcomingEventsContainer, latestCycle.getStartDate(), "Heat Cycle Start", Color.RED);

        // Calculate and add fertile window start
        Calendar fertileStartCal = Calendar.getInstance();
        fertileStartCal.setTime(latestCycle.getStartDate());
        fertileStartCal.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_START_DAY);
        addEventIndicator(upcomingEventsContainer, fertileStartCal.getTime(), "Fertile Window Start", Color.parseColor("#9966CC"));

        // Calculate and add heat cycle end
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(latestCycle.getStartDate());
        endCal.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_DURATION_DAYS);
        addEventIndicator(upcomingEventsContainer, endCal.getTime(), "Heat Cycle End", Color.parseColor("#FF9800"));
    }

    private void addEventIndicator(LinearLayout container, Date date, String label, int color) {
        try {
            // Create a horizontal layout
            LinearLayout indicatorLayout = new LinearLayout(this);
            indicatorLayout.setOrientation(LinearLayout.HORIZONTAL);
            indicatorLayout.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 8, 0, 8);
            indicatorLayout.setLayoutParams(layoutParams);

            // Add colored dot
            View dot = new View(this);
            int dotSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            dot.setLayoutParams(dotParams);

            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);
            dotDrawable.setColor(color);
            dot.setBackground(dotDrawable);

            // Add date and label
            TextView eventText = new TextView(this);
            eventText.setText(dateFormatter.format(date) + " - " + label);
            eventText.setTextColor(ContextCompat.getColor(this, R.color.gray_600));

            // Add to container
            indicatorLayout.addView(dot);
            indicatorLayout.addView(eventText);
            container.addView(indicatorLayout);

            // Make indicator clickable to show the date in calendar
            indicatorLayout.setOnClickListener(v -> {
                if (calendarView != null) {
                    calendarView.setDate(date.getTime());
                    // Scroll to calendar
                    findViewById(R.id.calendarCard).requestFocus();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error adding event indicator: " + e.getMessage());
        }
    }

    private void updateStatusCard(List<HeatCycle> heatCycles) {
        try {
            if (heatCycles == null || heatCycles.isEmpty()) {
                // No cycles yet - show default message
                lastHeatDate.setText("No previous cycles");
                lastHeatDuration.setText("Duration: N/A");
                nextHeatDate.setText("Add a heat cycle to see predictions");
                daysUntilNextHeat.setText("No data yet");
                fertileWindowIndicator.setVisibility(View.GONE);
                return;
            }

            // Get the latest heat cycle
            HeatCycle latestCycle = heatCycles.get(0);

            // Update last heat info
            lastHeatDate.setText(dateFormatter.format(latestCycle.getStartDate()));

            // Calculate cycle duration
            int duration = latestCycle.getDuration();
            if (duration <= 0 && latestCycle.getEndDate() != null) {
                // Calculate duration from start and end dates
                long diffInMillis = latestCycle.getEndDate().getTime() - latestCycle.getStartDate().getTime();
                duration = (int) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            } else if (duration <= 0) {
                duration = AVERAGE_HEAT_DURATION_DAYS; // Default duration
            }
            lastHeatDuration.setText("Duration: " + duration + " days");

            // Calculate and set next estimated heat cycle
            Calendar nextHeatCal = Calendar.getInstance();
            nextHeatCal.setTime(latestCycle.getStartDate());
            nextHeatCal.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS);

            // Update to show a range instead of an exact date
            Calendar nextHeatEarliest = Calendar.getInstance();
            nextHeatEarliest.setTime(latestCycle.getStartDate());
            nextHeatEarliest.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS - 10);

            Calendar nextHeatLatest = Calendar.getInstance();
            nextHeatLatest.setTime(latestCycle.getStartDate());
            nextHeatLatest.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS + 10);

            nextHeatDate.setText("Estimated: " + dateFormatter.format(nextHeatEarliest.getTime()) +
                    " to " + dateFormatter.format(nextHeatLatest.getTime()));

            // Calculate days until next heat (use the midpoint)
            long daysUntil = getDaysUntil(nextHeatCal.getTime());
            daysUntilNextHeat.setText(daysUntil + " days from now (approximate)");

            // Set text color based on urgency
            if (daysUntil <= 30) {
                daysUntilNextHeat.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else if (daysUntil <= 60) {
                daysUntilNextHeat.setTextColor(Color.parseColor("#FF9800")); // Orange
            } else {
                daysUntilNextHeat.setTextColor(ContextCompat.getColor(this, R.color.brand_green));
            }

            // Update fertile window info
            Calendar fertileStartCal = Calendar.getInstance();
            fertileStartCal.setTime(nextHeatCal.getTime());
            fertileStartCal.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_START_DAY);

            Calendar fertileEndCal = Calendar.getInstance();
            fertileEndCal.setTime(fertileStartCal.getTime());
            fertileEndCal.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_DURATION_DAYS - 1);

            fertileWindowDates.setText(
                    dateFormatter.format(fertileStartCal.getTime()) + " - " +
                            dateFormatter.format(fertileEndCal.getTime())
            );

            // Show fertile window info if we're close to or in a heat cycle
            if (daysUntil <= 60) {
                fertileWindowIndicator.setVisibility(View.VISIBLE);
            } else {
                fertileWindowIndicator.setVisibility(View.GONE);
            }

            // Highlight dates in calendar
            markDatesInCalendar(heatCycles);

        } catch (Exception e) {
            Log.e(TAG, "Error updating status card: " + e.getMessage());
        }
    }

    private void markDatesInCalendar(List<HeatCycle> heatCycles) {
        // With standard Android CalendarView, we can't directly decorate dates
        // We'll use the dialog approach when user clicks on a date
    }

    private long getDaysUntil(Date futureDate) {
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffMillis = futureDate.getTime() - today.getTimeInMillis();
            return Math.max(0, TimeUnit.MILLISECONDS.toDays(diffMillis));
        } catch (Exception e) {
            Log.e(TAG, "Error calculating days until: " + e.getMessage());
            return 0;
        }
    }

    private void checkDateForHeatEvents(Date selectedDate) {
        try {
            // Format the date for display
            String formattedDate = dateFormatter.format(selectedDate);

            // Check if this date matches any heat cycle events
            boolean isHeatStart = false;
            boolean isHeatEnd = false;
            boolean isFertileWindow = false;
            boolean isPredicted = false;

            // Get all heat cycles from adapter
            List<HeatCycle> cycles = adapter.getHeatCycles();
            if (cycles == null || cycles.isEmpty()) {
                Toast.makeText(this, "No heat cycles found for this date", Toast.LENGTH_SHORT).show();
                return; // No cycles to check
            }

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            selectedCal.set(Calendar.HOUR_OF_DAY, 0);
            selectedCal.set(Calendar.MINUTE, 0);
            selectedCal.set(Calendar.SECOND, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            // Check for heat cycle events
            for (HeatCycle cycle : cycles) {
                // Check if it's a heat start date
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(cycle.getStartDate());
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                if (startCal.getTimeInMillis() == selectedCal.getTimeInMillis()) {
                    isHeatStart = true;
                }

                // Check if it's a heat end date
                if (cycle.getEndDate() != null) {
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(cycle.getEndDate());
                    endCal.set(Calendar.HOUR_OF_DAY, 0);
                    endCal.set(Calendar.MINUTE, 0);
                    endCal.set(Calendar.SECOND, 0);
                    endCal.set(Calendar.MILLISECOND, 0);

                    if (endCal.getTimeInMillis() == selectedCal.getTimeInMillis()) {
                        isHeatEnd = true;
                    }
                }

                // Check if it's in fertile window
                Calendar fertileStart = Calendar.getInstance();
                fertileStart.setTime(cycle.getStartDate());
                fertileStart.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_START_DAY);

                Calendar fertileEnd = Calendar.getInstance();
                fertileEnd.setTime(fertileStart.getTime());
                fertileEnd.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_DURATION_DAYS - 1);

                if (selectedCal.getTimeInMillis() >= fertileStart.getTimeInMillis() &&
                        selectedCal.getTimeInMillis() <= fertileEnd.getTimeInMillis()) {
                    isFertileWindow = true;
                }
            }

            // Check if it's a predicted heat date or fertile window
            if (!cycles.isEmpty()) {
                HeatCycle latestCycle = cycles.get(0);

                // Predicted next heat start range
                Calendar nextHeatEarliest = Calendar.getInstance();
                nextHeatEarliest.setTime(latestCycle.getStartDate());
                nextHeatEarliest.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS - 10);

                Calendar nextHeatLatest = Calendar.getInstance();
                nextHeatLatest.setTime(latestCycle.getStartDate());
                nextHeatLatest.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS + 10);

                if (selectedCal.getTimeInMillis() >= nextHeatEarliest.getTimeInMillis() &&
                        selectedCal.getTimeInMillis() <= nextHeatLatest.getTimeInMillis()) {
                    isHeatStart = true;
                    isPredicted = true;
                }

                // Predicted fertile window - use middle of next heat range as reference point
                Calendar nextHeatMidpoint = Calendar.getInstance();
                nextHeatMidpoint.setTime(latestCycle.getStartDate());
                nextHeatMidpoint.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_CYCLE_DAYS);

                Calendar predictedFertileStart = Calendar.getInstance();
                predictedFertileStart.setTime(nextHeatMidpoint.getTime());
                predictedFertileStart.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_START_DAY);

                Calendar predictedFertileEnd = Calendar.getInstance();
                predictedFertileEnd.setTime(predictedFertileStart.getTime());
                predictedFertileEnd.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_DURATION_DAYS - 1);

                if (selectedCal.getTimeInMillis() >= predictedFertileStart.getTimeInMillis() &&
                        selectedCal.getTimeInMillis() <= predictedFertileEnd.getTimeInMillis()) {
                    isFertileWindow = true;
                    isPredicted = true;
                }
            }

            // If no events, just show a simple toast message
            if (!isHeatStart && !isHeatEnd && !isFertileWindow) {
                Toast.makeText(this, "No heat cycle events on " + formattedDate, Toast.LENGTH_SHORT).show();
                return;
            }

            // Build message based on event types
            StringBuilder message = new StringBuilder();
            message.append(formattedDate).append("\n\n");

            if (isHeatStart) {
                if (isPredicted) {
                    message.append("• Predicted heat cycle start date\n");
                } else {
                    message.append("• Heat cycle start date\n");
                }
            }

            if (isHeatEnd) {
                message.append("• Heat cycle end date\n");
            }

            if (isFertileWindow) {
                if (isPredicted) {
                    message.append("• Predicted fertile window\n");
                } else {
                    message.append("• Fertile window\n");
                }
            }

            // Show dialog with event information
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Heat Cycle Details")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", null)
                    .create();

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error checking date for heat events: " + e.getMessage());
            Toast.makeText(this, "Error checking date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddHeatCycleDialog() {
        try {
            // Create dialog directly with the theme
            final Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_add_heat_cycle);

            // Configure dialog window
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
                window.setAttributes(layoutParams);
            }

            // Initialize dialog views - do this AFTER setting content view
            final TextInputEditText startDateInput = dialog.findViewById(R.id.startDateInput);
            final TextInputEditText notesInput = dialog.findViewById(R.id.notesInput);
            final Switch reminderSwitch = dialog.findViewById(R.id.reminderSwitch);
            final Button cancelButton = dialog.findViewById(R.id.cancelButton);
            final Button saveButton = dialog.findViewById(R.id.saveButton);

            // Set current date for start date (default to today)
            final Calendar startCalendar = Calendar.getInstance();
            startDateInput.setText(inputDateFormat.format(startCalendar.getTime()));

            // Handle date selection for start date
            startDateInput.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            startCalendar.set(year, month, dayOfMonth);
                            startDateInput.setText(inputDateFormat.format(startCalendar.getTime()));
                        },
                        startCalendar.get(Calendar.YEAR),
                        startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)
                );

                // Set the maximum date as today
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            });

            // Set button click listeners
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            saveButton.setOnClickListener(v -> {
                // Basic validation
                if (startDateInput.getText() == null || startDateInput.getText().toString().isEmpty()) {
                    Toast.makeText(HeatTrackerActivity.this, "Start date is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create heat cycle object
                HeatCycle newCycle = new HeatCycle();
                newCycle.setPetId(currentPetId);

                try {
                    // Parse dates
                    Date startDate = inputDateFormat.parse(startDateInput.getText().toString());
                    if (startDate != null) {
                        newCycle.setStartDate(startDate);

                        // Calculate estimated end date (21 days after start)
                        Calendar endCal = Calendar.getInstance();
                        endCal.setTime(newCycle.getStartDate());
                        endCal.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_DURATION_DAYS);
                        newCycle.setEndDate(endCal.getTime());

                        // Set default duration
                        newCycle.setDuration(AVERAGE_HEAT_DURATION_DAYS);

                        // Set other fields
                        newCycle.setIntensity("Normal"); // Default intensity
                        newCycle.setSymptoms(""); // No default symptoms
                        newCycle.setNotes(notesInput.getText() != null ?
                                notesInput.getText().toString() : "");
                        newCycle.setReminderSet(reminderSwitch.isChecked());
                        newCycle.setCreatedAt(new Date());

                        // Save to database
                        heatCycleViewModel.insert(newCycle);

                        // Show information about the related events
                        showAddedCycleEventsInfo(newCycle);

                        dialog.dismiss();
                    } else {
                        Toast.makeText(HeatTrackerActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    Toast.makeText(HeatTrackerActivity.this, "Error parsing dates", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Date parsing error: " + e.getMessage());
                }
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Dialog creation error: " + e.getMessage(), e);
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show information about the events related to the newly added cycle
     */
    private void showAddedCycleEventsInfo(HeatCycle cycle) {
        try {
            // Calculate fertile window
            Calendar fertileStart = Calendar.getInstance();
            fertileStart.setTime(cycle.getStartDate());
            fertileStart.add(Calendar.DAY_OF_MONTH, FERTILE_WINDOW_START_DAY);

            // Calculate end date
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(cycle.getStartDate());
            endDate.add(Calendar.DAY_OF_MONTH, AVERAGE_HEAT_DURATION_DAYS);

            StringBuilder message = new StringBuilder();
            message.append("Heat cycle added successfully!\n\n");
            message.append("Heat Cycle Start: ").append(dateFormatter.format(cycle.getStartDate())).append("\n\n");
            message.append("Fertile Window Start: ").append(dateFormatter.format(fertileStart.getTime())).append("\n\n");
            message.append("Heat Cycle End: ").append(dateFormatter.format(endDate.getTime()));

            new AlertDialog.Builder(this)
                    .setTitle("Heat Cycle Information")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing cycle info: " + e.getMessage());
            // Still show a success message even if detailed info fails
            Toast.makeText(this, "Heat cycle added successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        if (currentPetId != -1) {
            loadHeatCycles();
        }
    }
}