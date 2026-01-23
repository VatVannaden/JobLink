package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean hasNavigated = false;
    private boolean isAnimationFinished = false;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Initialize the AuthStateListener
        authStateListener = firebaseAuth -> {
            // Only trigger navigation if the animation has also finished
            if (isAnimationFinished) {
                checkAndNavigate();
            }
        };

        binding.main.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                isAnimationFinished = true;
                // Transition finished, now check if we can navigate
                checkAndNavigate();
            }

            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {}

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {}

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {}
        });

        // Start splash animation after 1 second
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.main.transitionToState(R.id.enlarged), 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for Auth changes
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Clean up listener
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void checkAndNavigate() {
        if (hasNavigated || !isAnimationFinished) return;
        
        // Wait a tiny bit (500ms) to ensure Firebase has fully initialized its local token
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (hasNavigated) return;
            hasNavigated = true;

            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(MainActivity.this, StartScreen1Activity.class);
            }
            startActivity(intent);
            finish();
        }, 500);
    }
}
