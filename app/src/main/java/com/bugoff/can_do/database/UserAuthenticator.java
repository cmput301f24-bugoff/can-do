package com.bugoff.can_do.database;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/**
 * Authenticates users by their Android ID.
 */
public class UserAuthenticator {
    private static final String TAG = "UserAuthenticator";
    private static Task<User> mockTask = null;
    private static GlobalRepository repository;

    static {
        repository = new GlobalRepository();
    }

    // For testing only
    @VisibleForTesting
    public static void setMockTask(Task<User> task) {
        mockTask = task;
    }

    // For testing only
    @VisibleForTesting
    public static void setRepository(GlobalRepository repo) {
        repository = repo;
    }

    /**
     * Authenticates a user by their Android ID.
     * If the user exists, returns the user.
     * If not, creates a new user and returns it.
     *
     * @param androidId The Android ID of the user.
     * @return A Task that resolves to the authenticated User.
     */
    @NonNull
    public static Task<User> authenticateUser(@NonNull String androidId) {
        if (mockTask != null && GlobalRepository.isInTestMode()) {
            return mockTask;
        }

        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();

        // Attempt to get the user from repository
        Task<User> getUserTask = GlobalRepository.getUser(androidId);

        getUserTask.addOnSuccessListener(user -> {
            Log.d(TAG, "User found: " + user.getId());
            taskCompletionSource.setResult(user);
        }).addOnFailureListener(e -> {
            // User not found, attempt to create a new user
            Log.d(TAG, "User not found: " + androidId + ". Creating new user.");
            onUserNotFound(androidId, taskCompletionSource);
        });

        return taskCompletionSource.getTask();
    }

    /**
     * Handles the scenario where the user is not found.
     * Creates a new user and adds it to the repository.
     */
    private static void onUserNotFound(@NonNull String androidId,
                                       TaskCompletionSource<User> taskCompletionSource) {
        if (GlobalRepository.isInTestMode()) {
            // In test mode, use a mock user that doesn't trigger setRemote
            User newUser = new User(androidId) {
                @Override
                public void setRemote() {
                    // Do nothing in tests
                }
            };
            taskCompletionSource.setResult(newUser);
            return;
        }

        // Create a new user
        User newUser = new User(androidId);

        // Add the new user to repository
        if (repository != null) {
            repository.addUser(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User added: " + newUser.getId());
                        taskCompletionSource.setResult(newUser);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding user", e);
                        taskCompletionSource.setException(e);
                    });
        } else {
            taskCompletionSource.setException(new IllegalStateException("Repository not initialized"));
        }
    }

    /**
     * Resets the authenticator state. Useful for testing.
     */
    @VisibleForTesting
    public static void reset() {
        mockTask = null;
        repository = new GlobalRepository();
    }
}
