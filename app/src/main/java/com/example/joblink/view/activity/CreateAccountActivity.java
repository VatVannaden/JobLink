package com.example.joblink.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityCreateAccountBinding;
import com.example.joblink.viewmodel.CreateAccountViewModel;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    private CreateAccountViewModel viewModel;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

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

        binding.passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            toggleVisibility(binding.txPassword, binding.passwordToggle, isPasswordVisible);
        });

        binding.confirmPasswordToggle.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            toggleVisibility(binding.txConfirmPassword, binding.confirmPasswordToggle, isConfirmPasswordVisible);
        });
    }

    private void toggleVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ico_eye_open);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ico_eye_slash);
        }
        editText.setSelection(editText.getText().length());
    }

    private void observeViewModel() {
        viewModel.getAuthResult().observe(this, authResult -> {
            switch (authResult.status) {
                case LOADING:
                    setLoadingState(true, null);
                    break;

                case SUCCESS:
                    Runnable successAction = () -> {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToCompleteProfile();
                    };
                    setLoadingState(false, successAction);
                    break;

                case ERROR:
                    Runnable errorAction = () -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, authResult.errorMessage, Toast.LENGTH_LONG).show();
                        }
                    };
                    setLoadingState(false, errorAction);
                    break;
            }
        });
    }

    private void navigateToCompleteProfile() {
        Intent intent = new Intent(CreateAccountActivity.this, CompleteProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void setLoadingState(boolean isLoading, Runnable onFinished) {
        float targetAlpha = isLoading ? 1.0f : 0.0f;

        if (isLoading) {
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.loadingOverlay.setAlpha(0.0f);
            binding.loadingOverlay.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
        }

        binding.signUpButton.setEnabled(!isLoading);

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