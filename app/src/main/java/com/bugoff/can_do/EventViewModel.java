package com.bugoff.can_do;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EventViewModel extends ViewModel {
    private final Event event;
    private final MutableLiveData<String> eventName = new MutableLiveData<>();
    private final MutableLiveData<String> facilityId = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public EventViewModel(String eventId) {
        // Initialize Event fetched from repository
        this.event = GlobalRepository.getEvent(eventId).getResult();
        this.eventName.setValue(event.getName());
        this.facilityId.setValue(event.getFacility().getId());

        // Set onUpdateListener to update LiveData
        this.event.setOnUpdateListener(this::updateLiveData);

        // Attach Firestore listener
        this.event.attachListener();
    }

    private void updateLiveData() {
        eventName.postValue(event.getName());
        facilityId.postValue(event.getFacility().getId());
    }

    public LiveData<String> getEventName() {
        return eventName;
    }

    public LiveData<String> getFacilityId() {
        return facilityId;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Methods to interact with Event
    public void setName(String name) {
        event.setName(name);
        event.setRemote();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        event.detachListener();
    }
}
