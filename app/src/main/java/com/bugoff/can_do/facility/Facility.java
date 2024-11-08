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
/**
 * Represents a facility in the system.
 *
 * <p>A Facility has an owner (User), a name, an address, and a list of events.
 * It implements the DatabaseEntity interface, meaning it can be serialized and
 * deserialized to/from a database.</p>
 */
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
    /**
     * Constructs a new Facility with the specified owner.
     *
     * <p>The facility's ID is set to the owner's ID, and the owner is linked to this facility.</p>
     *
     * @param owner The owner (organizer) of the facility. Must not be null.
     */
    public Facility(@NonNull User owner) {
        this.id = owner.getId(); // The id of the facility is the Android ID of the user
        this.owner = owner;
        owner.setFacility(this); // Set the facility of the owner to this facility
        this.name = "";
        this.address = "";
        this.events = new ArrayList<>();
        this.db = FirestoreHelper.getInstance().getDb();
    }
    /**
     * Constructs a Facility from a Firestore document.
     *
     * <p>The events are deserialized asynchronously.</p>
     *
     * @param doc The Firestore DocumentSnapshot representing the facility. Must not be null.
     */
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
    /**
     * Deserializes a User object from a given user ID.
     *
     * @param userId The ID of the user to deserialize.
     * @return The User object corresponding to the given ID, or null if not found.
     */
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
    /**
     * Deserializes events from Firestore data.
     *
     * @param data The Firestore data representing event IDs.
     */
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

    /**
     * Serializes a list of events into a list of event IDs.
     *
     * @param events The list of Event objects to serialize.
     * @return A list of event IDs corresponding to the given events.
     */
    private List<String> serializeEvents(List<Event> events) {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }
    /**
     * Gets the unique identifier of the facility.
     *
     * @return The facility's unique identifier.
     */
    @Override
    public String getId() {
        return id;
    }
    /**
     * Converts the Facility object into a Map suitable for serialization to a database.
     *
     * @return A Map containing the Facility's data fields as key-value pairs.
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("ownerId", owner.getId());
        map.put("name", name);
        map.put("address", address);
        map.put("events", serializeEvents(events));
        return map;
    }
    /**
     * Gets the owner of the facility.
     *
     * @return The User who owns the facility.
     */
    public User getOwner() {
        return owner;
    }
    /**
     * Sets the owner of the facility and updates the remote database.
     *
     * <p>This method also logs the owner's ID and synchronizes the change with the database.</p>
     *
     * @param user The new owner to set for the facility.
     */
    public void setOwner(User user) {
        Log.d("Facility", "Setting owner to: " + user.getId());
        this.owner = user;
        setRemote();
    }
    /**
     * Gets the name of the facility.
     *
     * @return The name of the facility.
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the name of the facility and updates the remote database.
     *
     * @param name The new name to set for the facility.
     */
    public void setName(String name) {
        this.name = name;
        setRemote();
    }
    /**
     * Gets an unmodifiable list of events held at the facility.
     *
     * @return An unmodifiable List of Event objects.
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }
    /**
     * Sets the list of events held at the facility and updates the remote database.
     *
     * @param events The new list of events to associate with the facility.
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        setRemote();
    }
    /**
     * Gets the physical address of the facility.
     *
     * @return The facility's address.
     */
    public String getAddress() {
        return address;
    }
    /**
     * Sets the physical address of the facility and updates the remote database.
     *
     * @param address The new address to set for the facility.
     */
    public void setAddress(String address) {
        this.address = address;
        setRemote();
    }
    /**
     * Adds an event to the facility's list of events and updates the remote database.
     *
     * @param event The Event to add to the facility. Must not be null.
     */
    public void addEvent(@NonNull Event event) {
        events.add(event);
        setRemote();
    }
    /**
     * Removes an event from the facility's list of events and updates the remote database.
     *
     * <p>This method also detaches any listeners associated with the removed event.</p>
     *
     * @param event The Event to remove from the facility.
     */
    public void removeEvent(Event event) {
        events.remove(event);
        event.detachListener();
        setRemote();
    }
    /**
     * Saves or updates the facility in the remote Firestore database.
     *
     * <p>This method serializes the facility's data and sends it to Firestore with merge options
     * to allow partial updates.</p>
     */
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
    /**
     * Retrieves a list of event IDs associated with the facility.
     *
     * @return A List of event IDs as Strings.
     */
    @NonNull
    private List<String> getEventIds() {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }
    /**
     * Attaches a real-time listener to the facility document in Firestore.
     *
     * <p>The listener will monitor changes to the facility's document in Firestore
     * and update the facility's properties accordingly.</p>
     */
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
    /**
     * Fetches events from Firestore in batches using the 'whereIn' query.
     *
     * <p>This method processes the provided list of event IDs in batches of 10 to optimize
     * Firestore queries. Each batch is fetched asynchronously, and any events retrieved are
     * added to the facility's event list.</p>
     *
     * @param eventIds The list of event IDs to fetch.
     */
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
    /**
     * Updates the local events list based on a provided list of event IDs.
     *
     * <p>This method determines which events need to be added or removed from the facility.
     * Any events that are no longer associated with the facility are removed, while new events
     * are fetched and added asynchronously.</p>
     *
     * @param eventIds The list of event IDs that represent the current state of events for the facility.
     */
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
    /**
     * Detaches the real-time listener from the facility document in Firestore.
     *
     * <p>This method is called when the facility no longer needs to be monitored, such as when
     * the app is closed or the ViewModel is cleared, to prevent memory leaks.</p>
     */
    // Stop listening to the events when it's no longer needed
    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
    /**
     * Notifies listeners that the events list has been updated.
     *
     * <p>This method logs the update and triggers the onUpdateListener to notify any attached listeners.</p>
     */
    private void onEventsUpdated() {
        // Notify that the events list has been updated
        Log.d("Facility", "Events list updated");
        onUpdate(); // Trigger the onUpdateListener
    }
    /**
     * Triggers the onUpdateListener when the facility's data is updated.
     *
     * <p>This method is called whenever any property of the facility changes,
     * such as the name, address, or events list.</p>
     */
    @Override
    public void onUpdate() {
        if (onUpdateListener != null) {
            onUpdateListener.run();
        }
    }
    /**
     * Sets a listener to be called when the facility is updated.
     *
     * <p>This listener can be used by other components to respond to changes
     * in the facility's data, such as refreshing the UI when events are modified.</p>
     *
     * @param listener A Runnable to execute on facility updates.
     */
    @Override
    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }
}
