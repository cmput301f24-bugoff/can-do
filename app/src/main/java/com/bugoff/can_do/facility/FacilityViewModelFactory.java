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
    /**
     * Constructs a {@code FacilityViewModelFactory} with the specified facility ID.
     * Uses a default {@link GlobalRepository} instance for data operations.
     *
     * @param facilityId The ID of the facility for which the ViewModel is created.
     */
    public FacilityViewModelFactory(String facilityId) {
        this.facilityId = facilityId;
        this.repository = new GlobalRepository();
    }
    /**
     * Constructs a {@code FacilityViewModelFactory} with the specified facility ID and repository.
     * Primarily used for testing to inject a custom repository.
     *
     * @param facilityId The ID of the facility for which the ViewModel is created.
     * @param repository The {@link GlobalRepository} to use for data operations.
     */
    @VisibleForTesting
    public FacilityViewModelFactory(String facilityId, GlobalRepository repository) {
        this.facilityId = facilityId;
        this.repository = repository;
    }
    /**
     * Creates a new instance of the specified {@link ViewModel} class.
     *
     * @param modelClass The {@link Class} of the ViewModel to create.
     * @param <T>        The type of the ViewModel.
     * @return A new instance of the requested {@link FacilityViewModel}.
     * @throws IllegalArgumentException If the {@code modelClass} is not assignable to {@link FacilityViewModel}.
     */
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
