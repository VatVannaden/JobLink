package com.example.joblink.repository;

import com.example.joblink.model.User;
import com.example.joblink.service.UserService;
import com.example.joblink.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private UserService userService;

    public UserRepository() {
        userService = RetrofitClient.getClient().create(UserService.class);
    }

//    public void getAllUsers(final RepositoryCallback<List<User>> callback) {
//        Call<List<User>> call = userService.getAllUsers();
//        call.enqueue(new Callback<List<User>>() {
//            @Override
//            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError(new Exception("Failed to fetch users"));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<User>> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }

    // Get a single user by ID
    public void getUser(String userId, final RepositoryCallback<User> callback) {
        Call<User> call = userService.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Create a new user
    public void createUser(User user, final RepositoryCallback<User> callback) {
        Call<User> call = userService.createUser(user);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to create user"));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Update an existing user
    public void updateUser(String userId, User user, final RepositoryCallback<User> callback) {
        Call<User> call = userService.updateUser(userId, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to update user"));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Delete a user
    public void deleteUser(String userId, final RepositoryCallback<Void> callback) {
        Call<Void> call = userService.deleteUser(userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Exception("Failed to delete user"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Callback interface to return data/errors
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }
}
