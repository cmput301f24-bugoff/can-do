package com.bugoff.can_do.user;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ViewModel for browsing user profiles. Fetches user profiles from Firestore and exposes them to the UI.
 */
public class BrowseProfilesViewModel extends ViewModel {

    private static final String TAG = "BrowseProfilesViewModel";

    private final MutableLiveData<List<User>> profilesList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;

    private final boolean isAdmin;

    /**
     * Constructor accepting isAdmin flag.
     *
     * @param isAdmin Indicates whether the user is an admin.
     */
    public BrowseProfilesViewModel(boolean isAdmin) {
        this.isAdmin = isAdmin;
        fetchProfiles();
    }

    /**
     * Fetches profiles from Firestore based on isAdmin flag.
     * Admins fetch all profiles.
     */
    private void fetchProfiles() {
        if (!isAdmin) {
            errorMessage.setValue("Unauthorized access.");
            return;
        }

        listenerRegistration = GlobalRepository.getUsersCollection()
                .orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            errorMessage.setValue("Error fetching profiles: " + error.getMessage());
                            return;
                        }

                        if (snapshots != null) {
                            List<DocumentSnapshot> documents = snapshots.getDocuments();
                            if (documents.isEmpty()) {
                                profilesList.setValue(new ArrayList<>());
                                return;
                            }

                            List<User> fetchedUsers = new ArrayList<>();
                            final int total = documents.size();
                            final AtomicInteger counter = new AtomicInteger(0);

                            for (DocumentSnapshot doc : documents) {
                                if (doc.exists()) {
                                    User user = new User(doc);
                                    fetchedUsers.add(user);
                                }
                                if (counter.incrementAndGet() == total) {
                                    profilesList.postValue(fetchedUsers);
                                }
                            }
                        } else {
                            Log.w(TAG, "Current data: null");
                            errorMessage.setValue("No profiles found.");
                        }
                    }
                });
    }

    /**
     * Exposes the list of profiles.
     *
     * @return LiveData containing the list of profiles.
     */
    public LiveData<List<User>> getProfilesList() {
        return profilesList;
    }

    /**
     * Exposes error messages.
     *
     * @return LiveData containing error messages.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    /**
     * Deletes a user from the database.
     *
     * @param user The user to delete
     */
    public void deleteUser(User user) {
        if (!isAdmin) {
            errorMessage.setValue("Only administrators can delete users");
            return;
        }

        GlobalRepository.getUsersCollection()
                .document(user.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // No need to manually update the list as the snapshot listener will handle it
                    errorMessage.setValue("User deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    errorMessage.setValue("Failed to delete user: " + e.getMessage());
                });
    }
}
