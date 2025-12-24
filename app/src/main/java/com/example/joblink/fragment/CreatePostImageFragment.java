package com.example.joblink.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.viewmodel.CreatePostViewModel;
import com.example.joblink.model.Post;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CreatePostImageFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Post post;

    private final List<ConstraintLayout> containers = new ArrayList<>();
    private final List<ImageView> imageViews = new ArrayList<>();
    private final List<MaterialCardView> coverCards = new ArrayList<>();
    private final List<ImageView> checkedIcons = new ArrayList<>();

    private ConstraintLayout editImageSection;
    private ConstraintLayout navigationSection;
    private MaterialCardView addImageButton;
    private MaterialCardView backButton, selectButton, cancelButton, setAsCoverButton;
    private ImageView deleteFromEditButton, blank;
    private TextView selectButtonText;

    private static final int MAX_IMAGES = 5;
    private ArrayList<Uri> imageUris;
    private int coverImageIndex = -1;
    private int selectedImageIndex = -1;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = viewModel.getPost();
        imageUris = viewModel.getImageUris();
        setupLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        containers.clear();
        imageViews.clear();
        coverCards.clear();
        checkedIcons.clear();

        initializeViews(view);
        setupListeners();

        coverImageIndex = viewModel.getCoverImageIndex();

        refreshImageViews();
        exitEditMode();
    }

    private void initializeViews(View view) {
        editImageSection = view.findViewById(R.id.editImageSection);
        navigationSection = view.findViewById(R.id.constraintLayout4);
        blank = view.findViewById(R.id.blank);
        backButton = view.findViewById(R.id.backButton);
        selectButton = view.findViewById(R.id.selectButton);
        selectButtonText = view.findViewById(R.id.selectButtonText);
        addImageButton = view.findViewById(R.id.addImageButton);

        cancelButton = view.findViewById(R.id.cancelButton);
        setAsCoverButton = view.findViewById(R.id.setAsCoverButton);
        deleteFromEditButton = view.findViewById(R.id.deleteButton);

        containers.add(view.findViewById(R.id.container1));
        containers.add(view.findViewById(R.id.container2));
        containers.add(view.findViewById(R.id.container3));
        containers.add(view.findViewById(R.id.container4));
        containers.add(view.findViewById(R.id.container5));

        imageViews.add(view.findViewById(R.id.image1));
        imageViews.add(view.findViewById(R.id.image2));
        imageViews.add(view.findViewById(R.id.image3));
        imageViews.add(view.findViewById(R.id.image4));
        imageViews.add(view.findViewById(R.id.image5));

        coverCards.add(view.findViewById(R.id.cover1));
        coverCards.add(view.findViewById(R.id.cover2));
        coverCards.add(view.findViewById(R.id.cover3));
        coverCards.add(view.findViewById(R.id.cover4));
        coverCards.add(view.findViewById(R.id.cover5));

        checkedIcons.add(view.findViewById(R.id.checked1));
        checkedIcons.add(view.findViewById(R.id.checked2));
        checkedIcons.add(view.findViewById(R.id.checked3));
        checkedIcons.add(view.findViewById(R.id.checked4));
        checkedIcons.add(view.findViewById(R.id.checked5));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        selectButton.setOnClickListener(v -> handleNextOrSelect());
        addImageButton.setOnClickListener(v -> checkPermissionAndOpenPicker());
        blank.setOnClickListener(v -> exitEditMode());

        for (int i = 0; i < MAX_IMAGES; i++) {
            int index = i;
            imageViews.get(i).setOnClickListener(v -> {
                if (index < imageUris.size()) {
                    if (selectedImageIndex != index) {
                        enterEditMode(index);
                    }
                }
            });
        }

        cancelButton.setOnClickListener(v -> exitEditMode());
        deleteFromEditButton.setOnClickListener(v -> {
            if (selectedImageIndex != -1) {
                removeImage(selectedImageIndex);
                exitEditMode();
            }
        });
        setAsCoverButton.setOnClickListener(v -> {
            if (selectedImageIndex != -1) {
                setCoverImage(selectedImageIndex);
                exitEditMode();
            }
        });
    }

    private void setupLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openImagePicker();
            else Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                boolean wasEmpty = imageUris.isEmpty();

                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    for (int i = 0; i < count && imageUris.size() < MAX_IMAGES; i++) {
                        imageUris.add(result.getData().getClipData().getItemAt(i).getUri());
                    }
                } else if (result.getData().getData() != null) {
                    if (imageUris.size() < MAX_IMAGES) {
                        imageUris.add(result.getData().getData());
                    }
                }

                if (wasEmpty && !imageUris.isEmpty()) {
                    setCoverImage(0);
                }

                refreshImageViews();
                updateUI();
            }
        });

    }

    private void checkPermissionAndOpenPicker() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        if (imageUris.size() >= MAX_IMAGES) {
            Toast.makeText(getContext(), "You can only select up to " + MAX_IMAGES + " images.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void refreshImageViews() {
        if (imageUris == null || !isAdded() || getContext() == null) return;

        for (int i = 0; i < MAX_IMAGES; i++) {
            if (i < imageUris.size()) {
                containers.get(i).setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(imageUris.get(i))
                        .into(imageViews.get(i));
            } else {
                containers.get(i).setVisibility(View.GONE);
                Glide.with(this).clear(imageViews.get(i));
            }
        }
    }

    private void updateUI() {
        if (imageUris == null) return;

        for (int i = 0; i < imageUris.size(); i++) {
            coverCards.get(i).setVisibility(i == coverImageIndex ? View.VISIBLE : View.INVISIBLE);
            checkedIcons.get(i).setVisibility(i == selectedImageIndex ? View.VISIBLE : View.INVISIBLE);
        }
        for (int i = imageUris.size(); i < MAX_IMAGES; i++) {
            coverCards.get(i).setVisibility(View.GONE);
            checkedIcons.get(i).setVisibility(View.GONE);
        }

        addImageButton.setVisibility(imageUris.size() < MAX_IMAGES ? View.VISIBLE : View.GONE);
    }

    private void removeImage(int index) {
        if (index < 0 || index >= imageUris.size()) return;

        imageUris.remove(index);

        if (imageUris.isEmpty()) {
            coverImageIndex = -1;
            viewModel.setCoverImageIndex(-1);
        } else if (index == coverImageIndex) {
            setCoverImage(0);
        } else if (index < coverImageIndex) {
            coverImageIndex--;
            viewModel.setCoverImageIndex(coverImageIndex);
        }

        refreshImageViews();
        updateUI();
    }

    private void setCoverImage(int index) {
        if (index < 0 || index >= imageUris.size()) return;
        coverImageIndex = index;
        viewModel.setCoverImageIndex(index);
        updateUI();
    }

    private void enterEditMode(int index) {
        selectedImageIndex = index;
        navigationSection.setVisibility(View.GONE);
        editImageSection.setVisibility(View.VISIBLE);
        updateUI();
    }

    private void exitEditMode() {
        selectedImageIndex = -1;
        navigationSection.setVisibility(View.VISIBLE);
        editImageSection.setVisibility(View.GONE);
        updateUI();
    }

    private void handleNextOrSelect() {
        if (imageUris.isEmpty()) {
            Toast.makeText(getContext(), "Please upload at least one image.", Toast.LENGTH_SHORT).show();
        } else if (coverImageIndex == -1) {
            Toast.makeText(getContext(), "Please select a cover image.", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.setCoverImageIndex(coverImageIndex);
            navigateToReviewFragment();
        }
    }

    private void navigateToReviewFragment() {
        if (isAdded()) {
            CreatePostReviewFragment nextFragment = new CreatePostReviewFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragment, nextFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
