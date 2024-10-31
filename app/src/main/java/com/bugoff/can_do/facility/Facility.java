package com.bugoff.can_do.facility;

import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.database.DatabaseEntity;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.database.FirestoreHelper;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Facility implements DatabaseEntity {
    // data fields
    private String id;
    private User owner; // Owner (organizer) of the facility
    private String name;
    private List<Event> events; // Events held at the facility
    private String address; // Physical display address of the facility

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private Runnable onUpdateListener;

    public Facility(@NonNull User owner) {
        this.id = owner.getId(); // The id of the facility is the Android ID of the user
        this.owner = owner;
        owner.setFacility(this); // Set the facility of the owner to this facility
        this.name = "";
        this.address = "";
        this.events = new ArrayList<>();
        this.db = FirestoreHelper.getInstance().getDb();
    }

    // Constructor from Firestore document
    public Facility(@NonNull DocumentSnapshot doc) {
        this.id = doc.getId();
        this.owner = deserializeUser(doc.getString("ownerId"));
        this.name = doc.getString("name");
        this.address = doc.getString("address");
        this.events = new ArrayList<>();
        this.db = FirestoreHelper.getInstance().getDb();

        // Deserialize events asynchronously
        deserializeEvents(doc.get("events"));
    }

    private User deserializeUser(String userId) {
        if (userId != null) {
            // Fetch the User object asynchronously
            GlobalRepository.getUser(userId).addOnSuccessListener(user -> {
                this.owner = user;
                user.setFacility(this); // Set the facility reference in the User object
                onUpdate(); // Notify listeners
            }).addOnFailureListener(e -> {
                Log.e("Facility", "Error fetching user with ID: " + userId, e);
            });
        }
        return null; // Actual user will be set asynchronously
    }

    private void deserializeEvents(Object data) {
        if (data instanceof List<?>) {
            List<?> eventIds = (List<?>) data;
            for (Object eventIdObj : eventIds) {
                if (eventIdObj instanceof String) {
                    String eventId = (String) eventIdObj;

                    // Fetch the Event DocumentSnapshot asynchronously
                    GlobalRepository.getEventsCollection().document(eventId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    // Create a new Event
                                    Event event = new Event(this, doc);
                                    events.add(event);
                                    onUpdate(); // Notify listeners
                                } else {
                                    Log.w("Facility", "No such event with ID: " + eventId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Facility", "Error fetching event with ID: " + eventId, e);
                            });
                }
            }
        }
    }

    // Serialization helper methods
    private List<String> serializeEvents(List<Event> events) {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerId", owner.getId());
        map.put("name", name);
        map.put("address", address);
        map.put("events", serializeEvents(events));
        return map;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
        setRemote();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setRemote();
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        setRemote();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        setRemote();
    }

    public void addEvent(@NonNull Event event) {
        events.add(event);
        event.setRemote();
        setRemote();
    }

    public void removeEvent(Event event) {
        events.remove(event);
        event.detachListener();
        setRemote();
    }

    // Method to save the facility to Firestore
    @Override
    public void setRemote() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        Map<String, Object> update = this.toMap();

        facilityRef.set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Facility successfully saved or updated.");
                    onUpdate();
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

    @Override
    public void attachListener() {
        DocumentReference facilityRef = GlobalRepository.getFacilitiesCollection().document(id);

        // Attach a listener to the Facility document
        listener = facilityRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to facility changes for facility: " + id, e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Update owner information if it has changed
                String newOwnerId = documentSnapshot.getString("ownerId");
                if (newOwnerId != null && !newOwnerId.equals(this.owner.getId())) {
                    // Fetch the updated Owner object
                    GlobalRepository.getUser(newOwnerId)
                            .addOnSuccessListener(user -> {
                                this.owner = user;
                                user.setFacility(this); // Ensure bidirectional reference
                                onUpdate(); // Notify listeners about the update
                            })
                            .addOnFailureListener(error -> {
                                Log.e("Firestore", "Error fetching updated owner for facility: " + id, error);
                            });
                    // Early return since owner fetching is asynchronous
                    return;
                }

                // Update other fields
                this.name = documentSnapshot.getString("name");
                this.address = documentSnapshot.getString("address");

                // Update events list
                List<String> eventIds = (List<String>) documentSnapshot.get("events");
                if (eventIds != null) {
                    updateLocalEventIds(eventIds);
                }
                onUpdate();
            }
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

    // Update local events based on the list of event IDs
    private void updateLocalEventIds(@NonNull List<String> eventIds) {
        // Determine which events to add or remove
        Set<String> currentEventIds = new HashSet<>();
        for (Event event : events) {
            currentEventIds.add(event.getId());
        }

        Set<String> newEventIds = new HashSet<>(eventIds);

        // Events to add
        Set<String> toAdd = new HashSet<>(newEventIds);
        toAdd.removeAll(currentEventIds);

        // Events to remove
        Set<String> toRemove = new HashSet<>(currentEventIds);
        toRemove.removeAll(newEventIds);

        // Remove events
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if (toRemove.contains(event.getId())) {
                iterator.remove();
                event.detachListener();
            }
        }

        // Add new events
        for (String eventId : toAdd) {
            GlobalRepository.getEvent(eventId)
                    .addOnSuccessListener(event -> {
                        if (event != null) {
                            event.setFacility(this); // Ensure the event references this facility
                            events.add(event);
                            onUpdate(); // Notify listeners about the update
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching event: " + eventId, e);
                    });
        }
    }

    // Stop listening to the events when it's no longer needed
    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private void onEventsUpdated() {
        // Notify that the events list has been updated
        Log.d("Facility", "Events list updated");
        onUpdate(); // Trigger the onUpdateListener
    }

    @Override
    public void onUpdate() {
        if (onUpdateListener != null) {
            onUpdateListener.run();
        }
    }

    @Override
    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }
}
