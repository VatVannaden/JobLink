package com.example.joblink.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.joblink.model.User;
import com.example.joblink.repository.PostRepository;
import com.example.joblink.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final LiveData<User> userLiveData;
    private final LiveData<Long> postCountLiveData;

    public ProfileViewModel() {
        userRepository = new UserRepository();
        postRepository = new PostRepository();

        userLiveData = userRepository.getCurrentUser();
        postCountLiveData = postRepository.getPostCountForCurrentUser();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<Long> getPostCount() {
        return postCountLiveData;
    }

    public boolean isUserLoggedIn() {
        return userRepository.isUserLoggedIn();
    }

    public int calculateAge(String dateOfBirthString) {
        if (dateOfBirthString == null || dateOfBirthString.isEmpty()) {
            return 0;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
            LocalDate birthDate = LocalDate.parse(dateOfBirthString, formatter);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            System.err.println("Error parsing date of birth: " + e.getMessage());
            return 0;
        }
    }

    public int calculateAge(long timestamp) {
        if (timestamp == 0) {
            return 0;
        }
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateOfBirthString = sdf.format(date);

        return calculateAge(dateOfBirthString);
    }

    public void signOut() {
        userRepository.signOut();
    }
}