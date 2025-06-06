package com.example.taskperf1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.taskperf1.database.Pet;
import com.example.taskperf1.viewmodels.PetViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

public class HomePageActivity extends AppCompatActivity {

    private PetViewModel petViewModel;
    private int currentPetId;

    
    private ImageView petImage;
    private TextView petNameText;
    private TextView petBreedText;
    private TextView petTipText;
    private MaterialCardView heatTrackerCard;
    private MaterialCardView galleryCard;

    
    private String[] petCareTips = {
            "Regular exercise is important for your pet's physical and mental health. Aim for at least 30 minutes daily.",
            "Dental care is crucial - try to brush your pet's teeth several times a week to prevent dental disease.",
            "Always keep fresh water available for your pet.",
            "Ensure your pet is up to date on all vaccinations and preventive care.",
            "Regular grooming helps keep your pet's coat healthy and reduces shedding.",
            "Make sure your pet has a balanced diet appropriate for their age, size, and health status.",
            "Provide mental stimulation with toys and activities to prevent boredom.",
            "Regular wellness check-ups with your veterinarian are essential for preventive care.",
            "Maintain a consistent feeding schedule for your pet.",
            "Keep your pet at a healthy weight to prevent health issues."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        
        currentPetId = getIntent().getIntExtra("pet_id", -1);

        if (currentPetId == -1) {
            Toast.makeText(this, "Error: No pet selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);

        
        petImage = findViewById(R.id.petImage);
        petNameText = findViewById(R.id.petNameText);
        petBreedText = findViewById(R.id.petBreedText);
        petTipText = findViewById(R.id.petTipText);
        heatTrackerCard = findViewById(R.id.heatTrackerCard);
        galleryCard = findViewById(R.id.galleryCard);

        
        petTipText.setText(getRandomPetCareTip());

        
        petViewModel.getPetById(currentPetId).observe(this, pet -> {
            if (pet != null) {
                updatePetInfo(pet);

                
                if (pet.getGender() != null && pet.getGender().equals("Female")) {
                    heatTrackerCard.setVisibility(View.VISIBLE);
                } else {
                    heatTrackerCard.setVisibility(View.GONE);
                }
            }
        });

        
        setupCardClickListeners();
    }

    private void updatePetInfo(Pet pet) {
        petNameText.setText(pet.getName());

        
        String breedGenderAge = "";
        if (pet.getBreed() != null && !pet.getBreed().isEmpty()) {
            breedGenderAge = pet.getBreed();
        }

        
        if (pet.getGender() != null && !pet.getGender().isEmpty()) {
            if (!breedGenderAge.isEmpty()) {
                breedGenderAge += " • ";
            }
            breedGenderAge += pet.getGender();

            
            if (pet.getGender().equals("Female")) {
                heatTrackerCard.setVisibility(View.VISIBLE);
            } else {
                heatTrackerCard.setVisibility(View.GONE);
            }
        } else {
            
            heatTrackerCard.setVisibility(View.GONE);
        }

        
        if (pet.getBirthDate() != null) {
            long ageInMillis = System.currentTimeMillis() - pet.getBirthDate().getTime();
            long ageInDays = ageInMillis / (1000 * 60 * 60 * 24);

            if (!breedGenderAge.isEmpty()) {
                breedGenderAge += " • ";
            }

            if (ageInDays < 30) {
                breedGenderAge += ageInDays + " days";
            } else if (ageInDays < 365) {
                int months = (int) (ageInDays / 30);
                breedGenderAge += months + (months == 1 ? " month" : " months");
            } else {
                int years = (int) (ageInDays / 365);
                breedGenderAge += years + (years == 1 ? " year" : " years");
            }
        }

        petBreedText.setText(breedGenderAge);

        
        if (pet.getProfilePicture() != null && !pet.getProfilePicture().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getProfilePicture());
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

    private void setupCardClickListeners() {
        MaterialCardView growthTrackerCard = findViewById(R.id.growthTrackerCard);
        growthTrackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, GrowthTrackerActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });

        MaterialCardView vaccineTrackerCard = findViewById(R.id.vaccineTrackerCard);
        vaccineTrackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, VaccineTrackerActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });

        heatTrackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, HeatTrackerActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });

        MaterialCardView cameraCard = findViewById(R.id.cameraCard);
        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, CameraActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });

        galleryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, GalleryActivity.class);
                intent.putExtra("pet_id", currentPetId);
                startActivity(intent);
            }
        });
    }

    private String getRandomPetCareTip() {
        Random random = new Random();
        return petCareTips[random.nextInt(petCareTips.length)];
    }
}