package com.bugoff.can_do.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.database.GlobalRepository;

import java.util.List;

public class UserViewModel extends ViewModel {
    private User user;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdmin = new MutableLiveData<>();
    private final MutableLiveData<Facility> facility = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> eventsJoined = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> eventsEnrolled = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel(String userId) {
        // Fetch user asynchronously
        GlobalRepository.getUser(userId)
                .addOnSuccessListener(fetchedUser -> {
                    this.user = fetchedUser;
                    userName.setValue(user.getName());
                    isAdmin.setValue(user.getIsAdmin());
                    facility.setValue(user.getFacility());
                    // Set listeners
                    this.user.setOnUpdateListener(this::updateLiveData);
                    this.user.attachListener();
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                });
    }

    private void updateLiveData() {
        userName.postValue(user.getName());
        email.postValue(user.getEmail());
        phoneNumber.postValue(user.getPhoneNumber());
        isAdmin.postValue(user.getIsAdmin());
        facility.postValue(user.getFacility());
        eventsJoined.postValue(user.getEventsJoined());
        eventsEnrolled.postValue(user.getEventsEnrolled());
    }

    // Getters for LiveData
    public LiveData<String> getUserName() { return userName; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getPhoneNumber() { return phoneNumber; }
    public LiveData<Boolean> getIsAdmin() { return isAdmin; }
    public LiveData<Facility> getFacility() { return facility; }
    public LiveData<List<Event>> getEventsJoined() { return eventsJoined; }
    public LiveData<List<Event>> getEventsEnrolled() { return eventsEnrolled; }
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

    @Override
    protected void onCleared() {
        super.onCleared();
        user.detachListener();
    }
}
