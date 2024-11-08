package com.bugoff.can_do.event;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ViewModel responsible for managing and providing a list of events for the current user's facility.
 * Retrieves data from Firebase and listens for real-time updates. Exposes LiveData objects
 * for observing the list of events and error messages.
 */
public class EventsListViewModel extends ViewModel {

    private static final String TAG = "EventsListViewModel";

    private final MutableLiveData<List<Event>> eventsList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;
    private final List<ListenerRegistration> eventListeners = new ArrayList<>(); // Event listeners to detach upon clearing

    /**
     * Constructs a new EventsListViewModel and initiates the fetch of event data.
     */
    public EventsListViewModel() {
        fetchEvents();
    }

    /**
     * Fetches the list of events associated with the current user's facility and updates the LiveData.
     * If the user or their associated facility is null, sets an error message in LiveData.
     */
    private void fetchEvents() {
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser == null || currentUser.getFacility() == null) {
            errorMessage.setValue("User or Facility not found.");
            return;
        }

        Facility facility = currentUser.getFacility();
        String facilityId = facility.getId();

        listenerRegistration = GlobalRepository.getEventsCollection()
                .whereEqualTo("facilityId", facilityId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            errorMessage.setValue("Error fetching events: " + error.getMessage());
                            return;
                        }

                        if (snapshots != null) {
                            List<DocumentSnapshot> documents = snapshots.getDocuments();
                            if (documents.isEmpty()) {
                                eventsList.setValue(new ArrayList<>());
                                return;
                            }

                            List<Event> fetchedEvents = new ArrayList<>();
                            final int total = documents.size();
                            final AtomicInteger counter = new AtomicInteger(0);

                            // Process each document to fetch event data and its facility
                            for (DocumentSnapshot doc : documents) {
                                String facilityId = doc.getString("facilityId");
                                GlobalRepository.getFacility(facilityId).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Facility eventFacility = task.getResult();
                                        if (eventFacility != null) {
                                            Event event = new Event(eventFacility, doc);
                                            fetchedEvents.add(event);

                                            // Set up onUpdateListener for each Event to update LiveData when Event changes
                                            event.setOnUpdateListener(() -> refreshEventsList());
                                        } else {
                                            Log.w(TAG, "Facility not found for event: " + doc.getId());
                                        }
                                    } else {
                                        Log.e(TAG, "Error fetching facility for event: " + doc.getId(), task.getException());
                                    }

                                    // When all documents are processed, update LiveData
                                    if (counter.incrementAndGet() == total) {
                                        eventsList.postValue(new ArrayList<>(fetchedEvents));
                                    }
                                });
                            }
                        } else {
                            Log.w(TAG, "Current data: null");
                            errorMessage.setValue("No events found.");
                        }
                    }
                });
    }

    /**
     * Refreshes the events list LiveData to notify observers of any changes.
     * This is typically called when an event in the list is updated.
     */
    private void refreshEventsList() {
        if (eventsList.getValue() != null) {
            // Create a new list instance to trigger LiveData observers
            List<Event> updatedList = new ArrayList<>(eventsList.getValue());
            eventsList.postValue(updatedList);
        }
    }

    /**
     * Returns the LiveData object representing the list of events.
     *
     * @return A LiveData object containing the list of events.
     */
    public LiveData<List<Event>> getEventsList() {
        return eventsList;
    }

    /**
     * Returns the LiveData object containing error messages.
     *
     * @return A LiveData object with the error message, or null if no error occurred.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Cleans up resources when the ViewModel is no longer needed.
     * Removes the Firebase listener registration for events and any Event listeners.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        // Detach all Event listeners
        for (ListenerRegistration reg : eventListeners) {
            reg.remove();
        }
    }
}

