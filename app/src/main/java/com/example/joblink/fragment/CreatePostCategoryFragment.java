package com.example.joblink.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.joblink.R;
import com.example.joblink.activity.HomeActivity;
import com.google.android.material.card.MaterialCardView;

public class CreatePostCategoryFragment extends Fragment {

    private MaterialCardView fullTimeCard, partTimeCard, freelanceCard, volunteerCard, internshipCard, otherCard;
    private View backButton;

    public CreatePostCategoryFragment() {}

    public static CreatePostCategoryFragment newInstance() {
        return new CreatePostCategoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
        setupLongPressListeners();
    }

    private void initializeViews(View view) {
        fullTimeCard = view.findViewById(R.id.fullTimeCard);
        partTimeCard = view.findViewById(R.id.partTimeCard);
        freelanceCard = view.findViewById(R.id.freelanceCard);
        volunteerCard = view.findViewById(R.id.volunteerCard);
        internshipCard = view.findViewById(R.id.internshipCard);
        otherCard = view.findViewById(R.id.otherCard);
        backButton = view.findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackButtonClicked());
        fullTimeCard.setOnClickListener(v -> navigateToAboutFragment("Full time"));
        partTimeCard.setOnClickListener(v -> navigateToAboutFragment("Part time"));
        freelanceCard.setOnClickListener(v -> navigateToAboutFragment("Freelance"));
        volunteerCard.setOnClickListener(v -> navigateToAboutFragment("Volunteer"));
        internshipCard.setOnClickListener(v -> navigateToAboutFragment("Internship"));
        otherCard.setOnClickListener(v -> navigateToAboutFragment("Other"));
    }

    private void setupLongPressListeners() {
        View.OnLongClickListener longClickListener = v -> {
            String description = "";
            int viewId = v.getId();
            if (viewId == R.id.fullTimeCard) description = "Full time employment typically involves 35-40 hours per week with benefits";
            else if (viewId == R.id.partTimeCard) description = "Part time work involves fewer hours than full-time, often without benefits";
            else if (viewId == R.id.freelanceCard) description = "Freelance work allows you to self-employed and hired to work for different companies on particular assignments.";
            else if (viewId == R.id.volunteerCard) description = "Volunteer positions are unpaid roles for gaining experience or helping communities";
            else if (viewId == R.id.internshipCard) description = "Internships provide practical experience for students or recent graduates";
            else if (viewId == R.id.otherCard) description = "Other types of work arrangements not covered above";

            if (!description.isEmpty()) {
                Toast.makeText(requireContext(), description, Toast.LENGTH_LONG).show();
            }
            return true;
        };

        fullTimeCard.setOnLongClickListener(longClickListener);
        partTimeCard.setOnLongClickListener(longClickListener);
        freelanceCard.setOnLongClickListener(longClickListener);
        volunteerCard.setOnLongClickListener(longClickListener);
        internshipCard.setOnLongClickListener(longClickListener);
        otherCard.setOnLongClickListener(longClickListener);
    }

    private void navigateToAboutFragment(String category) {
        Toast.makeText(requireContext(), "Selected: " + category, Toast.LENGTH_SHORT).show();
        Fragment createPostAboutFragment = new CreatePostAboutFragment();
        Bundle args = new Bundle();
        args.putString("selectedCategory", category);
        createPostAboutFragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFragment, createPostAboutFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void onBackButtonClicked() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(false);
        }
    }
}
