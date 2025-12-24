package com.example.joblink.fragment;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreatePostRequirementFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Post post;

    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;
    private EditText writeSomethingText;

    private final int currentStep = 5;
    private final int totalSteps = 6;

    private final ArrayList<Integer> allRadioIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_requirement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = viewModel.getPost();

        initializeViews(view);
        setupRadioIds();
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

    private void setupRadioIds() {
        allRadioIds.clear();
        allRadioIds.add(R.id.radioNoExperience);
        allRadioIds.add(R.id.radio1Year);
        allRadioIds.add(R.id.radio2Years);
        allRadioIds.add(R.id.radio3Years);
        allRadioIds.add(R.id.radio4Years);
        allRadioIds.add(R.id.radio5Years);
        allRadioIds.add(R.id.radioCustom);
    }

    private void restoreUIState(View view) {
        if (post.getRequirements() != null && !post.getRequirements().isEmpty()) {
            writeSomethingText.setText(post.getRequirements().stream()
                    .map(s -> "• " + s)
                    .collect(Collectors.joining("\n")));
        } else {
            writeSomethingText.setText("• ");
            writeSomethingText.setSelection(writeSomethingText.getText().length());
        }


        String experienceLevel = post.getExperienceLevel();
        if (experienceLevel != null && !experienceLevel.isEmpty()) {
            boolean isCustom = true;
            for (int id : allRadioIds) {
                RadioButton rb = view.findViewById(id);
                if (rb != null && rb.getText().toString().equalsIgnoreCase(experienceLevel)) {
                    rb.setChecked(true);
                    rb.setTag(true);
                    isCustom = false;
                    break;
                }
            }
            if (isCustom) {
                RadioButton customRb = view.findViewById(R.id.radioCustom);
                if (customRb != null) {
                    customRb.setText(experienceLevel);
                    customRb.setChecked(true);
                    customRb.setTag(true);
                }
            }
        }
    }

    private void setupListeners(View view) {
        writeSomethingText.addTextChangedListener(new BulletPointTextWatcher(writeSomethingText, s -> {
            List<String> reqs = Arrays.stream(s.split("\\s*\\n\\s*"))
                    .map(line -> line.replace("• ", "").trim())
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
            post.setRequirements(reqs);
        }));

        for (int radioId : allRadioIds) {
            RadioButton rb = view.findViewById(radioId);
            if (rb != null) {
                rb.setOnClickListener(v -> {
                    boolean wasChecked = rb.getTag() != null && (boolean) rb.getTag();
                    uncheckAllRadios(view);

                    if (wasChecked) {
                        rb.setTag(false);
                        post.setExperienceLevel(null);
                    } else {
                        rb.setChecked(true);
                        rb.setTag(true);

                        if (radioId == R.id.radioCustom) {
                            showCustomExperienceDialog(rb);
                        } else {
                            post.setExperienceLevel(rb.getText().toString());
                        }
                    }
                });
            }
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        nextButton.setOnClickListener(v -> {
            if (validateForm()) {
                navigateToNextFragment();
            }
        });
    }

    private void showCustomExperienceDialog(RadioButton customRadioButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Custom Experience");

        final EditText input = new EditText(requireContext());
        input.setHint("e.g., 7+ years");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String customExperience = input.getText().toString().trim();
            if (!customExperience.isEmpty()) {
                customRadioButton.setText(customExperience);
                post.setExperienceLevel(customExperience);
            } else {
                customRadioButton.setText("Custom");
                customRadioButton.setChecked(false);
                customRadioButton.setTag(false);
                post.setExperienceLevel(null);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            customRadioButton.setChecked(false);
            customRadioButton.setTag(false);
            post.setExperienceLevel(null);
            dialog.cancel();
        });

        builder.create().show();
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

        if (post.getExperienceLevel() == null || post.getExperienceLevel().isEmpty()) {
            Toast.makeText(getContext(), "Please select an experience level", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (post.getRequirements() == null || post.getRequirements().isEmpty() || post.getRequirements().get(0).isEmpty()) {
            Toast.makeText(getContext(), "Please enter at least one requirement", Toast.LENGTH_SHORT).show();
            writeSomethingText.requestFocus();
            return false;
        }

        return true;
    }

    private void navigateToNextFragment() {
        CreatePostContactFragment nextFragment = new CreatePostContactFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, nextFragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateProgress() {
        int progress = (int) ((currentStep / (float) totalSteps) * 100);
        progressBar.setProgress(progress);
        stepText.setText("Step " + currentStep + " of " + totalSteps);
    }

    private static class BulletPointTextWatcher implements TextWatcher {
        private final EditText editText;
        private final java.util.function.Consumer<String> onChange;
        private boolean isEditing = false;

        public BulletPointTextWatcher(EditText editText, java.util.function.Consumer<String> onChange) {
            this.editText = editText;
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isEditing) {
                return;
            }
            isEditing = true;

            String text = s.toString();
            if (text.endsWith("\n")) {
                s.append("• ");
            }

            if (!text.startsWith("• ")) {
                s.insert(0, "• ");
            }

            onChange.accept(s.toString());
            isEditing = false;
        }
    }
}
