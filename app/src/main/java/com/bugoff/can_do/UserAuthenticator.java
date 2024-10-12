package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

// Intended to be a purely stateless class
public class UserAuthenticator {
    /**
     * Method to authenticate a user using the given UserRepository.
     * If the user exists in Firestore, it logs that the user was found.
     * If the user does not exist, it creates a new user and adds it to Firestore.
     *
     * @param userRepository The repository used to access user data.
     * @param androidId The Android ID of the user to authenticate.
     */
    public static void authenticateUser(@NonNull UserRepository userRepository, @NonNull String androidId) {
        // Get user from Firestore
        userRepository.getUser(androidId,
                UserAuthenticator::onUserFound,
                e -> onUserNotFound(userRepository, androidId));
    }

    /**
     * Callback method to handle when a user is found in Firestore.
     * Logs the user's Android ID.
     *
     * @param user The user that was found in Firestore.
     */
    private static void onUserFound(@NonNull User user) {
        Log.d("UserAuthenticator", "User found: " + user.getAndroidId());
    }

    /**
     * Callback method to handle when a user is not found in Firestore.
     * Creates a new user with the given Android ID and adds it to Firestore.
     *
     * @param userRepository The repository used to add the new user.
     * @param androidId The Android ID of the user to add.
     */
    private static void onUserNotFound(@NonNull UserRepository userRepository, @NonNull String androidId) {
        User newUser = new User(androidId);
        userRepository.addUser(newUser,
                aVoid -> onUserAdded(newUser),
                UserAuthenticator::onUserAddError);
    }

    /**
     * Callback method to handle when a user is successfully added to Firestore.
     * Logs the user's Android ID.
     *
     * @param user The user that was successfully added to Firestore.
     */
    private static void onUserAdded(@NonNull User user) {
        Log.d("UserAuthenticator", "User added: " + user.getAndroidId());
    }

    /**
     * Callback method to handle an error that occurred while adding a user to Firestore.
     * Logs the error message.
     *
     * @param e The exception that occurred during the user addition process.
     */
    private static void onUserAddError(Exception e) {
        Log.e("UserAuthenticator", "Error adding user", e);
    }
}
