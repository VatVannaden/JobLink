package com.example.joblink.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.viewmodel.CreatePostViewModel;
import com.example.joblink.model.Post;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

public class CreatePostEncourageFragment extends Fragment {

    private CreatePostViewModel createPostViewModel;
    private Post post;
    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;
    private EditText writeSomethingText;

    private final Map<Integer, String> amenityIdToStringMap = new HashMap<>();

    private final int currentStep = 4;
    private final int totalSteps = 6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_encourage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createPostViewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = createPostViewModel.getPost();

        initializeViews(view);
        populateAmenityMap();
        setupListeners(view);
        restoreUIState(view);
        updateProgress();
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
        backButton = view.findViewById(R.id.backButton);
        nextButton = view.findViewById(R.id.nextButton);
        writeSomethingText = view.findViewById(R.id.writeSomethingText);
    }

    private void populateAmenityMap() {
        amenityIdToStringMap.clear();
        amenityIdToStringMap.put(R.id.radioFreeWifi, "Free wifi");
        amenityIdToStringMap.put(R.id.radioRestArea, "Rest area");
        amenityIdToStringMap.put(R.id.radioFlexibleBreaks, "Flexible breaks");
        amenityIdToStringMap.put(R.id.radioParkingSpot, "Parking spot");
        amenityIdToStringMap.put(R.id.radioLocker, "Locker");
        amenityIdToStringMap.put(R.id.radioEmployeeEvents, "Employee events");
        amenityIdToStringMap.put(R.id.radioAirConditioned, "Air-conditioned");
        amenityIdToStringMap.put(R.id.radioSafetyEquipment, "Safety equipment");
    }

    private void setupListeners(View view) {
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        nextButton.setOnClickListener(v -> {
            if (validateForm()) {
                navigateToNext();
            }
        });

        writeSomethingText.addTextChangedListener(new SimpleTextWatcher(s -> post.setWhyWorkHere(s)));

        for (Integer amenityId : amenityIdToStringMap.keySet()) {
            RadioButton radioButton = view.findViewById(amenityId);
            if (radioButton != null) {
                radioButton.setOnClickListener(v -> {
                    boolean wasChecked = (boolean) radioButton.getTag();
                    boolean isNowChecked = !wasChecked;

                    radioButton.setChecked(isNowChecked);
                    radioButton.setTag(isNowChecked);

                    String amenityName = amenityIdToStringMap.get(radioButton.getId());
                    if (amenityName != null) {
                        setAmenity(amenityName, isNowChecked);
                    }
                });
            }
        }
    }

    private void restoreUIState(View view) {
        if (post == null) return;

        writeSomethingText.setText(post.getWhyWorkHere());

        for (Map.Entry<Integer, String> entry : amenityIdToStringMap.entrySet()) {
            RadioButton rb = view.findViewById(entry.getKey());
            if (rb != null) {
                boolean isSelected = isAmenitySelected(entry.getValue());
                rb.setChecked(isSelected);
                rb.setTag(isSelected);
            }
        }
    }

    private boolean validateForm() {
        if (writeSomethingText.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please write a few words of encouragement", Toast.LENGTH_SHORT).show();
            writeSomethingText.requestFocus();
            return false;
        }

        return true;
    }

    private void navigateToNext() {
        CreatePostRequirementFragment nextFragment = new CreatePostRequirementFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, nextFragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateProgress() {
        int progress = (int) (((float) currentStep / totalSteps) * 100);
        progressBar.setProgress(progress);
        stepText.setText("Step " + currentStep + " of " + totalSteps);
    }

    private void setAmenity(String amenityName, boolean isSelected) {
        switch (amenityName) {
            case "Free wifi":
                post.setAmenityFreeWifi(isSelected);
                break;
            case "Rest area":
                post.setAmenityRestArea(isSelected);
                break;
            case "Flexible breaks":
                post.setAmenityFlexibleBreaks(isSelected);
                break;
            case "Parking spot":
                post.setAmenityParkingSpot(isSelected);
                break;
            case "Locker":
                post.setAmenityLocker(isSelected);
                break;
            case "Employee events":
                post.setAmenityEmployeeEvents(isSelected);
                break;
            case "Air-conditioned":
                post.setAmenityAirConditioned(isSelected);
                break;
            case "Safety equipment":
                post.setAmenitySafetyEquipment(isSelected);
                break;
        }
    }

    private boolean isAmenitySelected(String amenityName) {
        if (post == null) return false;
        switch (amenityName) {
            case "Free wifi": return post.isAmenityFreeWifi();
            case "Rest area": return post.isAmenityRestArea();
            case "Flexible breaks": return post.isAmenityFlexibleBreaks();
            case "Parking spot": return post.isAmenityParkingSpot();
            case "Locker": return post.isAmenityLocker();
            case "Employee events": return post.isAmenityEmployeeEvents();
            case "Air-conditioned": return post.isAmenityAirConditioned();
            case "Safety equipment": return post.isAmenitySafetyEquipment();
            default: return false;
        }
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> onChange;
        public SimpleTextWatcher(java.util.function.Consumer<String> onChange) { this.onChange = onChange; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) { onChange.accept(s.toString().trim()); }
    }
}