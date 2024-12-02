package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;
/**
 * Factory for creating a new instance of EventsListViewModel.
 */
public class EventsListViewModelFactory implements ViewModelProvider.Factory {
    private final GlobalRepository repository;
    private final boolean isAdmin;
    private final boolean isFromAdmin;

    public EventsListViewModelFactory(boolean isAdmin, boolean isFromAdmin) {
        this.repository = new GlobalRepository();
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
    }

    public EventsListViewModelFactory(GlobalRepository repository, boolean isAdmin, boolean isFromAdmin) {
        this.repository = repository;
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventsListViewModel.class)) {
            return (T) new EventsListViewModel(repository, isAdmin, isFromAdmin);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}