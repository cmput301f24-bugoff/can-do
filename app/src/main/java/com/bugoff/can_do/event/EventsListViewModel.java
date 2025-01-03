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
/**
 * ViewModel for the EventsListFragment.
 * Manages the list of events to be displayed in the fragment.
 */
public class EventsListViewModel extends ViewModel {
    private static final String TAG = "EventsListViewModel";
    private final GlobalRepository repository;
    private final boolean isAdmin;
    private final boolean isFromAdmin;
    private final MutableLiveData<List<Event>> eventsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private ListenerRegistration eventListener;
    /**
     * Creates a new instance of EventsListViewModel.
     *
     * @param repository The repository to fetch data from.
     * @param isAdmin    Boolean flag indicating if the user is an admin.
     * @param isFromAdmin Boolean flag indicating if the user is navigating from the AdminActivity.
     */
    EventsListViewModel(GlobalRepository repository, boolean isAdmin, boolean isFromAdmin) {
        this.repository = repository;
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
        User currentUser = GlobalRepository.getLoggedInUser();

        if (isFromAdmin && isAdmin) {
            // If we're coming from AdminActivity and user is admin, fetch all events
            fetchAllEvents();
        } else if (currentUser != null && currentUser.getFacility() != null) {
            // Otherwise, fetch only facility-specific events
            fetchEvents(currentUser.getFacility());
        } else {
            errorMessage.setValue("No facility found for current user");
            statusMessage.setValue("Waiting for facility...");
        }
    }
    /**
     * Fetches all events from the repository and updates the eventsList LiveData.
     */
    private void fetchAllEvents() {
        if (GlobalRepository.isInTestMode()) {
            // Handle test mode
            statusMessage.setValue("Events loaded from test repository");
            return;
        }

        statusMessage.setValue("Loading all events...");

        if (eventListener != null) {
            eventListener.remove();
        }

        eventListener = repository.getEventsCollection()
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
                            String facilityId = doc.getString("facilityId");
                            repository.getFacility(facilityId)
                                    .addOnSuccessListener(facility -> {
                                        Event event = new Event(facility, doc);
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
    /**
     * Fetches events for a specific facility and updates the eventsList LiveData.
     *
     * @param facility The facility to fetch events for.
     */
    private void fetchEvents(Facility facility) {
        if (GlobalRepository.isInTestMode()) {
            updateEventsList(facility.getEvents());
            statusMessage.setValue("Events loaded from test repository");
            return;
        }

        statusMessage.setValue("Loading events...");

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
    /**
     * Updates the eventsList LiveData with the provided list of events.
     *
     * @param events The list of events to update the LiveData with.
     */
    private void updateEventsList(List<Event> events) {
        eventsList.setValue(events);
    }
    /**
     * Returns a LiveData object containing the list of events.
     *
     * @return LiveData object containing the list of events.
     */
    public LiveData<List<Event>> getEventsList() {
        return eventsList;
    }
    /**
     * Returns a LiveData object containing the error message.
     *
     * @return LiveData object containing the error message.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    /**
     * Returns a LiveData object containing the status message.
     *
     * @return LiveData object containing the status message.
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    /**
     * Returns a boolean flag indicating if the user is an admin.
     *
     * @return True if the user is an admin, false otherwise.
     */
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
    /**
     * Deletes an event from the repository and updates the eventsList LiveData.
     *
     * @param event The event to delete.
     */
    public void deleteEvent(Event event) {
        if (event == null) return;

        statusMessage.setValue("Deleting event...");

        if (GlobalRepository.isInTestMode()) {
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
}
