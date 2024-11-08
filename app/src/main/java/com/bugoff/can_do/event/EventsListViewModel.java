package com.bugoff.can_do.event;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.FirestoreHelper;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;
    private boolean isAdmin;

    // Keep track of Event listeners to detach them when ViewModel is cleared
    private final List<ListenerRegistration> eventListeners = new ArrayList<>();

    /**
     * Constructor that accepts isAdmin flag.
     *
     * @param isAdmin Indicates whether the user is an admin.
     */
    public EventsListViewModel(boolean isAdmin) {
        this.isAdmin = isAdmin;
        fetchEvents();
    }

    public EventsListViewModel() {
        fetchEvents();
    }

    /**
     * Fetches the list of events associated with the current user's facility and updates the LiveData.
     * If the user or their associated facility is null, sets an error message in LiveData.
     */
    private void fetchEvents() {
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser == null) {
            errorMessage.setValue("User not found.");
            Log.e(TAG, "Current user is null.");
            return;
        }

        String currentUserId = currentUser.getId();
        Log.d(TAG, "Fetching events for user ID: " + currentUserId + ", isAdmin: " + isAdmin);

        Query query;
        if (isAdmin) {
            // Admins can see all events
            query = GlobalRepository.getEventsCollection();
        } else {
            // Normal users see only their events
            query = GlobalRepository.getEventsCollection()
                    .whereEqualTo("facilityId", currentUserId);
        }

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error);
                    errorMessage.setValue("Error fetching events: " + error.getMessage());
                    return;
                }

                if (snapshots != null) {
                    List<DocumentSnapshot> documents = snapshots.getDocuments();
                    Log.d(TAG, "Number of events fetched: " + documents.size());

                    if (documents.isEmpty()) {
                        eventsList.setValue(new ArrayList<>());
                        return;
                    }

                    List<Event> fetchedEvents = new ArrayList<>();
                    final int total = documents.size();
                    final AtomicInteger counter = new AtomicInteger(0);

                    for (DocumentSnapshot doc : documents) {
                        // Assuming Facility is needed for Event; adjust as necessary
                        String facilityId = doc.getString("facilityId");
                        if (facilityId != null) {
                            GlobalRepository.getFacility(facilityId).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Facility eventFacility = task.getResult();
                                    if (eventFacility != null) {
                                        Event event = new Event(eventFacility, doc);
                                        fetchedEvents.add(event);

                                        // Set up onUpdateListener for each Event
                                        event.setOnUpdateListener(() -> {
                                            // Trigger LiveData update when any Event is updated
                                            refreshEventsList();
                                        });

                                        Log.d(TAG, "Fetched event: " + event.getName() + ", Facility: " + eventFacility.getName());
                                    } else {
                                        Log.w(TAG, "Facility not found for event: " + doc.getId());
                                    }
                                } else {
                                    Log.e(TAG, "Error fetching facility for event: " + doc.getId(), task.getException());
                                }

                                // Increment the counter and check if all tasks are completed
                                if (counter.incrementAndGet() == total) {
                                    // All tasks have completed
                                    eventsList.postValue(new ArrayList<>(fetchedEvents));
                                    Log.d(TAG, "All events fetched successfully.");
                                }
                            });
                        } else {
                            Log.w(TAG, "facilityId is null for event: " + doc.getId());
                            // Even if facilityId is null, still include the event
                            Event event = new Event(null, doc);
                            fetchedEvents.add(event);
                            if (counter.incrementAndGet() == total) {
                                eventsList.postValue(new ArrayList<>(fetchedEvents));
                                Log.d(TAG, "All events fetched successfully (without facilities).");
                            }
                        }
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
            Log.d(TAG, "Events list refreshed.");
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

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    /**
     * Deletes an event and removes all references in users' documents.
     *
     * @param event The event to delete.
     */
    public void deleteEvent(Event event) {
        // Delete the event document
        DocumentReference eventRef = GlobalRepository.getEventsCollection().document(event.getId());

        // Create a batch
        WriteBatch batch = FirestoreHelper.getInstance().getDb().batch();

        // Delete the event document
        batch.delete(eventRef);

        // Query all users where 'eventsJoined' contains this event's ID
        GlobalRepository.getUsersCollection()
                .whereArrayContains("eventsJoined", event.getId())
                .get()
                .addOnSuccessListener(joinedSnapshot -> {
                    for (DocumentSnapshot doc : joinedSnapshot.getDocuments()) {
                        DocumentReference userRef = doc.getReference();
                        batch.update(userRef, "eventsJoined", FieldValue.arrayRemove(event.getId()));
                    }

                    // Query all users where 'eventsEnrolled' contains this event's ID
                    GlobalRepository.getUsersCollection()
                            .whereArrayContains("eventsEnrolled", event.getId())
                            .get()
                            .addOnSuccessListener(enrolledSnapshot -> {
                                for (DocumentSnapshot doc : enrolledSnapshot.getDocuments()) {
                                    DocumentReference userRef = doc.getReference();
                                    batch.update(userRef, "eventsEnrolled", FieldValue.arrayRemove(event.getId()));
                                }

                                // Commit the batch
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Event and references successfully deleted.");
                                            statusMessage.postValue("Event deleted successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error deleting event and references.", e);
                                            errorMessage.postValue("Error deleting event: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error querying enrolled users.", e);
                                errorMessage.postValue("Error deleting event: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying joined users.", e);
                    errorMessage.postValue("Error deleting event: " + e.getMessage());
                });
    }

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
