package com.example.taskperf1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.R;
import com.example.taskperf1.database.HeatCycle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HeatCycleAdapter extends RecyclerView.Adapter<HeatCycleAdapter.HeatCycleViewHolder> {

    private List<HeatCycle> heatCycles = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    public HeatCycleAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HeatCycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heat_cycle, parent, false);
        return new HeatCycleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HeatCycleViewHolder holder, int position) {
        HeatCycle currentCycle = heatCycles.get(position);

        // Set start date
        if (currentCycle.getStartDate() != null) {
            holder.startDateTextView.setText(dateFormat.format(currentCycle.getStartDate()));
        }

        // Set duration
        holder.durationTextView.setText(currentCycle.getDuration() + " days");

        // Set end date if available
        if (currentCycle.getEndDate() != null) {
            holder.endDateTextView.setText("End: " + dateFormat.format(currentCycle.getEndDate()));
            holder.endDateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.endDateTextView.setVisibility(View.GONE);
        }

        // Set notes if available
        if (currentCycle.getNotes() != null && !currentCycle.getNotes().isEmpty()) {
            holder.notesTextView.setText(currentCycle.getNotes());
            holder.notesTextView.setVisibility(View.VISIBLE);
        } else {
            holder.notesTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return heatCycles.size();
    }

    public void setHeatCycles(List<HeatCycle> heatCycles) {
        this.heatCycles = heatCycles;
        notifyDataSetChanged();
    }
    public List<HeatCycle> getHeatCycles() {
        return this.heatCycles;
    }

    class HeatCycleViewHolder extends RecyclerView.ViewHolder {
        private TextView startDateTextView;
        private TextView durationTextView;
        private TextView endDateTextView;
        private TextView notesTextView;

        HeatCycleViewHolder(View itemView) {
            super(itemView);
            startDateTextView = itemView.findViewById(R.id.cycleStartDate);
            durationTextView = itemView.findViewById(R.id.cycleDuration);
            endDateTextView = itemView.findViewById(R.id.cycleEndDate);
            notesTextView = itemView.findViewById(R.id.cycleNotes);
        }
    }
}