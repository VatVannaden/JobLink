package com.example.joblink.fragment;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.example.joblink.R;
import com.example.joblink.adapter.FeaturedJobsAdapter;
import com.example.joblink.adapter.PostAdapter;
import com.example.joblink.databinding.FragmentHomeBinding;
import com.example.joblink.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {
    private volatile FragmentHomeBinding binding;
    private int selectedCategoryId = -1;
    private String selectedLocation = "Phnom Penh";
    private boolean isCategoriesVisible = true;
    private FeaturedJobsAdapter featuredJobsAdapter;
    private PostAdapter postAdapter;
    private DatabaseReference postsRef;
    private DatabaseReference bookmarksRef;
    private ValueEventListener bookmarksListener;
    private ValueEventListener postsListener;
    private final List<Post> featuredPosts = new ArrayList<>();
    private final List<Post> recentPosts = new ArrayList<>();
    private final List<Post> allRecentPostsMaster = new ArrayList<>();
    private final Set<String> bookmarkedPostIds = new HashSet<>();
    private ValueAnimator animator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupFeaturedJobs();
        setupRecentJobs();
        setupCategoryClickListeners();
        setupLocationClickListener();
        setupScrollListener();
        selectCategory(R.id.all);
        setupFirebaseListeners();
        setupSearchNavigation();
    }

    private void setupLocationClickListener() {
        if (binding == null) return;
        binding.location.setOnClickListener(v -> showLocationDialog());
    }

    private void showLocationDialog() {
        if (getContext() == null) return;

        final CharSequence[] provinces = getResources().getTextArray(R.array.provinces);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select a Location");
        builder.setItems(provinces, (dialog, which) -> {
            if (binding != null) {
                String newLocation = provinces[which].toString();
                binding.currentLocation.setText(newLocation);
                selectedLocation = newLocation;
                filterRecentJobs();
            }
        });
        builder.show();
    }


    private void setupFirebaseListeners() {
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            bookmarksRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("bookmarks");

            bookmarksListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;
                    bookmarkedPostIds.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (Boolean.TRUE.equals(postSnapshot.getValue(Boolean.class))) {
                            bookmarkedPostIds.add(postSnapshot.getKey());
                        }
                    }
                    for (Post post : allRecentPostsMaster) {
                        post.setBookmarked(bookmarkedPostIds.contains(post.getPostId()));
                    }
                    if (postAdapter != null) {
                        postAdapter.notifyDataSetChanged();
                    }
                    if (featuredJobsAdapter != null) {
                        featuredJobsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (binding == null) return;
                    Log.e("HomeFragment", "Failed to read bookmarks.", error.toException());
                }
            };
            bookmarksRef.addValueEventListener(bookmarksListener);
        }

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (binding == null) return;

                allRecentPostsMaster.clear();
                List<Post> allPosts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey());
                        post.setBookmarked(bookmarkedPostIds.contains(post.getPostId()));
                        allPosts.add(post);
                    }
                }

                Collections.reverse(allPosts);
                allRecentPostsMaster.addAll(allPosts);

                featuredPosts.clear();
                for (int i = 0; i < allPosts.size() && i < 3; i++) {
                    featuredPosts.add(allPosts.get(i));
                }

                filterRecentJobs();
                if (featuredJobsAdapter != null) {
                    featuredJobsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (binding == null) return;
                Log.e("HomeFragment", "Failed to read posts.", databaseError.toException());
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        postsRef.addValueEventListener(postsListener);
    }

    private void filterRecentJobs() {
        if (binding == null) return;
        String category = getCategoryStringFromId(selectedCategoryId);
        recentPosts.clear();

        List<Post> filteredPosts = new ArrayList<>();

        if (category.equals("All")) {
            filteredPosts.addAll(allRecentPostsMaster);
        } else {
            for (Post post : allRecentPostsMaster) {
                if (post.getJobCategory() != null && post.getJobCategory().equalsIgnoreCase(category)) {
                    filteredPosts.add(post);
                }
            }
        }

        if (!selectedLocation.equals("Phnom Penh")) {
            List<Post> locationFilteredPosts = new ArrayList<>();
            for (Post post : filteredPosts) {
                if (post.getLocation() != null && post.getLocation().equalsIgnoreCase(selectedLocation)) {
                    locationFilteredPosts.add(post);
                }
            }
            filteredPosts = locationFilteredPosts;
        }

        recentPosts.addAll(filteredPosts);

        if (postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }

    private void selectCategory(int categoryId) {
        if (binding == null || selectedCategoryId == categoryId) return;
        selectedCategoryId = categoryId;
        updateCategoryUnderlines();
        filterRecentJobs();
    }

    private String getCategoryStringFromId(int categoryId) {
        if (categoryId == R.id.fullTime) return "Full time";
        if (categoryId == R.id.partTime) return "Part time";
        if (categoryId == R.id.internship) return "Internship";
        if (categoryId == R.id.volunteer) return "Volunteer";
        if (categoryId == R.id.freelance) return "Freelance";
        if (categoryId == R.id.other) return "Other";
        return "All";
    }

    private void setupCategoryClickListeners() {
        if (binding == null) return;
        binding.all.setOnClickListener(v -> selectCategory(R.id.all));
        binding.fullTime.setOnClickListener(v -> selectCategory(R.id.fullTime));
        binding.partTime.setOnClickListener(v -> selectCategory(R.id.partTime));
        binding.internship.setOnClickListener(v -> selectCategory(R.id.internship));
        binding.volunteer.setOnClickListener(v -> selectCategory(R.id.volunteer));
        binding.freelance.setOnClickListener(v -> selectCategory(R.id.freelance));
        binding.other.setOnClickListener(v -> selectCategory(R.id.other));
    }

    private void updateCategoryUnderlines() {
        if (binding == null) return;
        int[] layoutIds = {R.id.all, R.id.fullTime, R.id.partTime, R.id.internship, R.id.volunteer, R.id.freelance, R.id.other};
        for (int layoutId : layoutIds) {
            LinearLayout layout = binding.getRoot().findViewById(layoutId);
            if (layout != null && layout.getChildCount() > 2) {
                View underline = layout.getChildAt(2);
                if (underline != null) {
                    underline.setVisibility(layoutId == selectedCategoryId ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }
    }

    private void setupFeaturedJobs() {
        if (binding == null) return;
        binding.featuredJobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredJobsAdapter = new FeaturedJobsAdapter(getContext(), featuredPosts, this::navigateToPostDetail, this::toggleBookmark);
        binding.featuredJobsRecyclerView.setAdapter(featuredJobsAdapter);
    }

    private void setupRecentJobs() {
        if (binding == null) return;
        binding.recentJobsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postAdapter = new PostAdapter(getContext(), recentPosts, this::navigateToPostDetail, this::toggleBookmark);
        binding.recentJobsRecyclerView.setAdapter(postAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
        }
        if (bookmarksRef != null && bookmarksListener != null) {
            bookmarksRef.removeEventListener(bookmarksListener);
        }
        if (animator != null) {
            animator.cancel();
        }
        binding = null;
    }

    private void navigateToPostDetail(Post post) {
        if (!isAdded() || post == null || post.getPostId() == null) {
            Log.w("HomeFragment", "Navigation cancelled. Fragment not attached or post invalid.");
            return;
        }
        PostDetailFragment postDetailFragment = new PostDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getPostId());
        postDetailFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainFragment, postDetailFragment)
                .addToBackStack("home")
                .commit();
    }

    private void toggleBookmark(Post post, ImageButton bookmarkButton) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (isAdded())
                Toast.makeText(getContext(), "You must be logged in to bookmark posts.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isAdded() || post == null || post.getPostId() == null) return;

        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("bookmarks")
                .child(post.getPostId());

        boolean newBookmarkState = !post.isBookmarked();
        bookmarkRef.setValue(newBookmarkState).addOnCompleteListener(task -> {
            if (isAdded() && getContext() != null) {
                if (task.isSuccessful()) {
                    String message = newBookmarkState ? "Bookmarked" : "Removed bookmark for";
                    Toast.makeText(getContext(), message + " " + post.getTitle(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update bookmark.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupScrollListener() {
        if (binding == null) return;

        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (binding == null) return;
            int dy = scrollY - oldScrollY;

            if (dy > 15 && scrollY > 50 && isCategoriesVisible) {
                animateCategories(false);
                isCategoriesVisible = false;
            } else if (dy < -15 && scrollY < 100 && !isCategoriesVisible) {
                animateCategories(true);
                isCategoriesVisible = true;
            } else if (scrollY == 0 && !isCategoriesVisible) {
                animateCategories(true);
                isCategoriesVisible = true;
            }
        });
    }

    private void animateCategories(boolean show) {
        if (binding == null || getContext() == null) return;
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        float density = getResources().getDisplayMetrics().density;
        float startHeight = binding.categoryScrollView.getLayoutParams().height;
        float endHeight = show ? (120 * density) : (50 * density);

        animator = ValueAnimator.ofFloat(startHeight, endHeight);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            if (binding == null) return;
            float value = (float) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = binding.categoryScrollView.getLayoutParams();
            params.height = (int) value;
            binding.categoryScrollView.setLayoutParams(params);

            GridLayout grid = binding.categoryGrid;
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                if (child instanceof ViewGroup) {
                    ViewGroup categoryLayout = (ViewGroup) child;
                    ImageView icon = findImageViewInChildren(categoryLayout);
                    TextView text = findTextViewInChildren(categoryLayout);

                    if (icon != null) {
                        icon.setAlpha(animation.getAnimatedFraction());
                        icon.setScaleX(animation.getAnimatedFraction());
                        icon.setScaleY(animation.getAnimatedFraction());
                        icon.setVisibility(animation.getAnimatedFraction() > 0 ? View.VISIBLE : View.GONE);
                    }
                    if (text != null) {
                        ViewGroup.MarginLayoutParams textParams = (ViewGroup.MarginLayoutParams) text.getLayoutParams();
                        textParams.topMargin = (int) ((1 - animation.getAnimatedFraction()) * 15 * density);
                        text.setLayoutParams(textParams);
                    }
                }
            }
        });

        if (show) {
            animator.start();
        } else {
            animator.reverse();
        }
    }

    private ImageView findImageViewInChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ImageView) {
                try {
                    String resourceName = getResources().getResourceName(view.getId());
                    if (resourceName.contains("Image")) {
                        return (ImageView) view;
                    }
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    private TextView findTextViewInChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }


    private void setupSearchNavigation() {
        if (binding == null) return;
        binding.searchBar.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.mainFragment, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

}
