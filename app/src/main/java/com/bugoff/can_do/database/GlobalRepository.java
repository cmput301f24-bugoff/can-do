package com.bugoff.can_do.database;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The {@code GlobalRepository} class acts as a centralized repository for managing operations on Firestore collections.
 * It supports global access to users, facilities, and events collections, and provides functionality to switch between
 * test mode and production mode for unit testing purposes.
 */
public class GlobalRepository {
    // Behavior management for production and test modes
    private static DatabaseBehavior behavior = new FirebaseBehavior();
    private static boolean isTestMode = false;
    private static User loggedInUser;

    // Firestore collection references
    private static CollectionReference usersCollection;
    private static CollectionReference facilitiesCollection;
    private static CollectionReference eventsCollection;
    private FirebaseFirestore db;
    /**
     * Sets the {@link DatabaseBehavior} implementation to be used for database operations.
     *
     * @param behavior The {@link DatabaseBehavior} implementation to use.
     */
    public static void setBehavior(DatabaseBehavior behavior) {
        GlobalRepository.behavior = behavior;
    }
    /**
     * Gets the current {@link DatabaseBehavior} implementation.
     *
     * @return The current {@link DatabaseBehavior} implementation.
     */
    public static DatabaseBehavior getBehavior() {
        return behavior;
    }
    /**
     * Toggles test mode for the repository. In test mode, a no-operation database behavior is used
     * to avoid real Firestore interactions.
     *
     * @param testMode {@code true} to enable test mode; {@code false} for production mode.
     */
    public static void setTestMode(boolean testMode) {
        isTestMode = testMode;
        if (testMode) {
            behavior = new NoOpDatabaseBehavior();
        } else {
            behavior = new FirebaseBehavior();
        }
    }
    /**
     * Checks whether the repository is in test mode.
     *
     * @return {@code true} if test mode is enabled; {@code false} otherwise.
     */
    public static boolean isInTestMode() {
        return isTestMode;
    }
    /**
     * Retrieves an {@link Event} by its ID.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A {@link Task} representing the asynchronous operation to fetch the event.
     */
    public static Task<Event> getEvent(String eventId) {
        return behavior.getEvent(eventId);
    }
    /**
     * Retrieves a {@link User} by its ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return A {@link Task} representing the asynchronous operation to fetch the user.
     */
    public static Task<User> getUser(String userId) {
        return behavior.getUser(userId);
    }
    /**
     * Retrieves a {@link Facility} by its ID.
     *
     * @param facilityId The ID of the facility to retrieve.
     * @return A {@link Task} representing the asynchronous operation to fetch the facility.
     */
    public static Task<Facility> getFacility(String facilityId) {
        return behavior.getFacility(facilityId);
    }
    /**
     * Adds a new {@link User} to the Firestore database.
     *
     * @param user The {@link User} object to add.
     * @return A {@link Task} representing the asynchronous operation to add the user.
     */
    public static Task<Void> addUser(User user) {
        return behavior.addUser(user);
    }
    /**
     * Adds a new {@link Event} to the Firestore database.
     *
     * @param event The {@link Event} object to add.
     * @return A {@link Task} representing the asynchronous operation to add the event.
     */
    public static Task<Void> addEvent(Event event) {
        return behavior.addEvent(event);
    }
    /**
     * Adds a new {@link Facility} to the Firestore database.
     *
     * @param facility The {@link Facility} object to add.
     * @return A {@link Task} representing the asynchronous operation to add the facility.
     */
    public static Task<Void> addFacility(Facility facility) {
        return behavior.addFacility(facility);
    }
    /**
     * Gets the currently logged-in {@link User}.
     *
     * @return The logged-in {@link User}, or {@code null} if no user is logged in.
     */
    public static User getLoggedInUser() {
        return loggedInUser;
    }
    /**
     * Sets the currently logged-in {@link User}.
     *
     * @param user The {@link User} to set as logged in.
     */
    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    /**
     * Constructor for {@code GlobalRepository}.
     * Initializes Firestore collections based on the current mode (test or production).
     */
    public GlobalRepository() {
        if (!isTestMode) {
            db = FirestoreHelper.getInstance().getDb();
            usersCollection = db.collection("users");
            facilitiesCollection = db.collection("facilities");
            eventsCollection = db.collection("events");
        }
    }
    /**
     * Adds a {@link Notification} to the Firestore database.
     *
     * @param notification The {@link Notification} object to add.
     */
    public static void addNotification(Notification notification) {
        behavior.addNotification(notification);
    }
    /**
     * Gets the Firestore database instance.
     *
     * @return The {@link FirebaseFirestore} instance.
     */
    public FirebaseFirestore getDb() {
        return db;
    }
    /**
     * Gets the Firestore collection for users.
     *
     * @return The {@link CollectionReference} for the "users" collection, or {@code null} if in test mode.
     */
    public static CollectionReference getUsersCollection() {
        return isTestMode ? null : usersCollection;
    }
    /**
     * Gets the Firestore collection for facilities.
     *
     * @return The {@link CollectionReference} for the "facilities" collection, or {@code null} if in test mode.
     */
    public static CollectionReference getFacilitiesCollection() {
        return isTestMode ? null : facilitiesCollection;
    }
    /**
     * Gets the Firestore collection for events.
     *
     * @return The {@link CollectionReference} for the "events" collection, or {@code null} if in test mode.
     */
    public static CollectionReference getEventsCollection() {
        return isTestMode ? null : eventsCollection;
    }
}
