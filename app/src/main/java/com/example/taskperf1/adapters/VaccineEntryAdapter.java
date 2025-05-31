package com.example.taskperf1.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskperf1.R;
import com.example.taskperf1.database.VaccineEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VaccineEntryAdapter extends RecyclerView.Adapter<VaccineEntryAdapter.VaccineEntryViewHolder> {

    private List<VaccineEntry> vaccineEntries = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final boolean isUpcoming;  

    public VaccineEntryAdapter(Context context, boolean isUpcoming) {
        this.context = context;
        this.isUpcoming = isUpcoming;
    }

    @NonNull
    @Override
    public VaccineEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (isUpcoming) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_upcoming_vaccine, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vaccine_history, parent, false);
        }
        return new VaccineEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineEntryViewHolder holder, int position) {
        VaccineEntry currentEntry = vaccineEntries.get(position);

        
        holder.vaccineNameTextView.setText(currentEntry.getVaccineType());

        if (isUpcoming) {
            
            if (currentEntry.getNextDueDate() != null) {
                holder.dueDateTextView.setText("Due: " + dateFormat.format(currentEntry.getNextDueDate()));

                
                long daysLeft = getDaysLeft(currentEntry.getNextDueDate());
                holder.daysLeftTextView.setText(daysLeft + " days left");

                
                if (daysLeft <= 7) {
                    holder.daysLeftTextView.setTextColor(Color.parseColor("#E53935")); 
                    if (holder.statusIndicator != null) {
                        holder.statusIndicator.setBackgroundColor(Color.parseColor("#E53935"));
                    }
                } else if (daysLeft <= 30) {
                    holder.daysLeftTextView.setTextColor(Color.parseColor("#FFC107")); 
                    if (holder.statusIndicator != null) {
                        holder.statusIndicator.setBackgroundColor(Color.parseColor("#FFC107"));
                    }
                } else {
                    holder.daysLeftTextView.setTextColor(ContextCompat.getColor(context, R.color.brand_green));
                    if (holder.statusIndicator != null) {
                        holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context,R.color.brand_green));
                    }
                }
            }

            
            if (holder.reminderIcon != null) {
                holder.reminderIcon.setVisibility(currentEntry.isReminderSet() ? View.VISIBLE : View.GONE);
            }
        } else {
            
            if (currentEntry.getAdministeredDate() != null) {
                holder.adminDateTextView.setText(dateFormat.format(currentEntry.getAdministeredDate()));
            }

            
            String vetInfo = "";
            if (currentEntry.getVeterinarian() != null && !currentEntry.getVeterinarian().isEmpty()) {
                vetInfo = "Dr. " + currentEntry.getVeterinarian();

                if (currentEntry.getClinic() != null && !currentEntry.getClinic().isEmpty()) {
                    vetInfo += " • " + currentEntry.getClinic();
                }
            } else if (currentEntry.getClinic() != null && !currentEntry.getClinic().isEmpty()) {
                vetInfo = currentEntry.getClinic();
            }

            if (!vetInfo.isEmpty() && holder.vetInfoTextView != null) {
                holder.vetInfoTextView.setText(vetInfo);
                holder.vetInfoTextView.setVisibility(View.VISIBLE);
            } else if (holder.vetInfoTextView != null) {
                holder.vetInfoTextView.setVisibility(View.GONE);
            }

            
            if (currentEntry.getNextDueDate() != null && holder.nextDueTextView != null) {
                holder.nextDueTextView.setText("Next due: " + dateFormat.format(currentEntry.getNextDueDate()));
                holder.nextDueTextView.setVisibility(View.VISIBLE);
            } else if (holder.nextDueTextView != null) {
                holder.nextDueTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return vaccineEntries.size();
    }

    public void setVaccineEntries(List<VaccineEntry> vaccineEntries) {
        this.vaccineEntries = vaccineEntries;
        notifyDataSetChanged();
    }

    private long getDaysLeft(Date dueDate) {
        Calendar due = Calendar.getInstance();
        due.setTime(dueDate);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffMillis = due.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    class VaccineEntryViewHolder extends RecyclerView.ViewHolder {
        private TextView vaccineNameTextView;
        private TextView dueDateTextView;
        private TextView daysLeftTextView;
        private TextView adminDateTextView;
        private TextView vetInfoTextView;
        private TextView nextDueTextView;
        private ImageView reminderIcon;
        private View statusIndicator;

        VaccineEntryViewHolder(View itemView) {
            super(itemView);

            
            vaccineNameTextView = itemView.findViewById(R.id.vaccineName);

            if (isUpcoming) {
                
                dueDateTextView = itemView.findViewById(R.id.vaccineDueDate);
                daysLeftTextView = itemView.findViewById(R.id.daysLeft);
                reminderIcon = itemView.findViewById(R.id.reminderIcon);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            } else {
                
                adminDateTextView = itemView.findViewById(R.id.vaccineDate);
                vetInfoTextView = itemView.findViewById(R.id.vaccineVetInfo);
                nextDueTextView = itemView.findViewById(R.id.vaccineNextDue);
            }
        }
    }
}