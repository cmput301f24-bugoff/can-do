package com.bugoff.can_do.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory for creating UserViewModel with userId parameter.
 */
public class UserViewModelFactory implements ViewModelProvider.Factory {
    private final String userId;

    public UserViewModelFactory(String userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}