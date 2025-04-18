package com.example.taskperf1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;


public class VaccineTrackerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_tracker);


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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


        MaterialButton viewAllButton = findViewById(R.id.viewAllVaccinesButton);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VaccineTrackerActivity.this, "View all vaccines clicked", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void showAddVaccineDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_vaccine);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);
            window.setAttributes(layoutParams);
        }


        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextInputEditText vaccineTypeInput = dialog.findViewById(R.id.vaccineTypeInput);
        TextInputEditText dateInput = dialog.findViewById(R.id.dateInput);
        TextInputEditText nextDueDateInput = dialog.findViewById(R.id.nextDueDateInput);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vaccineTypeInput.getText().toString().isEmpty() ||
                        dateInput.getText().toString().isEmpty() ||
                        nextDueDateInput.getText().toString().isEmpty()) {

                    Toast.makeText(VaccineTrackerActivity.this,
                            "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(VaccineTrackerActivity.this,
                        "Vaccine record saved successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }



}