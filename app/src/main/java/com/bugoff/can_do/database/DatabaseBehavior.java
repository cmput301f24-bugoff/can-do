package com.bugoff.can_do.database;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;

public interface DatabaseBehavior {
    void saveEvent(Event event);
    void attachListener(Event event, Runnable onUpdate);
    void detachListener(Event event);
    Task<User> getUser(String userId);
    Task<Event> getEvent(String eventId);
    Task<Facility> getFacility(String facilityId);
    Task<Void> addUser(User user);
    Task<Void> addEvent(Event event);
    Task<Void> addFacility(Facility facility);
    void addNotification(Notification notification);
}
