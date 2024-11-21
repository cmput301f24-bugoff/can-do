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
                    updateLiveData();

                    // Setup listener for updates
                    this.user.setOnUpdateListener(this::updateLiveData);
                    this.user.attachListener();
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    Log.e("UserViewModel", "Error fetching user", e);
                });
    }

    private void updateLiveData() {
        if (user != null) {
            userName.postValue(user.getName());
            email.postValue(user.getEmail());
            phoneNumber.postValue(user.getPhoneNumber());
            isAdmin.postValue(user.getIsAdmin());
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
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // User interaction methods
    public void setName(String name) {
        if (user != null && name != null) {
            user.setName(name);
            // Update LiveData immediately for UI response
            userName.setValue(name);
            // Then update remote
            user.setRemote();
        }
    }

    public void setEmail(String email) {
        if (user != null && email != null) {
            user.setEmail(email);
            // Update LiveData immediately
            this.email.setValue(email);
            // Then update remote
            user.setRemote();
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        if (user != null && phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
            // Update LiveData immediately
            this.phoneNumber.setValue(phoneNumber);
            // Then update remote
            user.setRemote();
        }
    }

    public void setFacility(Facility newFacility) {
        user.setFacility(newFacility);
        user.setRemote();
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
