package com.bugoff.can_do.database;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
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

/**
 * Repository for managing global operations on Firestore collections, such as users, facilities, and events.
 * Supports test mode for unit testing.
 */
public class GlobalRepository {
    // Testing
    private static boolean isTestMode = false;
    private static FirebaseFirestore mockDb = null;
    private static CollectionReference mockUsersCollection = null;
    private static CollectionReference mockFacilitiesCollection = null;
    private static CollectionReference mockEventsCollection = null;
    private static User loggedInUser;

    /**
     * Sets the test mode flag.
     * @param testMode true to enable test mode, false to disable
     */
    public static void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    /**
     * Sets the mock Firestore instance for testing.
     * @param db the mock Firestore instance
     */
    public static void setMockFirestore(FirebaseFirestore db) {
        mockDb = db;
        if (db != null) {
            mockUsersCollection = db.collection("users");
            mockFacilitiesCollection = db.collection("facilities");
            mockEventsCollection = db.collection("events");
        }
    }

    private static CollectionReference usersCollection;
    private static CollectionReference facilitiesCollection;
    private static CollectionReference eventsCollection;
    private FirebaseFirestore db;

    /**
     * Get the currently logged in user.
     * @return the currently logged in user
     */
    public static User getLoggedInUser() {
        return loggedInUser;
    }
    /**
     * Set the currently logged in user.
     * @param user the currently logged in user
     */
    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    /**
     * Constructor for GlobalRepository.
     * Initializes Firestore collections based on the mode (test or production).
     */
    public GlobalRepository() {
        if (!isTestMode) {
            db = FirestoreHelper.getInstance().getDb();
            usersCollection = db.collection("users");
            facilitiesCollection = db.collection("facilities");
            eventsCollection = db.collection("events");
        } else {
            db = mockDb;
            usersCollection = mockUsersCollection;
            facilitiesCollection = mockFacilitiesCollection;
            eventsCollection = mockEventsCollection;
        }
    }
    /**
     * Adds a notification to the Firestore database.
     * @param notification the notification to add
     */
    public static void addNotification(Notification notification) {
        if (isTestMode) {
            // Handle mock notification add
            return;
        }

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("id", notification.getId());
        notificationMap.put("type", notification.getType());
        notificationMap.put("message", notification.getContent());
        notificationMap.put("from", notification.getFrom());
        notificationMap.put("pendingRecipients", notification.getPendingRecipients());
        notificationMap.put("event", notification.getEvent());

        FirestoreHelper.getInstance().getDb().collection("notifications")
                .document(notification.getId())
                .set(notificationMap);
    }

    /**
     * Gets the Firestore instance.
     * @return the Firestore instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Gets the users collection.
     * @return the users collection
     */
    public static CollectionReference getUsersCollection() {
        return isTestMode ? mockUsersCollection : usersCollection;
    }

    /**
     * Gets the facilities collection.
     * @return the facilities collection
     */
    public static CollectionReference getFacilitiesCollection() {
        return isTestMode ? mockFacilitiesCollection : facilitiesCollection;
    }

    /**
     * Gets the events collection.
     * @return the events collection
     */
    public static CollectionReference getEventsCollection() {
        return isTestMode ? mockEventsCollection : eventsCollection;
    }

    /**
     * Adds a user to the Firestore database.
     * @param user the user to add
     * @return a task that adds the user
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<Void> addUser(@NonNull User user) {
        if (isTestMode) {
            return MockGlobalRepository.addUser(user);
        }

        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        Map<String, Object> userMap = user.toMap();

        getUsersCollection().document(user.getId())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("GlobalRepository", "User added successfully: " + user.getId());
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("GlobalRepository", "Error adding user: " + user.getId(), e);
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }

    /**
     * Gets a user from the Firestore database.
     * @param androidId the ID of the user to get
     * @return a task that gets the user
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<User> getUser(String androidId) {
        if (isTestMode) {
            return MockGlobalRepository.getUser(androidId);
        }

        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        getUsersCollection().document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
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
     * Adds a facility to the Firestore database.
     * @param facility the facility to add
     * @return a task that adds the facility
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<Void> addFacility(@NonNull Facility facility) {
        if (isTestMode) {
            return MockGlobalRepository.addFacility(facility);
        }

        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        Map<String, Object> facilityMap = new HashMap<>();
        facilityMap.put("id", facility.getId());
        facilityMap.put("ownerId", facility.getOwner().getId());
        facilityMap.put("name", facility.getName());
        facilityMap.put("address", facility.getAddress());

        DocumentReference facilityRef = getFacilitiesCollection().document(facility.getId());
        DocumentReference userRef = getUsersCollection().document(facility.getOwner().getId());

        WriteBatch batch = FirestoreHelper.getInstance().getDb().batch();
        batch.set(facilityRef, facilityMap);
        batch.update(userRef, "facilityId", facility.getId());

        batch.commit()
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

     /**
     * Adds an event to the Firestore database.
     * @param eventId the event to add
     * @return a task that adds the event
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<Event> getEvent(String eventId) {
        if (isTestMode) {
            return MockGlobalRepository.getEvent(eventId);
        }

        TaskCompletionSource<Event> taskCompletionSource = new TaskCompletionSource<>();

        getEventsCollection().document(eventId)
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
     * Gets a facility from the Firestore database.
     * @param facilityId the ID of the facility to get
     * @return a task that gets the facility
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<Facility> getFacility(String facilityId) {
        if (isTestMode) {
            return MockGlobalRepository.getFacility(facilityId);
        }

        TaskCompletionSource<Facility> taskCompletionSource = new TaskCompletionSource<>();

        getFacilitiesCollection().document(facilityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Facility facility = new Facility(documentSnapshot);
                        taskCompletionSource.setResult(facility);
                    } else {
                        taskCompletionSource.setException(new Exception("Facility not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    /**
     * Adds an event to the Firestore database.
     * @param event the event to add
     * @return a task that adds the event
     */
    @SuppressLint("RestrictedApi")
    @NonNull
    public static Task<Void> addEvent(@NonNull Event event) {
        if (isTestMode) {
            return MockGlobalRepository.addEvent(event);
        }

        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        Map<String, Object> eventMap = event.toMap();

        DocumentReference eventRef = getEventsCollection().document(event.getId());
        DocumentReference facilityRef = getFacilitiesCollection().document(event.getFacility().getId());

        WriteBatch batch = FirestoreHelper.getInstance().getDb().batch();
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

    /**
     * Checks if the repository is running in test mode.
     * @return true if in test mode, false otherwise
     */
    public static boolean isInTestMode() {
        return isTestMode;
    }
}
