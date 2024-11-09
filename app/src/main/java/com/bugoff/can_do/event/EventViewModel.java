package com.bugoff.can_do.event;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.EntrantStatus;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing and exposing an Event's data to the UI layer.
 * This ViewModel asynchronously fetches the Event data from Firestore
 * and provides it through LiveData for real-time UI updates.
 *
 * The EventViewModel also handles automatic updates when the Event data
 * changes in Firestore by attaching snapshot listeners, and it fetches
 * associated Facility and User details for further UI integration.
 */
public class EventViewModel extends ViewModel {
    private static final String TAG = "EventViewModel";
    private Event event;
    private final MutableLiveData<String> eventName = new MutableLiveData<>();
    private final MutableLiveData<Facility> facility = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();
    private final MutableLiveData<String> qrCodeHash = new MutableLiveData<>();
    private final MutableLiveData<Date> registrationStartDate = new MutableLiveData<>();
    private final MutableLiveData<Date> registrationEndDate = new MutableLiveData<>();
    private final MutableLiveData<Date> eventStartDate = new MutableLiveData<>();
    private final MutableLiveData<Date> eventEndDate = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxNumberOfParticipants = new MutableLiveData<>();
    private final MutableLiveData<Boolean> geolocationRequired = new MutableLiveData<>();
    private final MutableLiveData<List<String>> waitingListEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, Location>> entrantsLocations = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, EntrantStatus>> entrantStatuses = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<String>> selectedEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> enrolledEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData for fetching User details
    private final MutableLiveData<Map<String, User>> waitingListUsers = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, User>> selectedEntrantsUsers = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, User>> enrolledEntrantsUsers = new MutableLiveData<>(new HashMap<>());
    /**
     * Constructor that initializes the EventViewModel with the specified event ID.
     * It asynchronously fetches the event data from Firestore, including the associated
     * facility and participant details, and sets up LiveData for UI observation.
     *
     * @param eventId The ID of the event to fetch and observe.
     */
    public EventViewModel(String eventId) {
        // Fetch Event asynchronously
        GlobalRepository.getEventsCollection().document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String facilityId = doc.getString("facilityId");
                        GlobalRepository.getFacility(facilityId).addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Facility eventFacility = task.getResult();
                                if (eventFacility != null) {
                                    event = new Event(eventFacility, doc);
                                    // Initialize LiveData
                                    eventName.setValue(event.getName());
                                    description.setValue(event.getDescription());
                                    qrCodeHash.setValue(event.getQrCodeHash());
                                    registrationStartDate.setValue(event.getRegistrationStartDate());
                                    registrationEndDate.setValue(event.getRegistrationEndDate());
                                    eventStartDate.setValue(event.getEventStartDate());
                                    eventEndDate.setValue(event.getEventEndDate());
                                    maxNumberOfParticipants.setValue(event.getMaxNumberOfParticipants());
                                    geolocationRequired.setValue(event.getGeolocationRequired());
                                    waitingListEntrants.setValue(event.getWaitingListEntrants());
                                    entrantsLocations.setValue(event.getEntrantsLocations());
                                    entrantStatuses.setValue(event.getEntrantStatuses());
                                    selectedEntrants.setValue(event.getSelectedEntrants());
                                    enrolledEntrants.setValue(event.getEnrolledEntrants());

                                    facility.setValue(event.getFacility());

                                    // Set listeners
                                    event.setOnUpdateListener(this::updateLiveData);
                                    event.attachListener();

                                    // Fetch User details based on user IDs
                                    fetchUsersForList(waitingListEntrants.getValue(), waitingListUsers);
                                    fetchUsersForList(selectedEntrants.getValue(), selectedEntrantsUsers);
                                    fetchUsersForList(enrolledEntrants.getValue(), enrolledEntrantsUsers);
                                } else {
                                    errorMessage.setValue("Facility not found for the event.");
                                }
                            } else {
                                errorMessage.setValue("Facility not found for the event.");
                            }
                        });
                    } else {
                        errorMessage.setValue("Event does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    Log.e("EventViewModel", "Error fetching event", e);
                });
    }
    /**
     * Updates the LiveData objects with the current values of the event fields.
     * This method is triggered when the event data is updated in Firestore, ensuring
     * that the UI layer remains synchronized with the latest event data.
     */
    private void updateLiveData() {
        if (event != null) {
            eventName.postValue(event.getName());
            description.postValue(event.getDescription());
            qrCodeHash.postValue(event.getQrCodeHash());
            registrationStartDate.postValue(event.getRegistrationStartDate());
            registrationEndDate.postValue(event.getRegistrationEndDate());
            eventStartDate.postValue(event.getEventStartDate());
            eventEndDate.postValue(event.getEventEndDate());
            maxNumberOfParticipants.postValue(event.getMaxNumberOfParticipants());
            geolocationRequired.postValue(event.getGeolocationRequired());
            waitingListEntrants.postValue(event.getWaitingListEntrants());
            entrantsLocations.postValue(event.getEntrantsLocations());
            entrantStatuses.postValue(event.getEntrantStatuses());
            selectedEntrants.postValue(event.getSelectedEntrants());
            enrolledEntrants.postValue(event.getEnrolledEntrants());

            // Update User LiveData
            fetchUsersForList(event.getWaitingListEntrants(), waitingListUsers);
            fetchUsersForList(event.getSelectedEntrants(), selectedEntrantsUsers);
            fetchUsersForList(event.getEnrolledEntrants(), enrolledEntrantsUsers);
        }
    }
    /**
     * Fetches user details for a given list of user IDs in batches and updates the target LiveData map.
     * Each batch is fetched asynchronously to avoid limitations on query size.
     *
     * @param userIds       A list of user IDs to fetch.
     * @param targetLiveData The target LiveData map to store the fetched User objects, mapped by their ID.
     */
    private void fetchUsersForList(List<String> userIds, MutableLiveData<Map<String, User>> targetLiveData) {
        if (userIds == null || userIds.isEmpty()) {
            Log.d(TAG, "fetchUsersForList: userIds is null or empty");
            targetLiveData.setValue(new HashMap<>());
            return;
        }

        Log.d(TAG, "fetchUsersForList: Fetching users for IDs: " + userIds);

        Map<String, User> usersMap = new HashMap<>();
        int batchSize = 10;
        List<List<String>> batches = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, userIds.size());
            batches.add(new ArrayList<>(userIds.subList(i, end)));
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (List<String> batch : batches) {
            tasks.add(GlobalRepository.getUsersCollection()
                    .whereIn(FieldPath.documentId(), batch)
                    .get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    for (Object result : results) {
                        if (result instanceof QuerySnapshot) {
                            QuerySnapshot snapshot = (QuerySnapshot) result;
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                User user = new User(document);
                                usersMap.put(user.getId(), user);
                                Log.d(TAG, "fetchUsersForList: Fetched user: " + user.getName());
                            }
                        }
                    }
                    targetLiveData.postValue(usersMap);
                    Log.d(TAG, "fetchUsersForList: Updated targetLiveData with usersMap size: " + usersMap.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchUsersForList: Error fetching user batches", e);
                });
    }

    // Getters for LiveData
    public LiveData<String> getEventName() { return eventName; }
    public LiveData<Facility> getFacility() { return facility; }
    public LiveData<String> getDescription() { return description; }
    public LiveData<String> getQrCodeHash() { return qrCodeHash; }
    public LiveData<Date> getRegistrationStartDate() { return registrationStartDate; }
    public LiveData<Date> getRegistrationEndDate() { return registrationEndDate; }
    public LiveData<Date> getEventStartDate() { return eventStartDate; }
    public LiveData<Date> getEventEndDate() { return eventEndDate; }
    public LiveData<Integer> getMaxNumberOfParticipants() { return maxNumberOfParticipants; }
    public LiveData<Boolean> getGeolocationRequired() { return geolocationRequired; }
    public LiveData<List<String>> getWaitingListEntrants() { return waitingListEntrants; }
    public LiveData<Map<String, Location>> getEntrantsLocations() { return entrantsLocations; }
    public LiveData<Map<String, EntrantStatus>> getEntrantStatuses() { return entrantStatuses; }
    public LiveData<List<String>> getSelectedEntrants() { return selectedEntrants; }
    public LiveData<List<String>> getEnrolledEntrants() { return enrolledEntrants; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // LiveData getters for User details
    public LiveData<Map<String, User>> getWaitingListUsers() { return waitingListUsers; }
    public LiveData<Map<String, User>> getSelectedEntrantsUsers() { return selectedEntrantsUsers; }
    public LiveData<Map<String, User>> getEnrolledEntrantsUsers() { return enrolledEntrantsUsers; }

    /**
     * Adds a user to the waiting list and fetches the updated list of users.
     *
     * @param userId The ID of the user to add to the waiting list.
     */
    public void addWaitingListEntrant(String userId) {
        event.addWaitingListEntrant(userId);
        fetchUsersForList(waitingListEntrants.getValue(), waitingListUsers);
    }
    /**
     * Removes a user from the waiting list and updates the LiveData.
     *
     * @param userId The ID of the user to remove from the waiting list.
     */
    public void removeWaitingListEntrant(String userId) {
        event.removeWaitingListEntrant(userId);
        // Optionally, remove from waitingListUsers LiveData
        Map<String, User> currentMap = waitingListUsers.getValue();
        if (currentMap != null) {
            currentMap.remove(userId);
            waitingListUsers.postValue(currentMap);
        }
    }
    /**
     * Updates the status of an entrant in the event.
     *
     * @param userId The ID of the entrant to update.
     * @param status The new status to assign to the entrant.
     */
    public void updateEntrantStatus(String userId, EntrantStatus status) {
        event.updateEntrantStatus(userId, status);
        // Update entrantStatuses LiveData if needed
    }
    /**
     * Adds a user to the selected entrants list and fetches the updated list of users.
     *
     * @param userId The ID of the user to add to the selected entrants list.
     */
    public void addSelectedEntrant(String userId) {
        event.addSelectedEntrant(userId);
        fetchUsersForList(selectedEntrants.getValue(), selectedEntrantsUsers);
    }
    /**
     * Removes a user from the selected entrants list and updates the LiveData.
     *
     * @param userId The ID of the user to remove from the selected entrants list.
     */
    public void removeSelectedEntrant(String userId) {
        event.removeSelectedEntrant(userId);
        // Optionally, remove from selectedEntrantsUsers LiveData
        Map<String, User> currentMap = selectedEntrantsUsers.getValue();
        if (currentMap != null) {
            currentMap.remove(userId);
            selectedEntrantsUsers.postValue(currentMap);
        }
    }
    /**
     * Enrolls a user as an entrant and fetches the updated list of enrolled users.
     *
     * @param userId The ID of the user to enroll.
     */
    public void enrollEntrant(String userId) {
        event.enrollEntrant(userId);
        fetchUsersForList(enrolledEntrants.getValue(), enrolledEntrantsUsers);
    }
    /**
     * Called when the ViewModel is cleared, typically used to clean up resources or listeners.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (event != null) {
            event.detachListener();
        }
    }
}