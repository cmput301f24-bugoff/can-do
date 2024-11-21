package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.bugoff.can_do.database.GlobalRepository;

public class EventsListViewModelFactory implements ViewModelProvider.Factory {
    private final GlobalRepository repository;
    private final boolean isAdmin;

    public EventsListViewModelFactory(boolean isAdmin) {
        this.repository = new GlobalRepository();
        this.isAdmin = isAdmin;
    }

    @VisibleForTesting
    public EventsListViewModelFactory(GlobalRepository repository, boolean isAdmin) {
        this.repository = repository;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventsListViewModel.class)) {
            return (T) new EventsListViewModel(repository, isAdmin);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}