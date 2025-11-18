package com.example.joblink.activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityHomeBinding;
import com.example.joblink.fragment.BookmarksFragment;
import com.example.joblink.fragment.CreatePostFragment;
import com.example.joblink.fragment.ExploreFragment;
import com.example.joblink.fragment.MessageFragment;
import com.example.joblink.fragment.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    public ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the listener for bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new ExploreFragment();
            } else if (itemId == R.id.nav_bookmarks) {
                selectedFragment = new BookmarksFragment();
            } else if (itemId == R.id.nav_create) {
                selectedFragment = new CreatePostFragment();
            } else if (itemId == R.id.nav_messages) {
                selectedFragment = new MessageFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                LoadFragment(selectedFragment);
            }

            return true;
        });

        // Set the default selected item
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Add a listener to handle back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment);
            if (currentFragment instanceof ExploreFragment) {
                binding.bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
            } else if (currentFragment instanceof BookmarksFragment) {
                binding.bottomNavigation.getMenu().findItem(R.id.nav_bookmarks).setChecked(true);
            } else if (currentFragment instanceof CreatePostFragment) {
                binding.bottomNavigation.getMenu().findItem(R.id.nav_create).setChecked(true);
            } else if (currentFragment instanceof MessageFragment) {
                binding.bottomNavigation.getMenu().findItem(R.id.nav_messages).setChecked(true);
            } else if (currentFragment instanceof ProfileFragment) {
                binding.bottomNavigation.getMenu().findItem(R.id.nav_profile).setChecked(true);
            }
        });
    }

    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragment, fragment)
                .addToBackStack(null) // This is crucial for the back button to work
                .commit();
    }

    public void showBottomNavigationView(boolean isVisible) {
        binding.bottomNavigation.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        // Handle back press to navigate through the fragment back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            finish(); // Exit the app if there are no more fragments in the back stack
        }
    }
}
