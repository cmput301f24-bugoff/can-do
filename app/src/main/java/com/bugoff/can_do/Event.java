package com.bugoff.can_do;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Event implements DatabaseEntity {
    // data fields
    private String id; // Unique ID of the event
    private Facility facility; // The facility where the event is held
    private String name; // The name of the event
    private String description; // The description of the event
    private String qrCodeHash; // The hash of the QR code for the event
    private Date registrationStartDate; // The start date and time for registration
    private Date registrationEndDate; // The end date and time for registration
    private Date eventStartDate; // The start date and time for the event
    private Date eventEndDate; // The end date and time for the event
    private Integer numberOfParticipants; // The number of participants to be selected for the event
    private Boolean geolocationRequired; // Whether the event requires geo-location verification
    private List<User> waitingListEntrants; // The list of users on the waiting list
    private Map<User, Location> entrantsLocations; // The locations of the entrants
    private Map<User, EntrantStatus> entrantStatuses; // The statuses of the entrants
    private List<User> selectedEntrants; // Entrants selected in the lottery
    private List<User> enrolledEntrants; // Entrants who accepted and enrolled

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
