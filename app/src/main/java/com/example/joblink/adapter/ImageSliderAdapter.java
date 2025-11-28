package com.example.joblink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.joblink.R;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private List<String> imageUrls;
    private OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ImageSliderAdapter(List<String> imageUrls, OnImageClickListener onImageClickListener) {
        this.imageUrls = imageUrls;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .placeholder(R.color.gray)
                .error(R.color.gray)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewSlider);
        }
    }
}