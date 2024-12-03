package com.bugoff.can_do.database;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository for managing global operations on Firestore collections, such as users, facilities, and events.
 * Supports test mode for unit testing.
 */
public class GlobalRepository {
    // Testing
    private static DatabaseBehavior behavior = new FirebaseBehavior();
    private static boolean isTestMode = false;
    private static User loggedInUser;


    private static CollectionReference usersCollection;
    private static CollectionReference facilitiesCollection;
    private static CollectionReference eventsCollection;
    private FirebaseFirestore db;

    public static void setBehavior(DatabaseBehavior behavior) {
        GlobalRepository.behavior = behavior;
    }

    public static void setTestMode(boolean testMode) {
        isTestMode = testMode;
        if (testMode) {
            behavior = new NoOpDatabaseBehavior();
        } else {
            behavior = new FirebaseBehavior();
        }
    }

    public static boolean isInTestMode() {
        return isTestMode;
    }

    public static Task<Event> getEvent(String eventId) {
        return behavior.getEvent(eventId);
    }

    public static Task<User> getUser(String userId) {
        return behavior.getUser(userId);
    }

    public static Task<Facility> getFacility(String facilityId) {
        return behavior.getFacility(facilityId);
    }

    public static Task<Void> addUser(User user) {
        return behavior.addUser(user);
    }

    public static Task<Void> addEvent(Event event) {
        return behavior.addEvent(event);
    }

    public static Task<Void> addFacility(Facility facility) {
        return behavior.addFacility(facility);
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

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
        }
    }

    public static void addNotification(Notification notification) {
        behavior.addNotification(notification);
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public static CollectionReference getUsersCollection() {
        return isTestMode ? null : usersCollection;
    }

    public static CollectionReference getFacilitiesCollection() {
        return isTestMode ? null : facilitiesCollection;
    }

    public static CollectionReference getEventsCollection() {
        return isTestMode ? null : eventsCollection;
    }
}
