package com.example.joblink.fragment;

import android.app.AlertDialog;import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.adapter.PostAdapter;
import com.example.joblink.model.Post;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class BookmarksFragment extends Fragment implements PostAdapter.OnItemClickListener, PostAdapter.OnBookmarkClickListener, PostAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> allBookmarkedPosts;
    private List<Post> filteredBookmarkedPosts;
    private Set<String> bookmarkedPostIds;

    private DatabaseReference bookmarksRef;
    private DatabaseReference postsRef;
    private FirebaseUser currentUser;
    private LinearLayout emptyStateLayout;

    private List<MaterialButton> filterButtons;
    private MaterialButton selectedFilterButton;

    private MaterialButton btnDateAdded, btnTitleAsc, btnTitleDesc;
    private List<MaterialButton> sortButtons;
    private MaterialButton selectedSortButton;

    private MaterialButton editButton;
    private boolean isEditMode = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.myPostRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        allBookmarkedPosts = new ArrayList<>();
        filteredBookmarkedPosts = new ArrayList<>();
        bookmarkedPostIds = new HashSet<>();

        postAdapter = new PostAdapter(getContext(), filteredBookmarkedPosts, false, this, this, this);
        recyclerView.setAdapter(postAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to see bookmarks.", Toast.LENGTH_SHORT).show();
            showEmptyState(true);
            return;
        }

        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        bookmarksRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("bookmarks");

        editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> toggleEditMode());

        setupFilterButtons(view);
        setupSortButtons(view);
        fetchBookmarkedPostIds();
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            editButton.setText("Done");
        } else {
            editButton.setText("Edit");
        }
        postAdapter.setEditMode(isEditMode);
    }

    private void showDeleteConfirmationDialog(Post post) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Bookmark")
                .setMessage("Are you sure you want to remove this job from your bookmarks?")
                .setPositiveButton("Remove", (dialog, which) -> removeBookmark(post))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeBookmark(Post post) {
        if (currentUser != null && post.getPostId() != null) {
            bookmarksRef.child(post.getPostId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Bookmark removed.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove bookmark.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDeleteClick(Post post) {
        showDeleteConfirmationDialog(post);
    }

    private void setupFilterButtons(View view) {
        filterButtons = new ArrayList<>();
        filterButtons.add(view.findViewById(R.id.btnAll));
        filterButtons.add(view.findViewById(R.id.btnFullTime));
        filterButtons.add(view.findViewById(R.id.btnPartTime));
        filterButtons.add(view.findViewById(R.id.btnInternship));
        filterButtons.add(view.findViewById(R.id.btnFreelance));
        filterButtons.add(view.findViewById(R.id.btnVolunteer));
        filterButtons.add(view.findViewById(R.id.btnOther));

        selectedFilterButton = filterButtons.get(0);

        for (MaterialButton button : filterButtons) {
            button.setOnClickListener(v -> {
                String filterText = button.getText().toString();
                updateFilterButtonStyles(button);
                applyFilter(filterText);
            });
        }
    }

    private void setupSortButtons(View view) {
        sortButtons = new ArrayList<>();
        btnDateAdded = view.findViewById(R.id.btnDateAdded);
        btnTitleAsc = view.findViewById(R.id.btnTitleAscending);
        btnTitleDesc = view.findViewById(R.id.btnTitleDescending);

        sortButtons.add(btnDateAdded);
        sortButtons.add(btnTitleAsc);
        sortButtons.add(btnTitleDesc);

        selectedSortButton = btnDateAdded;

        btnDateAdded.setOnClickListener(v -> {
            updateSortButtonStyles(btnDateAdded);
            sortPosts("Date");
        });
        btnTitleAsc.setOnClickListener(v -> {
            updateSortButtonStyles(btnTitleAsc);
            sortPosts("TitleAsc");
        });
        btnTitleDesc.setOnClickListener(v -> {
            updateSortButtonStyles(btnTitleDesc);
            sortPosts("TitleDesc");
        });
    }

    private void applyFilter(String filter) {
        filteredBookmarkedPosts.clear();
        if (filter.equalsIgnoreCase("All")) {
            filteredBookmarkedPosts.addAll(allBookmarkedPosts);
        } else {
            for (Post post : allBookmarkedPosts) {
                if (post.getJobCategory() != null && post.getJobCategory().equalsIgnoreCase(filter)) {
                    filteredBookmarkedPosts.add(post);
                }
            }
        }
        postAdapter.notifyDataSetChanged();
        showEmptyState(filteredBookmarkedPosts.isEmpty());
    }

    private void updateFilterButtonStyles(MaterialButton clickedButton) {
        for (MaterialButton button : filterButtons) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setTextColor(ContextCompat.getColor(getContext(), R.color.text_ico));
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray)));
            button.setStrokeWidth(2);
        }

        clickedButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.blue)));
        clickedButton.setTextColor(Color.WHITE);
        clickedButton.setStrokeWidth(0);

        selectedFilterButton = clickedButton;
    }

    private void updateSortButtonStyles(MaterialButton clickedButton) {
        for (MaterialButton button : sortButtons) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setTextColor(ContextCompat.getColor(getContext(), R.color.text_ico));
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray)));
            button.setStrokeWidth(2);
        }

        clickedButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.blue)));
        clickedButton.setTextColor(Color.WHITE);
        clickedButton.setStrokeWidth(0);

        selectedSortButton = clickedButton;
    }

    private void fetchBookmarkedPostIds() {
        bookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookmarkedPostIds.clear();
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    allBookmarkedPosts.clear();
                    filteredBookmarkedPosts.clear();
                    postAdapter.notifyDataSetChanged();
                    showEmptyState(true);
                    return;
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    if (postSnapshot.exists() && postSnapshot.getValue(Boolean.class) != null && postSnapshot.getValue(Boolean.class)) {
                        bookmarkedPostIds.add(postSnapshot.getKey());
                    }
                }

                if (bookmarkedPostIds.isEmpty()) {
                    showEmptyState(true);
                } else {
                    fetchFullPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmptyState(true);
            }
        });
    }

    private void fetchFullPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBookmarkedPosts.clear();
                for (DataSnapshot postDataSnapshot : snapshot.getChildren()) {
                    if (bookmarkedPostIds.contains(postDataSnapshot.getKey())) {
                        Post post = postDataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(postDataSnapshot.getKey());
                            post.setBookmarked(true);
                            allBookmarkedPosts.add(post);
                        }
                    }
                }

                if (allBookmarkedPosts.isEmpty()) {
                    filteredBookmarkedPosts.clear();
                    postAdapter.notifyDataSetChanged();
                    showEmptyState(true);
                } else {
                    sortPosts("Date");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch posts.", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void sortPosts(String criteria) {
        if ("Date".equals(criteria)) {
            allBookmarkedPosts.sort(Comparator.comparing(Post::getTimestamp).reversed());
        } else if ("TitleAsc".equals(criteria)) {
            allBookmarkedPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else if ("TitleDesc".equals(criteria)) {
            allBookmarkedPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
        }
        applyFilter(selectedFilterButton.getText().toString());
    }

    private void showEmptyState(boolean show) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Post post) {
        if (isEditMode) return;

        if (post != null && post.getPostId() != null && isAdded()) {
            PostDetailFragment detailFragment = new PostDetailFragment();
            Bundle args = new Bundle();
            args.putString("postId", post.getPostId());
            detailFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.mainFragment, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBookmarkClick(Post post, ImageButton bookmarkButton) {
    }
}
