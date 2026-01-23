package com.example.joblink.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CreateAccountViewModel extends ViewModel {

    private static final String TAG = "CreateAccountViewModel";

    public enum AuthStatus {
        LOADING,
        SUCCESS,
        ERROR
    }

    public static class AuthResult {
        public AuthStatus status;
        public String errorMessage;

        private AuthResult(AuthStatus status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public static AuthResult loading() {
            return new AuthResult(AuthStatus.LOADING, null);
        }

        public static AuthResult success() {
            return new AuthResult(AuthStatus.SUCCESS, null);
        }

        public static AuthResult error(String message) {
            return new AuthResult(AuthStatus.ERROR, message);
        }
    }

    private final FirebaseAuth mAuth;
    private final DatabaseReference databaseReference;
    private final MutableLiveData<AuthResult> authResult = new MutableLiveData<>();

    public CreateAccountViewModel() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public void createUser(String username, String email, String password) {
        authResult.postValue(AuthResult.loading());
        Log.d(TAG, "createUser: Status set to LOADING.");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUser: Firebase Auth user created successfully.");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserEntry(firebaseUser, username);
                        } else {
                            Log.e(TAG, "createUser: Firebase user is null after successful creation.");
                            authResult.postValue(AuthResult.error("An unknown error occurred."));
                        }
                    } else {
                        Log.e(TAG, "createUser: Firebase Auth creation failed.", task.getException());
                        if (task.getException() != null) {
                            authResult.postValue(AuthResult.error(task.getException().getMessage()));
                        } else {
                            authResult.postValue(AuthResult.error("An unknown error occurred during registration."));
                        }
                    }
                });
    }

    private void createUserEntry(FirebaseUser firebaseUser, String username) {
        // Format ISO timestamp to match Web: "2026-01-19T11:04:23.553Z"
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = isoFormat.format(new Date());

        Map<String, Object> user = new HashMap<>();
        user.put("uid", firebaseUser.getUid());
        user.put("username", username);
        user.put("email", firebaseUser.getEmail());
        user.put("setupComplete", false);
        user.put("updatedAt", timestamp);

        databaseReference.child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserEntry: Realtime Database entry created. Setting status to SUCCESS.");
                        authResult.postValue(AuthResult.success());
                    } else {
                        Log.e(TAG, "createUserEntry: Failed to create Realtime Database entry.", task.getException());
                        authResult.postValue(AuthResult.error("Failed to save user profile."));
                    }
                });
    }
}