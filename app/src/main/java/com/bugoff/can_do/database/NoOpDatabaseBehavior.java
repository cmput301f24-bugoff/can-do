package com.bugoff.can_do.database;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.HashMap;
import java.util.Map;
/**
 * A No-Operation (NoOp) implementation of {@link DatabaseBehavior} for testing purposes.
 * This implementation avoids real database interactions and uses in-memory storage for mock data.
 */
public class NoOpDatabaseBehavior implements DatabaseBehavior {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Event> events = new HashMap<>();
    private final Map<String, Facility> facilities = new HashMap<>();
    private final Map<String, Notification> notifications = new HashMap<>();
    /**
     * Saves an {@link Event} in the in-memory storage.
     *
     * @param event The {@link Event} to save.
     */
    @Override
    public void saveEvent(Event event) {
        events.put(event.getId(), event);
    }
    /**
     * No-operation for attaching a listener to an {@link Event}.
     *
     * @param event    The {@link Event} to listen to.
     * @param onUpdate The {@link Runnable} to execute on updates.
     */
    @Override
    public void attachListener(Event event, Runnable onUpdate) {
        // No-op in test environment
    }
    /**
     * No-operation for detaching a listener from an {@link Event}.
     *
     * @param event The {@link Event} to detach the listener from.
     */
    @Override
    public void detachListener(Event event) {
        // No-op in test environment
    }
    /**
     * Retrieves a {@link User} from the in-memory storage by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return A {@link Task} containing the {@link User} or an exception if not found.
     */
    @Override
    public Task<User> getUser(String userId) {
        User user = users.get(userId);
        return user != null ? Tasks.forResult(user) : Tasks.forException(new Exception("User not found"));
    }
    /**
     * Retrieves an {@link Event} from the in-memory storage by its ID.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A {@link Task} containing the {@link Event} or an exception if not found.
     */
    @Override
    public Task<Event> getEvent(String eventId) {
        Event event = events.get(eventId);
        return event != null ? Tasks.forResult(event) : Tasks.forException(new Exception("Event not found"));
    }
    /**
     * Retrieves a {@link Facility} from the in-memory storage by its ID.
     *
     * @param facilityId The ID of the facility to retrieve.
     * @return A {@link Task} containing the {@link Facility} or an exception if not found.
     */
    @Override
    public Task<Facility> getFacility(String facilityId) {
        Facility facility = facilities.get(facilityId);
        return facility != null ? Tasks.forResult(facility) : Tasks.forException(new Exception("Facility not found"));
    }
    /**
     * Adds a {@link User} to the in-memory storage.
     *
     * @param user The {@link User} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @Override
    public Task<Void> addUser(User user) {
        users.put(user.getId(), user);
        return Tasks.forResult(null);
    }
    /**
     * Adds an {@link Event} to the in-memory storage.
     *
     * @param event The {@link Event} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @Override
    public Task<Void> addEvent(Event event) {
        events.put(event.getId(), event);
        return Tasks.forResult(null);
    }
    /**
     * Adds a {@link Facility} to the in-memory storage.
     *
     * @param facility The {@link Facility} to add.
     * @return A {@link Task} representing the success of the operation.
     */
    @Override
    public Task<Void> addFacility(Facility facility) {
        facilities.put(facility.getId(), facility);
        return Tasks.forResult(null);
    }
    /**
     * Adds a {@link Notification} to the in-memory storage.
     *
     * @param notification The {@link Notification} to add.
     */
    @Override
    public void addNotification(Notification notification) {
        notifications.put(notification.getId(), notification);
    }

    // Helper methods for tests
    /**
     * Clears all data from the in-memory storage.
     */
    public void clearAll() {
        users.clear();
        events.clear();
        facilities.clear();
        notifications.clear();
    }
}
