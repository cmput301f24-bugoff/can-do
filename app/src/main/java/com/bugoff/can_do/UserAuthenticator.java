package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

// Intended to be a purely stateless class
public class UserAuthenticator {
    // Method to authenticate user
    public static void authenticateUser(@NonNull UserRepository userRepository, @NonNull String androidId) {
        // Get user from Firestore
        userRepository.getUser(androidId,
                UserAuthenticator::onUserFound,
                e -> onUserNotFound(userRepository, androidId));
    }

    // Handle user found
    private static void onUserFound(@NonNull User user) {
        Log.d("UserAuthenticator", "User found: " + user.getAndroidId());
    }

    // Handle user not found and add the user to Firestore
    private static void onUserNotFound(@NonNull UserRepository userRepository, @NonNull String androidId) {
        User newUser = new User(androidId);
        userRepository.addUser(newUser,
                aVoid -> onUserAdded(newUser),
                UserAuthenticator::onUserAddError);
    }

    // Handle successful user addition
    private static void onUserAdded(@NonNull User user) {
        Log.d("UserAuthenticator", "User added: " + user.getAndroidId());
    }

    // Handle error in user addition
    private static void onUserAddError(Exception e) {
        Log.e("UserAuthenticator", "Error adding user", e);
    }
}
