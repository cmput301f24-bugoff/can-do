package com.bugoff.can_do.facility;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;

/**
 * Factory class for creating instances of {@link FacilityViewModel}.
 *
 * <p>This factory allows passing a facility ID to the ViewModel,
 * which is necessary for initializing the ViewModel with the correct data.</p>
 */
public class FacilityViewModelFactory implements ViewModelProvider.Factory {
    private final String facilityId;
    private final GlobalRepository repository;

    public FacilityViewModelFactory(String facilityId) {
        this.facilityId = facilityId;
        this.repository = new GlobalRepository();
    }

    @VisibleForTesting
    public FacilityViewModelFactory(String facilityId, GlobalRepository repository) {
        this.facilityId = facilityId;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FacilityViewModel.class)) {
            return (T) new FacilityViewModel(facilityId, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
