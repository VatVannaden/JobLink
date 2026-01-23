package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            navigateToHome();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.signInButton.setOnClickListener(v -> userLogin());

        binding.passwordToggle.setOnClickListener(v -> togglePasswordVisibility());

        binding.signUpTx.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class))
        );
        binding.skip.setOnClickListener(v -> navigateToHome());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.txPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            binding.passwordToggle.setImageResource(R.drawable.ico_eye_slash);
        } else {
            binding.txPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            binding.passwordToggle.setImageResource(R.drawable.ico_eye_open);
        }
        isPasswordVisible = !isPasswordVisible;
        binding.txPassword.setSelection(binding.txPassword.getText().length());
    }

    private void userLogin() {
        binding.txEmail.setError(null);
        binding.txPassword.setError(null);

        String email = binding.txEmail.getText().toString().trim();
        String password = binding.txPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.txEmail.setError("Please enter email!");
            binding.txEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.txEmail.setError("Invalid email format!");
            binding.txEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.txPassword.setError("Password is required!");
            binding.txPassword.requestFocus();
            return;
        }

        setLoadingState(true, null);
        loginWithEmail(email, password);
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Runnable onAnimationComplete = () -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            String errorMessage = "Authentication failed. Please re-check your Email or Password.";
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    setLoadingState(false, onAnimationComplete);
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean isLoading, Runnable onFinished) {
        float targetAlpha = isLoading ? 1f : 0f;

        if (isLoading) {
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.loadingOverlay.setAlpha(0f);
            binding.loadingOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
        }

        binding.signInButton.setEnabled(!isLoading);

        binding.loadingOverlay.animate()
                .alpha(targetAlpha)
                .setDuration(500)
                .setStartDelay(isLoading ? 0 : 1500)
                .withEndAction(() -> {
                    if (!isLoading) {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        if (onFinished != null) {
                            onFinished.run();
                        }
                    }
                });
    }
}