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

/**
 * ViewModel for managing and exposing Facility data to UI components.
 *
 * <p>This ViewModel handles the asynchronous fetching of a Facility from the repository
 * and provides LiveData objects for observing changes to the facility's properties,
 * such as its ID, owner, name, address, and events. It also allows updating the facility's
 * information and manages the lifecycle of data listeners to prevent memory leaks.</p>
 */
public class FacilityViewModel extends ViewModel {
    /** The Facility object being managed by this ViewModel. */
    private Facility facility;
    /** LiveData for the facility's unique identifier. */
    private final MutableLiveData<String> facilityId = new MutableLiveData<>();
    /** LiveData for the owner of the facility. */
    private final MutableLiveData<User> owner = new MutableLiveData<>();

    /** LiveData for the name of the facility. */
    private final MutableLiveData<String> name = new MutableLiveData<>();

    /** LiveData for the physical address of the facility. */
    private final MutableLiveData<String> address = new MutableLiveData<>();

    /** LiveData for the list of events held at the facility. */
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());

    /** LiveData for any error messages encountered during data fetching or updates. */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    /**
     * Constructs a new FacilityViewModel and initiates the fetching of Facility data.
     *
     * <p>Asynchronously retrieves the Facility from the repository using the provided facility ID.
     * If successful, sets up listeners for updates to the Facility data.</p>
     *
     * @param facilityId The unique identifier of the facility to fetch.
     */
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
    /**
     * Updates the LiveData properties based on the current state of the Facility.
     *
     * <p>This method is called whenever the Facility data changes to ensure the UI
     * components receive the latest information.</p>
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
    /**
     * Retrieves the LiveData object for the facility's unique identifier.
     *
     * @return A LiveData containing the facility's ID.
     */
    public LiveData<String> getFacilityId() {
        return facilityId;
    }
    /**
     * Retrieves the LiveData object for the facility's owner.
     *
     * @return A LiveData containing the User who owns the facility.
     */
    public LiveData<User> getOwner() {
        return owner;
    }
    /**
     * Retrieves the LiveData object for the facility's name.
     *
     * @return A LiveData containing the facility's name.
     */
    public LiveData<String> getName() {
        return name;
    }
    /**
     * Retrieves the LiveData object for the facility's address.
     *
     * @return A LiveData containing the facility's address.
     */
    public LiveData<String> getAddress() {
        return address;
    }
    /**
     * Retrieves the LiveData object for the list of events at the facility.
     *
     * @return A LiveData containing a list of events held at the facility.
     */
    public LiveData<List<Event>> getEvents() {
        return events;
    }
    /**
     * Retrieves the LiveData object for error messages.
     *
     * @return A LiveData containing error messages, if any.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    /**
     * Updates the name of the facility.
     *
     * <p>This method updates the facility's name locally and synchronizes the change
     * with the remote database.</p>
     *
     * @param newName The new name to set for the facility.
     */
    public void setName(String newName) {
        if (facility != null) {
            facility.setName(newName);
            facility.setRemote();
        }
    }
    /**
     * Updates the address of the facility.
     *
     * <p>This method updates the facility's address locally and synchronizes the change
     * with the remote database.</p>
     *
     * @param newAddress The new address to set for the facility.
     */
    public void setAddress(String newAddress) {
        if (facility != null) {
            facility.setAddress(newAddress);
            facility.setRemote();
        }
    }
    /**
     * Adds a new event to the facility's list of events.
     *
     * <p>This method updates the facility's events locally and ensures the change
     * is reflected in the remote database.</p>
     *
     * @param event The Event object to add to the facility.
     */
    public void addEvent(Event event) {
        if (facility != null) {
            facility.addEvent(event);
            // No need to call setRemote() here as it's handled inside addEvent()
        }
    }
    /**
     * Removes an event from the facility's list of events.
     *
     * <p>This method updates the facility's events locally and ensures the change
     * is reflected in the remote database.</p>
     *
     * @param event The Event object to remove from the facility.
     */
    public void removeEvent(Event event) {
        if (facility != null) {
            facility.removeEvent(event);
            // LiveData is updated within the Facility's onUpdateListener
        }
    }
    /**
     * Cleans up resources when the ViewModel is no longer in use.
     *
     * <p>Detaches any listeners attached to the Facility to prevent memory leaks.</p>
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (facility != null) {
            facility.detachListener();
        }
    }
}
