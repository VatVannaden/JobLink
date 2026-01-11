package com.example.joblink.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.FullScreenImageActivity;
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.adapter.ImageSliderAdapter;
import com.example.joblink.databinding.FragmentPostDetailBinding;
import com.example.joblink.model.Post;
import com.example.joblink.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostDetailFragment extends Fragment implements OnMapReadyCallback {

    private FragmentPostDetailBinding binding;
    private DatabaseReference postsRef;
    private DatabaseReference usersRef;
    private DatabaseReference bookmarksRef;
    private String postId;
    private ValueEventListener postListener;
    private ValueEventListener bookmarkListener;
    private final List<ImageView> indicatorViews = new ArrayList<>();
    private GoogleMap googleMap;
    private Post currentPost;
    private boolean isBookmarked = false;
    private FirebaseUser currentUser;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String postId, boolean isBookmarked) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putBoolean("isBookmarked", isBookmarked);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            isBookmarked = getArguments().getBoolean("isBookmarked", false);
        }

        updateBookmarkIcon();

        if (postId != null && !postId.isEmpty()) {
            setupFirebaseListeners();
            fetchPostDetails();
        } else {
            Toast.makeText(getContext(), "Error: Post ID not found.", Toast.LENGTH_SHORT).show();
            if (isAdded()) getParentFragmentManager().popBackStack();
        }

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.bookmarkIcon.setOnClickListener(v -> toggleBookmark());
        binding.shareButton.setOnClickListener(v -> sharePost());
    }

    private void setupFirebaseListeners() {
        if (currentUser != null && postId != null) {
            bookmarksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("bookmarks").child(postId);

            bookmarkListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    isBookmarked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    updateBookmarkIcon();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostDetailFragment", "Failed to read bookmark status.", error.toException());
                }
            };
            bookmarksRef.addValueEventListener(bookmarkListener);
        }
    }


    private void fetchPostDetails() {
        if (postListener != null) {
            postsRef.child(postId).removeEventListener(postListener);
        }
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getContext() != null) {
                    currentPost = snapshot.getValue(Post.class);
                    if (currentPost != null) {
                        currentPost.setPostId(snapshot.getKey());
                        currentPost.setBookmarked(isBookmarked);
                        updateUI(currentPost);
                        checkPostOwnership(currentPost);
                    }
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "Post not found or has been deleted.", Toast.LENGTH_SHORT).show();
                    if (isAdded()) getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailFragment", "Failed to read post details.", error.toException());
            }
        };
        postsRef.child(postId).addValueEventListener(postListener);
    }

    private void updateUI(Post post) {
        binding.textJobTitle.setText(post.getTitle());
        binding.postDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(post.getTimestamp())));
        binding.jobCategory.setText(post.getJobCategory());
        binding.jobType.setText(post.getWorkType());
        binding.textJobDescription.setText(post.getDescription());
        binding.textBusinessName.setText(post.getBusinessName());
        binding.textLocation.setText(post.getLocation());
        binding.writeSomethingText.setText(post.getAddressDetails());
        binding.textWorkType.setText(post.getWorkModel());
        binding.textWorkHours.setText(post.getWorkHours());
        binding.textWorkDays.setText(post.getWorkDays());
        binding.textWhyWorkHere.setText(post.getWhyWorkHere());

        if (post.getSalary() != null && !post.getSalary().isEmpty()) {
            binding.salaryLayout.setVisibility(View.VISIBLE);
            binding.textSalary.setText(post.getSalary());
        } else {
            binding.salaryLayout.setVisibility(View.GONE);
        }

        if (post.getDayOff() != null && !post.getDayOff().isEmpty() && !post.getDayOff().equalsIgnoreCase("Not specified")) {
            binding.dayOffContainer.setVisibility(View.VISIBLE);
            binding.textDayOff.setText(post.getDayOff());
        } else {
            binding.dayOffContainer.setVisibility(View.GONE);
        }

        binding.textExperience.setText(String.format("• Experience: %s", post.getExperienceLevel()));
        if (post.getRequirements() != null && !post.getRequirements().isEmpty()) {
            post.getRequirements().removeIf(String::isEmpty);
            binding.textRequirements.setText("• " + TextUtils.join("\n• ", post.getRequirements()));
        } else {
            binding.textRequirements.setText("No requirements specified.");
        }

        setupImageSlider(post.getImages());
        populateItems(binding.benefitsContainer, post.getSelectedBenefits(), true);
        populateItems(binding.amenitiesContainer, post.getSelectedAmenities(), false);
        fetchEmployerInfo(post.getEmployerId());

        if (googleMap != null) {
            setupMap(post);
        }

        if (post.isAvailable()) {
            binding.availableSection.setVisibility(View.VISIBLE);
            binding.notAvailableSection.setVisibility(View.GONE);
        } else {
            binding.availableSection.setVisibility(View.GONE);
            binding.notAvailableSection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (currentPost != null) {
            setupMap(currentPost);
        }
    }

    private void setupMap(Post post) {
        LatLng location = getLatLngFromUrl(post.getMapsLink());
        String title = post.getBusinessName();

        View mapContainerCard = binding.getRoot().findViewById(R.id.mapView).getParent() instanceof View ? (View) binding.getRoot().findViewById(R.id.mapView).getParent() : null;

        if (location == null) {
            if (mapContainerCard != null) mapContainerCard.setVisibility(View.GONE);
            return;
        }

        if (mapContainerCard != null) mapContainerCard.setVisibility(View.VISIBLE);
        if (googleMap == null) return;

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(location).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        googleMap.getUiSettings().setAllGesturesEnabled(true);
    }

    private LatLng getLatLngFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        Pattern pattern = Pattern.compile("@(-?[\\d.]+),(-?[\\d.]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find() && matcher.groupCount() >= 2) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lng = Double.parseDouble(matcher.group(2));
                return new LatLng(lat, lng);
            } catch (NumberFormatException e) {
                Log.e("getLatLngFromUrl", "Failed to parse coordinates: " + url, e);
                return null;
            }
        }
        return null;
    }

    private void setupImageSlider(List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(imageUrls, position -> {Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
            intent.putExtra("position", position);
            startActivity(intent);
        });
            binding.viewPagerImageSlider.setAdapter(sliderAdapter);
            setupIndicators(imageUrls.size());
            binding.viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setCurrentIndicator(position);
                }
            });
            binding.pagination.setVisibility(View.VISIBLE);
        } else {
            binding.viewPagerImageSlider.setVisibility(View.GONE);
            binding.pagination.setVisibility(View.GONE);
        }
    }

    private void populateItems(LinearLayout container, List<String> items, boolean isBenefit) {
        container.removeAllViews();
        if (items.isEmpty() && getContext() != null) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText(isBenefit ? "No specific benefits listed." : "No specific amenities listed.");
            emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_ico));
            container.addView(emptyView);
        } else {
            for (String item : items) {
                container.addView(createItemView(item, isBenefit));
            }
        }
    }

    private View createItemView(String item, boolean isBenefit) {
        if (getContext() == null) return null;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_perk, (ViewGroup) getView(), false);
        ImageView icon = layout.findViewById(R.id.perkIcon);
        TextView text = layout.findViewById(R.id.perkText);
        text.setText(item);
        icon.setImageResource(isBenefit ? getBenefitIcon(item) : getAmenityIcon(item));
        return layout;
    }

    private void fetchEmployerInfo(String employerId) {
        if (employerId == null || employerId.isEmpty()) return;
        usersRef.child(employerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getContext() != null) {
                    User employer = snapshot.getValue(User.class);
                    if (employer != null) {
                        binding.employerUsername.setText(employer.getUsername());
                        if(isAdded()) {
                            Glide.with(PostDetailFragment.this)
                                    .load(employer.getProfileImageUrl())
                                    .placeholder(R.drawable.img)
                                    .error(R.drawable.img)
                                    .into(binding.employerProfileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailFragment", "Failed to fetch employer info", error.toException());
            }
        });
    }

    private void checkPostOwnership(Post post) {
        if (currentUser != null && post.getEmployerId() != null && currentUser.getUid().equals(post.getEmployerId())) {
            setupUIForOwner();
        } else {
            setupUIForViewer(post);
        }
    }

    private void setupUIForOwner() {
        binding.userControls.setVisibility(View.GONE);
        binding.bottomSection.setVisibility(View.GONE);
        binding.ownerControls.setVisibility(View.VISIBLE);

        binding.availableButton.setOnClickListener(v -> updatePostAvailability(true));
        binding.notAvailableButton.setOnClickListener(v -> updatePostAvailability(false));
        binding.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }


    private void setupUIForViewer(Post post) {
        binding.ownerControls.setVisibility(View.GONE);
        binding.userControls.setVisibility(View.VISIBLE);
        binding.bottomSection.setVisibility(View.VISIBLE);

        boolean isAvailable = post.isAvailable();
        binding.callToAction.setEnabled(isAvailable);
        binding.emailToAction.setEnabled(isAvailable);
        binding.telegramToAction.setEnabled(isAvailable);
        binding.callToAction.setAlpha(isAvailable ? 1.0f : 0.5f);
        binding.emailToAction.setAlpha(isAvailable ? 1.0f : 0.5f);
        binding.telegramToAction.setAlpha(isAvailable ? 1.0f : 0.5f);

        binding.callToAction.setOnClickListener(v -> {
            String phoneNumber = post.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber.trim()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "No application can handle this action.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Phone number not provided.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.emailToAction.setOnClickListener(v -> {
            String email = post.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email.trim()});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about your post: " + post.getTitle());
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "No email client installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Email not provided.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.telegramToAction.setOnClickListener(v -> {
            String telegramLink = post.getTelegramLink();
            if (telegramLink != null && !telegramLink.trim().isEmpty()) {
                String fullUri;
                if (telegramLink.startsWith("t.me/")) {
                    fullUri = "https://" + telegramLink;
                } else if (!telegramLink.startsWith("http")) {
                    fullUri = "https://t.me/" + telegramLink.replace("@", "");
                } else {
                    fullUri = telegramLink;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUri));

                intent.setPackage("org.telegram.messenger");

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "Telegram is not installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Telegram contact not provided.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void toggleBookmark() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to bookmark posts.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPost == null || bookmarksRef == null) return;

        boolean newBookmarkState = !isBookmarked;
        bookmarksRef.setValue(newBookmarkState).addOnCompleteListener(task -> {
            if (isAdded() && getContext() != null) {
                if (task.isSuccessful()) {
                    String message = newBookmarkState ? "Bookmarked" : "Removed bookmark";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update bookmark.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sharePost() {
        if (currentPost == null || getContext() == null) {
            Toast.makeText(getContext(), "Post details not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "Check out this job on JobLink: " + currentPost.getTitle() +
                " at " + currentPost.getBusinessName() + ".";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Share post via"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No application can handle this action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (!isAdded()) return;
        if (isBookmarked) {
            binding.bookmarkIcon.setImageResource(R.drawable.ico_bookmarks_added);
        } else {
            binding.bookmarkIcon.setImageResource(R.drawable.ico_bookmarks_add);
        }
    }

    private void updatePostAvailability(boolean isAvailable) {
        if (postId == null) return;
        postsRef.child(postId).child("available").setValue(isAvailable).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Availability updated.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update availability.", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext()).setTitle("Delete Post").setMessage("Are you sure you want to delete this post? This action cannot be undone.").setPositiveButton("Delete", (dialog, which) -> deletePost()).setNegativeButton("Cancel", null).show();
    }

    private void deletePost() {
        if (postId == null) return;
        postsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null && post.getImages() != null) {
                    for (String imageUrl : post.getImages()) {
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            photoRef.delete().addOnSuccessListener(aVoid -> Log.d("DeletePost", "Image deleted: " + imageUrl)).addOnFailureListener(e -> Log.e("DeletePost", "Failed to delete image: " + imageUrl, e));
                        }
                    }
                }
                deletePostFromDatabase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DeletePost", "Failed to read image URLs for deletion. Deleting database entry anyway.", error.toException());
                deletePostFromDatabase();
            }
        });
    }

    private void deletePostFromDatabase() {
        postsRef.child(postId).removeValue().addOnSuccessListener(aVoid -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.getSupportFragmentManager().popBackStack();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete post.", Toast.LENGTH_SHORT).show());
    }

    private void setupIndicators(int count) {
        if (getContext() == null) return;
        binding.pagination.removeAllViews();
        indicatorViews.clear();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(params);
            indicatorViews.add(imageView);
            binding.pagination.addView(imageView);
        }
        if (count > 0) setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int index) {
        for (int i = 0; i < indicatorViews.size(); i++) {
            indicatorViews.get(i).setImageResource(i == index ? R.drawable.pagination_tab_long : R.drawable.pagination_tab_short);
        }
    }


    private int getBenefitIcon(String benefit) {
        switch (benefit) {
            case "Free meal":
                return R.drawable.ico_food;
            case "Monthly bonus":
                return R.drawable.ico_emoji;
            case "Overtime pay":
                return R.drawable.ico_timer;
            case "Uniform provided":
                return R.drawable.ico_uniform;
            case "Staff discounts":
                return R.drawable.ico_discount;
            case "Health insurance":
                return R.drawable.ico_health;
            case "Holiday":
                return R.drawable.ico_holiday;
            case "End-of-year bonus":
                return R.drawable.ico_end_year_bonus;
            case "Equipment provided":
                return R.drawable.ico_equipment;
            case "Internet allowance":
                return R.drawable.ico_internet;
            case "Hotel provided":
                return R.drawable.ico_hotel;
            case "Certificate":
                return R.drawable.ico_certificate;
            case "Transport provided":
                return R.drawable.ico_transport;
            default:
                return R.drawable.ico_checked;
        }
    }

    private int getAmenityIcon(String amenity) {
        switch (amenity) {
            case "Free wifi":
                return R.drawable.ico_wifi;
            case "Rest area":
                return R.drawable.ico_rest;
            case "Flexible breaks":
                return R.drawable.ico_flexible;
            case "Parking spot":
                return R.drawable.ico_parking;
            case "Locker":
                return R.drawable.ico_locker;
            case "Employee events":
                return R.drawable.ico_events;
            case "Air-conditioned":
                return R.drawable.ico_ac;
            case "Safety equipment":
                return R.drawable.ico_safety;
            default:
                return R.drawable.ico_checked;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isAdded() && getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(true);
        }
        if (postListener != null && postId != null) {
            postsRef.child(postId).removeEventListener(postListener);
        }
        if (bookmarkListener != null && bookmarksRef != null) {
            bookmarksRef.removeEventListener(bookmarkListener);
        }
    }
}
