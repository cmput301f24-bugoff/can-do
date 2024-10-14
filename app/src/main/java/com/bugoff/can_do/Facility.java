package com.bugoff.can_do;

import androidx.annotation.NonNull;

public class Facility {
    private String id;
    private User owner;

    public Facility(@NonNull User owner) {
        this.id = owner.getAndroidId(); // The id of the facility is the Android ID of the user
        owner.setFacility(this); // Set the facility of the owner to this facility
    }

    public String getId() {
        return id;
    }
    public User getOwner() {
        return owner;
    }
}
