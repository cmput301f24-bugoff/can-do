package com.bugoff.can_do.event;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EventsListViewModel extends ViewModel {
    private static final String TAG = "EventsListViewModel";
    private final GlobalRepository repository;
    private final boolean isAdmin;
    private final MutableLiveData<List<Event>> eventsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private ListenerRegistration eventListener;

    /* package */ EventsListViewModel(GlobalRepository repository, boolean isAdmin) {
        this.repository = repository;
        this.isAdmin = isAdmin;
        User currentUser = GlobalRepository.getLoggedInUser();

        if (currentUser != null && currentUser.getFacility() != null) {
            fetchEvents(currentUser.getFacility());
        } else {
            errorMessage.setValue("No facility found for current user");
            statusMessage.setValue("Waiting for facility...");
        }
    }

    private void fetchEvents(Facility facility) {
        if (GlobalRepository.isInTestMode()) {
            // In test mode, we'll handle events through the mock repository
            updateEventsList(facility.getEvents());
            statusMessage.setValue("Events loaded from test repository");
            return;
        }

        statusMessage.setValue("Loading events...");

        // Set up real-time listener for events
        if (eventListener != null) {
            eventListener.remove();
        }

        eventListener = repository.getEventsCollection()
                .whereEqualTo("facilityId", facility.getId())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        errorMessage.setValue("Failed to load events");
                        statusMessage.setValue("Error occurred while loading");
                        return;
                    }

                    if (snapshots != null) {
                        List<Event> newEventsList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            repository.getFacility(doc.getString("facilityId"))
                                    .addOnSuccessListener(eventFacility -> {
                                        Event event = new Event(eventFacility, doc);
                                        newEventsList.add(event);
                                        updateEventsList(newEventsList);
                                        statusMessage.setValue("Events loaded successfully");
                                    })
                                    .addOnFailureListener(error -> {
                                        Log.e(TAG, "Error fetching facility for event", error);
                                        errorMessage.setValue("Error loading event details");
                                        statusMessage.setValue("Failed to load some events");
                                    });
                        }
                    }
                });
    }

    private void updateEventsList(List<Event> events) {
        eventsList.setValue(events);
    }

    public LiveData<List<Event>> getEventsList() {
        return eventsList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void deleteEvent(Event event) {
        if (event == null) return;

        statusMessage.setValue("Deleting event...");

        if (GlobalRepository.isInTestMode()) {
            // In test mode, just remove from the local list
            List<Event> currentEvents = eventsList.getValue();
            if (currentEvents != null) {
                currentEvents.remove(event);
                eventsList.setValue(currentEvents);
                statusMessage.setValue("Event deleted successfully");
            }
            return;
        }

        repository.getEventsCollection().document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    statusMessage.setValue("Event deleted successfully");
                    // Remove from facility's events list
                    Facility facility = event.getFacility();
                    if (facility != null) {
                        facility.removeEvent(event);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to delete event");
                    statusMessage.setValue("Error occurred while deleting");
                    Log.e(TAG, "Error deleting event", e);
                });
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (eventListener != null) {
            eventListener.remove();
        }
    }
}
