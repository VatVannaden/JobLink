package com.example.joblink.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.LoginActivity;
import com.example.joblink.model.User;
import com.example.joblink.viewmodel.ProfileViewModel;
import com.google.android.material.button.MaterialButton;

public class SignOutFragment extends DialogFragment {

    private ProfileViewModel viewModel;
    private TextView usernameTextView;
    private ImageView profileImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_out, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        observeViewModel();
    }

    private void initializeViews(View view) {
        Button cancelButton = view.findViewById(R.id.cancelButton);
        MaterialButton signOutButton = view.findViewById(R.id.signOutButton);
        ImageView x = view.findViewById(R.id.x);

        usernameTextView = view.findViewById(R.id.username);
        profileImageView = view.findViewById(R.id.profileImage);

        x.setOnClickListener(v -> dismiss());
        cancelButton.setOnClickListener(v -> dismiss());
        signOutButton.setOnClickListener(v -> {
            signOutUser();
            dismiss();
        });
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && getContext() != null) {
                updateUI(user);
            } else {
                usernameTextView.setText("User");
                profileImageView.setImageResource(R.drawable.img);
            }
        });
    }

    private void updateUI(User user) {
        usernameTextView.setText(user.getUsername());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.img);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setDimAmount(0.6f);
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void signOutUser() {
        try {
            viewModel.signOut();

            if (getContext() != null) {
                SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                preferences.edit().clear().apply();
            }

            redirectToLogin();

        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Error during sign out", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToLogin() {
        if (getContext() == null) return;
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
