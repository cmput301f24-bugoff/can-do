package com.bugoff.can_do.user;

import android.util.Log;

import androidx.annotation.VisibleForTesting;
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
    private final GlobalRepository repository;
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

    /**
     * Constructor used by ViewModelFactory. Package-private to prevent direct instantiation.
     */
    /* package */ UserViewModel(String userId, GlobalRepository repository) {
        this.repository = repository;
        fetchUser(userId);
    }

    private void fetchUser(String userId) {
        repository.getUser(userId)
                .addOnSuccessListener(fetchedUser -> {
                    this.user = fetchedUser;
                    userName.setValue(user.getName());
                    email.setValue(user.getEmail());
                    phoneNumber.setValue(user.getPhoneNumber());
                    isAdmin.setValue(user.getIsAdmin());
                    facility.setValue(user.getFacility());
                    eventsJoined.setValue(user.getEventsJoined());
                    eventsEnrolled.setValue(user.getEventsEnrolled());

                    fetchEventsDetails(user.getEventsJoined(), eventsJoinedDetails);
                    fetchEventsDetails(user.getEventsEnrolled(), eventsEnrolledDetails);

                    this.user.setOnUpdateListener(this::updateLiveData);
                    this.user.attachListener();
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                });
    }

    private void updateLiveData() {
        if (user != null) {
            userName.postValue(user.getName());
            email.postValue(user.getEmail());
            phoneNumber.postValue(user.getPhoneNumber());
            isAdmin.postValue(user.getIsAdmin());
            facility.postValue(user.getFacility());
            eventsJoined.postValue(user.getEventsJoined());
            eventsEnrolled.postValue(user.getEventsEnrolled());
            fetchEventsDetails(user.getEventsJoined(), eventsJoinedDetails);
            fetchEventsDetails(user.getEventsEnrolled(), eventsEnrolledDetails);
        }
    }

    private void fetchEventsDetails(List<String> eventIds, MutableLiveData<List<Event>> targetLiveData) {
        if (eventIds == null || eventIds.isEmpty()) {
            targetLiveData.setValue(new ArrayList<>());
            return;
        }

        List<Event> fetchedEvents = new ArrayList<>();
        int total = eventIds.size();
        final int[] counter = {0};

        for (String eventId : eventIds) {
            repository.getEvent(eventId)
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

    // User interaction methods
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
        repository.getEvent(eventId)
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
        List<Event> currentEvents = eventsJoinedDetails.getValue();
        if (currentEvents != null) {
            currentEvents.removeIf(event -> event.getId().equals(eventId));
            eventsJoinedDetails.postValue(currentEvents);
        }
    }

    public void addEventEnrolled(String eventId) {
        user.addEventEnrolled(eventId);
        repository.getEvent(eventId)
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

    @VisibleForTesting
    public User getUser() {
        return user;
    }
}
