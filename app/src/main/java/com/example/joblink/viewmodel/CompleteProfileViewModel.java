package com.example.joblink.viewmodel;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.joblink.model.User;
import com.example.joblink.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class CompleteProfileViewModel extends ViewModel {

    public enum ProfileDataStatus {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final MutableLiveData<ProfileDataStatus> _status = new MutableLiveData<>(ProfileDataStatus.IDLE);
    public LiveData<ProfileDataStatus> getStatus() {
        return _status;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final UserRepository userRepository;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    public CompleteProfileViewModel() {
        userRepository = new UserRepository();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void saveUserProfile(Uri imageUri, User userUpdates) {
        _status.postValue(ProfileDataStatus.LOADING);

        FirebaseUser fUser = auth.getCurrentUser();
        if (fUser == null) {
            _errorMessage.postValue("Authentication error. Please log in again.");
            _status.postValue(ProfileDataStatus.ERROR);
            return;
        }

        uploadImageAndSaveProfile(fUser, imageUri, userUpdates);
    }

    private void uploadImageAndSaveProfile(FirebaseUser firebaseUser, Uri imageUri, User userUpdates) {
        String userId = firebaseUser.getUid();
        StorageReference imageRef = storage.getReference().child("profile_images").child(userId + ".jpg");

        imageRef.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                saveUserProfileToDatabase(firebaseUser, downloadUri.toString(), userUpdates);
            } else {
                _errorMessage.postValue("Image upload failed: " + Objects.requireNonNull(task.getException()).getMessage());
                _status.postValue(ProfileDataStatus.ERROR);
            }
        });
    }

    private void saveUserProfileToDatabase(FirebaseUser firebaseUser, String profileImageUrl, User userUpdates) {
        String userId = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User existingUser;
                if (snapshot.exists()) {
                    existingUser = snapshot.getValue(User.class);
                    if (existingUser == null) {
                        existingUser = new User();
                        existingUser.setUserId(userId);
                        existingUser.setEmail(firebaseUser.getEmail());
                        existingUser.setUsername("User");
                    }
                } else {
                    existingUser = new User();
                    existingUser.setUserId(userId);
                    existingUser.setEmail(firebaseUser.getEmail());
                    existingUser.setUsername("User");
                }

                existingUser.setProfileImageUrl(profileImageUrl);
                existingUser.setPhoneNumber(userUpdates.getPhoneNumber());
                existingUser.setGender(userUpdates.getGender());
                existingUser.setLocation(userUpdates.getLocation());
                existingUser.setProfession(userUpdates.getProfession());
                existingUser.setDateOfBirth(userUpdates.getDateOfBirth());
                existingUser.setProfileComplete(true);

                userRepository.updateUser(userId, existingUser, new UserRepository.UserRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _status.postValue(ProfileDataStatus.SUCCESS);
                    }

                    @Override
                    public void onError(Exception e) {
                        _errorMessage.postValue("Failed to save profile: " + e.getMessage());
                        _status.postValue(ProfileDataStatus.ERROR);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _errorMessage.postValue("Failed to read user data before saving: " + error.getMessage());
                _status.postValue(ProfileDataStatus.ERROR);
            }
        });
    }
}
