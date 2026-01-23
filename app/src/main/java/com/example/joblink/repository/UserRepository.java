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

        String uid = firebaseUser.getUid();
        user.setUid(uid);

        usersRef.child(uid)
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

        // Listens to the 'uid' node in the 'users' root
        usersRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Firebase automatically maps DB keys (photoURL, setupComplete, etc.)
                    // to your User model fields
                    User user = snapshot.getValue(User.class);
                    userLiveData.postValue(user);
                } else {
                    userLiveData.postValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userLiveData.postValue(null);
            }
        });

        return userLiveData;
    }

    public void updateUser(String uid, User user, final UserRepositoryCallback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError(new Exception("Invalid User ID."));
            return;
        }

        // Updates the entire user object at the specific uid node
        usersRef.child(uid)
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