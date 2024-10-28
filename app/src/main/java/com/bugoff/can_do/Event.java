package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Event implements DatabaseEntity {
    private String id;
    private Facility facility;
    private String name;

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private Runnable onUpdateListener;

    public Event(@NonNull Facility facility) {
        this.id = GlobalRepository.getEventsCollection().document().getId();
        this.facility = facility;
        facility.addEvent(this);
        this.name = "";
    }

    // Constructor from Firestore document
    public Event(@NonNull Facility facility, @NonNull DocumentSnapshot doc) {
        this.id = doc.getId();
        this.facility = facility;
        this.name = doc.getString("name");
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("facilityId", facility.getId());
        map.put("name", name);
        return map;
    }

    @Override
    public String getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        DocumentReference eventRef = GlobalRepository.getEventsCollection().document(id);

        // Attach a snapshot listener to the Event document
        listener = eventRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to event changes for event: " + id, e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Update local fields with data from Firestore
                String updatedFacilityId = documentSnapshot.getString("facilityId");

                AtomicBoolean isChanged = new AtomicBoolean(false);

                if (updatedFacilityId != null && !updatedFacilityId.equals(this.facility.getId())) {
                    // Fetch the updated Facility object
                    GlobalRepository.getFacility(updatedFacilityId)
                            .addOnSuccessListener(facility -> {
                                this.facility = facility;
                                this.facility.addEvent(this); // Ensure bidirectional reference
                                isChanged.set(true);
                                onUpdate(); // Notify listeners about the update
                            })
                            .addOnFailureListener(error -> {
                                Log.e("Firestore", "Error fetching updated facility for event: " + id, error);
                            });
                    // Early return since facility fetching is asynchronous
                    return;
                }

                if (isChanged.get()) {
                    onUpdate(); // Notify listeners about the update
                }
            }
        });
    }

    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
            Log.d("Firestore", "Listener detached for event: " + id);
        }
    }

    @Override
    public void onUpdate() {
        if (onUpdateListener != null) {
            onUpdateListener.run();
        }
    }

    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }
}
