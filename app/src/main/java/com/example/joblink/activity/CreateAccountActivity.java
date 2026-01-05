package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.databinding.ActivityCreateAccountBinding;
import com.example.joblink.viewmodel.CreateAccountViewModel;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    private CreateAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.signUpButton.setOnClickListener(v -> attemptCreateUser());
        binding.signTnTx.setOnClickListener(v ->
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class)));
    }

    private void observeViewModel() {
        viewModel.getAuthResult().observe(this, authResult -> {
            switch (authResult.status) {
                case LOADING:
                    setLoadingState(true);
                    break;

                case SUCCESS:
                    setLoadingState(false);
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    navigateToCompleteProfile();
                    break;

                case ERROR:
                    setLoadingState(false);
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, authResult.errorMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
    }

    private void navigateToCompleteProfile() {
        Intent intent = new Intent(CreateAccountActivity.this, CompleteProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void attemptCreateUser() {
        clearInputErrors();

        String username = binding.txFullName.getText().toString().trim();
        String email = binding.txEmail.getText().toString().trim();
        String password = binding.txPassword.getText().toString().trim();
        String confirmPassword = binding.txConfirmPassword.getText().toString().trim();

        if (isInputValid(username, email, password, confirmPassword)) {
            viewModel.createUser(username, email, password);
        }
    }

    private void setLoadingState(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.signUpButton.setEnabled(!isLoading);
    }

    private void clearInputErrors() {
        binding.txFullName.setError(null);
        binding.txEmail.setError(null);
        binding.txPassword.setError(null);
        binding.txConfirmPassword.setError(null);
    }

    private boolean isInputValid(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            binding.txFullName.setError("Username is required!");
            binding.txFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            binding.txEmail.setError("Email is required!");
            binding.txEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.txEmail.setError("Please enter a valid email address!");
            binding.txEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.txPassword.setError("Password is required!");
            binding.txPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            binding.txPassword.setError("Password must be at least 8 characters!");
            binding.txPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.txConfirmPassword.setError("Passwords do not match!");
            binding.txConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }
}
