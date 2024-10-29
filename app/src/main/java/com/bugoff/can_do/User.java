package com.bugoff.can_do;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class User implements DatabaseEntity {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean isAdmin;
    private Facility facility; // Associated facility if the user is an organizer
    private List<Event> eventsJoined; // Events where the user joined the waiting list
    private List<Event> eventsEnrolled; // Events where the user is enrolled

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private Runnable onUpdateListener;

    public User(String androidId) {
        this.id = androidId;
        this.name = "";
        this.email = "";
        this.phoneNumber = "";
        this.isAdmin = false;
        this.facility = null;
        this.eventsJoined = new ArrayList<>();
        this.eventsEnrolled = new ArrayList<>();
    }

    public User(String id, String name, String email, String phoneNumber, Boolean isAdmin, Facility facility) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isAdmin = isAdmin;
        this.facility = facility;
        this.eventsJoined = new ArrayList<>();
        this.eventsEnrolled = new ArrayList<>();
    }

    // Updated Constructor from Firestore document
    public User(@NonNull DocumentSnapshot doc) {
        this.id = doc.getId();
        this.name = doc.getString("name");
        this.email = doc.getString("email");
        this.phoneNumber = doc.getString("phoneNumber");
        this.isAdmin = doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : Boolean.FALSE;

        String facilityId = doc.getString("facilityId");
        if (facilityId != null) {
            // Fetch the Facility object asynchronously
            GlobalRepository.getFacility(facilityId)
                    .addOnSuccessListener(facility -> {
                        this.facility = facility;
                        facility.setOwner(this); // Ensure bidirectional reference
                        onUpdate(); // Notify listeners about the update
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching facility for user: " + id, e);
                    });
        } else {
            this.facility = null;
        }

        this.eventsJoined = new ArrayList<>();
        this.eventsEnrolled = new ArrayList<>();

        deserializeEventsJoined(doc.get("eventsJoined"));
        deserializeEventsEnrolled(doc.get("eventsEnrolled"));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        map.put("phoneNumber", phoneNumber);
        map.put("isAdmin", isAdmin);
        if (facility != null) {
            map.put("facilityId", facility.getId());
        } else {
            map.put("facilityId", null);
        }
        map.put("eventsJoined", serializeEventsJoined(eventsJoined));
        map.put("eventsEnrolled", serializeEventsEnrolled(eventsEnrolled));
        return map;
    }

    private List<String> serializeEventsJoined(List<Event> events) {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    private List<String> serializeEventsEnrolled(List<Event> events) {
        List<String> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        return eventIds;
    }

    private void deserializeEventsJoined(Object data) {
        if (data instanceof List<?>) {
            List<?> eventIds = (List<?>) data;
            for (Object eventIdObj : eventIds) {
                if (eventIdObj instanceof String) {
                    String eventId = (String) eventIdObj;
                    GlobalRepository.getEventsCollection().document(eventId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Event event = new Event(this.getFacility(), doc);
                                    this.eventsJoined.add(event);
                                    onUpdate(); // Notify listeners if necessary
                                } else {
                                    Log.w("User", "No such event with ID: " + eventId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("User", "Error fetching event with ID: " + eventId, e);
                            });
                }
            }
        }
    }

    private void deserializeEventsEnrolled(Object data) {
        if (data instanceof List<?>) {
            List<?> eventIds = (List<?>) data;
            for (Object eventIdObj : eventIds) {
                if (eventIdObj instanceof String) {
                    String eventId = (String) eventIdObj;
                    GlobalRepository.getEventsCollection().document(eventId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Event event = new Event(this.getFacility(), doc);
                                    this.eventsEnrolled.add(event);
                                    onUpdate(); // Notify listeners if necessary
                                } else {
                                    Log.w("User", "No such event with ID: " + eventId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("User", "Error fetching event with ID: " + eventId, e);
                            });
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        setRemote();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        setRemote();
    }

    public List<Event> getEventsJoined() {
        return Collections.unmodifiableList(eventsJoined);
    }

    public void setEventsJoined(List<Event> eventsJoined) {
        this.eventsJoined = eventsJoined;
        setRemote();
    }

    public void addEventJoined(Event event) {
        this.eventsJoined.add(event);
        setRemote();
    }

    public void removeEventJoined(Event event) {
        this.eventsJoined.remove(event);
        setRemote();
    }

    public List<Event> getEventsEnrolled() {
        return Collections.unmodifiableList(eventsEnrolled);
    }

    public void setEventsEnrolled(List<Event> eventsEnrolled) {
        this.eventsEnrolled = eventsEnrolled;
        setRemote();
    }

    public void addEventEnrolled(Event event) {
        this.eventsEnrolled.add(event);
        setRemote();
    }

    public void removeEventEnrolled(Event event) {
        this.eventsEnrolled.remove(event);
        setRemote();
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @Override
    public void setRemote() {
        DocumentReference userRef = GlobalRepository.getUsersCollection().document(id);

        // Create a map of fields to be saved or updated
        Map<String, Object> update = this.toMap();

        // Save or update the facility in Firestore
        userRef.set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully saved or updated.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving or updating user", e);
                });
    }

    @Override
    public void attachListener() {
        DocumentReference userRef = GlobalRepository.getUsersCollection().document(id);

        // Attach a listener to the User document
        listener = userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to user changes for user: " + id, e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                AtomicBoolean isChanged = new AtomicBoolean(false);

                // Update name
                String updatedName = documentSnapshot.getString("name");
                if (updatedName != null && !updatedName.equals(this.name)) {
                    this.name = updatedName;
                    isChanged.set(true);
                }

                // Update email
                String updatedEmail = documentSnapshot.getString("email");
                if (updatedEmail != null && !updatedEmail.equals(this.email)) {
                    this.email = updatedEmail;
                    isChanged.set(true);
                }

                // Update phoneNumber
                String updatedPhoneNumber = documentSnapshot.getString("phoneNumber");
                if (updatedPhoneNumber != null && !updatedPhoneNumber.equals(this.phoneNumber)) {
                    this.phoneNumber = updatedPhoneNumber;
                    isChanged.set(true);
                }

                // Update isAdmin
                Boolean updatedIsAdmin = documentSnapshot.getBoolean("isAdmin");
                if (updatedIsAdmin != null && !updatedIsAdmin.equals(this.isAdmin)) {
                    this.isAdmin = updatedIsAdmin;
                    isChanged.set(true);
                }

                // Update facility
                String updatedFacilityId = documentSnapshot.getString("facilityId");
                if (updatedFacilityId != null && (this.facility == null || !updatedFacilityId.equals(this.facility.getId()))) {
                    // Fetch the updated Facility object
                    GlobalRepository.getFacility(updatedFacilityId)
                            .addOnSuccessListener(facility -> {
                                this.facility = facility;
                                facility.setOwner(this); // Ensure bidirectional reference
                                isChanged.set(true);
                                onUpdate(); // Notify listeners about the update
                            })
                            .addOnFailureListener(error -> {
                                Log.e("Firestore", "Error fetching updated facility for user: " + id, error);
                            });
                    // Early return since facility fetching is asynchronous
                    return;
                }

                // Deserialize eventsJoined
                List<String> updatedEventsJoinedIds = (List<String>) documentSnapshot.get("eventsJoined");
                if (updatedEventsJoinedIds != null) {
                    updateEventsJoined(updatedEventsJoinedIds);
                }

                // Deserialize eventsEnrolled
                List<String> updatedEventsEnrolledIds = (List<String>) documentSnapshot.get("eventsEnrolled");
                if (updatedEventsEnrolledIds != null) {
                    updateEventsEnrolled(updatedEventsEnrolledIds);
                }

                if (isChanged.get()) {
                    onUpdate(); // Notify listeners about the update
                }
            }
        });
    }

    private void updateEventsJoined(List<String> eventIds) {
        this.eventsJoined.clear();
        for (String eventId : eventIds) {
            GlobalRepository.getEventsCollection().document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Event event = new Event(this.getFacility(), doc);
                            this.eventsJoined.add(event);
                            onUpdate(); // Notify listeners if necessary
                        } else {
                            Log.w("User", "No such event with ID: " + eventId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("User", "Error fetching event with ID: " + eventId, e);
                    });
        }
    }

    private void updateEventsEnrolled(List<String> eventIds) {
        this.eventsEnrolled.clear();
        for (String eventId : eventIds) {
            GlobalRepository.getEventsCollection().document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Event event = new Event(this.getFacility(), doc);
                            this.eventsEnrolled.add(event);
                            onUpdate(); // Notify listeners if necessary
                        } else {
                            Log.w("User", "No such event with ID: " + eventId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("User", "Error fetching event with ID: " + eventId, e);
                    });
        }
    }

    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
            Log.d("Firestore", "Listener detached for user: " + id);
        }
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
