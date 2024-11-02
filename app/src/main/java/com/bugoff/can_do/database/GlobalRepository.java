package com.bugoff.can_do.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
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
    private static CollectionReference usersCollection;
    private static CollectionReference facilitiesCollection;
    private static CollectionReference eventsCollection;

    private static User loggedInUser;

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    // Constructor to initialize Firestore
    public GlobalRepository() {
        FirebaseFirestore db = FirestoreHelper.getInstance().getDb();
        usersCollection = db.collection("users");
        facilitiesCollection = db.collection("facilities");
        eventsCollection = db.collection("events");
    }

    public FirebaseFirestore getDb() {
        return FirestoreHelper.getInstance().getDb();
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
    @NonNull
    public static Task<Void> addUser(@NonNull User user) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        Map<String, Object> userMap = user.toMap();

        usersCollection.document(user.getId())
                .set(userMap)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    @NonNull
    public static Task<User> getUser(String androidId) {
        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();

        usersCollection.document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Use the updated User constructor that handles deserialization
                        User user = new User(documentSnapshot);
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
    @NonNull
    public static Task<Void> addFacility(@NonNull Facility facility) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Prepare Facility data
        Map<String, Object> facilityMap = new HashMap<>();
        facilityMap.put("id", facility.getId());
        facilityMap.put("ownerId", facility.getOwner().getId());

        DocumentReference facilityRef = facilitiesCollection.document(facility.getId());
        DocumentReference userRef = usersCollection.document(facility.getOwner().getId());

        // Prevent partial updates by using a batch write
        WriteBatch batch = FirestoreHelper.getInstance().getDb().batch();
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
    @NonNull
    public static Task<Void> addEvent(@NonNull Event event) {
        // Create a TaskCompletionSource to handle the asynchronous task
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Serialize all Event fields into a Map using the toMap() method
        Map<String, Object> eventMap = event.toMap();

        // Get references to the Event document and the corresponding Facility document
        DocumentReference eventRef = eventsCollection.document(event.getId());
        DocumentReference facilityRef = facilitiesCollection.document(event.getFacility().getId());

        // Create a Firestore batch to perform atomic writes
        WriteBatch batch = FirestoreHelper.getInstance().getDb().batch();

        // Set the Event document with the serialized data
        batch.set(eventRef, eventMap);

        // Update the Facility's "events" array to include the new Event ID
        batch.update(facilityRef, "events", FieldValue.arrayUnion(event.getId()));

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Log success and complete the task
                    Log.d("GlobalRepository", "Event added successfully: " + event.getId());
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    // Log the error and set the exception for the task
                    Log.e("GlobalRepository", "Error adding Event: " + event.getId(), e);
                    taskCompletionSource.setException(e);
                });

        // Return the Task to allow callers to attach listeners
        return taskCompletionSource.getTask();
    }

    @NonNull
    public static Task<Event> getEvent(String eventId) {
        TaskCompletionSource<Event> taskCompletionSource = new TaskCompletionSource<>();

        eventsCollection.document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String facilityId = documentSnapshot.getString("facilityId");
                        getFacility(facilityId)
                                .addOnSuccessListener(facility -> {
                                    Event event = new Event(facility, documentSnapshot);
                                    taskCompletionSource.setResult(event);
                                })
                                .addOnFailureListener(taskCompletionSource::setException);
                    } else {
                        taskCompletionSource.setException(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    /**
     * Fetches a Facility from Firestore using the facilityId.
     *
     * @param facilityId The ID of the Facility to fetch.
     * @return A Task representing the fetch operation, containing the Facility.
     */
    @NonNull
    public static Task<Facility> getFacility(String facilityId) {
        TaskCompletionSource<Facility> taskCompletionSource = new TaskCompletionSource<>();

        facilitiesCollection.document(facilityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Initialize Facility using the DocumentSnapshot constructor
                        Facility facility = new Facility(documentSnapshot);
                        taskCompletionSource.setResult(facility);
                    } else {
                        taskCompletionSource.setException(new Exception("Facility not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }
}
