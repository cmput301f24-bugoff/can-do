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
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The OrganizerTransition activity checks whether the logged-in user has an associated facility.
 * If the user has a facility, it redirects them to the main organizer activity. If no facility is
 * found, it prompts the user to create a new facility.
 */
public class OrganizerTransition extends AppCompatActivity {
    private FirebaseFirestore db;
    private String androidId;

    /**
     * Called when the activity is created. Initializes Firebase Firestore and retrieves the device's Android ID.
     * It then checks whether the user has an associated facility.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        checkUserFacility();
    }

    /**
     * Checks if the logged-in user has an associated facility in Firestore.
     * If a facility exists, the user is redirected to the next activity.
     * If not, the user is prompted to create a new facility.
     */
    private void checkUserFacility() {
        db.collection("facilities").document(androidId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Facility exists, redirect to the next activity
                        navigateToNextActivity();
                    } else {
                        // Facility does not exist, prompt the user to create one
                        promptUserToCreateFacility();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to check facility data", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Prompts the user to create a new facility by displaying the facility edit screen.
     * Sets up the continue button to save the facility and proceed.
     */
    private void promptUserToCreateFacility() {
        setContentView(R.layout.activity_facility_edit);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button continueButton = findViewById(R.id.saveFacilityButton);
        continueButton.setOnClickListener(v -> saveFacilityAndProceed());
    }

    /**
     * Navigates the user to the next activity (OrganizerMain).
     */
    private void navigateToNextActivity() {
        Intent intent = new Intent(this, OrganizerMain.class); // Replace NewActivity with the target activity class
        startActivity(intent);
        finish();
    }

    /**
     * Saves the new facility entered by the user and proceeds to the main organizer activity.
     * If the facility name or address is missing, a toast message is shown.
     */
    private void saveFacilityAndProceed() {
        // Retrieve input fields
        EditText facilityNameInput = findViewById(R.id.facilityNameInput);
        EditText facilityAddressInput = findViewById(R.id.facilityAddressInput);

        String facilityName = facilityNameInput.getText().toString().trim();
        String facilityAddress = facilityAddressInput.getText().toString().trim();

        // Validate inputs
        if (facilityName.isEmpty() || facilityAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = GlobalRepository.getLoggedInUser();

        // Retrieve the existing Facility or create a new one
        Facility facility = currentUser.getFacility();
        if (facility == null) {
            facility = new Facility(currentUser);
            currentUser.setFacility(facility); // Ensure bidirectional reference
            currentUser.setRemote(); // Persist the facilityId to Firestore
        }

        // Set the facility properties
        facility.setName(facilityName);
        facility.setAddress(facilityAddress);

        // Set an update listener to handle post-save actions
        facility.setOnUpdateListener(() -> {
            Toast.makeText(OrganizerTransition.this, "Facility saved successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrganizerTransition.this, OrganizerMain.class));
            finish();
        });

        // Attach the snapshot listener
        facility.attachListener();

        // Save the facility to Firestore
        facility.setRemote();
    }
}
