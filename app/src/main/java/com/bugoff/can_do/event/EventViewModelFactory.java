package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class EventViewModelFactory implements ViewModelProvider.Factory {
    private final String eventId;

    public EventViewModelFactory(String eventId) {
        this.eventId = eventId;
    }

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
