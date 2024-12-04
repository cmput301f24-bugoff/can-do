package com.bugoff.can_do.testmodels;

import java.util.HashMap;
import java.util.Map;

public class TestNoOpDatabaseBehavior {
    private final Map<String, TestUser> users = new HashMap<>();
    private final Map<String, TestFacility> facilities = new HashMap<>();
    private final Map<String, TestEvent> events = new HashMap<>();

    public void addUser(TestUser user) {
        users.put(user.getId(), user);
    }

    public TestUser getUser(String userId) throws Exception {
        TestUser user = users.get(userId);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    public void addFacility(TestFacility facility) {
        facilities.put(facility.getId(), facility);
    }

    public TestFacility getFacility(String facilityId) throws Exception {
        TestFacility facility = facilities.get(facilityId);
        if (facility == null) {
            throw new Exception("Facility not found");
        }
        return facility;
    }

    public void addEvent(TestEvent event) {
        events.put(event.getId(), event);
    }

    public TestEvent getEvent(String eventId) throws Exception {
        TestEvent event = events.get(eventId);
        if (event == null) {
            throw new Exception("Event not found");
        }
        return event;
    }

    public void saveEvent(TestEvent event) {
        events.put(event.getId(), event);
    }

    public void clearAll() {
        users.clear();
        facilities.clear();
        events.clear();
    }
}