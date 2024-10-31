package com.bugoff.can_do.event;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

public class EventsListViewModel extends ViewModel {

    private static final String TAG = "EventsListViewModel";

    private final MutableLiveData<List<Event>> eventsList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;

    public EventsListViewModel() {
        fetchEvents();
    }

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

                            for (DocumentSnapshot doc : documents) {
                                String facilityId = doc.getString("facilityId");
                                GlobalRepository.getFacility(facilityId).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Facility eventFacility = task.getResult();
                                        if (eventFacility != null) {
                                            Event event = new Event(eventFacility, doc);
                                            synchronized (fetchedEvents) {
                                                fetchedEvents.add(event);
                                            }
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

    public LiveData<List<Event>> getEventsList() {
        return eventsList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
