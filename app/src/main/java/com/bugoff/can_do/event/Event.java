package com.bugoff.can_do.event;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.EntrantStatus;
import com.bugoff.can_do.database.DatabaseEntity;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
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

/**
 * {@code Event} represents an event that can be held at a facility. It contains
 * details such as the event name, description, dates, registration details, and
 * participant information. Events can be created, updated, and monitored in real-time
 * through Firestore, allowing for dynamic updates to event details and participant lists.
 */
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
    private Integer maxNumberOfParticipants; // The number of participants to be selected for the event
    private Boolean geolocationRequired; // Whether the event requires geo-location verification
    private List<String> waitingListEntrants; // The list of user IDs on the waiting list
    private Map<String, Location> entrantsLocations; // The locations of the entrants (key: user ID)
    private Map<String, EntrantStatus> entrantStatuses; // The statuses of the entrants (key: user ID)
    private List<String> selectedEntrants; // Entrants selected in the lottery (user IDs)
    private List<String> enrolledEntrants; // Entrants who accepted and enrolled (user IDs)
    private List<String> cancelledEntrants; // Entrants who cancelled (user IDs)
    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private Runnable onUpdateListener;
    private String base64Image;

    /**
     * Default constructor for Firestore serialization.
     */
    public Event(@NonNull Facility facility) {
        this.id = GlobalRepository.getEventsCollection().document().getId();
        this.facility = facility;
        this.name = "";
        this.description = "";
        this.qrCodeHash = "";
        this.registrationStartDate = new Date();
        this.registrationEndDate = new Date();
        this.eventStartDate = new Date();
        this.eventEndDate = new Date();
        this.maxNumberOfParticipants = 0;
        this.geolocationRequired = false;
        this.waitingListEntrants = new ArrayList<>();
        this.entrantsLocations = new HashMap<>();
        this.entrantStatuses = new HashMap<>();
        this.selectedEntrants = new ArrayList<>();
        this.enrolledEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        // facility.addEvent(this); // Ensure bidirectional reference (edit: maybe not?)
    }

    /**
 * Constructs an Event object from a Firestore DocumentSnapshot.
 *
 * @param facility The facility where the event is held.
 * @param doc      The Firestore DocumentSnapshot containing event data.
 */
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
        this.maxNumberOfParticipants = doc.getLong("maxNumberOfParticipants") != null ? Objects.requireNonNull(doc.getLong("maxNumberOfParticipants")).intValue() : 0;
        this.geolocationRequired = doc.getBoolean("geolocationRequired") != null ? doc.getBoolean("geolocationRequired") : Boolean.FALSE;

        this.base64Image = doc.getString("base64Image");
        // Initialize user lists as lists of user IDs
        this.waitingListEntrants = new ArrayList<>();
        this.entrantsLocations = new HashMap<>();
        this.entrantStatuses = new HashMap<>();
        this.selectedEntrants = new ArrayList<>();
        this.enrolledEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        // Deserialize complex fields
        deserializeUserList(doc.get("waitingListEntrants"), waitingListEntrants);
        deserializeEntrantsLocations(doc.get("entrantsLocations"), entrantsLocations);
        deserializeEntrantStatuses(doc.get("entrantStatuses"), entrantStatuses);
        deserializeUserList(doc.get("selectedEntrants"), selectedEntrants);
        deserializeUserList(doc.get("enrolledEntrants"), enrolledEntrants);
        deserializeUserList(doc.get("cancelledEntrants"), cancelledEntrants);
    }

    /**
     * Converts the Event object to a Map format compatible with Firestore.
     *
     * @return A Map object representing the Event data.
     */
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
        map.put("maxNumberOfParticipants", maxNumberOfParticipants);
        map.put("geolocationRequired", geolocationRequired);
        map.put("waitingListEntrants", waitingListEntrants); // List of user IDs
        map.put("entrantsLocations", entrantsLocations); // Map of user IDs to Locations
        map.put("entrantStatuses", entrantStatuses); // Map of user IDs to statuses
        map.put("selectedEntrants", selectedEntrants); // List of user IDs
        map.put("enrolledEntrants", enrolledEntrants); // List of user IDs
        map.put("cancelledEntrants", cancelledEntrants); // List of user IDs
        map.put("base64Image", base64Image);
        return map;
    }

    /**
     * Deserialize a list of user IDs from Firestore and populate the provided list.
     *
     * @param data       The raw data from Firestore.
     * @param targetList The list to populate with user IDs.
     */
    private void deserializeUserList(Object data, List<String> targetList) {
        if (data instanceof List<?>) {
            List<?> userIds = (List<?>) data;
            for (Object userIdObj : userIds) {
                if (userIdObj instanceof String) {
                    String userId = (String) userIdObj;
                    targetList.add(userId);
                }
            }
        }
    }

    /**
     * Deserialize entrants' locations from Firestore and populate the provided map.
     *
     * @param data      The raw data from Firestore.
     * @param targetMap The map to populate with user IDs and their locations.
     */
    private void deserializeEntrantsLocations(Object data, Map<String, Location> targetMap) {
        if (data instanceof Map<?, ?>) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Map<?, ?>) {
                    String userId = (String) entry.getKey();
                    Map<?, ?> locMap = (Map<?, ?>) entry.getValue();
                    Double latitude = locMap.get("latitude") instanceof Number ? ((Number) Objects.requireNonNull(locMap.get("latitude"))).doubleValue() : null;
                    Double longitude = locMap.get("longitude") instanceof Number ? ((Number) Objects.requireNonNull(locMap.get("longitude"))).doubleValue() : null;
                    if (latitude != null && longitude != null) {
                        Location location = new Location("");
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        targetMap.put(userId, location);
                    }
                }
            }
        }
    }

    /**
     * Deserialize entrant statuses from Firestore and populate the provided map.
     *
     * @param data      The raw data from Firestore.
     * @param targetMap The map to populate with user IDs and their statuses.
     */
    private void deserializeEntrantStatuses(Object data, Map<String, EntrantStatus> targetMap) {
        if (data instanceof Map<?, ?>) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    String userId = (String) entry.getKey();
                    String statusStr = (String) entry.getValue();
                    EntrantStatus status;
                    try {
                        status = EntrantStatus.valueOf(statusStr);
                        targetMap.put(userId, status);
                    } catch (IllegalArgumentException e) {
                        Log.e("Event", "Invalid EntrantStatus: " + statusStr, e);
                    }
                }
            }
        }
    }

    /**
     * Deserialize a map of user IDs to locations.
     *
     * @param data The raw data from Firestore.
     * @return A map with user IDs as keys and Location objects as values.
     */
    private Map<String, Location> deserializeEntrantsLocations(Object data) {
        Map<String, Location> entrantsLoc = new HashMap<>();
        deserializeEntrantsLocations(data, entrantsLoc);
        return entrantsLoc;
    }

    /**
     * Deserialize a map of user IDs to entrant statuses.
     *
     * @param data The raw data from Firestore.
     * @return A map with user IDs as keys and EntrantStatus enums as values.
     */
    private Map<String, EntrantStatus> deserializeEntrantStatuses(Object data) {
        Map<String, EntrantStatus> entrantStatusMap = new HashMap<>();
        deserializeEntrantStatuses(data, entrantStatusMap);
        return entrantStatusMap;
    }

    /**
     * Deserialize a list of user IDs.
     *
     * @param data The raw data from Firestore.
     * @return A list of user IDs.
     */
    private List<String> deserializeUserList(Object data) {
        List<String> users = new ArrayList<>();
        deserializeUserList(data, users);
        return users;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
        setRemote();
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
        setRemote();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setRemote();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        setRemote();
    }

    public String getQrCodeHash() {
        return qrCodeHash;
    }

    public void setQrCodeHash(String qrCodeHash) {
        this.qrCodeHash = qrCodeHash;
        setRemote();
    }

    public Date getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
        setRemote();
    }

    public Date getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
        setRemote();
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
        setRemote();
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
        setRemote();
    }

    public Integer getMaxNumberOfParticipants() {
        return maxNumberOfParticipants;
    }

    public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
        this.maxNumberOfParticipants = maxNumberOfParticipants;
        setRemote();
    }

    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
        setRemote();
    }

    public List<String> getWaitingListEntrants() {
        return Collections.unmodifiableList(waitingListEntrants);
    }

    public void setWaitingListEntrants(List<String> waitingListEntrants) {
        this.waitingListEntrants = waitingListEntrants;
        setRemote();
    }

    public Map<String, Location> getEntrantsLocations() {
        return Collections.unmodifiableMap(entrantsLocations);
    }

    public void setEntrantsLocations(Map<String, Location> entrantsLocations) {
        this.entrantsLocations = entrantsLocations;
        setRemote();
    }

    public Map<String, EntrantStatus> getEntrantStatuses() {
        return Collections.unmodifiableMap(entrantStatuses);
    }

    public void setEntrantStatuses(Map<String, EntrantStatus> entrantStatuses) {
        this.entrantStatuses = entrantStatuses;
        setRemote();
    }

    public List<String> getSelectedEntrants() {
        return Collections.unmodifiableList(selectedEntrants);
    }

    public void setSelectedEntrants(List<String> selectedEntrants) {
        this.selectedEntrants = selectedEntrants;
        setRemote();
    }

    public List<String> getEnrolledEntrants() {
        return Collections.unmodifiableList(enrolledEntrants);
    }

    public void setEnrolledEntrants(List<String> enrolledEntrants) {
        this.enrolledEntrants = enrolledEntrants;
        setRemote();
    }

    public List<String> getCancelledEntrants() {
        return Collections.unmodifiableList(cancelledEntrants);
    }

    // Methods to interact with Event

    /**
     * Adds a user ID to the waiting list entrants.
     *
     * @param userId The ID of the user to add.
     */
    public void addWaitingListEntrant(String userId) {
        if (!waitingListEntrants.contains(userId)) {
            waitingListEntrants.add(userId);
            setRemote();
        }
    }

    /**
     * Removes a user ID from the waiting list entrants.
     *
     * @param userId The ID of the user to remove.
     */
    public void removeWaitingListEntrant(String userId) {
        if (waitingListEntrants.contains(userId)) {
            waitingListEntrants.remove(userId);
            setRemote();
        }
    }

    /**
     * Updates the entrant status for a specific user.
     *
     * @param userId The ID of the user.
     * @param status The new status to set.
     */
    public void updateEntrantStatus(String userId, EntrantStatus status) {
        entrantStatuses.put(userId, status);
        setRemote();
    }

    /**
     * Adds a user ID to the selected entrants list.
     *
     * @param userId The ID of the user to add.
     */
    public void addSelectedEntrant(String userId) {
        if (!selectedEntrants.contains(userId)) {
            selectedEntrants.add(userId);
            setRemote();
        }
    }

    /**
     * Removes a user ID from the selected entrants list.
     *
     * @param userId The ID of the user to remove.
     */
    public void removeSelectedEntrant(String userId) {
        if (selectedEntrants.contains(userId)) {
            selectedEntrants.remove(userId);
            setRemote();
        }
    }

    /**
     * Adds a user ID to the enrolled entrants list.
     *
     * @param userId The ID of the user to add.
     */
    public void enrollEntrant(String userId) {
        if (!enrolledEntrants.contains(userId)) {
            enrolledEntrants.add(userId);
            setRemote();
        }
    }

    /**
     * Saves the current event data to Firestore. This method transforms the current
     * event object into a Map format compatible with Firestore and updates the event
     * document with the specified ID in the "Events" Firestore collection.
     *
     * If the save operation is successful, a log entry is generated indicating
     * the event was updated successfully. In case of a failure, an error message
     * is logged.
     */
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

    /**
     * Attaches a snapshot listener to the Firestore document corresponding to this
     * event. This listener will monitor real-time changes in the event document and
     * automatically update the local instance fields whenever changes are detected in
     * Firestore.
     *
     * Upon detecting a change:
     * - If there is an updated facility ID, it fetches the updated Facility object,
     *   ensuring a bidirectional reference between Event and Facility.
     * - Other event details such as name, description, dates, and participant limits
     *   are also updated locally.
     *
     * If there are changes, it calls `onUpdate()` to notify any listeners of these updates.
     * Error handling includes logging failures to fetch the updated facility or
     * listen to event changes.
     */
    @Override
    public void attachListener() {
        DocumentReference eventRef = GlobalRepository.getEventsCollection().document(id);
        listener = eventRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening to event changes for event: " + id, e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String updatedFacilityId = documentSnapshot.getString("facilityId");
                AtomicBoolean isChanged = new AtomicBoolean(false);
                if (updatedFacilityId != null && !updatedFacilityId.equals(this.facility.getId())) {
                    GlobalRepository.getFacility(updatedFacilityId)
                            .addOnSuccessListener(facility -> {
                                this.facility = facility;
                                this.facility.addEvent(this);
                                isChanged.set(true);
                                onUpdate();
                            })
                            .addOnFailureListener(error -> {
                                Log.e("Firestore", "Error fetching updated facility for event: " + id, error);
                            });
                    return;
                }
                this.name = documentSnapshot.getString("name");
                this.description = documentSnapshot.getString("description");
                this.qrCodeHash = documentSnapshot.getString("qrCodeHash");
                this.registrationStartDate = documentSnapshot.getDate("registrationStartDate");
                this.registrationEndDate = documentSnapshot.getDate("registrationEndDate");
                this.eventStartDate = documentSnapshot.getDate("eventStartDate");
                this.eventEndDate = documentSnapshot.getDate("eventEndDate");
                this.maxNumberOfParticipants = documentSnapshot.getLong("maxNumberOfParticipants") != null
                        ? documentSnapshot.getLong("maxNumberOfParticipants").intValue()
                        : 0;
                this.geolocationRequired = documentSnapshot.getBoolean("geolocationRequired");
                this.base64Image = documentSnapshot.getString("base64Image");

                // Deserialize complex fields
                this.waitingListEntrants = deserializeUserList(documentSnapshot.get("waitingListEntrants"));
                this.cancelledEntrants = deserializeUserList(documentSnapshot.get("cancelledEntrants"));
                this.entrantsLocations = deserializeEntrantsLocations(documentSnapshot.get("entrantsLocations"));
                this.entrantStatuses = deserializeEntrantStatuses(documentSnapshot.get("entrantStatuses"));
                this.selectedEntrants = deserializeUserList(documentSnapshot.get("selectedEntrants"));
                this.enrolledEntrants = deserializeUserList(documentSnapshot.get("enrolledEntrants"));

                if (isChanged.get()) {
                    onUpdate();
                }
            }
        });
    }

    /**
     * Detaches the snapshot listener from the Firestore document. This stops real-time
     * updates for this event instance, helping to reduce network usage and unnecessary
     * updates if the event is no longer being monitored.
     *
     * After detaching, a log entry is generated to confirm the listener was removed.
     */
    @Override
    public void detachListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
            Log.d("Firestore", "Listener detached for event: " + id);
        }
    }

    /**
     * Invokes the `onUpdateListener` if it's set. This method is used to notify
     * any external components that a change has occurred in this event instance,
     * allowing for real-time updates to UI or other elements based on event changes.
     */
    @Override
    public void onUpdate() {
        if (onUpdateListener != null) {
            onUpdateListener.run();
        }
    }

    /**
     * Registers an `onUpdateListener` for this event instance. The listener will be
     * triggered whenever there is an update in the event's data, allowing the caller
     * to handle updates through a custom Runnable action.
     *
     * @param listener A Runnable that defines the actions to be executed on update.
     */
    @Override
    public void setOnUpdateListener(Runnable listener) {
        this.onUpdateListener = listener;
    }

}
