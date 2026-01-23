package com.example.joblink.model;

import androidx.annotation.NonNull;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String uid;
    private String username;
    private String email;
    private String gender;
    private String phone;
    private String location;
    private String profession;
    private String photoURL;
    private String dob;
    private boolean setupComplete;
    private String updatedAt;
    private String resumeURL;

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(Object uid) {
        this.uid = uid == null ? null : String.valueOf(uid);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(Object phone) {
        this.phone = phone == null ? null : String.valueOf(phone);
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(Object dob) {
        this.dob = dob == null ? null : String.valueOf(dob);
    }

    public boolean isSetupComplete() {
        return setupComplete;
    }

    public void setSetupComplete(boolean setupComplete) {
        this.setupComplete = setupComplete;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt == null ? null : String.valueOf(updatedAt);
    }

    public String getResumeURL() {
        return resumeURL;
    }

    public void setResumeURL(String resumeURL) {
        this.resumeURL = resumeURL;
    }

    public String getUserId() {
        return uid;
    }

    public void setUserId(Object userId) {
        setUid(userId);
    }

    public String getPhoneNumber() {
        return phone;
    }

    public void setPhoneNumber(Object phoneNumber) {
        setPhone(phoneNumber);
    }

    public String getProfileImageUrl() {
        return photoURL;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.photoURL = profileImageUrl;
    }

    public long getDateOfBirth() {
        if (dob == null) return 0;
        try {
            return Long.parseLong(dob.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public void setDateOfBirth(Object dateOfBirth) {
        setDob(dateOfBirth);
    }

    public boolean isProfileComplete() {
        return setupComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.setupComplete = profileComplete;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", location='" + location + '\'' +
                ", profession='" + profession + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", dob='" + dob + '\'' +
                ", setupComplete=" + setupComplete +
                ", updatedAt='" + updatedAt + '\'' +
                ", resumeURL='" + resumeURL + '\'' +
                '}';
    }
}