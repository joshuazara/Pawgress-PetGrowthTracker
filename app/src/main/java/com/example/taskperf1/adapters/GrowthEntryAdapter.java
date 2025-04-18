package com.example.taskperf1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.R;
import com.example.taskperf1.database.GrowthEntry;
import com.example.taskperf1.database.Pet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GrowthEntryAdapter extends RecyclerView.Adapter<GrowthEntryAdapter.GrowthEntryViewHolder> {

    private List<GrowthEntry> growthEntries = new ArrayList<>();
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private Pet currentPet;

    public GrowthEntryAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GrowthEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_growth_entry, parent, false);
        return new GrowthEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GrowthEntryViewHolder holder, int position) {
        GrowthEntry currentEntry = growthEntries.get(position);

        // Set entry date
        holder.dateTextView.setText(dateFormat.format(currentEntry.getEntryDate()));

        // Calculate and set age if pet's birth date is available
        if (holder.ageTextView != null && currentPet != null && currentPet.getBirthDate() != null) {
            // Calculate pet's age at the time of measurement
            long ageInDays = (currentEntry.getEntryDate().getTime() - currentPet.getBirthDate().getTime())
                    / (1000 * 60 * 60 * 24);

            if (ageInDays >= 365) {
                int years = (int) (ageInDays / 365);
                holder.ageTextView.setText("Age: " + years + (years == 1 ? " year" : " years"));
            } else if (ageInDays >= 30) {
                int months = (int) (ageInDays / 30);
                holder.ageTextView.setText("Age: " + months + (months == 1 ? " month" : " months"));
            } else {
                holder.ageTextView.setText("Age: " + ageInDays + (ageInDays == 1 ? " day" : " days"));
            }
        } else if (holder.ageTextView != null) {
            holder.ageTextView.setText("Age: Unknown");
        }

        // Set weight, height, and length values
        holder.weightTextView.setText(String.format(Locale.getDefault(), "%.1f kg", currentEntry.getWeight()));
        holder.heightTextView.setText(String.format(Locale.getDefault(), "%.1f cm", currentEntry.getHeight()));
        holder.lengthTextView.setText(String.format(Locale.getDefault(), "%.1f cm", currentEntry.getLength()));

        // Set notes if available
        if (holder.notesTextView != null && currentEntry.getNotes() != null && !currentEntry.getNotes().isEmpty()) {
            holder.notesTextView.setText(currentEntry.getNotes());
            holder.notesTextView.setVisibility(View.VISIBLE);
        } else if (holder.notesTextView != null) {
            holder.notesTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return growthEntries.size();
    }

    public void setGrowthEntries(List<GrowthEntry> growthEntries) {
        this.growthEntries = growthEntries;
        notifyDataSetChanged();
    }

    public void setPet(Pet pet) {
        this.currentPet = pet;
        notifyDataSetChanged(); // Refresh display to update age calculations
    }

    class GrowthEntryViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private TextView ageTextView;
        private TextView weightTextView;
        private TextView heightTextView;
        private TextView lengthTextView;
        private TextView notesTextView;

        GrowthEntryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateText);
            ageTextView = itemView.findViewById(R.id.ageText);
            weightTextView = itemView.findViewById(R.id.weightValue);
            heightTextView = itemView.findViewById(R.id.heightValue);
            lengthTextView = itemView.findViewById(R.id.lengthValue);
            notesTextView = itemView.findViewById(R.id.notesText);
        }
    }
}