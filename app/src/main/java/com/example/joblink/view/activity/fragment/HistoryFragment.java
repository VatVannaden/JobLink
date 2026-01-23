package com.example.joblink.view.activity.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.joblink.view.activity.HomeActivity;
import com.example.joblink.R;

public class HistoryFragment extends Fragment {
    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtonClickListeners(view);
    }

    private void setupButtonClickListeners(View view) {
        View backButton = view.findViewById(R.id.imageView12);

        if (backButton != null) {
            backButton.setOnClickListener(v -> handleBackButton());
        }
    }

    private void handleBackButton() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
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
}
