package com.example.joblink.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.activity.HomeActivity;
import com.example.joblink.R;
import com.example.joblink.activity.LoginActivity;
import com.example.joblink.viewmodel.ProfileViewModel;

public class CreatePostFragment extends Fragment {

    private ProfileViewModel viewModel;

    public CreatePostFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!viewModel.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        setupButtonClickListeners(view);
    }

    private void setupButtonClickListeners(View view) {
        View backButton = view.findViewById(R.id.imageView2);
        View getStartedButton = view.findViewById(R.id.getStartedButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> handleBackButton());
        }

        if (getStartedButton != null) {
            getStartedButton.setOnClickListener(v -> handleGetStartedButton());
        }
    }

    private void handleBackButton() {
        if (isAdded() && getActivity() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    private void handleGetStartedButton() {
        if (isAdded() && getActivity() != null) {
            Fragment createPostCategoryFragment = new CreatePostCategoryFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.mainFragment, createPostCategoryFragment)
                    .addToBackStack("createPostCategoryFragment")
                    .commit();
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(true);
        }
    }

    private void redirectToLogin() {
        if (!isAdded()) return;

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
