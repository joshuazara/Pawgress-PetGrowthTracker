package com.example.taskperf1.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.taskperf1.PhotoDetailActivity;
import com.example.taskperf1.R;
import com.example.taskperf1.database.PhotoEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<PhotoEntry> photos = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final Context context;

    public PhotoAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoEntry photo = photos.get(position);

        // Load image with Glide
        try {
            Uri photoUri = Uri.parse(photo.getFilePath());
            Glide.with(holder.itemView.getContext())
                    .load(photoUri)
                    .centerCrop()
                    .into(holder.photoImageView);
        } catch (Exception e) {
            // If loading fails, show placeholder
            holder.photoImageView.setImageResource(R.drawable.ic_camera);
        }

        // Set date
        holder.dateTextView.setText(dateFormat.format(photo.getCaptureDate()));

        // Setup click listener to view full photo
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra("photo_id", photo.getPhotoId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setPhotos(List<PhotoEntry> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView dateTextView;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImage);
            dateTextView = itemView.findViewById(R.id.photoDate);
        }
    }
}