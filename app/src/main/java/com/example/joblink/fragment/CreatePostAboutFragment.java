package com.example.joblink.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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

import java.util.Locale;

public class CreatePostAboutFragment extends Fragment {

    private CreatePostViewModel createPostViewModel;

    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;
    private EditText titleText, descriptionText, businessNameText;

    private String jobType = "";
    private String jobCategory = "";
    private final int currentStep = 1;
    private final int totalSteps = 6;

    private final int[] allRadioIds = {
            R.id.radioTechnology, R.id.radioFinance, R.id.radioMedical, R.id.radioEducation,
            R.id.radioAgriculture, R.id.radioEngineering, R.id.radioHospitality, R.id.radioOther,
            R.id.radioPartTechnology, R.id.radioPartDigitalMarketing, R.id.radioPartWriting,
            R.id.radioPartProjectManagement, R.id.radioPartOnlineTutoring, R.id.radioPartGraphicDesign,
            R.id.radioPartVirtualAssistant, R.id.radioPartOther,
            R.id.radioInternTechnology, R.id.radioInternMarketing, R.id.radioInternFinance,
            R.id.radioInternEducation, R.id.radioInternMedical, R.id.radioInternAgriculture,
            R.id.radioInternHospitality, R.id.radioInternOther,
            R.id.radioVolunteerCommunity, R.id.radioVolunteerNonprofit, R.id.radioVolunteerHealth,
            R.id.radioVolunteerEducation, R.id.radioVolunteerEnvironment, R.id.radioVolunteerEvent,
            R.id.radioVolunteerSport, R.id.radioVolunteerOther,
            R.id.radioFreelanceTechnology, R.id.radioFreelanceDigitalMarketing, R.id.radioFreelanceWriting,
            R.id.radioFreelanceProjectManagement, R.id.radioFreelanceOnlineTutoring, R.id.radioFreelanceGraphicDesign,
            R.id.radioFreelanceVirtualAssistant, R.id.radioFreelanceOther,
            R.id.radioOtherTypePainter, R.id.radioOtherTypeSeasonal, R.id.radioOtherTypeDailyLabor,
            R.id.radioOtherTypeTransport, R.id.radioOtherTypeRepair, R.id.radioOtherTypePhotographer,
            R.id.radioOtherTypeChildCare, R.id.radioOtherTypeOther
    };

    private final int[] otherRadioIds = {
            R.id.radioOther, R.id.radioPartOther, R.id.radioInternOther,
            R.id.radioVolunteerOther, R.id.radioFreelanceOther, R.id.radioOtherTypeOther
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createPostViewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);

        initializeViews(view);
        getArgumentsData();
        loadDataFromViewModel();
        setupClickListeners();
        showCategoryBasedOnJobType(view);
        setupCategorySelection(view);
        updateProgress();
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
        backButton = view.findViewById(R.id.backButton);
        nextButton = view.findViewById(R.id.nextButton);
        titleText = view.findViewById(R.id.titleText);
        descriptionText = view.findViewById(R.id.descriptionText);
        businessNameText = view.findViewById(R.id.businessNameText);
    }

    private void getArgumentsData() {
        if (getArguments() != null) {
            jobType = getArguments().getString("selectedCategory", "");
        }
    }

    private void loadDataFromViewModel() {
        Post post = createPostViewModel.getPost();
        if (post != null) {
            titleText.setText(post.getTitle());
            descriptionText.setText(post.getDescription());
            businessNameText.setText(post.getBusinessName());
            if (!TextUtils.isEmpty(post.getWorkType())) {
                jobCategory = post.getWorkType();
            }
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackButtonClicked());
        nextButton.setOnClickListener(v -> onNextButtonClicked());
    }

    private void showCategoryBasedOnJobType(View view) {
        View fullTimeGroup = view.findViewById(R.id.fullTimeRadioGroup);
        View partTimeGroup = view.findViewById(R.id.partTimeRadioGroup);
        View internshipGroup = view.findViewById(R.id.internshipRadioGroup);
        View volunteerGroup = view.findViewById(R.id.volunteerRadioGroup);
        View freelanceGroup = view.findViewById(R.id.freelanceRadioGroup);
        View otherGroup = view.findViewById(R.id.otherTypeRadioGroup);

        View[] allGroups = {fullTimeGroup, partTimeGroup, internshipGroup, volunteerGroup, freelanceGroup, otherGroup};
        for (View group : allGroups) {
            if (group != null) group.setVisibility(View.GONE);
        }

        switch (jobType) {
            case "Full time": if (fullTimeGroup != null) fullTimeGroup.setVisibility(View.VISIBLE); break;
            case "Part time": if (partTimeGroup != null) partTimeGroup.setVisibility(View.VISIBLE); break;
            case "Internship": if (internshipGroup != null) internshipGroup.setVisibility(View.VISIBLE); break;
            case "Volunteer": if (volunteerGroup != null) volunteerGroup.setVisibility(View.VISIBLE); break;
            case "Freelance": if (freelanceGroup != null) freelanceGroup.setVisibility(View.VISIBLE); break;
            case "Other": if (otherGroup != null) otherGroup.setVisibility(View.VISIBLE); break;
        }
    }

    private void setupCategorySelection(View view) {
        for (int radioId : allRadioIds) {
            RadioButton rb = view.findViewById(radioId);
            if (rb != null) {
                if (rb.getText().toString().equals(jobCategory)) {
                    rb.setChecked(true);
                    rb.setTag(true);
                }

                rb.setOnClickListener(v -> {
                    boolean isOtherButton = false;
                    for (int otherId : otherRadioIds) {
                        if (rb.getId() == otherId) {
                            isOtherButton = true;
                            break;
                        }
                    }

                    if (isOtherButton) {
                        showOtherCategoryDialog(rb);
                    } else {
                        handleRadioClick(rb, view);
                    }
                });
            }
        }
    }

    private void handleRadioClick(RadioButton rb, View view) {
        boolean wasChecked = rb.getTag() != null && (boolean) rb.getTag();
        uncheckAllRadios(view);

        if (wasChecked) {
            rb.setTag(false);
            jobCategory = "";
        } else {
            rb.setChecked(true);
            rb.setTag(true);
            jobCategory = rb.getText().toString();
        }
    }

    private void showOtherCategoryDialog(RadioButton otherRadioButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Job Category");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String inputText = input.getText().toString().trim();
            if (!TextUtils.isEmpty(inputText)) {
                uncheckAllRadios(getView());
                otherRadioButton.setChecked(true);
                otherRadioButton.setTag(true);
                otherRadioButton.setText(inputText);
                jobCategory = inputText;
            } else {
                Toast.makeText(getContext(), "Category cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            uncheckAllRadios(getView());
            jobCategory = "";
            dialog.cancel();
        });

        builder.show();
    }


    private void uncheckAllRadios(View view) {
        if (view == null) return;
        for (int id : allRadioIds) {
            RadioButton rb = view.findViewById(id);
            if (rb != null) {
                rb.setChecked(false);
                rb.setTag(false);
            }
        }
    }

    private void onNextButtonClicked() {
        if (!validateForm()) {
            return;
        }

        Post post = createPostViewModel.getPost();
        post.setJobCategory(jobType);
        post.setWorkType(jobCategory);
        post.setTitle(titleText.getText().toString().trim());
        post.setDescription(descriptionText.getText().toString().trim());
        post.setBusinessName(businessNameText.getText().toString().trim());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, new CreatePostLocationFragment())
                .addToBackStack(null)
                .commit();
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(titleText.getText().toString().trim())) {
            Toast.makeText(requireContext(), "Please enter a post title", Toast.LENGTH_SHORT).show();
            titleText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(descriptionText.getText().toString().trim())) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            descriptionText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(businessNameText.getText().toString().trim())) {
            Toast.makeText(requireContext(), "Please enter a business name", Toast.LENGTH_SHORT).show();
            businessNameText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(jobCategory)) {
            Toast.makeText(requireContext(), "Please select a job category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateProgress() {
        int progress = (int) ((currentStep / (float) totalSteps) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (stepText != null) stepText.setText(String.format(Locale.US, "Step %d of %d", currentStep, totalSteps));
    }

    private void onBackButtonClicked() {
        Post post = createPostViewModel.getPost();
        if (post != null) {
            post.setPostId(null);
            post.setEmployerId(null);
            post.setTitle(null);
            post.setDescription(null);
            post.setBusinessName(null);
            post.setLocation(null);
            post.setMapsLink(null);
            post.setAddressDetails(null);
            post.setWorkModel(null);
            post.setWorkHours(null);
            post.setWorkDays(null);
            post.setDayOff(null);
            post.setSalary(null);
            post.setWhyWorkHere(null);
            post.setJobCategory(null);
            post.setWorkType(null);
            post.setIndustry(null);
            post.setExperienceLevel(null);

            post.setImages(null);
            post.setRequirements(null);

            post.setBenefitFreeMeal(false);
            post.setBenefitMonthlyBonus(false);
            post.setBenefitOvertimePay(false);
            post.setBenefitUniformProvided(false);
            post.setBenefitStaffDiscounts(false);
            post.setBenefitHealthInsurance(false);
            post.setBenefitHoliday(false);
            post.setBenefitEndOfYearBonus(false);
            post.setBenefitEquipmentProvided(false);
            post.setBenefitInternetAllowance(false);
            post.setBenefitHotelProvided(false);
            post.setBenefitCertificate(false);
            post.setBenefitTransportProvided(false);

            post.setAmenityFreeWifi(false);
            post.setAmenityRestArea(false);
            post.setAmenityFlexibleBreaks(false);
            post.setAmenityParkingSpot(false);
            post.setAmenityLocker(false);
            post.setAmenityEmployeeEvents(false);
            post.setAmenityAirConditioned(false);
            post.setAmenitySafetyEquipment(false);

//            post.setBookmarked(false);
//            post.setAvailable(true);

            post.setLatitude(0.0);
            post.setLongitude(0.0);
            post.setTimestamp(0);
        }

        if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
