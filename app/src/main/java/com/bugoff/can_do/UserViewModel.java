package com.bugoff.can_do;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final User user;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdmin = new MutableLiveData<>();
    private final MutableLiveData<Facility> facility = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel(String userId) {
        // Initialize User fetched from repository
        this.user = GlobalRepository.getUser(userId).getResult();
        this.userName.setValue(user.getName());
        this.isAdmin.setValue(user.getIsAdmin());
        this.facility.setValue(user.getFacility());

        // Set onUpdateListener to update LiveData
        this.user.setOnUpdateListener(this::updateLiveData);

        // Attach Firestore listener
        this.user.attachListener();
    }

    private void updateLiveData() {
        userName.postValue(user.getName());
        isAdmin.postValue(user.getIsAdmin());
        facility.postValue(user.getFacility());
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<Boolean> getIsAdmin() {
        return isAdmin;
    }

    public LiveData<Facility> getFacility() {
        return facility;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Methods to interact with User
    public void setName(String name) {
        user.setName(name);
        user.setRemote();
    }

    public void setIsAdmin(Boolean adminStatus) {
        user.setIsAdmin(adminStatus);
        user.setRemote();
    }

    public void setFacility(Facility newFacility) {
        user.setFacility(newFacility);
        user.setRemote();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        user.detachListener();
    }
}
