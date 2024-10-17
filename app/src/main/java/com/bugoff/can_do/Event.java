package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Event implements DatabaseEntity {
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

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("facilityId", facility.getId());
        return map;
    }

    @Override
    public String getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    // Method to save the facility to Firestore
    @Override
    public void setRemote() {
        DocumentReference eventRef = GlobalRepository.getEventsCollection().document(id);

        Map<String, Object> update = this.toMap();

        eventRef.set(update)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating event", e);
                });
    }

    @Override
    public void attachListener() {

    }

    @Override
    public void detachListener() {

    }

    @Override
    public void onUpdate() {

    }
}
