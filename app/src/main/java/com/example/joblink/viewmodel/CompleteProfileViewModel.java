package com.example.joblink.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CompleteProfileViewModel extends ViewModel {

    public enum ProfileDataStatus {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final MutableLiveData<ProfileDataStatus> _status = new MutableLiveData<>();
    public LiveData<ProfileDataStatus> getStatus() {
        return _status;
    }

    public void fetchInitialData() {
        _status.postValue(ProfileDataStatus.LOADING);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) {
            _status.postValue(ProfileDataStatus.ERROR);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    _status.postValue(ProfileDataStatus.SUCCESS);
                } else {
                    _status.postValue(ProfileDataStatus.ERROR);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Database read failed: " + error.getMessage());
                _status.postValue(ProfileDataStatus.ERROR);
            }
        });
    }
}
