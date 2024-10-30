package com.bugoff.can_do.facility;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class FacilityViewModelFactory implements ViewModelProvider.Factory {
    private final String facilityId;

    public FacilityViewModelFactory(String facilityId) {
        this.facilityId = facilityId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FacilityViewModel.class)) {
            return (T) new FacilityViewModel(facilityId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
