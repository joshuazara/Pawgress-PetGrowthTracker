package com.example.taskperf1;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HeatTrackerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_tracker);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ImageButton addHeatCycleButton = findViewById(R.id.addHeatCycleButton);
        FloatingActionButton addHeatCycleFab = findViewById(R.id.addHeatCycleFab);

        View.OnClickListener addHeatCycleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddHeatCycleDialog();
            }
        };

        addHeatCycleButton.setOnClickListener(addHeatCycleClickListener);
        addHeatCycleFab.setOnClickListener(addHeatCycleClickListener);


        MaterialButton viewAllButton = findViewById(R.id.viewAllCyclesButton);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HeatTrackerActivity.this, "View all heat cycles clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddHeatCycleDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_heat_cycle);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);  // 95% of screen width
            window.setAttributes(layoutParams);
        }


        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HeatTrackerActivity.this,
                        "Heat cycle saved successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}