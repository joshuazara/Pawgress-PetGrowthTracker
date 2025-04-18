package com.example.taskperf1;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.viewmodels.GrowthEntryViewModel;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileSelectionActivity extends AppCompatActivity {

    private GrowthEntryViewModel growthEntryViewModel;
    private PetViewModel petViewModel;
    private LinearLayout petCardsContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    // Image selection variables
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private String tempSelectedImagePath;
    private ShapeableImageView dialogPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_selection);

        // Initialize ViewModel
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        growthEntryViewModel = new ViewModelProvider(this).get(GrowthEntryViewModel.class);

        // Find the container where pet cards will be added
        petCardsContainer = findViewById(R.id.petCardsContainer);

        // Setup the add profile button
        MaterialButton addProfileButton = findViewById(R.id.addProfileButton);
        addProfileButton.setOnClickListener(v -> showAddPetDialog());

        // Observe changes to the pets list
        petViewModel.getAllPets().observe(this, pets -> {
            updatePetProfiles(pets);
        });
    }

    private void updatePetProfiles(List<Pet> pets) {
        // Clear existing pet cards
        if (petCardsContainer != null) {
            petCardsContainer.removeAllViews();
        }

        if (pets == null || pets.isEmpty()) {
            // No pets yet - the add button is already visible
            return;
        }

        // Add a card for each pet
        for (Pet pet : pets) {
            addPetCard(pet);
        }
    }

    private void addPetCard(Pet pet) {
        // Inflate the pet card layout
        View petCardView = getLayoutInflater().inflate(
                R.layout.item_pet_profile, petCardsContainer, false);

        // Get references to the views in the card
        MaterialCardView profileCard = petCardView.findViewById(R.id.profileCard);
        ShapeableImageView profileImage = petCardView.findViewById(R.id.profileImage);
        TextView profileName = petCardView.findViewById(R.id.profileName);
        TextView profileBreed = petCardView.findViewById(R.id.profileBreed);
        TextView profileAge = petCardView.findViewById(R.id.profileAge);
        ImageButton editButton = petCardView.findViewById(R.id.editButton);

        // Set the pet data to the views
        profileName.setText(pet.getName());
        profileBreed.setText(pet.getBreed());

        // Calculate and format the pet's age
        if (pet.getBirthDate() != null) {
            long ageInMillis = System.currentTimeMillis() - pet.getBirthDate().getTime();
            long ageInDays = ageInMillis / (1000 * 60 * 60 * 24);

            if (ageInDays < 30) {
                profileAge.setText(ageInDays + " days");
            } else if (ageInDays < 365) {
                int months = (int) (ageInDays / 30);
                profileAge.setText(months + (months == 1 ? " month" : " months"));
            } else {
                int years = (int) (ageInDays / 365);
                profileAge.setText(years + (years == 1 ? " year" : " years"));
            }
        } else {
            profileAge.setText("Age unknown");
        }

        // Set pet image if available
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getProfilePicture());
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.dogpic)
                        .error(R.drawable.dogpic)
                        .centerCrop()
                        .into(profileImage);
            } catch (Exception e) {
                // If there's an error, load a placeholder
                profileImage.setImageResource(R.drawable.dogpic);
            }
        } else {
            // Set a default image
            profileImage.setImageResource(R.drawable.dogpic);
        }

        // Set click listener for the card to open the pet's profile
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileSelectionActivity.this, HomePageActivity.class);
            intent.putExtra("pet_id", pet.getPetId());
            startActivity(intent);
        });

        // Set click listener for the edit button
        editButton.setOnClickListener(v -> {
            showEditPetDialog(pet);
        });

        // Add the card to the container
        petCardsContainer.addView(petCardView);
    }

    private void showAddPetDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pet, null);


        // Get references to the input fields
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText breedInput = dialogView.findViewById(R.id.breedInput);
        TextInputEditText birthDateInput = dialogView.findViewById(R.id.birthDateInput);
        TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);

        // Image selection components
        dialogPreviewImage = dialogView.findViewById(R.id.previewImage);
        MaterialButton selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        // Reset the temp path
        tempSelectedImagePath = null;

        // Set up image selection button
        selectImageButton.setOnClickListener(v -> {
            checkGalleryPermission();
            if (hasGalleryPermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        // Set up date picker for birth date
        birthDateInput.setOnClickListener(v -> {
            showDatePickerDialog(birthDateInput);
        });

        // Create and show the dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Add New Pet")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Validate inputs
                    if (nameInput.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add validation for birth date
                    if (birthDateInput.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Please enter a birth date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create and save the new pet
                    Pet newPet = new Pet();
                    newPet.setName(nameInput.getText().toString().trim());
                    newPet.setBreed(breedInput.getText().toString().trim());
                    newPet.setProfilePicture(tempSelectedImagePath);

                    // Parse birth date
                    try {
                        String birthDateStr = birthDateInput.getText().toString().trim();
                        newPet.setBirthDate(dateFormat.parse(birthDateStr));
                    } catch (ParseException e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Parse initial weight
                    try {
                        String weightStr = weightInput.getText().toString().trim();
                        if (!weightStr.isEmpty()) {
                            newPet.setInitialWeight(Float.parseFloat(weightStr));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if not a valid number
                    }

                    // Parse initial height
                    try {
                        String heightStr = heightInput.getText().toString().trim();
                        if (!heightStr.isEmpty()) {
                            newPet.setInitialHeight(Float.parseFloat(heightStr));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if not a valid number
                    }

                    // Save the new pet to the database
                    petViewModel.insert(newPet);
                    int petId = newPet.getPetId();

                    // Create an initial growth entry if weight is provided
                    if (newPet.getInitialWeight() > 0) {
                        GrowthEntry initialGrowth = new GrowthEntry();
                        initialGrowth.setPetId((int)petId);
                        initialGrowth.setEntryDate(newPet.getBirthDate());
                        initialGrowth.setWeight(newPet.getInitialWeight());
                        initialGrowth.setHeight(newPet.getInitialHeight());
                        initialGrowth.setLength(0); // Default value
                        initialGrowth.setCreatedAt(new Date());
                        initialGrowth.setNotes("Initial measurement");

                        growthEntryViewModel.insert(initialGrowth);
                    }

                    Toast.makeText(this, "Pet added successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null);

// Get the dialog before showing it to customize window
        Dialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }
        dialog.show();

    }

    private void showEditPetDialog(Pet pet) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pet, null);

        // Get references to the input fields
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText breedInput = dialogView.findViewById(R.id.breedInput);
        TextInputEditText birthDateInput = dialogView.findViewById(R.id.birthDateInput);
        TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);

        // Image selection components
        dialogPreviewImage = dialogView.findViewById(R.id.previewImage);
        MaterialButton selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        // Initialize temp path with the current pet's image path
        tempSelectedImagePath = pet.getProfilePicture();

        // Fill the fields with existing pet data
        nameInput.setText(pet.getName());
        breedInput.setText(pet.getBreed());

        if (pet.getBirthDate() != null) {
            birthDateInput.setText(dateFormat.format(pet.getBirthDate()));
        }

        if (pet.getInitialWeight() > 0) {
            weightInput.setText(String.valueOf(pet.getInitialWeight()));
        }

        if (pet.getInitialHeight() > 0) {
            heightInput.setText(String.valueOf(pet.getInitialHeight()));
        }

        // Load existing image if available
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getProfilePicture());
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.dogpic)
                        .error(R.drawable.dogpic)
                        .centerCrop()
                        .into(dialogPreviewImage);
            } catch (Exception e) {
                // If there's an error, load a placeholder
                dialogPreviewImage.setImageResource(R.drawable.dogpic);
            }
        }

        // Set up image selection button
        selectImageButton.setOnClickListener(v -> {
            checkGalleryPermission();
            if (hasGalleryPermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        // Set up date picker for birth date
        birthDateInput.setOnClickListener(v -> {
            showDatePickerDialog(birthDateInput);
        });

        // Create dialog builder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Edit Pet")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Validate inputs
                    if (nameInput.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (birthDateInput.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Please enter a birth date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update the pet with new values
                    pet.setName(nameInput.getText().toString().trim());
                    pet.setBreed(breedInput.getText().toString().trim());
                    pet.setProfilePicture(tempSelectedImagePath);
                    pet.setUpdatedAt(new Date());

                    // Parse birth date
                    try {
                        String birthDateStr = birthDateInput.getText().toString().trim();
                        pet.setBirthDate(dateFormat.parse(birthDateStr));
                    } catch (ParseException e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Parse weight
                    try {
                        String weightStr = weightInput.getText().toString().trim();
                        if (!weightStr.isEmpty()) {
                            pet.setInitialWeight(Float.parseFloat(weightStr));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if not a valid number
                    }

                    // Parse height
                    try {
                        String heightStr = heightInput.getText().toString().trim();
                        if (!heightStr.isEmpty()) {
                            pet.setInitialHeight(Float.parseFloat(heightStr));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if not a valid number
                    }

                    // Save the updated pet to the database
                    petViewModel.update(pet);

                    Toast.makeText(this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", (dialog, which) -> {
                    // Confirm deletion
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Delete Pet")
                            .setMessage("Are you sure you want to delete " + pet.getName() + "?")
                            .setPositiveButton("Delete", (dialogInterface, i) -> {
                                petViewModel.delete(pet);
                                Toast.makeText(this, "Pet deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

        // Create dialog and customize window
        Dialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }

        // Show dialog
        alertDialog.show();
    }

    private void showDatePickerDialog(final TextInputEditText dateField) {
        Calendar calendar = Calendar.getInstance();

        // If there's an existing date, parse it
        try {
            String currentDate = dateField.getText().toString().trim();
            if (!currentDate.isEmpty()) {
                Date date = dateFormat.parse(currentDate);
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            // Use current date if parsing fails
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        dateField.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                year, month, day
        );

        // Set the maximum date as today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    // Image selection methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            // For Android 10+ (API 29+), persist permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                } catch (SecurityException e) {
                    // Handle exception
                }
            }

            // Store the image URI as a string
            tempSelectedImagePath = selectedImage.toString();

            // Update preview image if it exists
            if (dialogPreviewImage != null) {
                Glide.with(this)
                        .load(selectedImage)
                        .centerCrop()
                        .into(dialogPreviewImage);
            }
        }
    }

    // Permission handling
    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void checkGalleryPermission() {
        if (!hasGalleryPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            } else {
                Toast.makeText(this, "Permission required to select images",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}