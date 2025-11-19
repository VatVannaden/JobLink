package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.activity.CreateAccountActivity;
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.databinding.ActivityLoginBinding;

import android.text.TextUtils;
import android.widget.Toast;

import android.util.Patterns;
import com.google.firebase.auth.FirebaseAuth;

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

        binding.signInButton.setOnClickListener(v -> userLogin());

        binding.signUpTx.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        binding.skip.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeActivity.class)));
    }

    private void userLogin() {
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

        loginWithEmail(email, password);
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}