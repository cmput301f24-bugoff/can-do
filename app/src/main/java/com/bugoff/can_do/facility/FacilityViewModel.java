package com.bugoff.can_do.facility;

import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing and exposing Facility data to UI components.
 *
 * <p>This ViewModel handles the asynchronous fetching of a Facility from the repository
 * and provides LiveData objects for observing changes to the facility's properties,
 * such as its ID, owner, name, address, and events. It also allows updating the facility's
 * information and manages the lifecycle of data listeners to prevent memory leaks.</p>
 */
public class FacilityViewModel extends ViewModel {
    private final GlobalRepository repository;
    private Facility facility;
    private final MutableLiveData<String> facilityId = new MutableLiveData<>();
    private final MutableLiveData<User> owner = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> address = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    FacilityViewModel(String facilityId, GlobalRepository repository) {
        this.repository = repository;
        fetchFacility(facilityId);
    }

    private void fetchFacility(String facilityId) {
        repository.getFacility(facilityId)
                .addOnSuccessListener(fetchedFacility -> {
                    this.facility = fetchedFacility;
                    updateLiveData();

                    // Setup listener for updates
                    this.facility.setOnUpdateListener(this::updateLiveData);
                    this.facility.attachListener();
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

    // Getters for LiveData
    public LiveData<String> getFacilityId() { return facilityId; }
    public LiveData<User> getOwner() { return owner; }
    public LiveData<String> getName() { return name; }
    public LiveData<String> getAddress() { return address; }
    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Facility interaction methods
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
        }
    }

    public void removeEvent(Event event) {
        if (facility != null) {
            facility.removeEvent(event);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (facility != null) {
            facility.detachListener();
        }
    }

    @VisibleForTesting
    public Facility getFacility() {
        return facility;
    }
}
