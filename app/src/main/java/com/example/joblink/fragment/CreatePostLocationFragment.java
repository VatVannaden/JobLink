package com.example.joblink.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

public class CreatePostLocationFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Post post;

    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;

    private EditText locationText, writeSomethingText, exactLocationText;
    private RadioGroup workTypeRadioGroup;

    private final ArrayList<Integer> allRadioIds = new ArrayList<>();

    private final int currentStep = 2;
    private final int totalSteps = 6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post_location, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = viewModel.getPost();

        initializeViews(view);
        restoreUIState(view);
        setupListeners(view);
        updateProgress();

        return view;
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
        backButton = view.findViewById(R.id.backButton);
        nextButton = view.findViewById(R.id.nextButton);

        locationText = view.findViewById(R.id.locationText);
        writeSomethingText = view.findViewById(R.id.writeSomethingText);
        exactLocationText = view.findViewById(R.id.exactLocationText);
        workTypeRadioGroup = view.findViewById(R.id.workTypeRadioGroup);

        allRadioIds.clear();
        allRadioIds.add(R.id.onSite);
        allRadioIds.add(R.id.remote);
        allRadioIds.add(R.id.hybrid);
    }

    private void restoreUIState(View view) {
        if (post == null) return;

        locationText.setText(post.getLocation());
        writeSomethingText.setText(post.getAddressDetails());
        exactLocationText.setText(post.getMapsLink());

        String workModel = post.getWorkModel();
        if (workModel != null && !workModel.isEmpty()) {
            for (int id : allRadioIds) {
                RadioButton rb = view.findViewById(id);
                if (rb != null && rb.getText().toString().equalsIgnoreCase(workModel)) {
                    rb.setChecked(true);
                    rb.setTag(true);
                    break;
                }
            }
        }
    }

    private void setupListeners(View view) {
        locationText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (post != null) post.setLocation(s);
        }));
        writeSomethingText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (post != null) post.setAddressDetails(s);
        }));
        exactLocationText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (post != null) post.setMapsLink(s);
        }));

        for (int radioId : allRadioIds) {
            RadioButton rb = view.findViewById(radioId);
            if (rb != null) {
                rb.setOnClickListener(v -> {
                    boolean wasChecked = rb.getTag() != null && (boolean) rb.getTag();
                    uncheckAllRadios(view);

                    if (wasChecked) {
                        rb.setTag(false);
                        post.setWorkModel(null);
                    } else {
                        rb.setChecked(true);
                        rb.setTag(true);
                        post.setWorkModel(rb.getText().toString());
                    }
                });
            }
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (validateForm()) {
                navigateToNext();
            }
        });
    }

    private void uncheckAllRadios(View view) {
        for (int id : allRadioIds) {
            RadioButton rb = view.findViewById(id);
            if (rb != null) {
                rb.setChecked(false);
                rb.setTag(false);
            }
        }
    }

    private boolean validateForm() {
        if (post == null) {
            Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(post.getLocation())) {
            Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            locationText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(post.getMapsLink())) {
            Toast.makeText(getContext(), "Please enter a Google Maps link", Toast.LENGTH_SHORT).show();
            exactLocationText.requestFocus();
            return false;
        }

        String regex = "^(https?://)?(www\\.)?(google\\.com/maps|goo\\.gl/maps|maps\\.app\\.goo\\.gl|maps\\.google\\.com)/.*$";
        if (!post.getMapsLink().toLowerCase().matches(regex)) {
            Toast.makeText(getContext(), "Please enter a valid Google Maps link", Toast.LENGTH_SHORT).show();
            exactLocationText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(post.getAddressDetails())) {
            Toast.makeText(getContext(), "Please enter an address description", Toast.LENGTH_SHORT).show();
            writeSomethingText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(post.getWorkModel())) {
            Toast.makeText(getContext(), "Please select a work model (On-site, Remote, or Hybrid)", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToNext() {
        CreatePostScheduleFragment fragment = new CreatePostScheduleFragment();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void updateProgress() {
        int progress = (int) ((currentStep / (float) totalSteps) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (stepText != null) stepText.setText(String.format(Locale.US, "Step %d of %d", currentStep, totalSteps));
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onChange;

        public SimpleTextWatcher(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            onChange.accept(s.toString().trim());
        }
    }
}
