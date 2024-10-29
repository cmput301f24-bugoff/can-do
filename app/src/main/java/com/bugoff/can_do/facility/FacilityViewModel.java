package com.bugoff.can_do.facility;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;
import java.util.List;

public class FacilityViewModel extends ViewModel {
    private final Facility facility;
    private final MutableLiveData<String> facilityId = new MutableLiveData<>();
    private final MutableLiveData<User> owner = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public FacilityViewModel(String facilityId) {
        // Initialize Facility with owner fetched from repository
        User ownerUser = GlobalRepository.getUser(facilityId).getResult(); // facilityId is the Android ID of the owner
        this.facility = new Facility(ownerUser);
        this.facilityId.setValue(facility.getId());
        this.owner.setValue(facility.getOwner());
        this.events.setValue(facility.getEvents());

        // Set onUpdateListener to update LiveData
        this.facility.setOnUpdateListener(this::updateLiveData);

        // Attach Firestore listener
        this.facility.attachListener();
    }

    private void updateLiveData() {
        facilityId.postValue(facility.getId());
        owner.postValue(facility.getOwner());
        events.postValue(facility.getEvents());
    }

    public LiveData<String> getFacilityId() {
        return facilityId;
    }

    public LiveData<User> getOwner() {
        return owner;
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Methods to interact with Facility
    public void addEvent(Event event) {
        facility.addEvent(event);
    }

    public void removeEvent(Event event) {
        facility.getEvents().remove(event);
        facility.setRemote();
        events.postValue(facility.getEvents());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        facility.detachListener();
    }
}