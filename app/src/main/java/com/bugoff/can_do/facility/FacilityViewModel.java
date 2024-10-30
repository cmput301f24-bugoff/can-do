package com.bugoff.can_do.facility;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;
import java.util.List;

public class FacilityViewModel extends ViewModel {
    private Facility facility;
    private final MutableLiveData<String> facilityId = new MutableLiveData<>();
    private final MutableLiveData<User> owner = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> address = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public FacilityViewModel(String facilityId) {
        // Fetch Facility asynchronously
        GlobalRepository.getFacilitiesCollection().document(facilityId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        facility = new Facility(doc);
                        // Set LiveData values based on the fetched facility
                        this.facilityId.setValue(facility.getId());
                        // Owner and events are fetched asynchronously within Facility
                        // Observe changes
                        facility.setOnUpdateListener(this::updateLiveData);
                        facility.attachListener();
                    } else {
                        errorMessage.setValue("Facility does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    Log.e("FacilityViewModel", "Error fetching facility", e);
                });
    }

    private void updateLiveData() {
        if (facility != null) {
            facilityId.postValue(facility.getId());
            owner.postValue(facility.getOwner());
            name.postValue(facility.getName());
            address.postValue(facility.getAddress());
            events.postValue(facility.getEvents());
        }
    }

    public LiveData<String> getFacilityId() {
        return facilityId;
    }

    public LiveData<User> getOwner() {
        return owner;
    }

    public LiveData<String> getName() {
        return name;
    }

    public LiveData<String> getAddress() {
        return address;
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setName(String newName) {
        if (facility != null) {
            facility.setName(newName);
            facility.setRemote();
        }
    }

    public void setAddress(String newAddress) {
        if (facility != null) {
            facility.setAddress(newAddress);
            facility.setRemote();
        }
    }

    public void addEvent(Event event) {
        if (facility != null) {
            facility.addEvent(event);
            // No need to call setRemote() here as it's handled inside addEvent()
        }
    }

    public void removeEvent(Event event) {
        if (facility != null) {
            facility.removeEvent(event);
            // LiveData is updated within the Facility's onUpdateListener
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (facility != null) {
            facility.detachListener();
        }
    }
}
