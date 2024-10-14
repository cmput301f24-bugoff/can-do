package com.bugoff.can_do;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Facility {
    private String id;
    private User owner;
    private List<Event> events;

    public Facility(@NonNull User owner) {
        this.id = owner.getAndroidId(); // The id of the facility is the Android ID of the user
        this.owner = owner;
        owner.setFacility(this); // Set the facility of the owner to this facility
        events = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(@NonNull Event event) {
        events.add(event);
    }

}
