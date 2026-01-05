package com.example.joblink.fragment;

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
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.model.User;
import com.example.joblink.viewmodel.ProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserInformationFragment extends Fragment {

    private TextView profileName, profileGender, profileAge, profession, email, phoneNumber, location, birthday;
    private ImageView profileImage;

    private ProfileViewModel viewModel;

    public UserInformationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupButtonClickListeners(view);
        observeViewModel();
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profileName);
        profileGender = view.findViewById(R.id.profileGender);
        profileAge = view.findViewById(R.id.profileAge);
        profession = view.findViewById(R.id.profession);
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        location = view.findViewById(R.id.location);
        birthday = view.findViewById(R.id.birthday);
        profileImage = view.findViewById(R.id.profileImage);
    }

    private void setupButtonClickListeners(View view) {
        View backButton = view.findViewById(R.id.imageView12);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            } else {
                Toast.makeText(getContext(), "User data not available.", Toast.LENGTH_SHORT).show();
                setDefaultValues();
            }
        });
    }

    private void updateUI(User user) {
        profileName.setText(getDisplayValue(user.getUsername()));
        profileGender.setText(getDisplayValue(user.getGender()));
        profession.setText(getDisplayValue(user.getProfession()));
        email.setText(getDisplayValue(user.getEmail()));
        phoneNumber.setText(getDisplayValue(user.getPhoneNumber()));
        location.setText(getDisplayValue(user.getLocation()));

        int age = viewModel.calculateAge(user.getDateOfBirth());
        profileAge.setText(String.valueOf(age));
        birthday.setText(formatBirthday(user.getDateOfBirth()));

        if (getContext() != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.img);
        }
    }

    private void setDefaultValues() {
        profileName.setText("User");
        email.setText("Not specified");
        profileGender.setText("Not specified");
        profileAge.setText("0");
        profession.setText("Not specified");
        phoneNumber.setText("Not specified");
        location.setText("Not specified");
        birthday.setText("Not specified");
        profileImage.setImageResource(R.drawable.img);
    }

    public String formatBirthday(long timestamp) {
        if (timestamp == 0) {
            return "Not specified";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception e) {
            System.err.println("Error formatting birthday: " + e.getMessage());
            return "Not specified";
        }
    }

    private String getDisplayValue(String value) {
        return (value != null && !value.isEmpty()) ? value : "Not specified";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(true);
        }
    }
}
