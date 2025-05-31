package com.example.taskperf1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.GrowthEntryViewModel;
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

    private static final String TAG = "ProfileSelectionActivity";
    private GrowthEntryViewModel growthEntryViewModel;
    private PetViewModel petViewModel;
    private LinearLayout petCardsContainer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private String tempSelectedImagePath;
    private ShapeableImageView dialogPreviewImage;

    
    private final String[] genderOptions = {"Male", "Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_selection);
        Log.d(TAG, "Activity created");

        
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);
        growthEntryViewModel = new ViewModelProvider(this).get(GrowthEntryViewModel.class);

        
        petCardsContainer = findViewById(R.id.petCardsContainer);

        
        MaterialButton addProfileButton = findViewById(R.id.addProfileButton);
        addProfileButton.setOnClickListener(v -> showAddPetDialog());

        
        petViewModel.getAllPets().observe(this, pets -> {
            updatePetProfiles(pets);
        });
    }

    private void updatePetProfiles(List<Pet> pets) {
        
        if (petCardsContainer != null) {
            petCardsContainer.removeAllViews();
        }

        if (pets == null || pets.isEmpty()) {
            
            return;
        }

        
        for (Pet pet : pets) {
            addPetCard(pet);
        }
    }

    private void addPetCard(Pet pet) {
        
        View petCardView = getLayoutInflater().inflate(
                R.layout.item_pet_profile, petCardsContainer, false);

        
        MaterialCardView profileCard = petCardView.findViewById(R.id.profileCard);
        ShapeableImageView profileImage = petCardView.findViewById(R.id.profileImage);
        TextView profileName = petCardView.findViewById(R.id.profileName);
        TextView profileBreed = petCardView.findViewById(R.id.profileBreed);
        TextView profileAge = petCardView.findViewById(R.id.profileAge);
        ImageButton editButton = petCardView.findViewById(R.id.editButton);

        
        profileName.setText(pet.getName());
        profileBreed.setText(pet.getBreed());

        
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

        
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getProfilePicture());
                Glide.with(this)
                        .load(imageUri)
                        .centerCrop()
                        .into(profileImage);
            } catch (Exception e) {
                
                Log.e(TAG, "Error loading pet image: " + e.getMessage());
                profileImage.setImageResource(R.drawable.dogpic);
            }
        } else {
            
            profileImage.setImageResource(R.drawable.dogpic);
        }

        
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileSelectionActivity.this, HomePageActivity.class);
            intent.putExtra("pet_id", pet.getPetId());
            startActivity(intent);
        });

        
        editButton.setOnClickListener(v -> {
            showEditPetDialog(pet);
        });

        
        petCardsContainer.addView(petCardView);
    }

    private void showAddPetDialog() {
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pet, null);

        
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText breedInput = dialogView.findViewById(R.id.breedInput);
        TextInputEditText birthDateInput = dialogView.findViewById(R.id.birthDateInput);
        TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);

        
        AutoCompleteTextView genderInput = dialogView.findViewById(R.id.genderInput);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderInput.setAdapter(adapter);
        genderInput.setText(genderOptions[0], false); 

        
        dialogPreviewImage = dialogView.findViewById(R.id.previewImage);
        if (dialogPreviewImage == null) {
            Log.e(TAG, "Failed to find preview image view");
        }
        MaterialButton selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        
        tempSelectedImagePath = null;

        
        selectImageButton.setOnClickListener(v -> {
            checkGalleryPermission();
            if (hasGalleryPermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        
        birthDateInput.setOnClickListener(v -> {
            showDatePickerDialog(birthDateInput);
        });

        
        androidx.appcompat.app.AlertDialog addDialog = new MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Add New Pet")
                .setView(dialogView)
                .create();

        
        if (addDialog.getWindow() != null) {
            addDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }

        
        MaterialButton positiveButton = dialogView.findViewById(R.id.saveButton);
        MaterialButton negativeButton = dialogView.findViewById(R.id.cancelButton);

        
        negativeButton.setOnClickListener(v -> {
            addDialog.dismiss();
        });

        
        positiveButton.setOnClickListener(v -> {
            
            if (nameInput.getText() == null || nameInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            
            if (birthDateInput.getText() == null || birthDateInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please enter a birth date", Toast.LENGTH_SHORT).show();
                return;
            }

            
            if (genderInput.getText() == null || genderInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            
            Pet newPet = new Pet();
            newPet.setName(nameInput.getText().toString().trim());
            newPet.setBreed(breedInput.getText().toString().trim());
            newPet.setGender(genderInput.getText().toString().trim());

            
            if (tempSelectedImagePath != null && !tempSelectedImagePath.isEmpty()) {
                newPet.setProfilePicture(tempSelectedImagePath);
            }

            newPet.setCreatedAt(new Date()); 
            newPet.setUpdatedAt(new Date()); 

            
            try {
                String birthDateStr = birthDateInput.getText().toString().trim();
                newPet.setBirthDate(dateFormat.parse(birthDateStr));
            } catch (ParseException e) {
                Toast.makeText(ProfileSelectionActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            
            try {
                String weightStr = weightInput.getText().toString().trim();
                if (!weightStr.isEmpty()) {
                    newPet.setInitialWeight(Float.parseFloat(weightStr));
                }
            } catch (NumberFormatException e) {
                
                Log.w(TAG, "Invalid weight format: " + e.getMessage());
            }

            
            try {
                String heightStr = heightInput.getText().toString().trim();
                if (!heightStr.isEmpty()) {
                    newPet.setInitialHeight(Float.parseFloat(heightStr));
                }
            } catch (NumberFormatException e) {
                
                Log.w(TAG, "Invalid height format: " + e.getMessage());
            }

            
            new Thread(() -> {
                try {
                    
                    long petId = petViewModel.insertSync(newPet);

                    
                    if (petId > 0) {
                        
                        if (newPet.getInitialWeight() > 0) {
                            try {
                                GrowthEntry initialGrowth = new GrowthEntry();
                                initialGrowth.setPetId((int)petId);
                                initialGrowth.setEntryDate(newPet.getBirthDate());
                                initialGrowth.setWeight(newPet.getInitialWeight());
                                initialGrowth.setHeight(newPet.getInitialHeight());
                                initialGrowth.setLength(0); 
                                initialGrowth.setCreatedAt(new Date());
                                initialGrowth.setNotes("Initial measurement");

                                growthEntryViewModel.insert(initialGrowth);
                            } catch (Exception e) {
                                Log.e(TAG, "Error creating growth entry: " + e.getMessage());
                                
                            }
                        }

                        
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileSelectionActivity.this, "Pet added successfully", Toast.LENGTH_SHORT).show();
                            addDialog.dismiss();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileSelectionActivity.this, "Error adding pet", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving pet: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileSelectionActivity.this, "Error saving pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        
        addDialog.show();
    }

    private void showEditPetDialog(Pet pet) {
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pet, null);

        
        ImageButton deleteButton = dialogView.findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.VISIBLE);

        
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText breedInput = dialogView.findViewById(R.id.breedInput);
        TextInputEditText birthDateInput = dialogView.findViewById(R.id.birthDateInput);
        TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);

        
        AutoCompleteTextView genderInput = dialogView.findViewById(R.id.genderInput);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderInput.setAdapter(adapter);
        if (pet.getGender() != null && !pet.getGender().isEmpty()) {
            genderInput.setText(pet.getGender(), false);
        } else {
            genderInput.setText(genderOptions[0], false);
        }

        
        dialogPreviewImage = dialogView.findViewById(R.id.previewImage);
        MaterialButton selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        
        tempSelectedImagePath = pet.getProfilePicture();

        
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

        
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getProfilePicture());

                
                dialogPreviewImage.setImageTintList(null);

                
                Glide.with(this)
                        .load(imageUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.blankprofilepic)
                        .error(R.drawable.blankprofilepic)
                        .centerCrop()
                        .into(dialogPreviewImage);

                Log.d(TAG, "Loaded existing pet image: " + imageUri);
            } catch (Exception e) {
                Log.e(TAG, "Error loading pet image in dialog: " + e.getMessage());
                dialogPreviewImage.setImageResource(R.drawable.blankprofilepic);
            }
        } else {
            dialogPreviewImage.setImageResource(R.drawable.blankprofilepic);
        }

        
        selectImageButton.setOnClickListener(v -> {
            checkGalleryPermission();
            if (hasGalleryPermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        
        birthDateInput.setOnClickListener(v -> {
            showDatePickerDialog(birthDateInput);
        });

        
        androidx.appcompat.app.AlertDialog editDialog = new MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setView(dialogView)
                .create();
        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }

        
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> editDialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            
            new MaterialAlertDialogBuilder(ProfileSelectionActivity.this)
                    .setTitle("Delete Pet")
                    .setMessage("Are you sure you want to delete " + pet.getName() + "?")
                    .setPositiveButton("Delete", (dialogInterface, i) -> {
                        
                        new Thread(() -> {
                            try {
                                petViewModel.delete(pet);
                                runOnUiThread(() -> {
                                    Toast.makeText(ProfileSelectionActivity.this, "Pet deleted", Toast.LENGTH_SHORT).show();
                                    editDialog.dismiss();
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error deleting pet: " + e.getMessage());
                                runOnUiThread(() -> {
                                    Toast.makeText(ProfileSelectionActivity.this, "Error deleting pet", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        saveButton.setOnClickListener(v -> {
            
            if (nameInput.getText() == null || nameInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (birthDateInput.getText() == null || birthDateInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please enter a birth date", Toast.LENGTH_SHORT).show();
                return;
            }

            
            if (genderInput.getText() == null || genderInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(ProfileSelectionActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                
                String birthDateStr = birthDateInput.getText().toString().trim();
                Date birthDate = dateFormat.parse(birthDateStr);

                
                Pet updatedPet = new Pet();
                updatedPet.setPetId(pet.getPetId());
                updatedPet.setName(nameInput.getText().toString().trim());
                updatedPet.setBreed(breedInput.getText().toString().trim());
                updatedPet.setGender(genderInput.getText().toString().trim());
                updatedPet.setBirthDate(birthDate);
                updatedPet.setUpdatedAt(new Date());
                updatedPet.setProfilePicture(tempSelectedImagePath);

                
                try {
                    String weightStr = weightInput.getText().toString().trim();
                    if (!weightStr.isEmpty()) {
                        updatedPet.setInitialWeight(Float.parseFloat(weightStr));
                    } else {
                        updatedPet.setInitialWeight(pet.getInitialWeight());
                    }
                } catch (NumberFormatException e) {
                    updatedPet.setInitialWeight(pet.getInitialWeight());
                }

                
                try {
                    String heightStr = heightInput.getText().toString().trim();
                    if (!heightStr.isEmpty()) {
                        updatedPet.setInitialHeight(Float.parseFloat(heightStr));
                    } else {
                        updatedPet.setInitialHeight(pet.getInitialHeight());
                    }
                } catch (NumberFormatException e) {
                    updatedPet.setInitialHeight(pet.getInitialHeight());
                }

                
                updatedPet.setInitialLength(pet.getInitialLength());
                updatedPet.setCreatedAt(pet.getCreatedAt());

                
                new Thread(() -> {
                    try {
                        petViewModel.update(updatedPet);
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileSelectionActivity.this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
                            editDialog.dismiss();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating pet: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileSelectionActivity.this, "Error updating pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();

            } catch (ParseException e) {
                Toast.makeText(ProfileSelectionActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error preparing pet update: " + e.getMessage());
                Toast.makeText(ProfileSelectionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        
        editDialog.show();
    }

    private void showDatePickerDialog(final TextInputEditText dateField) {
        Calendar calendar = Calendar.getInstance();

        
        try {
            String currentDate = dateField.getText().toString().trim();
            if (!currentDate.isEmpty()) {
                Date date = dateFormat.parse(currentDate);
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            
            Log.w(TAG, "Error parsing existing date: " + e.getMessage());
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

        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        
        datePickerDialog.show();
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    Log.d(TAG, "Selected image URI: " + selectedImage);

                    
                    tempSelectedImagePath = selectedImage.toString();

                    if (dialogPreviewImage != null) {
                        try {
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                    getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                                } catch (SecurityException e) {
                                    Log.e(TAG, "Failed to get persistent permission: " + e.getMessage());
                                }
                            }

                            
                            dialogPreviewImage.setImageTintList(null);

                            
                            Glide.with(this)
                                    .load(selectedImage)
                                    .centerCrop()
                                    .into(dialogPreviewImage);

                            Log.d(TAG, "Image loading initiated");
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                            dialogPreviewImage.setImageResource(R.drawable.blankprofilepic);
                        }
                    } else {
                        Log.e(TAG, "dialogPreviewImage is null");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onActivityResult: " + e.getMessage());
            }
        }
    }

    
    private void loadImageIntoPreview(Uri imageUri, ShapeableImageView imageView) {
        if (imageUri == null || imageView == null) {
            Log.e(TAG, "Cannot load image: URI or ImageView is null");
            return;
        }

        try {
            
            imageView.setImageTintList(null);

            
            Glide.with(getApplicationContext())
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.blankprofilepic)
                    .error(R.drawable.blankprofilepic)
                    .centerCrop()
                    .into(imageView);

            Log.d(TAG, "Image loaded successfully into preview: " + imageUri);
        } catch (Exception e) {
            Log.e(TAG, "Error in loadImageIntoPreview: " + e.getMessage());
            imageView.setImageResource(R.drawable.blankprofilepic);
        }
    }

    
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