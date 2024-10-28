package com.bugoff.can_do;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

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
        this.name = null;
        this.isAdmin = false;
        this.facility = null;
    }

    public User(String id, String name, Boolean isAdmin, Facility facility) {
        this.id = id;
        this.name = name;
        this.isAdmin = isAdmin;
        this.facility = facility;
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
        map.put("isAdmin", isAdmin);
        if (facility != null) {
            map.put("facilityId", facility.getId());
        } else {
            map.put("facilityId", null);
        }
        return map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                // Update user information if fields have changed
                String updatedName = documentSnapshot.getString("name");
                Boolean updatedIsAdmin = documentSnapshot.getBoolean("isAdmin");
                String updatedFacilityId = documentSnapshot.getString("facilityId");

                AtomicBoolean isChanged = new AtomicBoolean(false);

                if (updatedName != null && !updatedName.equals(this.name)) {
                    this.name = updatedName;
                    isChanged.set(true);
                }

                if (updatedIsAdmin != null && !updatedIsAdmin.equals(this.isAdmin)) {
                    this.isAdmin = updatedIsAdmin;
                    isChanged.set(true);
                }

                if (updatedFacilityId != null && (this.facility == null || !updatedFacilityId.equals(this.facility.getId()))) {
                    // Fetch the updated Facility object
                    GlobalRepository.getFacility(updatedFacilityId)
                            .addOnSuccessListener(facility -> {
                                this.facility = facility;
                                isChanged.set(true);
                                onUpdate(); // Notify that the user has been updated
                            })
                            .addOnFailureListener(error -> {
                                Log.e("Firestore", "Error fetching updated facility for user: " + id, error);
                            });
                    // Early return since facility fetching is asynchronous
                    return;
                }

                if (isChanged.get()) {
                    onUpdate(); // Notify that the user has been updated
                }
            }
        });
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

    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }
}
