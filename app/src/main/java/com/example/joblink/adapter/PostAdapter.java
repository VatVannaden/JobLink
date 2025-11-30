package com.example.joblink.adapter;

import android.content.Context;
import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.model.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final OnItemClickListener itemClickListener;
    private final OnBookmarkClickListener bookmarkClickListener;
    private final OnDeleteClickListener deleteClickListener;
    private final Context context;
    private final boolean showBookmark;
    private boolean isEditMode = false;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Post post, ImageButton bookmarkButton);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Post post);
    }

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.itemClickListener = null;
        this.bookmarkClickListener = null;
        this.deleteClickListener = null;
        this.showBookmark = true;
    }

    public PostAdapter(Context context, List<Post> postList, OnItemClickListener itemClickListener, OnBookmarkClickListener bookmarkClickListener) {
        this.context = context;
        this.postList = postList;
        this.itemClickListener = itemClickListener;
        this.bookmarkClickListener = bookmarkClickListener;
        this.deleteClickListener = null;
        this.showBookmark = true;
    }

    public PostAdapter(Context context, List<Post> postList, boolean showBookmark, OnItemClickListener itemClickListener, OnBookmarkClickListener bookmarkClickListener) {
        this.context = context;
        this.postList = postList;
        this.showBookmark = showBookmark;
        this.itemClickListener = itemClickListener;
        this.bookmarkClickListener = bookmarkClickListener;
        this.deleteClickListener = null;
    }

    public PostAdapter(Context context, List<Post> postList, boolean showBookmark, OnItemClickListener itemClickListener, OnBookmarkClickListener bookmarkClickListener, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.postList = postList;
        this.showBookmark = showBookmark;
        this.itemClickListener = itemClickListener;
        this.bookmarkClickListener = bookmarkClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_card2, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        ImageButton bookmarkButton, deleteButton;
        TextView postTitle, postLocation, jobType, salary;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
            bookmarkButton = itemView.findViewById(R.id.bookmark_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            postTitle = itemView.findViewById(R.id.post_title);
            postLocation = itemView.findViewById(R.id.textView4);
            jobType = itemView.findViewById(R.id.jobType);
            salary = itemView.findViewById(R.id.salary);
        }

        public void bind(final Post post) {
            postTitle.setText(post.getTitle());
            postLocation.setText(post.getLocation());
            jobType.setText(post.getJobCategory());
            salary.setText(post.getSalary());

            if (post.getImages() != null && !post.getImages().isEmpty()) {
                Glide.with(context)
                        .load(post.getImages().get(0))
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .into(postImage);
            } else {
                postImage.setImageResource(R.drawable.img);
            }

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null && !isEditMode) {
                    itemClickListener.onItemClick(post);
                }
            });

            if (showBookmark) {
                bookmarkButton.setVisibility(View.VISIBLE);
                bookmarkButton.setSelected(post.isBookmarked());
                bookmarkButton.setOnClickListener(v -> {
                    if (bookmarkClickListener != null) {
                        bookmarkClickListener.onBookmarkClick(post, bookmarkButton);
                    }
                });
            } else {
                bookmarkButton.setVisibility(View.GONE);
            }

            if (isEditMode) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(post);
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }
    }
}
