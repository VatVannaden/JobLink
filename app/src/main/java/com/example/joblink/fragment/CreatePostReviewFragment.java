package com.example.joblink.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.FullScreenImageActivity;
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.adapter.ImageSliderAdapter;
import com.example.joblink.viewmodel.CreatePostViewModel;
import com.example.joblink.model.Post;
import com.example.joblink.model.User;
import com.example.joblink.repository.PostRepository;
import com.example.joblink.repository.UserRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostReviewFragment extends Fragment implements OnMapReadyCallback {

    private CreatePostViewModel createPostViewModel;
    private PostRepository postRepository;
    private UserRepository userRepository;

    private Post post;
    private LatLng postLocation;
    private ArrayList<Uri> imageUris;

    private TextView postDate, jobCategory, jobType, jobTitle, jobDescription, workType, businessName, location, writeSomethingText;
    private TextView workHours, workDays, dayOff, salary, requirements, whyWorkHere, textExperience;
    private TextView employerUsername;
    private ImageView employerProfileImage;
    private LinearLayout benefitsContainer, amenitiesContainer, paginationLayout, dayOffContainer;
    private ViewPager2 viewPagerImageSlider;
    private MapView mapView;
    private GoogleMap googleMap;
    private ProgressBar uploadProgressBar;
    private MaterialButton postButton;

    private ImageSliderAdapter sliderAdapter;
    private final List<ImageView> indicatorViews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createPostViewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = createPostViewModel.getPost();
        imageUris = createPostViewModel.getImageUris();

        postRepository = new PostRepository();
        userRepository = new UserRepository();

        if (post == null) {
            Toast.makeText(getContext(), "Error: Post data is missing.", Toast.LENGTH_LONG).show();
            if (isAdded()) getParentFragmentManager().popBackStack();
            return;
        }

        initializeViews(view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        updateUIWithData();
        setupImageSlider();
        setupButtonClickListeners(view);
        loadEmployerData();
    }

    private void initializeViews(View view) {
        postDate = view.findViewById(R.id.postDate);
        jobCategory = view.findViewById(R.id.jobCategory);
        jobType = view.findViewById(R.id.jobType);
        jobTitle = view.findViewById(R.id.textJobTitle);
        jobDescription = view.findViewById(R.id.textJobDescription);
        workType = view.findViewById(R.id.textWorkType);
        businessName = view.findViewById(R.id.textBusinessName);
        location = view.findViewById(R.id.textLocation);
        writeSomethingText = view.findViewById(R.id.writeSomethingText);
        workHours = view.findViewById(R.id.textWorkHours);
        workDays = view.findViewById(R.id.textWorkDays);
        dayOff = view.findViewById(R.id.textDayOff);
        dayOffContainer = view.findViewById(R.id.dayOffContainer);
        salary = view.findViewById(R.id.textSalary);
        requirements = view.findViewById(R.id.textRequirements);
        whyWorkHere = view.findViewById(R.id.textWhyWorkHere);
        textExperience = view.findViewById(R.id.textExperience);
        benefitsContainer = view.findViewById(R.id.benefitsContainer);
        amenitiesContainer = view.findViewById(R.id.amenitiesContainer);
        viewPagerImageSlider = view.findViewById(R.id.viewPagerImageSlider);
        paginationLayout = view.findViewById(R.id.pagination);
        mapView = view.findViewById(R.id.mapView);
        employerUsername = view.findViewById(R.id.employerUsername);
        employerProfileImage = view.findViewById(R.id.employerProfileImage);
        postButton = view.findViewById(R.id.postButton);

        // uploadProgressBar = view.findViewById(R.id.uploadProgressBar);
    }

    private void updateUIWithData() {
        if (post == null) return;

        postDate.setText(new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date()));

        jobCategory.setText(post.getJobCategory());
        jobType.setText(post.getWorkType());
        workType.setText(post.getWorkModel());

        jobTitle.setText(post.getTitle());
        jobDescription.setText(post.getDescription());

        businessName.setText(post.getBusinessName());
        location.setText(post.getLocation());

        if (post.getAddressDetails() != null && !post.getAddressDetails().isEmpty()) {
            writeSomethingText.setText(post.getAddressDetails());
            writeSomethingText.setVisibility(View.VISIBLE);
        } else {
            writeSomethingText.setVisibility(View.GONE);
        }

        workHours.setText(post.getWorkHours());
        workDays.setText(post.getWorkDays());

        if (post.getDayOff() != null && !post.getDayOff().isEmpty() && !post.getDayOff().equalsIgnoreCase("Not specified")) {
            dayOffContainer.setVisibility(View.VISIBLE);
            dayOff.setText(post.getDayOff());
        } else {
            dayOffContainer.setVisibility(View.GONE);
        }

        salary.setText(post.getSalary());
        textExperience.setText(post.getExperienceLevel());

        populateBenefits();
        populateAmenities();

        if (post.getRequirements() != null && !post.getRequirements().isEmpty()) {
            post.getRequirements().removeIf(String::isEmpty);
            requirements.setText(TextUtils.join("\n• ", post.getRequirements()));
        } else {
            requirements.setText("No requirements specified.");
        }
        whyWorkHere.setText(post.getWhyWorkHere());

        postLocation = getLatLngFromUrl(post.getMapsLink());
    }

    private void loadEmployerData() {
        userRepository.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && isAdded()) {
                employerUsername.setText(user.getUsername());
                Glide.with(this)
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .into(employerProfileImage);

                if (post != null) {
                    post.setEmployerId(user.getUserId());
                }
            } else if (isAdded()) {
                employerUsername.setText("Unknown User");
                employerProfileImage.setImageResource(R.drawable.img);
            }
        });
    }

    private void setupImageSlider() {
        if (imageUris == null || imageUris.isEmpty()) {
            viewPagerImageSlider.setVisibility(View.GONE);
            paginationLayout.setVisibility(View.GONE);
            return;
        }

        int coverIndex = createPostViewModel.getCoverImageIndex();
        List<String> imageUriStrings = new ArrayList<>();

        if (coverIndex >= 0 && coverIndex < imageUris.size()) {
            imageUriStrings.add(imageUris.get(coverIndex).toString());
        }

        for (int i = 0; i < imageUris.size(); i++) {
            if (i != coverIndex) {
                imageUriStrings.add(imageUris.get(i).toString());
            }
        }

        sliderAdapter = new ImageSliderAdapter(imageUriStrings, position -> {
            Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUriStrings));
            intent.putExtra("position", position);
            startActivity(intent);
        });

        viewPagerImageSlider.setAdapter(sliderAdapter);
        setupIndicators(imageUriStrings.size());

        viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });

        viewPagerImageSlider.setVisibility(View.VISIBLE);
        paginationLayout.setVisibility(View.VISIBLE);
    }

    private void setupButtonClickListeners(View view) {
        view.findViewById(R.id.backToEditButton).setOnClickListener(v -> {
            if (isAdded()) getParentFragmentManager().popBackStack();
        });

        postButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Post")
                    .setMessage("Are you sure you want to post this job?")
                    .setPositiveButton("Post", (dialog, which) -> uploadImagesToFirebase())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void populateBenefits() {
        benefitsContainer.removeAllViews();
        List<String> benefits = post.getSelectedBenefits();
        if (benefits.isEmpty()) {
            addEmptyStateView(benefitsContainer, "No specific benefits listed.");
        } else {
            for (String benefit : benefits) {
                benefitsContainer.addView(createBenefitAmenityItem(benefit, true));
            }
        }
    }

    private void populateAmenities() {
        amenitiesContainer.removeAllViews();
        List<String> amenities = post.getSelectedAmenities();
        if (amenities.isEmpty()) {
            addEmptyStateView(amenitiesContainer, "No specific amenities listed.");
        } else {
            for (String amenity : amenities) {
                amenitiesContainer.addView(createBenefitAmenityItem(amenity, false));
            }
        }
    }

    private LinearLayout createBenefitAmenityItem(String item, boolean isBenefit) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_perk, benefitsContainer, false);
        ImageView icon = layout.findViewById(R.id.perkIcon);
        TextView text = layout.findViewById(R.id.perkText);
        text.setText(item);
        icon.setImageResource(isBenefit ? getBenefitIcon(item) : getAmenityIcon(item));
        return layout;
    }

    private void addEmptyStateView(LinearLayout container, String message) {
        TextView emptyView = new TextView(getContext());
        emptyView.setText(message);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_ico));
        container.addView(emptyView);
    }

    private int getBenefitIcon(String benefit) {
        switch (benefit) {
            case "Free meal": return R.drawable.ico_food;
            case "Monthly bonus": return R.drawable.ico_emoji;
            case "Overtime pay": return R.drawable.ico_timer;
            case "Uniform provided": return R.drawable.ico_uniform;
            case "Staff discounts": return R.drawable.ico_discount;
            case "Health insurance": return R.drawable.ico_health;
            case "Holiday": return R.drawable.ico_holiday;
            case "End-of-year bonus": return R.drawable.ico_end_year_bonus;
            case "Equipment provided": return R.drawable.ico_equipment;
            case "Internet allowance": return R.drawable.ico_internet;
            case "Hotel provided": return R.drawable.ico_hotel;
            case "Certificate": return R.drawable.ico_certificate;
            case "Transport provided": return R.drawable.ico_transport;
            default: return R.drawable.ico_checked;
        }
    }

    private int getAmenityIcon(String amenity) {
        switch (amenity) {
            case "Free wifi": return R.drawable.ico_wifi;
            case "Rest area": return R.drawable.ico_rest;
            case "Flexible breaks": return R.drawable.ico_flexible;
            case "Parking spot": return R.drawable.ico_parking;
            case "Locker": return R.drawable.ico_locker;
            case "Employee events": return R.drawable.ico_events;
            case "Air-conditioned": return R.drawable.ico_ac;
            case "Safety equipment": return R.drawable.ico_safety;
            default: return R.drawable.ico_checked;
        }
    }

    private void uploadImagesToFirebase() {
        if (imageUris == null || imageUris.isEmpty()) {
            Toast.makeText(getContext(), "No images to upload.", Toast.LENGTH_SHORT).show();
            return;
        }

        postButton.setEnabled(false);
        TextView postButtonText = postButton.findViewById(R.id.textView5);
        if (postButtonText != null) {
            postButtonText.setText("Uploading...");
        }

        if (uploadProgressBar != null) {
            uploadProgressBar.setVisibility(View.VISIBLE);
        }

        int coverIndex = createPostViewModel.getCoverImageIndex();
        ArrayList<Uri> sortedUris = new ArrayList<>();
        sortedUris.add(imageUris.get(coverIndex));
        for (int i = 0; i < imageUris.size(); i++) {
            if (i != coverIndex) {
                sortedUris.add(imageUris.get(i));
            }
        }

        ArrayList<String> downloadedUrls = new ArrayList<>(Collections.nCopies(sortedUris.size(), null));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("post_images");
        final int[] uploadCount = {0};

        for (int i = 0; i < sortedUris.size(); i++) {
            final int index = i;
            Uri uri = sortedUris.get(i);
            StorageReference imageRef = storageRef.child(UUID.randomUUID().toString());

            imageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                downloadedUrls.set(index, downloadUri.toString());
                                uploadCount[0]++;

                                if (uploadCount[0] == sortedUris.size()) {
                                    post.setImages(downloadedUrls);
                                    savePostToFirestore();
                                }
                            }))
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            postButton.setEnabled(true);
                            if (postButtonText != null) {
                                postButtonText.setText("Post");
                            }
                            if (uploadProgressBar != null) {
                                uploadProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void savePostToFirestore() {
        postRepository.createPost(post, new PostRepository.PostCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Post created successfully!", Toast.LENGTH_SHORT).show();
                    createPostViewModel.clear();
                    navigateToHome();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    postButton.setEnabled(true);
                    TextView postButtonText = postButton.findViewById(R.id.textView5);
                    if (postButtonText != null) {
                        postButtonText.setText("Post");
                    }
                    if (uploadProgressBar != null) {
                        uploadProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void setupIndicators(int count) {
        paginationLayout.removeAllViews();
        indicatorViews.clear();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(params);
            indicatorViews.add(imageView);
            paginationLayout.addView(imageView);
        }
        if (count > 0) setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int index) {
        for (int i = 0; i < indicatorViews.size(); i++) {
            indicatorViews.get(i).setImageResource(
                    i == index ? R.drawable.pagination_tab_long : R.drawable.pagination_tab_short
            );
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        if (postLocation != null) {
            googleMap.addMarker(new MarkerOptions().position(postLocation).title(post.getBusinessName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 15));
        }
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
                return null;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();

        imageUris = createPostViewModel.getImageUris();
        setupImageSlider();
    }

    @Override
    public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    public void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}