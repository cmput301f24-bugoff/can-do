package com.bugoff.can_do.database;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mock/test implementation of GlobalRepository for testing purposes that stores data in memory instead of Firebase.
 * Designed to avoid actual Firebase connections by using in-memory storage for test data.
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
public class MockGlobalRepository extends GlobalRepository {
    // In-memory storage for mock data
    private final Map<String, User> mockUsers = new ConcurrentHashMap<>();
    private final Map<String, Facility> mockFacilities = new ConcurrentHashMap<>();
    private final Map<String, Event> mockEvents = new ConcurrentHashMap<>();
    private final Map<String, Notification> mockNotifications = new ConcurrentHashMap<>();

    public MockGlobalRepository() {
        super();
        // Override the test mode to ensure we're using mock collections
        setTestMode(true);
    }

    // Override methods that interact with Firebase
    /**
     * Overrides to return null as Firebase is not used in the mock implementation.
     */
    @NonNull
    @Override
    public FirebaseFirestore getDb() {
        // Return null since we won't be using actual Firebase
        return null;
    }
    /**
     * Adds a user to the in-memory storage.
     *
     * @param user The {@link User} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @NonNull
    public static Task<Void> addUser(@NonNull User user) {
        MockGlobalRepository instance = getInstance();
        instance.mockUsers.put(user.getId(), user);
        return Tasks.forResult(null);
    }
    /**
     * Retrieves a user from in-memory storage by their ID.
     *
     * @param androidId The ID of the user to retrieve.
     * @return A {@link Task} containing the user or an exception if not found.
     */
    @NonNull
    public static Task<User> getUser(String androidId) {
        MockGlobalRepository instance = getInstance();
        User user = instance.mockUsers.get(androidId);
        if (user != null) {
            return Tasks.forResult(user);
        }
        return Tasks.forException(new Exception("User not found"));
    }
    /**
     * Adds a facility to the in-memory storage and updates its owner.
     *
     * @param facility The {@link Facility} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @NonNull
    public static Task<Void> addFacility(@NonNull Facility facility) {
        MockGlobalRepository instance = getInstance();
        instance.mockFacilities.put(facility.getId(), facility);

        // Update the owner's facility reference
        User owner = instance.mockUsers.get(facility.getOwner().getId());
        if (owner != null) {
            owner.setFacility(facility);
        }

        return Tasks.forResult(null);
    }
    /**
     * Retrieves a facility from in-memory storage by its ID.
     *
     * @param facilityId The ID of the facility to retrieve.
     * @return A {@link Task} containing the facility or an exception if not found.
     */
    @NonNull
    public static Task<Facility> getFacility(String facilityId) {
        MockGlobalRepository instance = getInstance();
        Facility facility = instance.mockFacilities.get(facilityId);
        if (facility != null) {
            return Tasks.forResult(facility);
        }
        return Tasks.forException(new Exception("Facility not found"));
    }
    /**
     * Retrieves all facilities as an unmodifiable map.
     *
     * @return A map of all facilities in memory.
     */
    public Map<String, Facility> getFacilities() {
        return Collections.unmodifiableMap(mockFacilities);
    }
    /**
     * Adds an event to the in-memory storage and updates its facility's event list.
     *
     * @param event The {@link Event} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @NonNull
    public static Task<Void> addEvent(@NonNull Event event) {
        MockGlobalRepository instance = getInstance();
        instance.mockEvents.put(event.getId(), event);

        // Update the facility's events list
        Facility facility = instance.mockFacilities.get(event.getFacility().getId());
        if (facility != null) {
            facility.addEvent(event);
        }

        return Tasks.forResult(null);
    }
    /**
     * Retrieves an event from in-memory storage by its ID.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A {@link Task} containing the event or an exception if not found.
     */
    @NonNull
    public static Task<Event> getEvent(String eventId) {
        MockGlobalRepository instance = getInstance();
        Event event = instance.mockEvents.get(eventId);
        if (event != null) {
            return Tasks.forResult(event);
        }
        return Tasks.forException(new Exception("Event not found"));
    }
    /**
     * Adds a notification to the in-memory storage.
     *
     * @param notification The {@link Notification} to add.
     */
    public static void addNotification(Notification notification) {
        MockGlobalRepository instance = getInstance();
        instance.mockNotifications.put(notification.getId(), notification);
    }

    // Helper methods for testing
    /**
     * Clears all in-memory data for users, facilities, events, and notifications.
     */
    public void clearAllData() {
        mockUsers.clear();
        mockFacilities.clear();
        mockEvents.clear();
        mockNotifications.clear();
    }

    // Singleton pattern for the mock repository
    private static MockGlobalRepository instance;
    /**
     * Retrieves the singleton instance of the mock repository.
     *
     * @return The singleton instance.
     */
    public static synchronized MockGlobalRepository getInstance() {
        if (instance == null) {
            instance = new MockGlobalRepository();
        }
        return instance;
    }

    // Method to reset the instance (useful between tests)
    /**
     * Resets the singleton instance, clearing all data.
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.clearAllData();
        }
        instance = null;
    }
}
