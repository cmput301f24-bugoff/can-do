package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory class for creating an instance of EventViewModel with a specific event ID.
 * Used to pass the event ID into the ViewModel's constructor.
 */
public class EventViewModelFactory implements ViewModelProvider.Factory {
    private final String eventId; // ID of the event to be managed by EventViewModel

    /**
     * Constructs a new EventViewModelFactory with the specified event ID.
     *
     * @param eventId The ID of the event to be passed into the EventViewModel.
     */
    public EventViewModelFactory(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Creates a new instance of the given ViewModel class, injecting the event ID.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of EventViewModel with the specified event ID.
     * @throws IllegalArgumentException if the provided modelClass is not EventViewModel.
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventViewModel.class)) {
            return (T) new EventViewModel(eventId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

