package com.bugoff.can_do.event;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.database.DatabaseEntity;
import com.bugoff.can_do.EntrantStatus;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Event implements DatabaseEntity {
    // Data fields
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

    // Default constructor
    public Event(@NonNull Facility facility) {
        this.id = GlobalRepository.getEventsCollection().document().getId();
        this.facility = facility;
        facility.addEvent(this);
        this.name = "";
        this.description = "";
        this.qrCodeHash = "";
        this.registrationStartDate = new Date();
        this.registrationEndDate = new Date();
        this.eventStartDate = new Date();
        this.eventEndDate = new Date();
        this.numberOfParticipants = 0;
        this.geolocationRequired = false;
        this.waitingListEntrants = new ArrayList<>();
        this.entrantsLocations = new HashMap<>();
        this.entrantStatuses = new HashMap<>();
        this.selectedEntrants = new ArrayList<>();
        this.enrolledEntrants = new ArrayList<>();
    }

    // Constructor from Firestore document
    public Event(@NonNull Facility facility, @NonNull DocumentSnapshot doc) {
        this.id = doc.getId();
        this.facility = facility;
        this.name = doc.getString("name");
        this.description = doc.getString("description");
        this.qrCodeHash = doc.getString("qrCodeHash");
        this.registrationStartDate = doc.getDate("registrationStartDate");
        this.registrationEndDate = doc.getDate("registrationEndDate");
        this.eventStartDate = doc.getDate("eventStartDate");
        this.eventEndDate = doc.getDate("eventEndDate");
        this.numberOfParticipants = doc.getLong("numberOfParticipants") != null ? doc.getLong("numberOfParticipants").intValue() : 0;
        this.geolocationRequired = doc.getBoolean("geolocationRequired");
        this.waitingListEntrants = deserializeUserList(doc.get("waitingListEntrants"));
        this.entrantsLocations = deserializeEntrantsLocations(doc.get("entrantsLocations"));
        this.entrantStatuses = deserializeEntrantStatuses(doc.get("entrantStatuses"));
        this.selectedEntrants = deserializeUserList(doc.get("selectedEntrants"));
        this.enrolledEntrants = deserializeUserList(doc.get("enrolledEntrants"));
    }

    private List<User> deserializeUserList(Object data) {
        List<User> users = new ArrayList<>();
        if (data instanceof List<?>) {
            List<?> userIds = (List<?>) data;
            for (Object userIdObj : userIds) {
                if (userIdObj instanceof String) {
                    String userId = (String) userIdObj;
                    GlobalRepository.getUsersCollection().document(userId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String name = doc.getString("name");
                                    Boolean isAdmin = doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : Boolean.FALSE;
                                    User user = new User(userId, name, null, null, isAdmin, this.facility);
                                    users.add(user);
                                    onUpdate();
                                } else {
                                    Log.w("Event", "No such user with ID: " + userId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Event", "Error fetching user with ID: " + userId, e);
                            });
                }
            }
        }
        return users;
    }

    private Map<User, Location> deserializeEntrantsLocations(Object data) {
        Map<User, Location> entrantsLoc = new HashMap<>();
        if (data instanceof Map<?, ?>) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Map<?, ?>) {
                    String userId = (String) entry.getKey();
                    Map<?, ?> locMap = (Map<?, ?>) entry.getValue();
                    Double latitude = locMap.get("latitude") instanceof Number ? ((Number) Objects.requireNonNull(locMap.get("latitude"))).doubleValue() : null;
                    Double longitude = locMap.get("longitude") instanceof Number ? ((Number) Objects.requireNonNull(locMap.get("longitude"))).doubleValue() : null;

                    if (latitude != null && longitude != null) {
                        GlobalRepository.getUsersCollection().document(userId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        String name = doc.getString("name");
                                        Boolean isAdmin = doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : Boolean.FALSE;
                                        User user = new User(userId, name, null, null, isAdmin, this.facility);

                                        Location location = new Location("");
                                        location.setLatitude(latitude);
                                        location.setLongitude(longitude);
                                        entrantsLoc.put(user, location);
                                        onUpdate();
                                    } else {
                                        Log.w("Event", "No such user with ID: " + userId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error fetching user with ID: " + userId, e);
                                });
                    }
                }
            }
        }
        return entrantsLoc;
    }

    private Map<User, EntrantStatus> deserializeEntrantStatuses(Object data) {
        Map<User, EntrantStatus> entrantStatusMap = new HashMap<>();
        if (data instanceof Map<?, ?>) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    String userId = (String) entry.getKey();
                    String statusStr = (String) entry.getValue();
                    EntrantStatus status;
                    try {
                        status = EntrantStatus.valueOf(statusStr);
                    } catch (IllegalArgumentException e) {
                        Log.e("Event", "Invalid EntrantStatus: " + statusStr, e);
                        continue;
                    }

                    GlobalRepository.getUsersCollection().document(userId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String name = doc.getString("name");
                                    Boolean isAdmin = doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : Boolean.FALSE;
                                    User user = new User(userId, name, null, null, isAdmin, this.facility);

                                    entrantStatusMap.put(user, status);
                                    onUpdate();
                                } else {
                                    Log.w("Event", "No such user with ID: " + userId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Event", "Error fetching user with ID: " + userId, e);
                            });
                }
            }
        }
        return entrantStatusMap;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("facilityId", facility.getId());
        map.put("name", name);
        map.put("description", description);
        map.put("qrCodeHash", qrCodeHash);
        map.put("registrationStartDate", registrationStartDate);
        map.put("registrationEndDate", registrationEndDate);
        map.put("eventStartDate", eventStartDate);
        map.put("eventEndDate", eventEndDate);
        map.put("numberOfParticipants", numberOfParticipants);
        map.put("geolocationRequired", geolocationRequired);
        map.put("waitingListEntrants", serializeUserList(waitingListEntrants));
        map.put("entrantsLocations", serializeEntrantsLocations(entrantsLocations));
        map.put("entrantStatuses", serializeEntrantStatuses(entrantStatuses));
        map.put("selectedEntrants", serializeUserList(selectedEntrants));
        map.put("enrolledEntrants", serializeUserList(enrolledEntrants));
        return map;
    }

    private List<Map<String, Object>> serializeUserList(List<User> users) {
        // Convert User objects to a serializable format
        List<Map<String, Object>> serializedList = new ArrayList<>();
        for (User user : users) {
            serializedList.add(user.toMap());
        }
        return serializedList;
    }

    private Map<String, Object> serializeEntrantsLocations(Map<User, Location> entrantsLocations) {
        Map<String, Object> serializedMap = new HashMap<>();
        for (Map.Entry<User, Location> entry : entrantsLocations.entrySet()) {
            Location location = entry.getValue();
            Map<String, Object> locationMap = new HashMap<>();

            // Extract relevant data from Android Location object
            locationMap.put("latitude", location.getLatitude());
            locationMap.put("longitude", location.getLongitude());

            serializedMap.put(entry.getKey().getId(), locationMap);
        }
        return serializedMap;
    }

    private Map<String, Object> serializeEntrantStatuses(Map<User, EntrantStatus> entrantStatuses) {
        // Convert Map<User, EntrantStatus> to a serializable format
        Map<String, Object> serializedMap = new HashMap<>();
        for (Map.Entry<User, EntrantStatus> entry : entrantStatuses.entrySet()) {
            serializedMap.put(entry.getKey().getId(), entry.getValue().name());  // Store enum as a String
        }
        return serializedMap;
    }

    @Override
    public String getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQrCodeHash() {
        return qrCodeHash;
    }

    public void setQrCodeHash(String qrCodeHash) {
        this.qrCodeHash = qrCodeHash;
    }

    public Date getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public Date getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(Integer numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public List<User> getWaitingListEntrants() {
        return Collections.unmodifiableList(waitingListEntrants);
    }

    public void setWaitingListEntrants(List<User> waitingListEntrants) {
        this.waitingListEntrants = waitingListEntrants;
    }

    public Map<User, Location> getEntrantsLocations() {
        return Collections.unmodifiableMap(entrantsLocations);
    }

    public void setEntrantsLocations(Map<User, Location> entrantsLocations) {
        this.entrantsLocations = entrantsLocations;
    }

    public Map<User, EntrantStatus> getEntrantStatuses() {
        return Collections.unmodifiableMap(entrantStatuses);
    }

    public void setEntrantStatuses(Map<User, EntrantStatus> entrantStatuses) {
        this.entrantStatuses = entrantStatuses;
    }

    public List<User> getSelectedEntrants() {
        return Collections.unmodifiableList(selectedEntrants);
    }

    public void setSelectedEntrants(List<User> selectedEntrants) {
        this.selectedEntrants = selectedEntrants;
    }

    public List<User> getEnrolledEntrants() {
        return Collections.unmodifiableList(enrolledEntrants);
    }

    public void setEnrolledEntrants(List<User> enrolledEntrants) {
        this.enrolledEntrants = enrolledEntrants;
    }

    // Method to save the event to Firestore
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

                // Update other fields
                this.name = documentSnapshot.getString("name");
                this.description = documentSnapshot.getString("description");
                this.qrCodeHash = documentSnapshot.getString("qrCodeHash");
                this.registrationStartDate = documentSnapshot.getDate("registrationStartDate");
                this.registrationEndDate = documentSnapshot.getDate("registrationEndDate");
                this.eventStartDate = documentSnapshot.getDate("eventStartDate");
                this.eventEndDate = documentSnapshot.getDate("eventEndDate");
                this.numberOfParticipants = documentSnapshot.getLong("numberOfParticipants") != null ? documentSnapshot.getLong("numberOfParticipants").intValue() : 0;
                this.geolocationRequired = documentSnapshot.getBoolean("geolocationRequired");

                // Deserialize complex fields
                this.waitingListEntrants = deserializeUserList(documentSnapshot.get("waitingListEntrants"));
                this.entrantsLocations = deserializeEntrantsLocations(documentSnapshot.get("entrantsLocations"));
                this.entrantStatuses = deserializeEntrantStatuses(documentSnapshot.get("entrantStatuses"));
                this.selectedEntrants = deserializeUserList(documentSnapshot.get("selectedEntrants"));
                this.enrolledEntrants = deserializeUserList(documentSnapshot.get("enrolledEntrants"));

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

    @Override
    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }
}
