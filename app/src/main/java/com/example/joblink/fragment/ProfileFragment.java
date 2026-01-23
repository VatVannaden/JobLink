package com.example.joblink.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.activity.LoginActivity;
import com.example.joblink.model.User;
import com.example.joblink.viewmodel.ProfileViewModel;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileGender, profileAge, profilePostCount;
    private ImageView profileImage;
    private MaterialCardView profileCard, historyCard, myPostCard, editAccount;
    private View signOut;

    private ProfileViewModel viewModel;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeViews(view);
        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!viewModel.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }
        observeViewModel();
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profileName);
        profileGender = view.findViewById(R.id.profileGender);
        profileAge = view.findViewById(R.id.profileAge);
        profilePostCount = view.findViewById(R.id.postCount);
        profileImage = view.findViewById(R.id.profileImage);

        profileCard = view.findViewById(R.id.profileCard);
        historyCard = view.findViewById(R.id.historyCard);
        myPostCard = view.findViewById(R.id.myPostCard);

        editAccount = view.findViewById(R.id.editProfile);
        signOut = view.findViewById(R.id.signOut);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserInfoUI(user);
            }
        });

        viewModel.getPostCount().observe(getViewLifecycleOwner(), count -> {
            if (isAdded()) {
                profilePostCount.setText(String.valueOf(count != null ? count : 0));
            }
        });
    }

    private void updateUserInfoUI(User user) {
        if (!isAdded()) return;

        profileName.setText(user.getUsername());

        // Capitalize gender for display if stored as "male/female"
        String genderText = user.getGender();
        if (genderText != null && !genderText.isEmpty()) {
            genderText = genderText.substring(0, 1).toUpperCase() + genderText.substring(1);
        }
        profileGender.setText(genderText);

        // dob is already a String, just pass it directly
        int age = viewModel.calculateAge(user.getDob());
        profileAge.setText(String.valueOf(age));

        if (getContext() != null && user.getPhotoURL() != null && !user.getPhotoURL().isEmpty()) {
            Glide.with(this)
                    .load(user.getPhotoURL())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.img);
        }
    }

    private void setupClickListeners() {
        profileCard.setOnClickListener(v -> navigateTo(new UserInformationFragment(), "user_info"));
        historyCard.setOnClickListener(v -> navigateTo(new HistoryFragment(), "history"));

        // Updated My Post listener to show loading
        myPostCard.setOnClickListener(v -> {
            handleMyPostNavigation();
        });

        editAccount.setOnClickListener(v -> navigateTo(new EditProfileFragment(), "edit_profile"));

        signOut.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                new SignOutFragment().show(getActivity().getSupportFragmentManager(), "sign_out_dialog");
            }
        });
    }

    private void handleMyPostNavigation() {
        if (getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();

            // 1. Show the loading overlay
            homeActivity.setLoadingState(true, null);

            // 2. Perform the navigation immediately (it happens in background)
            navigateTo(new MyPostFragment(), "my_post");

            // 3. Hide loading (the 1.5s delay you set in HomeActivity will create the illusion)
            homeActivity.setLoadingState(false, null);
        } else {
            // Fallback if not inside HomeActivity
            navigateTo(new MyPostFragment(), "my_post");
        }
    }

    private void navigateTo(Fragment fragment, String backStackName) {
        if (isAdded() && getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragment, fragment)
                    .addToBackStack(backStackName)
                    .commit();
        }
    }

    private void redirectToLogin() {
        if (!isAdded() || getActivity() == null) return;

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}