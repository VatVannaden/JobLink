package com.example.joblink.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostScheduleFragment extends Fragment {

    private CreatePostViewModel createPostViewModel;
    private Post post;
    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton, durationCard;
    private TextView startTimeText, endTimeText;
    private EditText dayPerWeekText, salaryText;
    private RadioButton flexibleDayOff;

    private final Map<Integer, String> benefitIdToStringMap = new HashMap<>();

    private final int currentStep = 3;
    private final int totalSteps = 6;

    private int startHour = -1, startMinute = -1;
    private int endHour = -1, endMinute = -1;

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
        durationCard = view.findViewById(R.id.duration);
        startTimeText = view.findViewById(R.id.startTimeText);
        endTimeText = view.findViewById(R.id.endTimeText);
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

        if (post.getStartTime() != null) startTimeText.setText(post.getStartTime());
        if (post.getEndTime() != null) endTimeText.setText(post.getEndTime());

        // Restore dayPerWeekText without the suffix for editing
        String workDays = post.getWorkDays();
        if (workDays != null && workDays.contains(" days per week")) {
            dayPerWeekText.setText(workDays.replace(" days per week", ""));
        } else {
            dayPerWeekText.setText(workDays);
        }

        salaryText.setText(post.getSalary());

        parseTimesFromPost();

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

    private void parseTimesFromPost() {
        try {
            Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");

            if (post.getStartTime() != null) {
                Matcher matcher = timePattern.matcher(post.getStartTime());
                if (matcher.find()) {
                    startHour = Integer.parseInt(matcher.group(1));
                    startMinute = Integer.parseInt(matcher.group(2));
                }
            }

            if (post.getEndTime() != null) {
                Matcher matcher = timePattern.matcher(post.getEndTime());
                if (matcher.find()) {
                    endHour = Integer.parseInt(matcher.group(1));
                    endMinute = Integer.parseInt(matcher.group(2));
                }
            }
        } catch (Exception e) {
            Log.e("CreatePostSchedule", "Error parsing times from post", e);
            startHour = -1; startMinute = -1;
            endHour = -1; endMinute = -1;
        }
    }

    private void setupListeners(View view) {
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        nextButton.setOnClickListener(v -> {
            if (validateForm()) {
                navigateToNext();
            }
        });

        durationCard.setOnClickListener(v -> showStartTimePicker());

        dayPerWeekText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (!s.isEmpty()) {
                post.setWorkDays(s + " days per week");
            } else {
                post.setWorkDays("");
            }
        }));

        salaryText.addTextChangedListener(new SimpleTextWatcher(s -> post.setSalary(s)));

        flexibleDayOff.setOnClickListener(v -> {
            Object tag = flexibleDayOff.getTag();
            boolean wasChecked = tag != null && (boolean) tag;
            boolean isNowChecked = !wasChecked;

            flexibleDayOff.setChecked(isNowChecked);
            flexibleDayOff.setTag(isNowChecked);

            post.setDayOff(isNowChecked ? "Flexible day off" : null);
        });

        for (Integer benefitId : benefitIdToStringMap.keySet()) {
            RadioButton radioButton = view.findViewById(benefitId);
            if (radioButton != null) {
                radioButton.setOnClickListener(v -> {
                    Object tag = radioButton.getTag();
                    boolean wasChecked = tag != null && (boolean) tag;
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

    private void showStartTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = startHour != -1 ? startHour : calendar.get(Calendar.HOUR_OF_DAY);
        int minute = startMinute != -1 ? startMinute : calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, h, m) -> {
            startHour = h;
            startMinute = m;
            String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            startTimeText.setText(time);
            post.setStartTime(time);
            showEndTimePicker();
        }, hour, minute, true);
        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }

    private void showEndTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = endHour != -1 ? endHour : calendar.get(Calendar.HOUR_OF_DAY);
        int minute = endMinute != -1 ? endMinute : calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, h, m) -> {
            endHour = h;
            endMinute = m;
            String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            endTimeText.setText(time);
            post.setEndTime(time);
            calculateAndSetWorkHours();
        }, hour, minute, true);
        timePickerDialog.setTitle("Select End Time");
        timePickerDialog.show();
    }

    private void calculateAndSetWorkHours() {
        if (startHour == -1 || endHour == -1) return;

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMinute);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long diffInMillis = end.getTimeInMillis() - start.getTimeInMillis();
        if (diffInMillis < 0) {
            diffInMillis += TimeUnit.DAYS.toMillis(1);
        }

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long hours = diffInMinutes / 60;
        long minutes = diffInMinutes % 60;

        String workHoursStr;
        if (minutes == 0) {
            workHoursStr = String.format(Locale.getDefault(), "%d h, from %s to %s", hours, startTimeText.getText().toString(), endTimeText.getText().toString());
        } else {
            workHoursStr = String.format(Locale.getDefault(), "%d h %d m, from %s to %s", hours, minutes, startTimeText.getText().toString(), endTimeText.getText().toString());
        }
        post.setWorkHours(workHoursStr);
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
        if (post.getStartTime() == null || post.getStartTime().isEmpty()) {
            Toast.makeText(getContext(), "Please select start time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (post.getEndTime() == null || post.getEndTime().isEmpty()) {
            Toast.makeText(getContext(), "Please select end time", Toast.LENGTH_SHORT).show();
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
