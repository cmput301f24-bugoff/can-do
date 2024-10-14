package com.bugoff.can_do;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference facilitiesCollection;

    // Constructor to initialize Firestore
    public UserRepository() {
        db = FirestoreHelper.getInstance().getDb();
        usersCollection = db.collection("users");
        facilitiesCollection = db.collection("facilities");
    }

    /**
     * Adds a user to Firestore.
     *
     * @param user The User object to add.
     * @return A Task representing the add operation.
     */
    public Task<Void> addUser(@NonNull User user) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("androidId", user.getAndroidId());
        userMap.put("name", user.getName());
        userMap.put("isAdmin", user.getIsAdmin());
        // users by default will not have a facility

        usersCollection.document(user.getAndroidId())
                .set(userMap)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    /**
     * Retrieves a user from Firestore by Android ID.
     *
     * @param androidId The Android ID of the user.
     * @return A Task that resolves to the User if found.
     */
    public Task<User> getUser(String androidId) {
        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();

        usersCollection.document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        User user = new User(androidId, name, isAdmin, null);
                        taskCompletionSource.setResult(user);
                    } else {
                        taskCompletionSource.setException(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public Task<Void> addFacility(@NonNull Facility facility) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Prepare Facility data
        Map<String, Object> facilityMap = new HashMap<>();
        facilityMap.put("id", facility.getId());
        facilityMap.put("ownerId", facility.getOwner().getAndroidId());

        DocumentReference facilityRef = facilitiesCollection.document(facility.getId());
        DocumentReference userRef = usersCollection.document(facility.getOwner().getAndroidId());

        // Prevent partial updates by using a batch write
        WriteBatch batch = db.batch();
        batch.set(facilityRef, facilityMap);
        batch.update(userRef, "facilityId", facility.getId());

        batch.commit()
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }
}
