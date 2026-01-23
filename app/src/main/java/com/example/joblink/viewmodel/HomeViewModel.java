package com.example.joblink.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeViewModel extends ViewModel {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public HomeViewModel() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void checkSetupStatus(SetupStatusCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            callback.onResult(false);
            return;
        }

        mDatabase.child("users").child(user.getUid()).child("setupComplete")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Boolean isComplete = snapshot.getValue(Boolean.class);
                            callback.onResult(isComplete != null && isComplete);
                        } else {
                            callback.onResult(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(false);
                    }
                });
    }

    public interface SetupStatusCallback {
        void onResult(boolean isComplete);
    }
}