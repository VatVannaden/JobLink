package com.example.joblink.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.joblink.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserRepository {

    private final DatabaseReference usersRef;
    private final FirebaseAuth mAuth;

    public interface UserRepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public UserRepository() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
    }

    public void createUser(User user, final UserRepositoryCallback<Void> callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError(new Exception("No authenticated user found."));
            return;
        }
        String userId = firebaseUser.getUid();
        user.setUserId(userId);

        usersRef.child(userId)
                .setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            userLiveData.postValue(null);
            return userLiveData;
        }

        DatabaseReference currentUserRef = usersRef.child(firebaseUser.getUid());

        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    userLiveData.postValue(user);
                } else {
                    userLiveData.postValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Database read failed: " + error.getMessage());
                userLiveData.postValue(null);
            }
        });

        return userLiveData;
    }


    public void updateUser(String userId, User user, final UserRepositoryCallback<Void> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError(new Exception("Invalid User ID."));
            return;
        }
        usersRef.child(userId)
                .setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void signOut() {
        mAuth.signOut();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
