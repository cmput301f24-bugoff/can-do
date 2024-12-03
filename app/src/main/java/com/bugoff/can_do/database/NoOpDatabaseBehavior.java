package com.bugoff.can_do.database;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.HashMap;
import java.util.Map;

public class NoOpDatabaseBehavior implements DatabaseBehavior {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Event> events = new HashMap<>();
    private final Map<String, Facility> facilities = new HashMap<>();
    private final Map<String, Notification> notifications = new HashMap<>();

    @Override
    public void saveEvent(Event event) {
        events.put(event.getId(), event);
    }

    @Override
    public void attachListener(Event event, Runnable onUpdate) {
        // No-op in test environment
    }

    @Override
    public void detachListener(Event event) {
        // No-op in test environment
    }

    @Override
    public Task<User> getUser(String userId) {
        User user = users.get(userId);
        return user != null ? Tasks.forResult(user) : Tasks.forException(new Exception("User not found"));
    }

    @Override
    public Task<Event> getEvent(String eventId) {
        Event event = events.get(eventId);
        return event != null ? Tasks.forResult(event) : Tasks.forException(new Exception("Event not found"));
    }

    @Override
    public Task<Facility> getFacility(String facilityId) {
        Facility facility = facilities.get(facilityId);
        return facility != null ? Tasks.forResult(facility) : Tasks.forException(new Exception("Facility not found"));
    }

    @Override
    public Task<Void> addUser(User user) {
        users.put(user.getId(), user);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> addEvent(Event event) {
        events.put(event.getId(), event);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> addFacility(Facility facility) {
        facilities.put(facility.getId(), facility);
        return Tasks.forResult(null);
    }

    @Override
    public void addNotification(Notification notification) {
        notifications.put(notification.getId(), notification);
    }

    // Helper methods for tests
    public void clearAll() {
        users.clear();
        events.clear();
        facilities.clear();
        notifications.clear();
    }
}
