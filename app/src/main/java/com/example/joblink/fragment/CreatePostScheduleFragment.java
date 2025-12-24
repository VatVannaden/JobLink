package com.example.joblink.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class CreatePostScheduleFragment extends Fragment {

    private CreatePostViewModel createPostViewModel;
    private Post post;
    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;
    private EditText hourPerDayText, dayPerWeekText, salaryText;
    private RadioButton flexibleDayOff;

    private final Map<Integer, String> benefitIdToStringMap = new HashMap<>();

    private final int currentStep = 3;
    private final int totalSteps = 6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post_schedule, container, false);

        createPostViewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = createPostViewModel.getPost();

        initializeViews(view);
        populateBenefitMap();
        setupListeners(view);
        restoreUIState(view);
        updateProgress();

        return view;
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
        backButton = view.findViewById(R.id.backButton);
        nextButton = view.findViewById(R.id.nextButton);
        hourPerDayText = view.findViewById(R.id.hourPerDayText);
        dayPerWeekText = view.findViewById(R.id.dayPerWeekText);
        salaryText = view.findViewById(R.id.salary);
        flexibleDayOff = view.findViewById(R.id.flexibleDayOff);
    }

    private void populateBenefitMap() {
        benefitIdToStringMap.clear();
        benefitIdToStringMap.put(R.id.radioHealthInsurance, "Health insurance");
        benefitIdToStringMap.put(R.id.radioMonthlyBonus, "Monthly bonus");
        benefitIdToStringMap.put(R.id.radioEndOfYearBonus, "End-of-year bonus");
        benefitIdToStringMap.put(R.id.radioOvertimePay, "Overtime pay");
        benefitIdToStringMap.put(R.id.radioHoliday, "Holiday");
        benefitIdToStringMap.put(R.id.radioStaffDiscounts, "Staff discounts");
        benefitIdToStringMap.put(R.id.radioFreeMeal, "Free meal");
        benefitIdToStringMap.put(R.id.radioEquipmentProvided, "Equipment provided");
        benefitIdToStringMap.put(R.id.radioInternetAllowance, "Internet allowance");
        benefitIdToStringMap.put(R.id.radioAccommodation, "Accommodation");
        benefitIdToStringMap.put(R.id.radioHotelProvided, "Hotel provided");
        benefitIdToStringMap.put(R.id.radioCertificate, "Certificate");
        benefitIdToStringMap.put(R.id.radioUniformProvided, "Uniform provided");
        benefitIdToStringMap.put(R.id.radioTransportProvided, "Transport provided");
    }

    private void restoreUIState(View view) {
        if (post == null) return;

        hourPerDayText.setText(post.getWorkHours());
        dayPerWeekText.setText(post.getWorkDays());
        salaryText.setText(post.getSalary());

        String dayOff = post.getDayOff();
        boolean isFlexibleDayOff = dayOff != null && dayOff.equals("Flexible day off");
        flexibleDayOff.setChecked(isFlexibleDayOff);
        flexibleDayOff.setTag(isFlexibleDayOff);

        for (Map.Entry<Integer, String> entry : benefitIdToStringMap.entrySet()) {
            RadioButton rb = view.findViewById(entry.getKey());
            if (rb != null) {
                boolean isSelected = isBenefitSelected(entry.getValue());
                rb.setChecked(isSelected);
                rb.setTag(isSelected);
            }
        }
    }

    private void setupListeners(View view) {
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        nextButton.setOnClickListener(v -> {
            if (validateForm()) {
                navigateToNext();
            }
        });

        hourPerDayText.addTextChangedListener(new SimpleTextWatcher(s -> post.setWorkHours(s)));
        dayPerWeekText.addTextChangedListener(new SimpleTextWatcher(s -> post.setWorkDays(s)));
        salaryText.addTextChangedListener(new SimpleTextWatcher(s -> post.setSalary(s)));

        flexibleDayOff.setOnClickListener(v -> {
            boolean wasChecked = (boolean) flexibleDayOff.getTag();
            boolean isNowChecked = !wasChecked;

            flexibleDayOff.setChecked(isNowChecked);
            flexibleDayOff.setTag(isNowChecked);

            post.setDayOff(isNowChecked ? "Flexible day off" : null);
        });

        for (Integer benefitId : benefitIdToStringMap.keySet()) {
            RadioButton radioButton = view.findViewById(benefitId);
            if (radioButton != null) {
                radioButton.setOnClickListener(v -> {
                    boolean wasChecked = (boolean) radioButton.getTag();
                    boolean isNowChecked = !wasChecked;

                    radioButton.setChecked(isNowChecked);
                    radioButton.setTag(isNowChecked);

                    String benefitName = benefitIdToStringMap.get(radioButton.getId());
                    if (benefitName != null) {
                        setBenefit(benefitName, isNowChecked);
                    }
                });
            }
        }

        salaryText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if(salaryText.getCompoundDrawables()[2] != null) {
                    if (event.getRawX() >= (salaryText.getRight() - salaryText.getCompoundDrawables()[2].getBounds().width())) {
                        showSalaryTypeDialog();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void showSalaryTypeDialog() {
        final String[] salaryTypes = {"/month", "/day", "/hour"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Salary Type")
                .setItems(salaryTypes, (dialog, which) -> {
                    String currentSalary = salaryText.getText().toString();
                    currentSalary = currentSalary.split("/")[0].trim();
                    String newSalary = currentSalary + salaryTypes[which];
                    post.setSalary(newSalary);
                    salaryText.setText(newSalary);
                })
                .show();
    }


    private boolean validateForm() {
        if (post.getWorkHours() == null || post.getWorkHours().isEmpty()) {
            Toast.makeText(getContext(), "Please enter hours per day", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (post.getWorkDays() == null || post.getWorkDays().isEmpty()) {
            Toast.makeText(getContext(), "Please enter days per week", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (post.getSalary() == null || post.getSalary().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a salary", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void navigateToNext() {
        CreatePostEncourageFragment fragment = new CreatePostEncourageFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setBenefit(String benefitName, boolean isSelected) {
        switch (benefitName) {
            case "Free meal": post.setBenefitFreeMeal(isSelected); break;
            case "Monthly bonus": post.setBenefitMonthlyBonus(isSelected); break;
            case "Overtime pay": post.setBenefitOvertimePay(isSelected); break;
            case "Uniform provided": post.setBenefitUniformProvided(isSelected); break;
            case "Staff discounts": post.setBenefitStaffDiscounts(isSelected); break;
            case "Health insurance": post.setBenefitHealthInsurance(isSelected); break;
            case "Holiday": post.setBenefitHoliday(isSelected); break;
            case "End-of-year bonus": post.setBenefitEndOfYearBonus(isSelected); break;
            case "Equipment provided": post.setBenefitEquipmentProvided(isSelected); break;
            case "Internet allowance": post.setBenefitInternetAllowance(isSelected); break;
            case "Accommodation": post.setBenefitAccommodation(isSelected); break;
            case "Hotel provided": post.setBenefitHotelProvided(isSelected); break;
            case "Certificate": post.setBenefitCertificate(isSelected); break;
            case "Transport provided": post.setBenefitTransportProvided(isSelected); break;
        }
    }

    private boolean isBenefitSelected(String benefitName) {
        switch (benefitName) {
            case "Free meal": return post.isBenefitFreeMeal();
            case "Monthly bonus": return post.isBenefitMonthlyBonus();
            case "Overtime pay": return post.isBenefitOvertimePay();
            case "Uniform provided": return post.isBenefitUniformProvided();
            case "Staff discounts": return post.isBenefitStaffDiscounts();
            case "Health insurance": return post.isBenefitHealthInsurance();
            case "Holiday": return post.isBenefitHoliday();
            case "End-of-year bonus": return post.isBenefitEndOfYearBonus();
            case "Equipment provided": return post.isBenefitEquipmentProvided();
            case "Internet allowance": return post.isBenefitInternetAllowance();
            case "Hotel provided": return post.isBenefitHotelProvided();
            case "Accommodation": return post.isBenefitAccommodation();
            case "Certificate": return post.isBenefitCertificate();
            case "Transport provided": return post.isBenefitTransportProvided();
            default: return false;
        }
    }

    private void updateProgress() {
        int progress = (int) ((currentStep / (float) totalSteps) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (stepText != null) stepText.setText(String.format("Step %d of %d", currentStep, totalSteps));
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> onChange;
        public SimpleTextWatcher(java.util.function.Consumer<String> onChange) { this.onChange = onChange; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) { onChange.accept(s.toString().trim()); }
    }
}
