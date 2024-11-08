package com.bugoff.can_do.facility;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
/**
 * Factory class for creating instances of {@link FacilityViewModel}.
 *
 * <p>This factory allows passing a facility ID to the ViewModel,
 * which is necessary for initializing the ViewModel with the correct data.</p>
 */
public class FacilityViewModelFactory implements ViewModelProvider.Factory {
    /** The unique identifier of the facility to be used by the ViewModel. */
    private final String facilityId;
    /**
     * Constructs a new FacilityViewModelFactory with the specified facility ID.
     *
     * @param facilityId The unique identifier of the facility.
     */
    public FacilityViewModelFactory(String facilityId) {
        this.facilityId = facilityId;
    }
    /**
     * Creates a new instance of the given {@link ViewModel} class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @param <T>        The type parameter for the ViewModel.
     * @return A new instance of the requested ViewModel.
     * @throws IllegalArgumentException if the modelClass is not assignable from {@link FacilityViewModel}.
     */
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
