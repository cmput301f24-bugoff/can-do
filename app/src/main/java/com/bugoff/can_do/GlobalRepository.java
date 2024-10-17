package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class GlobalRepository {
    private FirebaseFirestore db;
    private static CollectionReference usersCollection;
    private static CollectionReference facilitiesCollection;
    private static CollectionReference eventsCollection;

    // Constructor to initialize Firestore
    public GlobalRepository() {
        db = FirestoreHelper.getInstance().getDb();
        usersCollection = db.collection("users");
        facilitiesCollection = db.collection("facilities");
        eventsCollection = db.collection("events");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public static CollectionReference getUsersCollection() {
        return usersCollection;
    }

    public static CollectionReference getFacilitiesCollection() {
        return facilitiesCollection;
    }

    public static CollectionReference getEventsCollection() {
        return eventsCollection;
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

    /**
     * Adds a facility to Firestore and associates it with the user (owner) by using a batch write.
     * This ensures both the facility and the user's association with the facility are updated atomically.
     *
     * @param facility The Facility object to add.
     * @return A Task representing the add operation.
     */
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

    /**
     * Adds an event to Firestore and associates it with a facility by updating the facility's event list.
     * The operation is performed using a batch write to ensure atomicity.
     *
     * @param event The Event object to add.
     * @return A Task representing the add operation.
     */
    public Task<Void> addEvent(@NonNull Event event) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Prepare Event data
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", event.getId());
        eventMap.put("facilityId", event.getFacility().getId());
        eventMap.put("ownerId", event.getFacility().getOwner().getAndroidId());

        DocumentReference eventRef = eventsCollection.document(event.getId());
        DocumentReference facilityRef = facilitiesCollection.document(event.getFacility().getId());

        WriteBatch batch = db.batch();
        batch.set(eventRef, eventMap);
        batch.update(facilityRef, "events", FieldValue.arrayUnion(event.getId()));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("GlobalRepository", "Event added successfully: " + event.getId());
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("GlobalRepository", "Error adding Event: " + event.getId(), e);
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }
}
