package com.bugoff.can_do.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory for creating BrowseProfilesViewModel with isAdmin parameter.
 */
public class BrowseProfilesViewModelFactory implements ViewModelProvider.Factory {
    private final boolean isAdmin;

    public BrowseProfilesViewModelFactory(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BrowseProfilesViewModel.class)) {
            return (T) new BrowseProfilesViewModel(isAdmin);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
