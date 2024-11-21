package com.bugoff.can_do.user;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;

/**
 * Factory for creating UserViewModel with userId parameter.
 */
public class UserViewModelFactory implements ViewModelProvider.Factory {
    private final String userId;
    private final GlobalRepository repository;

    public UserViewModelFactory(String userId) {
        this.userId = userId;
        this.repository = new GlobalRepository();
    }

    @VisibleForTesting
    public UserViewModelFactory(String userId, GlobalRepository repository) {
        this.userId = userId;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(userId, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
