package com.example.joblink.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityHomeBinding;
import com.example.joblink.view.activity.fragment.BookmarksFragment;
import com.example.joblink.view.activity.fragment.CreatePostFragment;
import com.example.joblink.view.activity.fragment.HomeFragment;
import com.example.joblink.view.activity.fragment.SearchFragment;
import com.example.joblink.view.activity.fragment.ProfileFragment;
import com.example.joblink.viewmodel.HomeViewModel;

public class HomeActivity extends AppCompatActivity {

    public ActivityHomeBinding binding;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 1. Setup listeners
        setupNavigation();

        // 2. Check user and load initial fragment
        checkUserStatus();
    }

    private void checkUserStatus() {
        setLoadingState(true, null);

        if (!viewModel.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        viewModel.checkSetupStatus(isComplete -> {
            if (!isComplete) {
                setLoadingState(false, () -> {
                    Intent intent = new Intent(HomeActivity.this, CompleteProfileActivity.class);
                    startActivity(intent);
                    finish();
                });
            } else {
                // Load HomeFragment immediately in the background
                if (getSupportFragmentManager().findFragmentById(R.id.mainFragment) == null) {
                    LoadFragment(new HomeFragment());
                    binding.bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
                }
                // The overlay will stay up for 1.5s based on the startDelay below
                setLoadingState(false, null);
            }
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (binding.bottomNavigation.getSelectedItemId() == itemId) return false;

            Fragment selectedFragment = null;
            boolean useLoading = false;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                useLoading = true;
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_create) {
                selectedFragment = new CreatePostFragment();
            } else if (itemId == R.id.nav_bookmark) {
                selectedFragment = new BookmarksFragment();
                useLoading = true;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                useLoading = true;
            }

            if (selectedFragment != null) {
                if (useLoading) {
                    setLoadingState(true, null);
                    LoadFragment(selectedFragment);
                    setLoadingState(false, null);
                } else {
                    LoadFragment(selectedFragment);
                }
            }
            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncBottomNavigation);
    }

    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.mainFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void setLoadingState(boolean isLoading, Runnable onFinished) {
        if (isLoading) {
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.loadingOverlay.setAlpha(1.0f);
            binding.loadingOverlay.bringToFront();
        } else {
            binding.loadingOverlay.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setStartDelay(2000)
                    .withEndAction(() -> {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        if (onFinished != null) onFinished.run();
                    })
                    .start();
        }
    }

    private void syncBottomNavigation() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        int menuId = -1;
        if (currentFragment instanceof HomeFragment) menuId = R.id.nav_home;
        else if (currentFragment instanceof SearchFragment) menuId = R.id.nav_search;
        else if (currentFragment instanceof CreatePostFragment) menuId = R.id.nav_create;
        else if (currentFragment instanceof BookmarksFragment) menuId = R.id.nav_bookmark;
        else if (currentFragment instanceof ProfileFragment) menuId = R.id.nav_profile;

        if (menuId != -1) {
            binding.bottomNavigation.getMenu().findItem(menuId).setChecked(true);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showBottomNavigationView(boolean isVisible) {
        binding.bottomNavigation.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            finish();
        }
    }
}