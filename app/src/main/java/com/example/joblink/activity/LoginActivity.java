package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

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
        binding.signUpTx.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class))
        );
        binding.skip.setOnClickListener(v -> navigateToHome());
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

        setLoadingState(true);
        loginWithEmail(email, password);
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        String errorMessage = "Authentication failed. Please re-check your Email or Password.";
                        if (task.getException() != null) {
                            System.err.println("Login Error: " + task.getException().getMessage());
                        }

                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.signInButton.setEnabled(!isLoading);
    }
}
