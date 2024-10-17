package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facility implements DatabaseEntity {
    // data fields
    private String id;
    private User owner;
    private List<Event> events;

    private FirebaseFirestore db;
    private ListenerRegistration listener;

    public Facility(@NonNull User owner) {
        this.id = owner.getId(); // The id of the facility is the Android ID of the user
        this.owner = owner;
        owner.setFacility(this); // Set the facility of the owner to this facility
        events = new ArrayList<>();
        this.db = FirestoreHelper.getInstance().getDb();
        loadExistingEvents();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerId", owner.getId());
        map.put("events", getEventIds());
        return map;
    }

    public User getOwner() {
        return owner;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(@NonNull Event event) {
        events.add(event);
        event.setRemote();
        setRemote();
    }

    @Override
    public void setRemote() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        // Create a map of fields to be saved or updated
        Map<String, Object> update = this.toMap();

        // Save or update the facility in Firestore
        facilityRef.set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Facility successfully saved or updated.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving or updating facility", e);
                });
    }

    @NonNull
    private List<String> getEventIds() {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    private void loadExistingEvents() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        // Fetch the facility document to get the list of event IDs
        facilityRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get the list of event IDs from the facility document
                List<String> eventIds = (List<String>) documentSnapshot.get("events");

                if (eventIds != null && !eventIds.isEmpty()) {
                    events.clear();

                    // Fetch the details of events in batches using whereIn
                    fetchRemoteEventsInBatches(eventIds);
                } else {
                    Log.d("Facility", "No events found for this facility.");
                }
            } else {
                Log.e("Firestore", "Facility document does not exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error loading facility events", e);
        });
    }

    // Method to fetch events in batches using whereIn
    private void fetchRemoteEventsInBatches(@NonNull List<String> eventIds) {
        // Process in batches of 10
        final int batchSize = 10;

        // Split the eventIds into smaller batches
        for (int i = 0; i < eventIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, eventIds.size());
            List<String> batch = eventIds.subList(i, end);

            // Perform the batched query
            GlobalRepository.getEventsCollection()
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Event event = new Event(this, doc);
                            events.add(event);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching events in batch", e);
                    });
        }
        // onEventsLoaded();
    }

    @Override
    public void attachListener() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        // Attach a listener to the Facility document
        listener = facilityRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to facility changes for user: " + owner.getId(), e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Update owner information if it has changed
                String newOwnerId = documentSnapshot.getString("ownerId");
                if (newOwnerId != null && !newOwnerId.equals(owner.getId())) {
                    throw new IllegalStateException("Facility owner cannot be changed.");
                }

                // Update events list if the events field has changed
                List<String> eventIds = (List<String>) documentSnapshot.get("events");
                if (eventIds != null) {
                    updateLocalEventIds(eventIds);
                }

                // Notify that the facility data has been updated
                onUpdate();
            }
        });
    }

    // Update local events based on the list of event IDs
    private void updateLocalEventIds(@NonNull List<String> eventIds) {
        events.clear();
        for (String eventId : eventIds) {
            GlobalRepository.getEvent(eventId)
                    .addOnSuccessListener(event -> {
                        if (event != null) {
                            events.add(event);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching event: " + eventId, e);
                    });
        }
        onEventsUpdated(); // Notify that the events have been updated
    }

    @Override
    public void onUpdate() {}

    // Stop listening to the events when it's no longer needed
    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    // TODO: Implement this method for UI
    // This method is called whenever the events list is updated from Firestore
    private void onEventsUpdated() {
        // Notify other parts of the app that the events list has been updated
        Log.d("Facility", "Events list updated");
        // This triggers a UI update or notify a ViewModel etc...
    }
}
