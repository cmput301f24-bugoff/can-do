package com.bugoff.can_do.user;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for a single user profile. Fetches user data from Firestore and exposes it to the UI.
 */
public class UserViewModel extends ViewModel {
    private User user;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdmin = new MutableLiveData<>();
    private final MutableLiveData<Facility> facility = new MutableLiveData<>();
    private final MutableLiveData<List<String>> eventsJoined = new MutableLiveData<>();
    private final MutableLiveData<List<String>> eventsEnrolled = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> eventsJoinedDetails = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> eventsEnrolledDetails = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel(String userId) {
        // Fetch user asynchronously
        GlobalRepository.getUser(userId)
                .addOnSuccessListener(fetchedUser -> {
                    this.user = fetchedUser;
                    userName.setValue(user.getName());
                    email.setValue(user.getEmail());
                    phoneNumber.setValue(user.getPhoneNumber());
                    isAdmin.setValue(user.getIsAdmin());
                    facility.setValue(user.getFacility());
                    eventsJoined.setValue(user.getEventsJoined());
                    eventsEnrolled.setValue(user.getEventsEnrolled());

                    // Fetch Event details based on event IDs
                    fetchEventsDetails(user.getEventsJoined(), eventsJoinedDetails);
                    fetchEventsDetails(user.getEventsEnrolled(), eventsEnrolledDetails);

                    // Set listeners
                    this.user.setOnUpdateListener(this::updateLiveData);
                    this.user.attachListener();
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                });
    }

    /**
     * Updates the LiveData with the latest user data.
     */
    private void updateLiveData() {
        if (user != null) {
            userName.postValue(user.getName());
            email.postValue(user.getEmail());
            phoneNumber.postValue(user.getPhoneNumber());
            isAdmin.postValue(user.getIsAdmin());
            facility.postValue(user.getFacility());

            // Update event IDs
            eventsJoined.postValue(user.getEventsJoined());
            eventsEnrolled.postValue(user.getEventsEnrolled());

            // Refresh Event details
            fetchEventsDetails(user.getEventsJoined(), eventsJoinedDetails);
            fetchEventsDetails(user.getEventsEnrolled(), eventsEnrolledDetails);
        }
    }

    /**
     * Fetches Event objects based on a list of event IDs and updates the corresponding LiveData.
     *
     * @param eventIds The list of event IDs to fetch.
     * @param targetLiveData The LiveData to update with fetched Event objects.
     */
    private void fetchEventsDetails(List<String> eventIds, MutableLiveData<List<Event>> targetLiveData) {
        if (eventIds == null || eventIds.isEmpty()) {
            targetLiveData.setValue(new ArrayList<>());
            return;
        }

        List<Event> fetchedEvents = new ArrayList<>();
        int total = eventIds.size();
        final int[] counter = {0};

        for (String eventId : eventIds) {
            GlobalRepository.getEvent(eventId)
                    .addOnSuccessListener(event -> {
                        if (event != null) {
                            fetchedEvents.add(event);
                        }
                        if (++counter[0] == total) {
                            targetLiveData.postValue(new ArrayList<>(fetchedEvents));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserViewModel", "Error fetching event with ID: " + eventId, e);
                        if (++counter[0] == total) {
                            targetLiveData.postValue(new ArrayList<>(fetchedEvents));
                        }
                    });
        }
    }

    // Getters for LiveData
    public LiveData<String> getUserName() { return userName; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getPhoneNumber() { return phoneNumber; }
    public LiveData<Boolean> getIsAdmin() { return isAdmin; }
    public LiveData<Facility> getFacility() { return facility; }
    public LiveData<List<String>> getEventsJoined() { return eventsJoined; }
    public LiveData<List<String>> getEventsEnrolled() { return eventsEnrolled; }
    public LiveData<List<Event>> getEventsJoinedDetails() { return eventsJoinedDetails; }
    public LiveData<List<Event>> getEventsEnrolledDetails() { return eventsEnrolledDetails; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Methods to interact with User
    public void setName(String name) {
        user.setName(name);
        user.setRemote();
    }

    public void setEmail(String email) {
        user.setEmail(email);
        user.setRemote();
    }

    public void setPhoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
        user.setRemote();
    }

    public void setIsAdmin(Boolean adminStatus) {
        user.setIsAdmin(adminStatus);
        user.setRemote();
    }

    public void setFacility(Facility newFacility) {
        user.setFacility(newFacility);
        user.setRemote();
    }

    public void addEventJoined(String eventId) {
        user.addEventJoined(eventId);
        // Fetch and update Event details
        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        List<Event> currentEvents = eventsJoinedDetails.getValue();
                        if (currentEvents != null) {
                            currentEvents.add(event);
                            eventsJoinedDetails.postValue(currentEvents);
                        }
                    }
                });
    }

    public void removeEventJoined(String eventId) {
        user.removeEventJoined(eventId);
        // Remove from Event details
        List<Event> currentEvents = eventsJoinedDetails.getValue();
        if (currentEvents != null) {
            currentEvents.removeIf(event -> event.getId().equals(eventId));
            eventsJoinedDetails.postValue(currentEvents);
        }
    }

    public void addEventEnrolled(String eventId) {
        user.addEventEnrolled(eventId);
        // Fetch and update Event details
        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        List<Event> currentEvents = eventsEnrolledDetails.getValue();
                        if (currentEvents != null) {
                            currentEvents.add(event);
                            eventsEnrolledDetails.postValue(currentEvents);
                        }
                    }
                });
    }

    public void removeEventEnrolled(String eventId) {
        user.removeEventEnrolled(eventId);
        // Remove from Event details
        List<Event> currentEvents = eventsEnrolledDetails.getValue();
        if (currentEvents != null) {
            currentEvents.removeIf(event -> event.getId().equals(eventId));
            eventsEnrolledDetails.postValue(currentEvents);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (user != null) {
            user.detachListener();
        }
    }
}
