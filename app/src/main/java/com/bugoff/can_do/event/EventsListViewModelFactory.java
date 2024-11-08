package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class EventsListViewModelFactory implements ViewModelProvider.Factory {
    private final boolean isAdmin;

    public EventsListViewModelFactory(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventsListViewModel.class)) {
            return (T) new EventsListViewModel(isAdmin);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
