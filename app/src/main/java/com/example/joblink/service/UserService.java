package com.example.joblink.service;

import com.example.joblink.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {

    // Get all users
    @GET("users.json")
    Call<List<User>> getAllUsers();

    // Get a single user by ID
    @GET("users/{id}.json")
    Call<User> getUser(@Path("id") String userId);

    // Create a new user
    @POST("users.json")
    Call<User> createUser(@Body User user);

    // Update an existing user
    @PUT("users/{id}.json")
    Call<User> updateUser(@Path("id") String userId, @Body User user);

    // Delete a user
    @DELETE("users/{id}.json")
    Call<Void> deleteUser(@Path("id") String userId);
}
