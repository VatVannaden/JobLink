package com.example.joblink.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.LoginActivity;
import com.example.joblink.model.User;
import com.example.joblink.viewmodel.ProfileViewModel;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        profileGender.setText(user.getGender());

        String dobString = convertTimestampToString(user.getDateOfBirth());
        int age = viewModel.calculateAge(dobString);

        profileAge.setText(String.valueOf(age));

        if (getContext() != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.img);
        }
    }

    private String convertTimestampToString(long timestamp) {
        if (timestamp == 0) {
            return null;
        }
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private void setupClickListeners() {
        profileCard.setOnClickListener(v -> navigateTo(new UserInformationFragment(), "user_info"));
//        historyCard.setOnClickListener(v -> navigateTo(new HistoryFragment(), "history"));
//        myPostCard.setOnClickListener(v -> navigateTo(new MyPostFragment(), "my_post"));
        editAccount.setOnClickListener(v -> navigateTo(new EditProfileFragment(), "edit_profile"));

        signOut.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                new SignOutFragment().show(getActivity().getSupportFragmentManager(), "sign_out_dialog");
            }
        });
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