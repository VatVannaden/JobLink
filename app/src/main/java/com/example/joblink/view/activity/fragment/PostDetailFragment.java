package com.example.joblink.view.activity.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.view.activity.FullScreenImageActivity;
import com.example.joblink.view.activity.HomeActivity;
import com.example.joblink.adapter.ImageSliderAdapter;
import com.example.joblink.databinding.FragmentPostDetailBinding;
import com.example.joblink.model.Post;
import com.example.joblink.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private User employerUser; // Store employer user data

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

    @SuppressLint("ClickableViewAccessibility")
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

        // Initialize Map
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        // FIX: Improved touch handling for MapView inside NestedScrollView using an overlay
        binding.mapOverlay.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    binding.nestedScrollView.requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    binding.nestedScrollView.requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Dispatch the touch event to the actual MapView
            return binding.mapView.dispatchTouchEvent(event);
        });

        binding.backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.bookmarkButton.setOnClickListener(v -> toggleBookmark());
        binding.shareButton.setOnClickListener(v -> sharePost());

        // Initialize employer info section
        setupEmployerInfoSection();
    }

    private void setupEmployerInfoSection() {
        // Make sure these views exist in your layout
        // If they don't exist, you need to add them to fragment_post_detail.xml
        binding.employerInfoSection.setVisibility(View.VISIBLE);
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
                    Log.e("PostDetailFragment", "Bookmark error", error.toException());
                }
            };
            bookmarksRef.addValueEventListener(bookmarkListener);
        }
    }

    private void fetchPostDetails() {
        if (postListener != null) postsRef.child(postId).removeEventListener(postListener);
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    currentPost = snapshot.getValue(Post.class);
                    if (currentPost != null) {
                        currentPost.setPostId(snapshot.getKey());
                        updateUI(currentPost);
                        checkPostOwnership(currentPost);
                        // Fetch employer user data
                        fetchEmployerInfo(currentPost.getEmployerId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailFragment", "Fetch error", error.toException());
            }
        };
        postsRef.child(postId).addValueEventListener(postListener);
    }

    private void updateUI(Post post) {
        binding.textJobTitle.setText(post.getTitle());
        binding.postDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(post.getTimestamp())));
        binding.jobCategory.setText(post.getJobCategory());
        binding.jobType.setText(post.getBusinessType());
        binding.textJobDescription.setText(post.getJobDescription());
        binding.textBusinessName.setText(post.getBusinessName());
        binding.textLocation.setText(post.getProvince());
        binding.writeSomethingText.setText(post.getLocationDescription());
        binding.textWorkType.setText(post.getWorkPlaceType());
        binding.textWorkHours.setText(post.getWorkHours());
        binding.textWorkDays.setText(post.getDaysPerWeek());
        binding.textWhyWorkHere.setText(post.getWhyWorkHere());

        binding.salaryLayout.setVisibility(!TextUtils.isEmpty(post.getSalary()) ? View.VISIBLE : View.GONE);
        if (binding.salaryLayout.getVisibility() == View.VISIBLE)
            binding.textSalary.setText(post.getSalary());

        binding.dayOffContainer.setVisibility(post.getDayOff() != null && !post.getDayOff().equalsIgnoreCase("Not specified") ? View.VISIBLE : View.GONE);
        if (binding.dayOffContainer.getVisibility() == View.VISIBLE)
            binding.textDayOff.setText(post.getDayOff());

        binding.textExperience.setText(String.format("• Experience: %s", post.getExperience()));
        if (post.getOtherRequirements() != null && !post.getOtherRequirements().isEmpty()) {
            binding.textRequirements.setText("• " + post.getOtherRequirements().replace("\n", "\n• "));
        } else {
            binding.textRequirements.setText("No specific requirements listed.");
        }

        setupImageSlider(post);
        populatePerks(binding.benefitsContainer, post.getSelectedBenefits(), true);
        populatePerks(binding.amenitiesContainer, post.getSelectedAmenities(), false);

        if (googleMap != null) setupMap(post);

        binding.availableSection.setVisibility(post.isAvailable() ? View.VISIBLE : View.GONE);
        binding.notAvailableSection.setVisibility(post.isAvailable() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        if (currentPost != null) setupMap(currentPost);
    }

    private void setupMap(Post post) {
        LatLng location = getLatLngFromUrl(post.getMapLink());
        if (location == null) {
            binding.mapView.setVisibility(View.GONE);
            return;
        }
        binding.mapView.setVisibility(View.VISIBLE);
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location).title(post.getBusinessName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }

    private LatLng getLatLngFromUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;
        Pattern pattern = Pattern.compile("@(-?[\\d.]+),(-?[\\d.]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find() && matcher.groupCount() >= 2) {
            try {
                return new LatLng(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private void setupImageSlider(Post post) {
        List<String> originalUrls = post.getImages();
        if (originalUrls != null && !originalUrls.isEmpty()) {
            // Reorder list to put cover image first
            List<String> reorderedUrls = new ArrayList<>();
            int coverIndex = post.getCoverImageIndex();

            if (coverIndex >= 0 && coverIndex < originalUrls.size()) {
                reorderedUrls.add(originalUrls.get(coverIndex));
            }

            for (int i = 0; i < originalUrls.size(); i++) {
                if (i != coverIndex) {
                    reorderedUrls.add(originalUrls.get(i));
                }
            }

            binding.viewPagerImageSlider.setAdapter(new ImageSliderAdapter(reorderedUrls, position -> {
                Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(reorderedUrls));
                intent.putExtra("position", position);
                startActivity(intent);
            }));

            setupIndicators(reorderedUrls.size());
            binding.viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    setCurrentIndicator(position);
                }
            });
            binding.pagination.setVisibility(View.VISIBLE);
            binding.viewPagerImageSlider.setVisibility(View.VISIBLE);
        } else {
            binding.viewPagerImageSlider.setVisibility(View.GONE);
            binding.pagination.setVisibility(View.GONE);
        }
    }

    private void populatePerks(LinearLayout container, List<String> items, boolean isBenefit) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText(isBenefit ? "No benefits listed." : "No amenities listed.");
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_ico));
            container.addView(tv);
        } else {
            for (String item : items) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_perk, container, false);
                ((ImageView) view.findViewById(R.id.perkIcon)).setImageResource(isBenefit ? getBenefitIcon(item) : getAmenityIcon(item));
                ((TextView) view.findViewById(R.id.perkText)).setText(item);
                container.addView(view);
            }
        }
    }

    private void fetchEmployerInfo(String employerId) {
        if (TextUtils.isEmpty(employerId)) {
            showDefaultEmployerInfo();
            return;
        }

        usersRef.child(employerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    employerUser = snapshot.getValue(User.class);
                    if (employerUser != null) {
                        updateEmployerUI(employerUser);
                    } else {
                        showDefaultEmployerInfo();
                    }
                } else {
                    showDefaultEmployerInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailFragment", "Error fetching employer info", error.toException());
                showDefaultEmployerInfo();
            }
        });
    }

    private void updateEmployerUI(User employer) {
        // Set username
        binding.employerUsername.setText(employer.getUsername() != null ? employer.getUsername() : "Employer");

        // Set email (use post email as fallback)
        String email = employer.getEmail() != null ? employer.getEmail() :
                (currentPost != null ? currentPost.getEmail() : "Not provided");
        binding.employerEmail.setText(email);

        // Set phone (use post phone as fallback)
        String phone = employer.getPhone() != null ? employer.getPhone() :
                (currentPost != null ? currentPost.getPhoneNumber() : "Not provided");
        binding.employerPhone.setText(phone);

        // Set profession
        String profession = employer.getProfession() != null ? employer.getProfession() :
                (currentPost != null ? currentPost.getBusinessType() : "Not specified");
        binding.employerProfession.setText(profession);

        // Set location
        if (employer.getLocation() != null && !employer.getLocation().isEmpty()) {
            binding.employerLocation.setText(employer.getLocation());
            binding.employerLocation.setVisibility(View.VISIBLE);
        } else {
            binding.employerLocation.setVisibility(View.GONE);
        }

        // Load profile image
        if (employer.getPhotoURL() != null && !employer.getPhotoURL().isEmpty()) {
            Glide.with(PostDetailFragment.this)
                    .load(employer.getPhotoURL())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(binding.employerProfileImage);
        } else {
            binding.employerProfileImage.setImageResource(R.drawable.img);
        }

        // Show contact info section
        binding.employerContactInfo.setVisibility(View.VISIBLE);
    }

    private void showDefaultEmployerInfo() {
        // Default employer info using post data
        String employerName = currentPost != null ? currentPost.getEmployerId() : "Employer";
        binding.employerUsername.setText(employerName != null ? employerName : "Employer");

        String email = currentPost != null ? currentPost.getEmail() : "Not provided";
        binding.employerEmail.setText(email);

        String phone = currentPost != null ? currentPost.getPhoneNumber() : "Not provided";
        binding.employerPhone.setText(phone);

        String profession = currentPost != null ? currentPost.getBusinessType() : "Not specified";
        binding.employerProfession.setText(profession);

        binding.employerLocation.setVisibility(View.GONE);

        // Fallback profile image (posts don't have author photos, we'll use a placeholder)
        binding.employerProfileImage.setImageResource(R.drawable.img);

        // Show contact info section
        binding.employerContactInfo.setVisibility(View.VISIBLE);
    }

    private void checkPostOwnership(Post post) {
        boolean isOwner = currentUser != null && TextUtils.equals(currentUser.getUid(), post.getEmployerId());
        binding.ownerControls.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        binding.userControls.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        binding.bottomSection.setVisibility(isOwner ? View.GONE : View.VISIBLE);

        if (isOwner) {
            binding.availableButton.setOnClickListener(v -> updateAvailability(true));
            binding.notAvailableButton.setOnClickListener(v -> updateAvailability(false));
            binding.deleteButton.setOnClickListener(v -> showDeleteDialog());
        } else {
            setupViewerActions(post);
        }
    }

    private void setupViewerActions(Post post) {
        float alpha = post.isAvailable() ? 1.0f : 0.5f;
        binding.callToAction.setAlpha(alpha);
        binding.emailToAction.setAlpha(alpha);
        binding.telegramToAction.setAlpha(alpha);

        binding.callToAction.setOnClickListener(v -> {
            if (post.isAvailable()) {
                // Use employer's phone if available, otherwise use post phone
                String phoneNumber = employerUser != null && employerUser.getPhone() != null ?
                        employerUser.getPhone() : post.getPhoneNumber();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                } else {
                    Toast.makeText(getContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.emailToAction.setOnClickListener(v -> {
            if (post.isAvailable()) {
                // Use employer's email if available, otherwise use post email
                String email = employerUser != null && employerUser.getEmail() != null ?
                        employerUser.getEmail() : post.getEmail();
                if (email != null && !email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about your job post: " + post.getTitle());
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } else {
                    Toast.makeText(getContext(), "Email not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.telegramToAction.setOnClickListener(v -> {
            if (post.isAvailable() && post.getTelegramLink() != null) {
                String link = post.getTelegramLink();
                if (!link.startsWith("http")) link = "https://" + link.replace("@", "");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            }
        });

        // Add click listeners to employer contact info
        binding.employerEmail.setOnClickListener(v -> {
            if (post.isAvailable()) {
                String email = binding.employerEmail.getText().toString();
                if (!email.equals("Not provided") && !email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about your job post: " + post.getTitle());
                    startActivity(Intent.createChooser(intent, "Send Email"));
                }
            }
        });

        binding.employerPhone.setOnClickListener(v -> {
            if (post.isAvailable()) {
                String phone = binding.employerPhone.getText().toString();
                if (!phone.equals("Not provided") && !phone.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                }
            }
        });
    }

    private void updateAvailability(boolean available) {
        if (postId != null) postsRef.child(postId).child("available").setValue(available)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Post status updated", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Post")
                .setMessage("Delete this post permanently?").setPositiveButton("Delete", (d, w) -> deletePost())
                .setNegativeButton("Cancel", null).show();
    }

    private void deletePost() {
        if (postId != null) postsRef.child(postId).removeValue().addOnSuccessListener(aVoid -> {
            if (isAdded()) getParentFragmentManager().popBackStack();
        });
    }

    private void toggleBookmark() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }
        bookmarksRef.setValue(!isBookmarked);
    }

    private void sharePost() {
        if (currentPost == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "Look at this job: " + currentPost.getTitle() +
                        "\n\nCompany: " + currentPost.getBusinessName() +
                        "\nLocation: " + currentPost.getProvince() +
                        "\nSalary: " + currentPost.getSalary());
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void updateBookmarkIcon() {
        if (isAdded())
            binding.bookmarkIcon.setImageResource(isBookmarked ? R.drawable.ico_bookmarks_added : R.drawable.ico_bookmarks_add);
    }

    private void setupIndicators(int count) {
        binding.pagination.removeAllViews();
        indicatorViews.clear();
        for (int i = 0; i < count; i++) {
            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8, 0, 8, 0);
            iv.setLayoutParams(lp);
            indicatorViews.add(iv);
            binding.pagination.addView(iv);
        }
        if (count > 0) setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int index) {
        for (int i = 0; i < indicatorViews.size(); i++) {
            indicatorViews.get(i).setImageResource(i == index ? R.drawable.pagination_tab_long : R.drawable.pagination_tab_short);
        }
    }

    private int getBenefitIcon(String p) {
        switch (p) {
            case "Free meal":
                return R.drawable.ico_food;
            case "Monthly bonus":
                return R.drawable.ico_emoji;
            case "Overtime pay":
                return R.drawable.ico_timer;
            case "Uniform provided":
                return R.drawable.ico_uniform;
            case "Health insurance":
                return R.drawable.ico_health;
            case "Staff discounts":
                return R.drawable.ico_discount;
            case "Holiday":
                return R.drawable.ico_holiday;
            default:
                return R.drawable.ico_checked;
        }
    }

    private int getAmenityIcon(String p) {
        switch (p) {
            case "Free wifi":
                return R.drawable.ico_wifi;
            case "Rest area":
                return R.drawable.ico_rest;
            case "Air-conditioned":
                return R.drawable.ico_ac;
            case "Parking spot":
                return R.drawable.ico_parking;
            default:
                return R.drawable.ico_checked;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
        if (getActivity() instanceof HomeActivity)
            ((HomeActivity) getActivity()).showBottomNavigationView(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        binding.mapView.onDestroy();
        super.onDestroyView();
        if (postListener != null) postsRef.child(postId).removeEventListener(postListener);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}
