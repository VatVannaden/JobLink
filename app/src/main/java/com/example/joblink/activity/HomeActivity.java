package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityHomeBinding;
import com.example.joblink.fragment.BookmarksFragment;
import com.example.joblink.fragment.CreatePostFragment;
import com.example.joblink.fragment.ExploreFragment;
import com.example.joblink.fragment.MessageFragment;
import com.example.joblink.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    public ActivityHomeBinding binding;
    // mAuth is not used here anymore, can be moved to ProfileFragment
    // private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // mAuth = FirebaseAuth.getInstance(); // Not needed here

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
                // FIX: Only load the ProfileFragment. Do NOT start a new activity.
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                LoadFragment(selectedFragment);
            }

            // Return true to show the item as selected
            return true;
        });

        // Set the default selected item
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragment, fragment)
                .commit();
    }
}
