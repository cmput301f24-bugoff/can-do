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
 * <p>This ViewModel facilitates asynchronous fetching of Facility data from the repository
 * and provides LiveData objects to observe changes in facility properties, such as its ID,
 * owner, name, address, and events. It also supports updating Facility details and handles
 * lifecycle-aware data listeners to prevent memory leaks.</p>
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
    /**
     * Constructs a new {@code FacilityViewModel} for the specified facility ID.
     *
     * @param facilityId The ID of the facility to manage.
     * @param repository The repository to use for fetching Facility data.
     */
    FacilityViewModel(String facilityId, GlobalRepository repository) {
        this.repository = repository;
        fetchFacility(facilityId);
    }
    /**
     * Fetches the facility data asynchronously from the repository and initializes LiveData values.
     *
     * @param facilityId The ID of the facility to fetch.
     */
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
    /**
     * Updates LiveData objects with the latest facility data.
     */
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
    /**
     * Adds an event to the facility's list of events.
     *
     * @param event The event to add.
     */
    public void addEvent(Event event) {
        if (facility != null) {
            facility.addEvent(event);
        }
    }
    /**
     * Removes an event from the facility's list of events.
     *
     * @param event The event to remove.
     */
    public void removeEvent(Event event) {
        if (facility != null) {
            facility.removeEvent(event);
        }
    }
    /**
     * Cleans up resources when the ViewModel is cleared, such as detaching listeners.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (facility != null) {
            facility.detachListener();
        }
    }
    /**
     * Exposes the facility object for testing purposes.
     *
     * @return The {@link Facility} object managed by this ViewModel.
     */
    @VisibleForTesting
    public Facility getFacility() {
        return facility;
    }
}
