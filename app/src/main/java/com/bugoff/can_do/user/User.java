package com.bugoff.can_do.user;

import android.util.Log;

import androidx.annotation.NonNull;

import com.bugoff.can_do.database.DatabaseEntity;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.google.firebase.firestore.CollectionReference;
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

/**
 * Represents a user in the application. A user can be an organizer, admin, or a regular user.
 */
public class User implements DatabaseEntity {
    /** The unique ID of the user. */
    private String id;
    /** The name of the user. */
    private String name;
    /** The email address of the user. */
    private String email;
    /** The phone number of the user. */
    private String phoneNumber;
    /** Indicates whether the user is an admin. */
    private Boolean isAdmin;
    /** The facility associated with the user if the user is an organizer. */
    private Facility facility; // Associated facility if the user is an organizer
    /** The list of event IDs where the user joined the waiting list. */
    private List<String> eventsJoined; // Event IDs where the user joined the waiting list
    /** The list of event IDs where the user is enrolled. */
    private List<String> eventsEnrolled; // Event IDs where the user is enrolled
    /** The list of notifications for the user. */
    private List<Notification> notificationList; // List of Notification objects
    private boolean shouldUpdateRemote = true;
    private Double latitude;
    private Double longitude;
    private String base64Image;
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
        this.notificationList = new ArrayList<>();
        this.latitude = null;
        this.longitude = null;
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
        this.notificationList = new ArrayList<>();
    }

    public User(@NonNull DocumentSnapshot doc) {
        shouldUpdateRemote = false;
        this.id = doc.getId();
        this.name = doc.getString("name");
        this.email = doc.getString("email");
        this.phoneNumber = doc.getString("phoneNumber");
        this.isAdmin = doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : Boolean.FALSE;
        this.latitude = doc.getDouble("latitude");
        this.longitude = doc.getDouble("longitude");
        this.facility = new Facility(this); // placeholder

        // Initialize lists from the document data
        List<String> joinedList = (List<String>) doc.get("eventsJoined");
        this.eventsJoined = joinedList != null ? new ArrayList<>(joinedList) : new ArrayList<>();

        List<String> enrolledList = (List<String>) doc.get("eventsEnrolled");
        this.eventsEnrolled = enrolledList != null ? new ArrayList<>(enrolledList) : new ArrayList<>();

        this.notificationList = new ArrayList<>();
        this.base64Image = doc.getString("base64Image");
        shouldUpdateRemote = true;
    }

    // Add a method to set the Facility post-construction
    public void linkFacility(@NonNull Facility facility) {
        this.facility = facility;
        facility.setOwner(this); // Establish bidirectional reference
        onUpdate(); // Notify listeners about the update
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name != null ? name : "");
        map.put("email", email != null ? email : "");
        map.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
        map.put("isAdmin", isAdmin != null ? isAdmin : false);

        // Handle facility
        if (facility != null) {
            map.put("facilityId", facility.getId());
        } else {
            map.put("facilityId", null);
        }

        // Handle lists - never put null for these fields
        map.put("eventsJoined", eventsJoined != null ? new ArrayList<>(eventsJoined) : new ArrayList<>());
        map.put("eventsEnrolled", eventsEnrolled != null ? new ArrayList<>(eventsEnrolled) : new ArrayList<>());
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("base64Image", base64Image);
        return map;
    }

    // Getters and Setters

    public List<Notification> getNotificationList() {
        return Collections.unmodifiableList(notificationList);
    }

    // Existing Getters and Setters...

    public void addNotification(Notification notification) {
        this.notificationList.add(notification);
        setRemote();
    }

    public void removeNotification(Notification notification) {
        if (this.notificationList.remove(notification)) {
            setRemote();
        }
    }

    public String getName() {
        return name;
    }
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
        setRemote();
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
        setRemote();
    }
    public void setName(String name) {
        this.name = name;
        setRemote();
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

    public List<String> getEventsJoined() {
        return Collections.unmodifiableList(eventsJoined);
    }

    public void setEventsJoined(List<String> eventsJoined) {
        this.eventsJoined = eventsJoined != null ? new ArrayList<>(eventsJoined) : new ArrayList<>();
        setRemote();
    }

    public void addEventJoined(String eventId) {
        // Log.d("User", "Adding event to joined list: " + eventId);
        if (this.eventsJoined == null) {
            this.eventsJoined = new ArrayList<>();
        }
        if (!this.eventsJoined.contains(eventId)) {
            this.eventsJoined.add(eventId);
            // Log.d("User", "Updated eventsJoined list: " + this.eventsJoined);
            setRemote();
        } else {
            // Log.d("User", "Event already in joined list");
        }
    }

    public void removeEventJoined(String eventId) {
        if (this.eventsJoined.remove(eventId)) {
            setRemote();
        }
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
        setRemote();
    }


    public List<String> getEventsEnrolled() {
        return Collections.unmodifiableList(eventsEnrolled);
    }

    public void setEventsEnrolled(List<String> eventsEnrolled) {
        this.eventsEnrolled = eventsEnrolled != null ? new ArrayList<>(eventsEnrolled) : new ArrayList<>();
        setRemote();
    }

    public void addEventEnrolled(String eventId) {
        if (!this.eventsEnrolled.contains(eventId)) {
            this.eventsEnrolled.add(eventId);
            setRemote();
        }
    }

    public void removeEventEnrolled(String eventId) {
        if (this.eventsEnrolled.remove(eventId)) {
            setRemote();
        }
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
        setRemote();
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
        setRemote();
    }

    @Override
    public void setRemote() {
        if (!shouldUpdateRemote) {
            Log.d("User", "Skipping remote update due to flag");
            return;
        }

        // Check if we're in test mode
        if (GlobalRepository.isInTestMode()) {
            Log.d("User", "Skipping remote update due to test mode");
            return;
        }

        CollectionReference usersCollection = GlobalRepository.getUsersCollection();
        if (usersCollection == null) {
            Log.e("User", "Cannot update remote: usersCollection is null");
            return;
        }

        DocumentReference userRef = usersCollection.document(id);
        if (userRef == null) {
            Log.e("User", "Cannot update remote: userRef is null");
            return;
        }

        Map<String, Object> update = this.toMap();
        Log.d("User", "Setting remote - User ID: " + id);
        Log.d("User", "Setting remote - Events Joined (before update): " + eventsJoined);

        userRef.set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("User", "User " + id + " successfully updated");
                    Log.d("User", "Events Joined (after update): " + eventsJoined);
                })
                .addOnFailureListener(e -> {
                    Log.e("User", "Error updating user " + id, e);
                });
    }

    @Override
    public void attachListener() {
        // Don't attach listeners in test mode
        if (GlobalRepository.isInTestMode()) {
            Log.d("User", "Skipping listener attachment in test mode");
            return;
        }

        DocumentReference userRef = GlobalRepository.getUsersCollection().document(id);
        if (userRef == null) {
            Log.e("User", "Cannot attach listener: userRef is null");
            return;
        }

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

                // Deserialize eventsJoined as List<String>
                List<String> updatedEventsJoinedIds = (List<String>) documentSnapshot.get("eventsJoined");
                if (updatedEventsJoinedIds != null) {
                    if (!updatedEventsJoinedIds.equals(this.eventsJoined)) {
                        this.eventsJoined = new ArrayList<>(updatedEventsJoinedIds);
                        isChanged.set(true);
                    }
                }

                // Deserialize eventsEnrolled as List<String>
                List<String> updatedEventsEnrolledIds = (List<String>) documentSnapshot.get("eventsEnrolled");
                if (updatedEventsEnrolledIds != null) {
                    if (!updatedEventsEnrolledIds.equals(this.eventsEnrolled)) {
                        this.eventsEnrolled = new ArrayList<>(updatedEventsEnrolledIds);
                        isChanged.set(true);
                    }
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
