package com.bugoff.can_do.event;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.EntrantStatus;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventViewModel extends ViewModel {
    private Event event;
    private final MutableLiveData<String> eventName = new MutableLiveData<>();
    private final MutableLiveData<Facility> facility = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();
    private final MutableLiveData<String> qrCodeHash = new MutableLiveData<>();
    private final MutableLiveData<Date> registrationStartDate = new MutableLiveData<>();
    private final MutableLiveData<Date> registrationEndDate = new MutableLiveData<>();
    private final MutableLiveData<Date> eventStartDate = new MutableLiveData<>();
    private final MutableLiveData<Date> eventEndDate = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxNumberOfParticipants = new MutableLiveData<>();
    private final MutableLiveData<Boolean> geolocationRequired = new MutableLiveData<>();
    private final MutableLiveData<List<User>> waitingListEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, Location>> entrantsLocations = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, EntrantStatus>> entrantStatuses = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<User>> selectedEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<User>> enrolledEntrants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public EventViewModel(String eventId) {
        // Fetch Event asynchronously
        GlobalRepository.getEventsCollection().document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        event = new Event(GlobalRepository.getFacility(doc.getString("facilityId")).getResult(), doc);
                        // Initialize LiveData
                        eventName.setValue(event.getName());
                        description.setValue(event.getDescription());
                        qrCodeHash.setValue(event.getQrCodeHash());
                        registrationStartDate.setValue(event.getRegistrationStartDate());
                        registrationEndDate.setValue(event.getRegistrationEndDate());
                        eventStartDate.setValue(event.getEventStartDate());
                        eventEndDate.setValue(event.getEventEndDate());
                        maxNumberOfParticipants.setValue(event.getMaxNumberOfParticipants());
                        geolocationRequired.setValue(event.getGeolocationRequired());
                        waitingListEntrants.setValue(event.getWaitingListEntrants());
                        entrantsLocations.setValue(serializeEntrantsLocations(event.getEntrantsLocations()));
                        entrantStatuses.setValue(serializeEntrantStatuses(event.getEntrantStatuses()));
                        selectedEntrants.setValue(event.getSelectedEntrants());
                        enrolledEntrants.setValue(event.getEnrolledEntrants());

                        // Set listeners
                        event.setOnUpdateListener(this::updateLiveData);
                        event.attachListener();
                    } else {
                        errorMessage.setValue("Event does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    Log.e("EventViewModel", "Error fetching event", e);
                });
    }

    private void updateLiveData() {
        if (event != null) {
            eventName.postValue(event.getName());
            description.postValue(event.getDescription());
            qrCodeHash.postValue(event.getQrCodeHash());
            registrationStartDate.postValue(event.getRegistrationStartDate());
            registrationEndDate.postValue(event.getRegistrationEndDate());
            eventStartDate.postValue(event.getEventStartDate());
            eventEndDate.postValue(event.getEventEndDate());
            maxNumberOfParticipants.postValue(event.getMaxNumberOfParticipants());
            geolocationRequired.postValue(event.getGeolocationRequired());
            waitingListEntrants.postValue(event.getWaitingListEntrants());
            entrantsLocations.postValue(serializeEntrantsLocations(event.getEntrantsLocations()));
            entrantStatuses.postValue(serializeEntrantStatuses(event.getEntrantStatuses()));
            selectedEntrants.postValue(event.getSelectedEntrants());
            enrolledEntrants.postValue(event.getEnrolledEntrants());
        }
    }

    private Map<String, Location> serializeEntrantsLocations(Map<User, Location> entrantsLoc) {
        Map<String, Location> serializedMap = new HashMap<>();
        for (Map.Entry<User, Location> entry : entrantsLoc.entrySet()) {
            serializedMap.put(entry.getKey().getId(), entry.getValue());
        }
        return serializedMap;
    }

    private Map<String, EntrantStatus> serializeEntrantStatuses(Map<User, EntrantStatus> entrantStatusMap) {
        Map<String, EntrantStatus> serializedMap = new HashMap<>();
        for (Map.Entry<User, EntrantStatus> entry : entrantStatusMap.entrySet()) {
            serializedMap.put(entry.getKey().getId(), entry.getValue());
        }
        return serializedMap;
    }

    public LiveData<String> getEventName() { return eventName; }
    public LiveData<Facility> getFacility() { return facility; }
    public LiveData<String> getDescription() { return description; }
    public LiveData<String> getQrCodeHash() { return qrCodeHash; }
    public LiveData<Date> getRegistrationStartDate() { return registrationStartDate; }
    public LiveData<Date> getRegistrationEndDate() { return registrationEndDate; }
    public LiveData<Date> getEventStartDate() { return eventStartDate; }
    public LiveData<Date> getEventEndDate() { return eventEndDate; }
    public LiveData<Integer> getMaxNumberOfParticipants() { return maxNumberOfParticipants; }
    public LiveData<Boolean> getGeolocationRequired() { return geolocationRequired; }
    public LiveData<List<User>> getWaitingListEntrants() { return waitingListEntrants; }
    public LiveData<Map<String, Location>> getEntrantsLocations() { return entrantsLocations; }
    public LiveData<Map<String, EntrantStatus>> getEntrantStatuses() { return entrantStatuses; }
    public LiveData<List<User>> getSelectedEntrants() { return selectedEntrants; }
    public LiveData<List<User>> getEnrolledEntrants() { return enrolledEntrants; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setName(String name) {
        event.setName(name);
        event.setRemote();
    }

    public void setDescription(String description) {
        if (event != null) {
            event.setDescription(description);
            event.setRemote();
        }
    }

    public void setQrCodeHash(String qrCodeHash) {
        if (event != null) {
            event.setQrCodeHash(qrCodeHash);
            event.setRemote();
        }
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        if (event != null) {
            event.setRegistrationStartDate(registrationStartDate);
            event.setRemote();
        }
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        if (event != null) {
            event.setRegistrationEndDate(registrationEndDate);
            event.setRemote();
        }
    }

    public void setEventStartDate(Date eventStartDate) {
        if (event != null) {
            event.setEventStartDate(eventStartDate);
            event.setRemote();
        }
    }

    public void setEventEndDate(Date eventEndDate) {
        if (event != null) {
            event.setEventEndDate(eventEndDate);
            event.setRemote();
        }
    }

    public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
        if (event != null) {
            event.setMaxNumberOfParticipants(maxNumberOfParticipants);
            event.setRemote();
        }
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        if (event != null) {
            event.setGeolocationRequired(geolocationRequired);
            event.setRemote();
        }
    }

    public void addWaitingListEntrant(User user) {
        if (event != null) {
            event.getWaitingListEntrants().add(user);
            event.setRemote();
        }
    }

    public void removeWaitingListEntrant(User user) {
        if (event != null) {
            event.getWaitingListEntrants().remove(user);
            event.setRemote();
        }
    }

    public void updateEntrantStatus(User user, EntrantStatus status) {
        if (event != null) {
            event.getEntrantStatuses().put(user, status);
            event.setRemote();
        }
    }

    public void addSelectedEntrant(User user) {
        if (event != null) {
            event.getSelectedEntrants().add(user);
            event.setRemote();
        }
    }

    public void removeSelectedEntrant(User user) {
        if (event != null) {
            event.getSelectedEntrants().remove(user);
            event.setRemote();
        }
    }

    public void enrollEntrant(User user) {
        if (event != null) {
            event.getEnrolledEntrants().add(user);
            event.setRemote();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (event != null) {
            event.detachListener();
        }
    }
}
