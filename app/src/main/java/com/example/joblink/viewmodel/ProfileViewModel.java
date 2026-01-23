package com.example.joblink.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.joblink.model.User;
import com.example.joblink.repository.PostRepository;
import com.example.joblink.repository.UserRepository;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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
        if (dateOfBirthString == null || dateOfBirthString.isEmpty() || dateOfBirthString.equals("Not specified")) {
            return 0;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
            LocalDate birthDate = LocalDate.parse(dateOfBirthString, formatter);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void signOut() {
        userRepository.signOut();
    }
}