package com.bugoff.can_do.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;
/**
 * Factory class for creating a new instance of {@link EventsListViewModel}.
 * It provides the necessary dependencies such as {@link GlobalRepository} and flags for admin status.
 */
public class EventsListViewModelFactory implements ViewModelProvider.Factory {
    private final GlobalRepository repository;
    private final boolean isAdmin;
    private final boolean isFromAdmin;
    /**
     * Constructs an {@code EventsListViewModelFactory} with default {@link GlobalRepository}.
     *
     * @param isAdmin     Indicates if the user is an admin.
     * @param isFromAdmin Indicates if the view model is accessed from an admin view.
     */
    public EventsListViewModelFactory(boolean isAdmin, boolean isFromAdmin) {
        this.repository = new GlobalRepository();
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
    }
    /**
     * Constructs an {@code EventsListViewModelFactory} with a specified {@link GlobalRepository}.
     *
     * @param repository  The {@link GlobalRepository} to use.
     * @param isAdmin     Indicates if the user is an admin.
     * @param isFromAdmin Indicates if the view model is accessed from an admin view.
     */
    public EventsListViewModelFactory(GlobalRepository repository, boolean isAdmin, boolean isFromAdmin) {
        this.repository = repository;
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
    }
    /**
     * Creates a new instance of the specified {@link ViewModel} class.
     *
     * @param modelClass The {@link Class} of the {@link ViewModel} to create.
     * @param <T>        The type of the {@link ViewModel}.
     * @return A new instance of the requested {@link ViewModel}.
     * @throws IllegalArgumentException If the model class is not assignable to {@link EventsListViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventsListViewModel.class)) {
            return (T) new EventsListViewModel(repository, isAdmin, isFromAdmin);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}