package com.bugoff.can_do;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;

public class Event {
    private String id;
    private Facility facility;

    public Event(@NonNull Facility facility, @NonNull CollectionReference eventsCollection) {
        this.id = eventsCollection.document().getId();
        this.facility = facility;
        facility.addEvent(this);
    }

    public String getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }
}
