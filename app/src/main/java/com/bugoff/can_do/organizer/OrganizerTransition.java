package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
/**
 * Activity for transitioning to the main organizer screen.
 * This activity checks if the user has a facility and prompts the user to create one if not.
 */
public class OrganizerTransition extends AppCompatActivity {
    private GlobalRepository repository;
    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        repository = new GlobalRepository();

        checkUserFacility();
    }
    /**
     * Checks if the user has a facility.
     * If the user has a facility, navigates to the main organizer screen.
     * If the user does not have a facility, prompts the user to create one.
     */
    private void checkUserFacility() {
        repository.getFacility(androidId)
                .addOnSuccessListener(facility -> {
                    if (facility != null) {
                        // Facility exists, redirect to the next activity
                        navigateToNextActivity();
                    } else {
                        // Facility does not exist, prompt the user to create one
                        promptUserToCreateFacility();
                    }
                })
                .addOnFailureListener(e -> {
                    // If facility doesn't exist, this is expected
                    promptUserToCreateFacility();
                });
    }
    /**
     * Prompts the user to create a facility.
     */
    private void promptUserToCreateFacility() {
        setContentView(R.layout.activity_facility_edit);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button continueButton = findViewById(R.id.saveFacilityButton);
        continueButton.setOnClickListener(v -> saveFacilityAndProceed());
    }
    /**
     * Navigates to the main organizer screen.
     */
    private void navigateToNextActivity() {
        Intent intent = new Intent(this, OrganizerMain.class);
        startActivity(intent);
        finish();
    }
    /**
     * Saves the facility details and proceeds to the main organizer screen.
     */
    private void saveFacilityAndProceed() {
        EditText facilityNameInput = findViewById(R.id.facilityNameInput);
        EditText facilityAddressInput = findViewById(R.id.facilityAddressInput);

        String facilityName = facilityNameInput.getText().toString().trim();
        String facilityAddress = facilityAddressInput.getText().toString().trim();

        if (facilityName.isEmpty() || facilityAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = GlobalRepository.getLoggedInUser();
        Facility facility = new Facility(currentUser);
        facility.setName(facilityName);
        facility.setAddress(facilityAddress);

        GlobalRepository.addFacility(facility)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Facility saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, OrganizerMain.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving facility", Toast.LENGTH_SHORT).show()
                );
    }

    // For testing purposes
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
    }
}