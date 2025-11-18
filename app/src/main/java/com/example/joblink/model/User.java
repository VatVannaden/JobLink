package com.example.joblink.model;

import androidx.annotation.NonNull;

public class User {
    private String name;
    private String email;
    private String userId;
    private String gender;
    private String phone;
    private String location;
    private String dob;
    private String profession;
    private String profileImageUrl;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String userId, String name, String email, String gender, String phone, String dob, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.location = location;
        this.dob = dob;
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getDob() {
        return dob;
    }

    public String getProfession() {
        return profession;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userId='" + userId + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", location='" + location + '\'' +
                ", dob='" + dob + '\'' +
                ", profession='" + profession + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
