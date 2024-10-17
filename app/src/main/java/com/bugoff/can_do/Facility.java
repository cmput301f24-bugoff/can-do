package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facility {
    // data fields
    private String id;
    private User owner;
    private List<Event> events;

    private FirebaseFirestore db;
    private ListenerRegistration eventsListener;

    public Facility(@NonNull User owner) {
        this.id = owner.getAndroidId(); // The id of the facility is the Android ID of the user
        this.owner = owner;
        owner.setFacility(this); // Set the facility of the owner to this facility
        events = new ArrayList<>();
        this.db = FirestoreHelper.getInstance().getDb();
        loadExistingEvents();
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
        event.setRemote();
        setRemote();
    }

    public void setRemote() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        // Create a map of fields to be saved or updated
        Map<String, Object> update = new HashMap<>();
        update.put("ownerId", owner.getAndroidId());
        update.put("events", getEventIds()); // Get the list of event IDs

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

    public void attachEventsListener() {
        Query eventsQuery = GlobalRepository.getEventsCollection();

        eventsListener = eventsQuery.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to events for facility of user: "
                        + owner.getAndroidId(), e);
            }

            if (querySnapshot != null) {
                // Clear the current list to reflect the updated state
                events.clear();

                // Iterate over the documents in the snapshot and convert them to Event objects
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        events.add(event);
                    }
                }

                // Notify that the events list has been updated
                onEventsUpdated();
            }
        });
    }

    // Stop listening to the events when it's no longer needed
    public void detachEventsListener() {
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    // TODO: Implement this method for UI
    // This method is called whenever the events list is updated from Firestore
    private void onEventsUpdated() {
        // Notify other parts of the app that the events list has been updated
        Log.d("Facility", "Events list updated: " + events);
        // This triggers a UI update or notify a ViewModel etc...
    }
}
