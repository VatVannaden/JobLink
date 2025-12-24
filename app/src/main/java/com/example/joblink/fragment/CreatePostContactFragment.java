package com.example.joblink.fragment;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.viewmodel.CreatePostViewModel;
import com.example.joblink.model.Post;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreatePostContactFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Post post;

    private EditText phoneNumberText;
    private EditText emailText;
    private EditText telegramLinkText;
    private MaterialCardView nextButton;
    private MaterialCardView backButton;
    private ProgressBar progressBar;
    private TextView stepText;

    private DatabaseReference userDatabaseReference;
    private FirebaseUser currentUser;

    private final int currentStep = 6;
    private final int totalSteps = 6;

    public CreatePostContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = viewModel.getPost();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        }

        initializeViews(view);
        setupListeners();
        restoreUIState();
        updateProgress();
    }

    private void initializeViews(View view) {
        phoneNumberText = view.findViewById(R.id.phoneNumberText);
        emailText = view.findViewById(R.id.emailText);
        telegramLinkText = view.findViewById(R.id.telegramLinkText);
        nextButton = view.findViewById(R.id.nextButton);
        backButton = view.findViewById(R.id.backButton);
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
    }

    private void restoreUIState() {
        if (post.getPhoneNumber() != null || post.getEmail() != null) {
            phoneNumberText.setText(post.getPhoneNumber());
            emailText.setText(post.getEmail());
            telegramLinkText.setText(post.getTelegramLink());
        } else if (userDatabaseReference != null) {
            userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String phone = snapshot.child("phoneNumber").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        phoneNumberText.setText(phone);
                        emailText.setText(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveDataToViewModel();
                navigateToNextFragment();
            }
        });
    }

    private boolean validateInput() {
        String phoneNumber = phoneNumberText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String telegram = telegramLinkText.getText().toString().trim();

        if (phoneNumber.isEmpty() && email.isEmpty() && telegram.isEmpty()) {
            Toast.makeText(getContext(), "Please provide at least one contact method.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private void saveDataToViewModel() {
        if (post != null) {
            post.setPhoneNumber(phoneNumberText.getText().toString().trim());
            post.setEmail(emailText.getText().toString().trim());
            post.setTelegramLink(telegramLinkText.getText().toString().trim());
        }
    }

    private void navigateToNextFragment() {
        CreatePostImageFragment nextFragment = new CreatePostImageFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, nextFragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateProgress() {
        progressBar.setProgress(100);
        stepText.setText("Step " + currentStep + " of " + totalSteps);
    }
}
