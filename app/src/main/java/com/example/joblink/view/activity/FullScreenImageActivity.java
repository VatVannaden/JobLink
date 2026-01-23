package com.example.joblink.view.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.Locale;

public class FullScreenImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<String> imageUrls;
    private int currentPosition;
    private TextView textViewPhotoCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        viewPager = findViewById(R.id.viewPagerFullScreen);
        ImageView closeButton = findViewById(R.id.closeButton);
        textViewPhotoCounter = findViewById(R.id.currentPhoto);

        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        currentPosition = getIntent().getIntExtra("position", 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            Toast.makeText(this, "No images to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewPhotoCounter.setVisibility(View.VISIBLE);
        updatePhotoCounter(currentPosition);

        viewPager.setAdapter(new SimpleImageAdapter(imageUrls));
        viewPager.setCurrentItem(currentPosition, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updatePhotoCounter(position);
            }
        });

        closeButton.setOnClickListener(v -> finish());
    }

    private void updatePhotoCounter(int position) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String counterText = String.format(Locale.getDefault(), "%d / %d", position + 1, imageUrls.size());
            textViewPhotoCounter.setText(counterText);
        }
    }

    private static class SimpleImageAdapter extends RecyclerView.Adapter<SimpleImageAdapter.ViewHolder> {
        private final List<String> imageUrls;

        public SimpleImageAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PhotoView photoView = new PhotoView(parent.getContext());
            photoView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new ViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.photoView.getContext())
                    .load(imageUrls.get(position))
                    .into(holder.photoView);
        }

        @Override
        public int getItemCount() {
            return imageUrls != null ? imageUrls.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final PhotoView photoView;

            ViewHolder(PhotoView imageView) {
                super(imageView);
                this.photoView = imageView;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
