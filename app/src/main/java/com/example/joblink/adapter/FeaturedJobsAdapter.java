package com.example.joblink.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.model.Post;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FeaturedJobsAdapter extends RecyclerView.Adapter<FeaturedJobsAdapter.ViewHolder> {
    private final Context context;
    private final List<Post> posts;
    private final OnItemClickListener itemClickListener;
    private final OnBookmarkClickListener bookmarkClickListener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Post post, ImageButton bookmarkButton);
    }

    public FeaturedJobsAdapter(Context context, List<Post> posts, OnItemClickListener itemClickListener, OnBookmarkClickListener bookmarkClickListener) {
        this.context = context;
        this.posts = posts;
        this.itemClickListener = itemClickListener;
        this.bookmarkClickListener = bookmarkClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_card1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.postTitle.setText(post.getTitle());
        holder.salary.setText(post.getSalary());
        holder.location.setText(post.getProvince()); // Changed from getLocation()
        holder.postTime.setText(getTimeAgo((Long) post.getTimestamp()));
        holder.bookmarkButton.setSelected(post.isBookmarked());

        if (post.getImages() != null && !post.getImages().isEmpty()) {
            int coverIndex = post.getCoverImageIndex();
            if (coverIndex < 0 || coverIndex >= post.getImages().size()) {
                coverIndex = 0;
            }

            Glide.with(context)
                    .load(post.getImages().get(coverIndex))
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(holder.postImage);
        } else {
            holder.postImage.setImageResource(R.drawable.img);
        }

        if (post.getEmployerId() != null && !post.getEmployerId().isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(post.getEmployerId());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String photoURL = snapshot.child("photoURL").getValue(String.class);

                        holder.username.setText(username != null ? username : "Unknown User");

                        if (photoURL != null && !photoURL.isEmpty()) {
                            Glide.with(context)
                                    .load(photoURL)
                                    .placeholder(R.drawable.img)
                                    .error(R.drawable.img)
                                    .circleCrop()
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.img);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.username.setText("Unknown User");
                    holder.profileImage.setImageResource(R.drawable.img);
                }
            });
        } else {
            holder.username.setText("Unknown User");
            holder.profileImage.setImageResource(R.drawable.img);
        }

        if (holder.jobDetailsLayout.getChildCount() >= 5) {
            TextView businessTypeText = (TextView) holder.jobDetailsLayout.getChildAt(0);
            TextView workplaceTypeText = (TextView) holder.jobDetailsLayout.getChildAt(2);
            TextView experienceText = (TextView) holder.jobDetailsLayout.getChildAt(4);

            businessTypeText.setText(post.getBusinessType() != null ? post.getBusinessType() : "N/A");
            workplaceTypeText.setText(post.getWorkPlaceType() != null ? post.getWorkPlaceType() : "N/A");
            experienceText.setText(post.getExperience() != null ? post.getExperience() : "N/A");
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(post);
        });

        holder.bookmarkButton.setOnClickListener(v -> {
            if (bookmarkClickListener != null) bookmarkClickListener.onBookmarkClick(post, holder.bookmarkButton);
        });
    }

    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + (days > 1 ? " days" : " day") + " ago";
        if (hours > 0) return hours + (hours > 1 ? " hours" : " hour") + " ago";
        if (minutes > 0) return minutes + (minutes > 1 ? " minutes" : " minute") + " ago";
        return "Just now";
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView postImage, profileImage;
        ImageButton bookmarkButton;
        TextView postTitle, salary, location, username, postTime;
        LinearLayout jobDetailsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
            bookmarkButton = itemView.findViewById(R.id.bookmark_button);
            profileImage = itemView.findViewById(R.id.profile);
            postTitle = itemView.findViewById(R.id.post_title);
            salary = itemView.findViewById(R.id.salary);
            location = itemView.findViewById(R.id.textView4);
            username = itemView.findViewById(R.id.username);
            postTime = itemView.findViewById(R.id.postTime);
            jobDetailsLayout = itemView.findViewById(R.id.linearLayout5);
        }
    }
}
