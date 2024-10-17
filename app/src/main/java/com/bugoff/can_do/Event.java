package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String id;
    private Facility facility;

    public Event(@NonNull Facility facility) {
        this.id = GlobalRepository.getEventsCollection().document().getId();
        this.facility = facility;
        facility.addEvent(this);
    }

    // Constructor from Firestore document
    public Event(@NonNull Facility facility, @NonNull DocumentSnapshot doc) {
        this.id = doc.getId();
        this.facility = facility;
    }

    public String getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    // Method to save the facility to Firestore
    public void setRemote() {
        DocumentReference eventRef = GlobalRepository.getEventsCollection().document(id);
        Map<String, Object> update = new HashMap<>();
        update.put("id", id);
        update.put("facilityId", facility.getId());

        eventRef.set(update)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating event", e);
                });
    }
}
