package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.databinding.ActivityCreateAccountBinding;
import com.example.joblink.model.User;
import com.example.joblink.repository.UserRepository;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = new UserRepository();

        binding.signUpButton.setOnClickListener(v -> createUser());

        binding.signTnTx.setOnClickListener(v -> {
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        });
    }

    private void createUser() {
        String username = binding.txFullName.getText().toString().trim();
        String email = binding.txEmail.getText().toString().trim();
        String password = binding.txPassword.getText().toString().trim();
        String confirmPassword = binding.txConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.txFullName.setError("Username is required!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.txEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.txPassword.setError("Password is required!");
            return;
        }
        if (password.length() < 8) {
            binding.txPassword.setError("Password must be at least 8 characters!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.txConfirmPassword.setError("Passwords do not match!");
            return;
        }

        User user = new User(username, email);

        userRepository.createUser(user, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                Toast.makeText(CreateAccountActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreateAccountActivity.this, CompleteProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                intent.putExtra("continueWithGoogle", false);

                System.out.println("SENDING - Username: " + username + ", Email: " + email);


                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(CreateAccountActivity.this, "Registration failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}