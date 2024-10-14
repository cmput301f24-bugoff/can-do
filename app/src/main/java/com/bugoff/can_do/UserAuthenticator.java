package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

// Intended to be a purely stateless class
public class UserAuthenticator {
    private static final String TAG = "UserAuthenticator";

    /**
     * Authenticates a user by their Android ID.
     * If the user exists, returns the user.
     * If not, creates a new user and returns it.
     *
     * @param globalRepository The repository to interact with Firestore.
     * @param androidId      The Android ID of the user.
     * @return A Task that resolves to the authenticated User.
     */
    @NonNull
    public static Task<User> authenticateUser(@NonNull GlobalRepository globalRepository,
                                              @NonNull String androidId) {
        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();

        // Attempt to get the user from Firestore
        globalRepository.getUser(androidId)
                .addOnSuccessListener(user -> {
                    // User found, set the result
                    Log.d(TAG, "User found: " + user.getAndroidId());
                    taskCompletionSource.setResult(user);
                })
                .addOnFailureListener(e -> {
                    // User not found, attempt to create a new user
                    Log.d(TAG, "User not found: " + androidId + ". Creating new user.");
                    onUserNotFound(globalRepository, androidId, taskCompletionSource);
                });
        return taskCompletionSource.getTask();
    }

    /**
     * Handles the scenario where the user is not found.
     * Creates a new user and adds it to Firestore.
     *
     * @param globalRepository       The repository to interact with Firestore.
     * @param androidId            The Android ID of the user.
     * @param taskCompletionSource The TaskCompletionSource to set the Task's outcome.
     */
    private static void onUserNotFound(@NonNull GlobalRepository globalRepository,
                                       @NonNull String androidId,
                                       TaskCompletionSource<User> taskCompletionSource) {
        User newUser = new User(androidId);
        // Add the new user to Firestore
        globalRepository.addUser(newUser)
                .addOnSuccessListener(aVoid -> {
                    // User successfully added, set the result
                    Log.d(TAG, "User added: " + newUser.getAndroidId());
                    taskCompletionSource.setResult(newUser);
                })
                .addOnFailureListener(e -> {
                    // Error adding user, set the exception
                    Log.e(TAG, "Error adding user", e);
                    taskCompletionSource.setException(e);
                });
    }
}
