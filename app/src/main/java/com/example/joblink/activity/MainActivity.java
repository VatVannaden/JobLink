package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.main.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                navigateOnceWithDelay();
            }

            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {}

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {}

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {}
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.main.transitionToState(R.id.enlarged), 1000);
    }

    private void navigateOnceWithDelay() {
        if (hasNavigated) return;
        hasNavigated = true;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(MainActivity.this, StartScreen1Activity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
